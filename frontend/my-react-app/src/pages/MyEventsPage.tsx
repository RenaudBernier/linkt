import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Button,
  Chip,
  TextField,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Box,
  Alert,
} from '@mui/material';
import {
  Edit as EditIcon,
  People as PeopleIcon,
  Assessment as AnalyticsIcon,
  Cancel as CancelIcon,
  ContentCopy as DuplicateIcon,
  Search as SearchIcon,
  QrCodeScanner as QrCodeScannerIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { getOrganizerEvents } from '../api/events.api';
import type { Event } from '../types/event.interface';

type EventStatusFilter = 'all' | 'upcoming' | 'past' | 'draft' | 'cancelled';

interface AnalyticsModalProps {
  open: boolean;
  onClose: () => void;
  event: Event;
}

const AnalyticsModal: React.FC<AnalyticsModalProps> = ({ open, onClose, event }) => {
  const ticketsSold = event.ticketsSold || 0;
  const capacity = event.capacity;
  const utilizationPercentage = capacity > 0 ? ((ticketsSold / capacity) * 100).toFixed(1) : '0.0';
  const totalRevenue = (ticketsSold * event.price).toFixed(2);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Event Analytics: {event.title}</DialogTitle>
      <DialogContent>
        <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Box>
            <Typography variant="subtitle2" color="text.secondary">
              Tickets Sold
            </Typography>
            <Typography variant="h4" fontWeight="bold">
              {ticketsSold} / {capacity}
            </Typography>
          </Box>
          <Box>
            <Typography variant="subtitle2" color="text.secondary">
              Capacity Utilization
            </Typography>
            <Typography variant="h4" fontWeight="bold" color="primary">
              {utilizationPercentage}%
            </Typography>
          </Box>
          <Box>
            <Typography variant="subtitle2" color="text.secondary">
              Total Revenue
            </Typography>
            <Typography variant="h4" fontWeight="bold" color="success.main">
              ${totalRevenue}
            </Typography>
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} variant="contained">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

const MyEventsPage: React.FC = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<EventStatusFilter>('all');
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null);
  const [showAnalytics, setShowAnalytics] = useState(false);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        const data = await getOrganizerEvents();
        setEvents(data);
        setError(null);
      } catch (err: any) {
        console.error('Error fetching organizer events:', err);
        setError(err.response?.data || 'Failed to load events. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  const filteredEvents = events.filter((event) => {
    const matchesSearch = 
      event.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      event.description.toLowerCase().includes(searchQuery.toLowerCase());
    
    if (statusFilter === 'all') return matchesSearch;
    if (statusFilter === 'upcoming') return matchesSearch && new Date(event.startDate) > new Date();
    if (statusFilter === 'past') return matchesSearch && new Date(event.endDate) < new Date();
    return matchesSearch && event.status === statusFilter;
  });

  const handleEditEvent = (eventId: number) => {
    navigate(`/events/${eventId}/edit`);
  };

  const handleViewAttendees = (eventId: number) => {
    navigate(`/events/${eventId}/attendees`);
  };

  const handleScanTickets = (eventId: number) => {
    navigate(`/my-events/scan/${eventId}`);
  };

  const handleShowAnalytics = (event: Event) => {
    setSelectedEvent(event);
    setShowAnalytics(true);
  };

  const handleDuplicateEvent = (event: Event) => {
    navigate('/events/create', { state: { duplicateFrom: event } });
  };

  const handleCancelEvent = async (eventId: number) => {
    if (window.confirm('Are you sure you want to cancel this event? This action cannot be undone.')) {
      try {
        setEvents(events.map(event => 
          event.eventID === eventId 
            ? { ...event, status: 'cancelled' } 
            : event
        ));
      } catch (err) {
        setError('Failed to cancel event. Please try again.');
      }
    }
  };

  if (loading) {
    return (
      <Container sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <div>
          <Typography variant="h4" component="h1" fontWeight="bold" gutterBottom>
            My Events
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            Manage and monitor all your events in one place
          </Typography>
        </div>
        <Button
          variant="contained"
          color="primary"
          size="large"
          onClick={() => navigate('/events/create')}
        >
          Create Event
        </Button>
      </Box>

      <Box sx={{ mb: 4, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField
          placeholder="Search events..."
          variant="outlined"
          size="small"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
          }}
          sx={{ flexGrow: 1, minWidth: 250 }}
        />
        <Select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as EventStatusFilter)}
          size="small"
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="all">All Events</MenuItem>
          <MenuItem value="upcoming">Upcoming</MenuItem>
          <MenuItem value="past">Past</MenuItem>
          <MenuItem value="draft">Draft</MenuItem>
          <MenuItem value="cancelled">Cancelled</MenuItem>
        </Select>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {events.length === 0 ? (
        <Box 
          sx={{ 
            textAlign: 'center', 
            py: 10,
            backgroundColor: 'background.paper',
            borderRadius: 2,
            border: '2px dashed',
            borderColor: 'divider'
          }}
        >
          <Typography variant="h5" gutterBottom fontWeight="medium">
            You haven't created any events yet
          </Typography>
          <Typography variant="body1" color="text.secondary" paragraph sx={{ mt: 2 }}>
            Get started by creating your first event!
          </Typography>
          <Button
            variant="contained"
            color="primary"
            size="large"
            onClick={() => navigate('/events/create')}
            sx={{ mt: 2 }}
          >
            Create Your First Event
          </Button>
        </Box>
      ) : filteredEvents.length === 0 ? (
        <Box 
          sx={{ 
            textAlign: 'center', 
            py: 8,
            backgroundColor: 'background.paper',
            borderRadius: 2
          }}
        >
          <Typography variant="h6" gutterBottom>
            No events match your search criteria
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Try adjusting your filters or search terms
          </Typography>
        </Box>
      ) : (
        <Box sx={{ 
          display: 'grid', 
          gridTemplateColumns: { 
            xs: '1fr', 
            sm: 'repeat(2, 1fr)', 
            md: 'repeat(3, 1fr)' 
          },
          gap: 3 
        }}>
          {filteredEvents.map((event) => (
            <Card key={event.eventID} sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
              <CardMedia
                component="img"
                height="180"
                image={event.imageUrl || event.image[0] || '/default-event-image.jpg'}
                alt={event.title}
                sx={{ objectFit: 'cover' }}
              />
              <CardContent sx={{ flexGrow: 1 }}>
                <Typography variant="h6" fontWeight="bold" gutterBottom>
                  {event.title}
                </Typography>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                    mb: 2,
                    minHeight: 40
                  }}
                >
                  {event.description}
                </Typography>
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                  <Chip 
                    label={event.status || 'published'} 
                    color={
                      event.status === 'draft' ? 'default' :
                      event.status === 'cancelled' ? 'error' :
                      'success'
                    }
                    size="small"
                  />
                  <Chip 
                    label={event.price === 0 ? 'Free' : `$${event.price}`}
                    variant="outlined"
                    size="small"
                  />
                </Box>
                <Typography variant="body2" color="text.secondary">
                  📍 {event.location}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  🎫 Tickets Sold: {event.ticketsSold || 0}/{event.capacity}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  📅 {new Date(event.startDate).toLocaleDateString()}
                </Typography>
              </CardContent>
              <CardActions sx={{ flexWrap: 'wrap', gap: 0.5, p: 2, pt: 0 }}>
                <Button
                  size="small"
                  startIcon={<EditIcon />}
                  onClick={() => handleEditEvent(event.eventID)}
                >
                  Edit
                </Button>
                <Button
                  size="small"
                  startIcon={<QrCodeScannerIcon />}
                  onClick={() => handleScanTickets(event.eventID)}
                >
                  Scan
                </Button>
                <Button
                  size="small"
                  startIcon={<PeopleIcon />}
                  onClick={() => handleViewAttendees(event.eventID)}
                >
                  Attendees
                </Button>
                <Button
                  size="small"
                  startIcon={<AnalyticsIcon />}
                  onClick={() => handleShowAnalytics(event)}
                >
                  Analytics
                </Button>
                <Button
                  size="small"
                  startIcon={<DuplicateIcon />}
                  onClick={() => handleDuplicateEvent(event)}
                >
                  Duplicate
                </Button>
                {event.status !== 'cancelled' && (
                  <Button
                    size="small"
                    color="error"
                    startIcon={<CancelIcon />}
                    onClick={() => handleCancelEvent(event.eventID)}
                  >
                    Cancel
                  </Button>
                )}
              </CardActions>
            </Card>
          ))}
        </Box>
      )}

      {selectedEvent && (
        <AnalyticsModal
          open={showAnalytics}
          onClose={() => setShowAnalytics(false)}
          event={selectedEvent}
        />
      )}
    </Container>
  );
};

export default MyEventsPage;