import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Button,
  Card,
  CardContent,
  Alert,
  Stack,
  Chip,
  Divider,
  Paper,
} from '@mui/material';
import QrCodeScannerIcon from '@mui/icons-material/QrCodeScanner';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import WarningIcon from '@mui/icons-material/Warning';
import CameraswitchIcon from '@mui/icons-material/Cameraswitch';
import { validateTicket, getScanStats, type ScanResponse, type ScanStatsResponse } from '../api/tickets.api';
import { useAuth } from '../contexts/AuthContext';
import jsQR from 'jsqr';

export default function ScanTicketPage() {
  const { eventId } = useParams<{ eventId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [scanning, setScanning] = useState(false);
  const [scanResult, setScanResult] = useState<ScanResponse | null>(null);
  const [stats, setStats] = useState<ScanStatsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [lastScannedCode, setLastScannedCode] = useState<string>('');
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const scanIntervalRef = useRef<number | null>(null);

  const fetchStats = useCallback(async () => {
    if (!eventId) return;

    try {
      const statsData = await getScanStats(Number(eventId));
      setStats(statsData);
      setError(null);
    } catch (err) {
      console.error('Error fetching stats:', err);
      const error = err as { response?: { status: number } };
      if (error.response?.status === 403) {
        setError('You are not authorized to scan tickets for this event');
      } else {
        setError('Failed to load event statistics');
      }
    }
  }, [eventId]);

  useEffect(() => {
    console.log('ScanTicketPage - useEffect triggered');
    console.log('User object:', user);
    console.log('User type:', user?.userType);
    console.log('Event ID:', eventId);

    // Check if user exists and has loaded
    if (user === null) {
      // Still loading or not logged in, check localStorage
      const storedUser = localStorage.getItem('user');
      console.log('Stored user in localStorage:', storedUser);

      if (!storedUser) {
        console.log('No stored user, redirecting to home');
        navigate('/');
        return;
      }

      // User exists in localStorage, wait for AuthContext to load it
      console.log('User in localStorage but not loaded yet, waiting...');
      return;
    }

    // User is loaded, check if organizer
    if (user.userType !== 'organizer') {
      console.log(`User type is "${user.userType}", not "organizer". Redirecting to home.`);
      navigate('/');
      return;
    }

    console.log('User is an organizer, fetching stats...');
    setIsCheckingAuth(false);

    // Fetch scan stats
    if (eventId) {
      fetchStats();
    }
  }, [eventId, user, navigate, fetchStats]);

  const handleQRCodeDetected = useCallback(async (qrCodeData: string) => {
    if (scanning || !eventId) return;

    console.log('QR Code detected:', qrCodeData);
    setLastScannedCode(qrCodeData);
    setScanning(true);
    setScanResult(null);

    try {
      const result = await validateTicket(Number(eventId), qrCodeData);
      setScanResult(result);

      // If scan was successful, refresh stats
      if (result.valid) {
        await fetchStats();
      }

      // Wait 3 seconds before allowing next scan
      setTimeout(() => {
        setLastScannedCode('');
        setScanning(false);
      }, 3000);
    } catch (err) {
      console.error('Error scanning ticket:', err);
      const error = err as { response?: { data?: { message?: string } } };
      setScanResult({
        valid: false,
        message: error.response?.data?.message || 'Failed to scan ticket. Please try again.',
        status: 'ERROR',
      });

      setTimeout(() => {
        setLastScannedCode('');
        setScanning(false);
      }, 3000);
    }
  }, [scanning, eventId, fetchStats]);

  const scanQRCode = useCallback(() => {
    if (!videoRef.current || !canvasRef.current || videoRef.current.readyState !== videoRef.current.HAVE_ENOUGH_DATA) {
      return;
    }

    const video = videoRef.current;
    const canvas = canvasRef.current;
    const context = canvas.getContext('2d');

    if (!context) return;

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
    const code = jsQR(imageData.data, imageData.width, imageData.height);

    if (code && code.data && code.data !== lastScannedCode) {
      handleQRCodeDetected(code.data);
    }
  }, [lastScannedCode]);

  const startCamera = useCallback(async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      });

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        streamRef.current = stream;
        videoRef.current.play();
      }

      // Start scanning loop
      scanIntervalRef.current = window.setInterval(() => {
        scanQRCode();
      }, 300);

      setCameraError(null);
    } catch (err) {
      console.error('Error accessing camera:', err);
      setCameraError('Unable to access camera. Please ensure camera permissions are granted.');
    }
  }, [scanQRCode]);

  const stopCamera = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }
    if (scanIntervalRef.current) {
      clearInterval(scanIntervalRef.current);
      scanIntervalRef.current = null;
    }
  }, []);

  // Start camera when component mounts
  useEffect(() => {
    if (!isCheckingAuth && user?.userType === 'organizer') {
      startCamera();
    }

    // Cleanup on unmount
    return () => {
      stopCamera();
    };
  }, [isCheckingAuth, user, startCamera, stopCamera]);

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return <CheckCircleIcon sx={{ fontSize: 60, color: 'success.main' }} />;
      case 'ALREADY_SCANNED':
        return <WarningIcon sx={{ fontSize: 60, color: 'warning.main' }} />;
      case 'INVALID':
      case 'WRONG_EVENT':
      case 'ERROR':
        return <ErrorIcon sx={{ fontSize: 60, color: 'error.main' }} />;
      default:
        return null;
    }
  };

  const getStatusColor = (status: string): 'success' | 'warning' | 'error' | 'default' => {
    switch (status) {
      case 'SUCCESS':
        return 'success';
      case 'ALREADY_SCANNED':
        return 'warning';
      case 'INVALID':
      case 'WRONG_EVENT':
      case 'ERROR':
        return 'error';
      default:
        return 'default';
    }
  };

  if (isCheckingAuth) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Typography variant="h5" textAlign="center">Loading...</Typography>
      </Container>
    );
  }

  if (error && !stats) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">{error}</Alert>
        <Button onClick={() => navigate('/')} sx={{ mt: 2 }}>
          Go Home
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <QrCodeScannerIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
        <Typography variant="h3" component="h1" gutterBottom fontWeight="bold">
          Scan Tickets
        </Typography>
        {stats && (
          <Typography variant="h5" color="text.secondary">
            {stats.eventTitle}
          </Typography>
        )}
      </Box>

      {/* Statistics */}
      {stats && (
        <Paper elevation={2} sx={{ p: 3, mb: 4, borderRadius: 2 }}>
          <Typography variant="h6" gutterBottom fontWeight="bold">
            Event Statistics
          </Typography>
          <Stack direction="row" spacing={3} sx={{ mt: 2 }}>
            <Box sx={{ flex: 1, textAlign: 'center' }}>
              <Typography variant="h3" fontWeight="bold" color="primary.main">
                {stats.totalTickets}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Total Tickets
              </Typography>
            </Box>
            <Divider orientation="vertical" flexItem />
            <Box sx={{ flex: 1, textAlign: 'center' }}>
              <Typography variant="h3" fontWeight="bold" color="success.main">
                {stats.scannedCount}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Scanned
              </Typography>
            </Box>
            <Divider orientation="vertical" flexItem />
            <Box sx={{ flex: 1, textAlign: 'center' }}>
              <Typography variant="h3" fontWeight="bold" color="warning.main">
                {stats.remainingCount}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Remaining
              </Typography>
            </Box>
          </Stack>
        </Paper>
      )}

      {/* Camera Scanner */}
      <Card sx={{ mb: 4, position: 'relative', overflow: 'hidden' }}>
        <CardContent sx={{ p: 0, position: 'relative' }}>
          {cameraError ? (
            <Alert severity="error" sx={{ m: 2 }}>{cameraError}</Alert>
          ) : (
            <>
              <Box sx={{ position: 'relative', width: '100%', paddingTop: '75%', backgroundColor: '#000' }}>
                <video
                  ref={videoRef}
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover'
                  }}
                  playsInline
                />
                <canvas ref={canvasRef} style={{ display: 'none' }} />

                {/* Scanning overlay */}
                <Box
                  sx={{
                    position: 'absolute',
                    top: '50%',
                    left: '50%',
                    transform: 'translate(-50%, -50%)',
                    width: '80%',
                    maxWidth: 300,
                    height: 300,
                    border: '3px solid',
                    borderColor: scanning ? 'success.main' : 'primary.main',
                    borderRadius: 2,
                    boxShadow: scanning ? '0 0 20px rgba(76, 175, 80, 0.5)' : 'none',
                    transition: 'all 0.3s ease'
                  }}
                />

                {/* Scanning status */}
                <Box
                  sx={{
                    position: 'absolute',
                    bottom: 20,
                    left: '50%',
                    transform: 'translateX(-50%)',
                    backgroundColor: 'rgba(0, 0, 0, 0.7)',
                    color: 'white',
                    px: 3,
                    py: 1.5,
                    borderRadius: 2
                  }}
                >
                  <Typography variant="body1" fontWeight="medium">
                    {scanning ? 'Processing...' : 'Point camera at QR code'}
                  </Typography>
                </Box>
              </Box>
            </>
          )}
        </CardContent>
      </Card>

      {/* Scan Result */}
      {scanResult && (
        <Card
          sx={{
            borderRadius: 2,
            border: 3,
            borderColor: `${getStatusColor(scanResult.status)}.main`,
          }}
        >
          <CardContent sx={{ p: 4, textAlign: 'center' }}>
            <Box sx={{ mb: 2 }}>
              {getStatusIcon(scanResult.status)}
            </Box>

            <Chip
              label={scanResult.status}
              color={getStatusColor(scanResult.status)}
              sx={{ mb: 2, fontWeight: 'bold', fontSize: 16, px: 2, py: 2.5 }}
            />

            <Typography variant="h5" gutterBottom fontWeight="bold">
              {scanResult.message}
            </Typography>

            {scanResult.ticketData && (
              <Box sx={{ mt: 3, textAlign: 'left' }}>
                <Divider sx={{ mb: 2 }} />
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  TICKET DETAILS
                </Typography>
                <Stack spacing={1.5}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Student Name
                    </Typography>
                    <Typography variant="body1" fontWeight="medium">
                      {scanResult.ticketData.studentName}
                    </Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Email
                    </Typography>
                    <Typography variant="body1" fontWeight="medium">
                      {scanResult.ticketData.studentEmail}
                    </Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Ticket Type
                    </Typography>
                    <Typography variant="body1" fontWeight="medium">
                      {scanResult.ticketData.ticketType}
                    </Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Ticket ID
                    </Typography>
                    <Typography variant="body1" fontWeight="medium">
                      #{scanResult.ticketData.ticketId}
                    </Typography>
                  </Box>
                </Stack>
              </Box>
            )}

            {scanResult.scannedAt && scanResult.status === 'ALREADY_SCANNED' && (
              <Box sx={{ mt: 3, textAlign: 'left' }}>
                <Divider sx={{ mb: 2 }} />
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  SCAN HISTORY
                </Typography>
                <Stack spacing={1}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Previously Scanned At
                    </Typography>
                    <Typography variant="body2" fontWeight="medium">
                      {scanResult.scannedAt}
                    </Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Scanned By
                    </Typography>
                    <Typography variant="body2" fontWeight="medium">
                      {scanResult.scannedBy}
                    </Typography>
                  </Box>
                </Stack>
              </Box>
            )}
          </CardContent>
        </Card>
      )}
    </Container>
  );
}
