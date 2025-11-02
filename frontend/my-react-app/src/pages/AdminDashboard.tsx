import React, { useState, useEffect } from 'react';
import { getGlobalStatistics, type GlobalStatsResponse } from '../api/administrators.api';
import {
    Container,
    Typography,
    Alert,
    Grid,
    Card,
    CardContent,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Box,
    CircularProgress
} from '@mui/material';
import {
    Event as EventIcon,
    ConfirmationNumber as TicketIcon,
    People as PeopleIcon,
    Business as BusinessIcon,
    CheckCircle as CheckCircleIcon,
    Cancel as CancelIcon
} from '@mui/icons-material';

const AdminDashboard: React.FC = () => {
    const [stats, setStats] = useState<GlobalStatsResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchGlobalStats = async () => {
            try {
                setLoading(true);
                const data = await getGlobalStatistics();
                setStats(data);
                setError(null);
            } catch (err) {
                setError('Failed to fetch global statistics. Please ensure you have admin privileges.');
                console.error('Error fetching stats:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchGlobalStats();
    }, []);

    if (loading) {
        return (
            <Container sx={{ mt: 5, display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
                <CircularProgress />
            </Container>
        );
    }

    if (error) {
        return (
            <Container sx={{ mt: 5 }}>
                <Alert severity="error">{error}</Alert>
            </Container>
        );
    }

    if (!stats) {
        return (
            <Container sx={{ mt: 5 }}>
                <Alert severity="info">No statistics available.</Alert>
            </Container>
        );
    }

    return (
        <Container maxWidth="xl" sx={{ mt: 5, mb: 5 }}>
            <Typography variant="h3" component="h1" gutterBottom fontWeight="bold">
                Admin Dashboard
            </Typography>
            <Typography variant="subtitle1" color="text.secondary" gutterBottom sx={{ mb: 4 }}>
                Global statistics and participation trends for Linkt
            </Typography>

            {/* Overview Stats Cards */}
            <Grid container spacing={3} sx={{ mb: 4 }}>
                <Grid item xs={12} sm={6} md={4} lg={2}>
                    <Card elevation={3} sx={{ backgroundColor: '#1976d2', color: 'white' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h4" fontWeight="bold">
                                        {stats.totalEvents}
                                    </Typography>
                                    <Typography variant="body2">Total Events</Typography>
                                </Box>
                                <EventIcon sx={{ fontSize: 48, opacity: 0.7 }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={4} lg={2}>
                    <Card elevation={3} sx={{ backgroundColor: '#9c27b0', color: 'white' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h4" fontWeight="bold">
                                        {stats.totalTickets}
                                    </Typography>
                                    <Typography variant="body2">Total Tickets</Typography>
                                </Box>
                                <TicketIcon sx={{ fontSize: 48, opacity: 0.7 }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={4} lg={2}>
                    <Card elevation={3} sx={{ backgroundColor: '#2e7d32', color: 'white' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h4" fontWeight="bold">
                                        {stats.totalScannedTickets}
                                    </Typography>
                                    <Typography variant="body2">Scanned Tickets</Typography>
                                </Box>
                                <CheckCircleIcon sx={{ fontSize: 48, opacity: 0.7 }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={4} lg={2}>
                    <Card elevation={3} sx={{ backgroundColor: '#d32f2f', color: 'white' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h4" fontWeight="bold">
                                        {stats.totalUnscannedTickets}
                                    </Typography>
                                    <Typography variant="body2">Unscanned Tickets</Typography>
                                </Box>
                                <CancelIcon sx={{ fontSize: 48, opacity: 0.7 }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={4} lg={2}>
                    <Card elevation={3} sx={{ backgroundColor: '#ed6c02', color: 'white' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h4" fontWeight="bold">
                                        {stats.totalStudents}
                                    </Typography>
                                    <Typography variant="body2">Students</Typography>
                                </Box>
                                <PeopleIcon sx={{ fontSize: 48, opacity: 0.7 }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={4} lg={2}>
                    <Card elevation={3} sx={{ backgroundColor: '#0288d1', color: 'white' }}>
                        <CardContent>
                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                <Box>
                                    <Typography variant="h4" fontWeight="bold">
                                        {stats.totalOrganizers}
                                    </Typography>
                                    <Typography variant="body2">Organizers</Typography>
                                </Box>
                                <BusinessIcon sx={{ fontSize: 48, opacity: 0.7 }} />
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Scan Rate */}
            <Card elevation={3} sx={{ mb: 4 }}>
                <CardContent>
                    <Typography variant="h5" gutterBottom fontWeight="bold">
                        Scan Rate
                    </Typography>
                    <Box display="flex" alignItems="center" gap={2}>
                        <Typography variant="h2" color="primary" fontWeight="bold">
                            {stats.scanRate.toFixed(2)}%
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            of all tickets have been scanned
                        </Typography>
                    </Box>
                </CardContent>
            </Card>

            {/* Top Events Table */}
            {stats.topEvents && stats.topEvents.length > 0 && (
                <Card elevation={3} sx={{ mb: 4 }}>
                    <CardContent>
                        <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
                            Top Events by Ticket Count
                        </Typography>
                        <TableContainer component={Paper} variant="outlined">
                            <Table>
                                <TableHead>
                                    <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                        <TableCell><strong>Event Name</strong></TableCell>
                                        <TableCell align="right"><strong>Total Tickets</strong></TableCell>
                                        <TableCell align="right"><strong>Scanned</strong></TableCell>
                                        <TableCell align="right"><strong>Unscanned</strong></TableCell>
                                        <TableCell align="right"><strong>Scan Rate</strong></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {stats.topEvents.map((event) => {
                                        const unscanned = event.ticketCount - event.scannedCount;
                                        const scanRate = event.ticketCount > 0
                                            ? ((event.scannedCount / event.ticketCount) * 100).toFixed(2)
                                            : '0.00';

                                        return (
                                            <TableRow key={event.eventId} hover>
                                                <TableCell>{event.eventName}</TableCell>
                                                <TableCell align="right">{event.ticketCount}</TableCell>
                                                <TableCell align="right" sx={{ color: '#2e7d32' }}>
                                                    {event.scannedCount}
                                                </TableCell>
                                                <TableCell align="right" sx={{ color: '#d32f2f' }}>
                                                    {unscanned}
                                                </TableCell>
                                                <TableCell align="right">
                                                    <strong>{scanRate}%</strong>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </CardContent>
                </Card>
            )}

            {/* Participation Trends Table */}
            {stats.participationTrends && stats.participationTrends.length > 0 && (
                <Card elevation={3}>
                    <CardContent>
                        <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
                            Participation Trends by Event Date
                        </Typography>
                        <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 440 }}>
                            <Table stickyHeader>
                                <TableHead>
                                    <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                        <TableCell><strong>Date</strong></TableCell>
                                        <TableCell align="right"><strong>Tickets Issued</strong></TableCell>
                                        <TableCell align="right"><strong>Tickets Scanned</strong></TableCell>
                                        <TableCell align="right"><strong>Scan Rate</strong></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {stats.participationTrends.map((trend, index) => {
                                        const scanRate = trend.ticketsIssued > 0
                                            ? ((trend.ticketsScanned / trend.ticketsIssued) * 100).toFixed(2)
                                            : '0.00';

                                        return (
                                            <TableRow key={index} hover>
                                                <TableCell>{trend.date}</TableCell>
                                                <TableCell align="right">{trend.ticketsIssued}</TableCell>
                                                <TableCell align="right" sx={{ color: '#2e7d32' }}>
                                                    {trend.ticketsScanned}
                                                </TableCell>
                                                <TableCell align="right">
                                                    <strong>{scanRate}%</strong>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </CardContent>
                </Card>
            )}
        </Container>
    );
};

export default AdminDashboard;
