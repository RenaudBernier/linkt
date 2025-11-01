import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  CircularProgress,
  Alert,
  Button,
  Chip,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { getRegisteredStudents } from '../api/registeredStudents.api';
import type { StudentRegistration } from '../api/registeredStudents.api';
import { useSnackbar } from 'notistack';

function RegisteredStudentsPage() {
  const { eventId } = useParams<{ eventId: string }>();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  const [students, setStudents] = useState<StudentRegistration[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [eventTitle, setEventTitle] = useState<string>('Event');

  useEffect(() => {
    const fetchStudents = async () => {
      if (!eventId) {
        setError('Event ID is missing');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        // Get token from localStorage
        const token = localStorage.getItem('token');
        if (!token) {
          setError('Not authenticated. Please log in.');
          navigate('/login');
          return;
        }

        // Fetch students
        const data = await getRegisteredStudents(parseInt(eventId), token);
        setStudents(data);

        // Fetch event details to get title
        const eventResponse = await fetch(
          `http://localhost:8080/api/events/${eventId}`,
          {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          }
        );
        if (eventResponse.ok) {
          const event = await eventResponse.json();
          setEventTitle(event.title || 'Event');
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load registered students';
        setError(errorMessage);
        enqueueSnackbar(errorMessage, { variant: 'error' });
      } finally {
        setLoading(false);
      }
    };

    fetchStudents();
  }, [eventId, navigate, enqueueSnackbar]);

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header with back button */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/events')}
          sx={{ textTransform: 'none' }}
        >
          Back to Events
        </Button>
        <Typography variant="h4">
          Registered Students for "{eventTitle}"
        </Typography>
      </Box>

      {/* Error message */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Students table */}
      {students.length > 0 ? (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Email</strong></TableCell>
                <TableCell><strong>Phone</strong></TableCell>
                <TableCell><strong>Ticket ID</strong></TableCell>
                <TableCell><strong>QR Code</strong></TableCell>
                <TableCell><strong>Check-in Status</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {students.map((student) => (
                <TableRow key={student.ticketId} hover>
                  <TableCell>
                    {student.firstName} {student.lastName}
                  </TableCell>
                  <TableCell>{student.email}</TableCell>
                  <TableCell>{student.phoneNumber || 'N/A'}</TableCell>
                  <TableCell>{student.ticketId}</TableCell>
                  <TableCell>
                    <Typography
                      variant="body2"
                      sx={{
                        fontFamily: 'monospace',
                        fontSize: '0.75rem',
                        wordBreak: 'break-all',
                      }}
                    >
                      {student.qrCode || 'N/A'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    {student.isScanned ? (
                      <Chip label="Checked In" color="success" variant="outlined" />
                    ) : (
                      <Chip label="Not Checked In" color="default" variant="outlined" />
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ) : (
        <Alert severity="info">
          No students registered for this event yet.
        </Alert>
      )}

      {/* Summary */}
      <Box sx={{ mt: 3 }}>
        <Typography variant="body1">
          <strong>Total Registrations:</strong> {students.length}
        </Typography>
        <Typography variant="body1">
          <strong>Checked In:</strong> {students.filter((s) => s.isScanned).length}
        </Typography>
        <Typography variant="body1">
          <strong>Not Checked In:</strong> {students.filter((s) => !s.isScanned).length}
        </Typography>
      </Box>
    </Container>
  );
}

export default RegisteredStudentsPage;
