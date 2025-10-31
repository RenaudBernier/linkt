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
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { getOrganizerEvents } from '../api/events.api';
import type { Event } from '../types/event.interface';

// Event status types
type EventStatus = 'all' | 'upcoming' | 'past' | 'draft' | 'cancelled';

// Analytics modal props type
interface AnalyticsModalProps {
  open: boolean;
  onClose: () => void;
  event: Event;
}

// Analytics Modal Component
const AnalyticsModal: React.FC<AnalyticsModalProps> = ({ open, onClose, event }) => {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Event Analytics: {event.title}</DialogTitle>
      <DialogContent>
        <Box sx={{ p: 2 }}>
          <Typography variant="body1" gutterBottom>
            Tickets Sold: {event.ticketsSold} / {event.capacity}
          </Typography>
          <Typography variant="body1" gutterBottom>
            Capacity Utilization: {((event.ticketsSold / event.capacity) * 100).toFixed(1)}%
          </Typography>
          <Typography variant="body1">
            Total Revenue: ${(event.ticketsSold * event.price).toFixed(2)}
          </Typography>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
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
  const [statusFilter, setStatusFilter] = useState<EventStatus>('all');
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null);
  const [showAnalytics, setShowAnalytics] = useState(false);

  // Fetch events on component mount
  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        const data = await getOrganizerEvents();
        setEvents(data);
        setError(null);
      } catch (err) {
        setError('Failed to load events. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  // Filter events based on search query and status
  const filteredEvents = events.filter((event) => {
    const matchesSearch = 
      event.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      event.description.toLowerCase().includes(searchQuery.toLowerCase());
    
    if (statusFilter === 'all') return matchesSearch;
    if (statusFilter === 'upcoming') return matchesSearch && new Date(event.startDate) > new Date();
    if (statusFilter === 'past') return matchesSearch && new Date(event.endDate) < new Date();
    return matchesSearch && event.status === statusFilter;
  });

  // Event handlers
  const handleEditEvent = (eventId: number) => {
    navigate(`/events/${eventId}/edit`);
  };

  const handleViewAttendees = (eventId: number) => {
    navigate(`/events/${eventId}/attendees`);
  };

  const handleShowAnalytics = (event: Event) => {
    setSelectedEvent(event);
    setShowAnalytics(true);
  };

  const handleDuplicateEvent = (event: Event) => {
    // TODO: Implement event duplication logic
    navigate('/events/create', { state: { duplicateFrom: event } });
  };

  const handleCancelEvent = async (eventId: number) => {
    // TODO: Implement event cancellation logic
    if (window.confirm('Are you sure you want to cancel this event?')) {
      try {
        // await cancelEvent(eventId);
        setEvents(events.map(event => 
          event.id === eventId 
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
      <Container sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header Section */}
      <Typography variant="h4" component="h1" gutterBottom>
        My Events
      </Typography>
      <Typography variant="subtitle1" color="text.secondary" paragraph>
        Manage and monitor all your events in one place
      </Typography>

      {/* Filters Section */}
      <Box sx={{ mb: 4, display: 'flex', gap: 2 }}>
        <TextField
          placeholder="Search events..."
          variant="outlined"
          size="small"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon sx={{ mr: 1, color: 'text.secondary' }} />,
          }}
          sx={{ flexGrow: 1 }}
        />
        <Select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as EventStatus)}
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

      {/* Error Message */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Empty State */}
      {events.length === 0 ? (
        <Box 
          sx={{ 
            textAlign: 'center', 
            py: 8,
            backgroundColor: 'background.paper',
            borderRadius: 1
          }}
        >
          <Typography variant="h6" gutterBottom>
            You haven't created any events yet
          </Typography>
          <Typography variant="body1" color="text.secondary" paragraph>
            Get started by creating your first event!
          </Typography>
          <Button
            variant="contained"
            color="primary"
            onClick={() => navigate('/events/create')}
          >
            Create Event
          </Button>
        </Box>
      ) : (
        // Events Grid
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
          {filteredEvents.map((event) => (
            <Box key={event.id} sx={{ width: { xs: '100%', sm: '45%', md: '30%' } }}>
              <Card>
                <CardMedia
                  component="img"
                  height="140"
                  image={event.imageUrl || '/default-event-image.jpg'}
                  alt={event.title}
                />
                <CardContent>
                  <Typography variant="h6" gutterBottom>
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
                      mb: 2
                    }}
                  >
                    {event.description}
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 1 }}>
                    <Chip 
                      label={event.status} 
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
                  <Typography variant="body2">
                    Tickets Sold: {event.ticketsSold}/{event.capacity}
                  </Typography>
                </CardContent>
                <CardActions sx={{ flexWrap: 'wrap', gap: 1, p: 2 }}>
                  <Button
                    size="small"
                    startIcon={<EditIcon />}
                    onClick={() => handleEditEvent(event.id)}
                  >
                    Edit
                  </Button>
                  <Button
                    size="small"
                    startIcon={<PeopleIcon />}
                    onClick={() => handleViewAttendees(event.id)}
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
                      onClick={() => handleCancelEvent(event.id)}
                    >
                      Cancel
                    </Button>
                  )}
                </CardActions>
              </Card>
            </Box>
          ))}
        </Box>
      )}

      {/* Analytics Modal */}
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