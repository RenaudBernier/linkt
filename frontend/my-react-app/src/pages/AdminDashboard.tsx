import React, { useState, useEffect } from 'react';
import {
    getGlobalStatistics,
    getAllOrganizers,
    getAllEventsAdmin,
    type GlobalStatsResponse,
    type Organizer,
    type EventData
} from '../api/administrators.api';
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
    CircularProgress,
    Chip
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
    const [organizers, setOrganizers] = useState<Organizer[]>([]);
    const [events, setEvents] = useState<EventData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchAdminData = async () => {
            try {
                setLoading(true);

                // Fetch stats (required)
                const statsData = await getGlobalStatistics();
                setStats(statsData);
                setError(null);

                // Fetch organizers (optional - won't break if endpoint doesn't exist)
                try {
                    const organizersData = await getAllOrganizers();
                    setOrganizers(organizersData);
                    console.log('Organizers fetched:', organizersData);
                } catch (err) {
                    console.error('Failed to fetch organizers:', err);
                }

                // Fetch events (optional - won't break if endpoint doesn't exist)
                try {
                    const eventsData = await getAllEventsAdmin();
                    setEvents(eventsData);
                    console.log('Events fetched:', eventsData);
                } catch (err) {
                    console.error('Failed to fetch events:', err);
                }

            } catch (err) {
                setError('Failed to fetch admin data. Please ensure you have admin privileges.');
                console.error('Error fetching admin data:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchAdminData();
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

            {/* All Organizers Table */}
            <Card elevation={3} sx={{ mb: 4 }}>
                <CardContent>
                    <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
                        All Organizers
                    </Typography>
                    <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 440 }}>
                        <Table stickyHeader>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                    <TableCell><strong>Name</strong></TableCell>
                                    <TableCell><strong>Email</strong></TableCell>
                                    <TableCell><strong>Organization</strong></TableCell>
                                    <TableCell><strong>Phone</strong></TableCell>
                                    <TableCell><strong>Status</strong></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {organizers.length > 0 ? (
                                    organizers.map((organizer) => (
                                        <TableRow key={organizer.userId} hover>
                                            <TableCell>
                                                {organizer.firstName} {organizer.lastName}
                                            </TableCell>
                                            <TableCell>{organizer.email}</TableCell>
                                            <TableCell>{organizer.organizationName || 'N/A'}</TableCell>
                                            <TableCell>{organizer.phoneNumber || 'N/A'}</TableCell>
                                            <TableCell>
                                                <Chip
                                                    label={organizer.approvalStatus}
                                                    color={organizer.approvalStatus === 'approved' ? 'success' : 'warning'}
                                                    size="small"
                                                />
                                            </TableCell>
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={5} align="center">
                                            No organizers found
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </CardContent>
            </Card>

            {/* All Events Table */}
            <Card elevation={3} sx={{ mb: 4 }}>
                <CardContent>
                    <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
                        All Events
                    </Typography>
                    <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 440 }}>
                        <Table stickyHeader>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                    <TableCell><strong>Event Name</strong></TableCell>
                                    <TableCell><strong>Type</strong></TableCell>
                                    <TableCell><strong>Location</strong></TableCell>
                                    <TableCell align="right"><strong>Capacity</strong></TableCell>
                                    <TableCell align="right"><strong>Price</strong></TableCell>
                                    <TableCell align="right"><strong>Tickets Sold</strong></TableCell>
                                    <TableCell align="right"><strong>Scanned</strong></TableCell>
                                    <TableCell align="right"><strong>Scan Rate</strong></TableCell>
                                    <TableCell><strong>Start Date</strong></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {events.length > 0 ? (
                                    events.map((event) => {
                                        const scanRate = event.ticketCount > 0
                                            ? ((event.scannedTicketCount / event.ticketCount) * 100).toFixed(2)
                                            : '0.00';

                                        return (
                                            <TableRow key={event.eventId} hover>
                                                <TableCell>{event.title}</TableCell>
                                                <TableCell>{event.eventType}</TableCell>
                                                <TableCell>{event.location}</TableCell>
                                                <TableCell align="right">{event.capacity}</TableCell>
                                                <TableCell align="right">
                                                    ${event.price.toFixed(2)}
                                                </TableCell>
                                                <TableCell align="right">{event.ticketCount}</TableCell>
                                                <TableCell align="right" sx={{ color: '#2e7d32', fontWeight: 'bold' }}>
                                                    {event.scannedTicketCount}
                                                </TableCell>
                                                <TableCell align="right">
                                                    <strong>{scanRate}%</strong>
                                                </TableCell>
                                                <TableCell>
                                                    {new Date(event.startDateTime).toLocaleDateString()}
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={9} align="center">
                                            No events found
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </CardContent>
            </Card>

            {/* Top 5 Events by Attendance (Scanned Tickets) */}
            {stats.topEvents && stats.topEvents.length > 0 && (
                <Card elevation={3}>
                    <CardContent>
                        <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
                            Top 5 Events by Attendance
                        </Typography>
                        <TableContainer component={Paper} variant="outlined">
                            <Table>
                                <TableHead>
                                    <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                        <TableCell><strong>Rank</strong></TableCell>
                                        <TableCell><strong>Event Name</strong></TableCell>
                                        <TableCell align="right"><strong>Attendance (Scanned)</strong></TableCell>
                                        <TableCell align="right"><strong>Total Tickets</strong></TableCell>
                                        <TableCell align="right"><strong>Attendance Rate</strong></TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {stats.topEvents
                                        .sort((a, b) => b.scannedCount - a.scannedCount)
                                        .slice(0, 5)
                                        .map((event, index) => {
                                            const attendanceRate = event.ticketCount > 0
                                                ? ((event.scannedCount / event.ticketCount) * 100).toFixed(2)
                                                : '0.00';

                                            return (
                                                <TableRow key={event.eventId} hover>
                                                    <TableCell>
                                                        <Chip
                                                            label={`#${index + 1}`}
                                                            color={index === 0 ? 'primary' : 'default'}
                                                            size="small"
                                                        />
                                                    </TableCell>
                                                    <TableCell>{event.eventName}</TableCell>
                                                    <TableCell align="right" sx={{ color: '#2e7d32', fontWeight: 'bold' }}>
                                                        {event.scannedCount}
                                                    </TableCell>
                                                    <TableCell align="right">{event.ticketCount}</TableCell>
                                                    <TableCell align="right">
                                                        <strong>{attendanceRate}%</strong>
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
