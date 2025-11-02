import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Container,
  Typography,
  TextField,
  Button,
  Box,
  Paper,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import { getEventById, updateEvent } from '../api/events.api';
import type { Event } from '../types/event.interface';

const EditEventPage: React.FC = () => {
  const navigate = useNavigate();
  const { eventId } = useParams<{ eventId: string }>();
  const { enqueueSnackbar } = useSnackbar();
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [event, setEvent] = useState<Event | null>(null);
  const [isDirty, setIsDirty] = useState(false);
  const [showWarningDialog, setShowWarningDialog] = useState(false);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  
  // Form state
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    eventType: '',
    location: '',
    price: '0',
    imageUrl: '',
    capacity: '',
    startDateTime: '',
    endDateTime: '',
  });

  // Load event data
  useEffect(() => {
    const fetchEvent = async () => {
      if (!eventId) {
        setError('Event ID is missing');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const eventData = await getEventById(parseInt(eventId));
        setEvent(eventData);
        
        // Pre-populate form
        const formatDateTime = (date: Date) => {
          const d = new Date(date);
          const year = d.getFullYear();
          const month = String(d.getMonth() + 1).padStart(2, '0');
          const day = String(d.getDate()).padStart(2, '0');
          const hours = String(d.getHours()).padStart(2, '0');
          const minutes = String(d.getMinutes()).padStart(2, '0');
          return `${year}-${month}-${day}T${hours}:${minutes}`;
        };

        setFormData({
          title: eventData.title,
          description: eventData.description,
          eventType: eventData.category,
          location: eventData.location,
          price: eventData.price.toString(),
          imageUrl: eventData.imageUrl || '',
          capacity: eventData.capacity.toString(),
          startDateTime: formatDateTime(eventData.startDate),
          endDateTime: formatDateTime(eventData.endDate),
        });
        
        setError(null);
      } catch (err: any) {
        console.error('Error fetching event:', err);
        if (err.response?.status === 404) {
          setError('Event not found. It may have been deleted.');
        } else if (err.response?.status === 403) {
          setError("You don't have permission to edit this event.");
        } else {
          setError('Failed to load event. Please try again.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchEvent();
  }, [eventId]);

  // Warn before leaving with unsaved changes
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (isDirty) {
        e.preventDefault();
        e.returnValue = '';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [isDirty]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    setIsDirty(true);
  };

  const validateForm = (): string | null => {
    if (!formData.title.trim()) {
      return 'Event title is required';
    }
    if (formData.title.length > 100) {
      return 'Event title must be 100 characters or less';
    }
    if (!formData.description.trim()) {
      return 'Event description is required';
    }
    if (formData.description.length > 1000) {
      return 'Event description must be 1000 characters or less';
    }
    if (!formData.eventType.trim()) {
      return 'Event category is required';
    }
    if (!formData.location.trim()) {
      return 'Event location is required';
    }
    if (!formData.capacity || parseInt(formData.capacity) < 1) {
      return 'Capacity must be at least 1';
    }
    if (!formData.startDateTime) {
      return 'Start date and time is required';
    }
    if (!formData.endDateTime) {
      return 'End date and time is required';
    }

    const startDate = new Date(formData.startDateTime);
    const endDate = new Date(formData.endDateTime);
    
    if (endDate <= startDate) {
      return 'End date must be after start date';
    }

    // Check if capacity is reduced below existing ticket count
    if (event && event.ticketsSold) {
      const newCapacity = parseInt(formData.capacity);
      if (newCapacity < event.ticketsSold) {
        return `Capacity cannot be reduced below ${event.ticketsSold} (current number of sold tickets)`;
      }
    }

    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const validationError = validateForm();
    if (validationError) {
      enqueueSnackbar(validationError, { variant: 'error' });
      return;
    }

    // Show warning if event has existing registrations
    if (event && event.ticketsSold && event.ticketsSold > 0) {
      setShowWarningDialog(true);
      return;
    }

    await saveEvent();
  };

  const saveEvent = async () => {
    setShowWarningDialog(false);
    setSaving(true);

    const eventData = {
      title: formData.title,
      description: formData.description,
      eventType: formData.eventType,
      location: formData.location,
      price: parseFloat(formData.price) || 0,
      image: formData.imageUrl,
      capacity: parseInt(formData.capacity),
      startDateTime: formData.startDateTime,
      endDateTime: formData.endDateTime,
    };

    try {
      await updateEvent(parseInt(eventId!), eventData);
      enqueueSnackbar('Event updated successfully!', { variant: 'success' });
      setIsDirty(false);
      navigate('/my-events');
    } catch (err: any) {
      console.error('Event update failed:', err);
      if (err.response?.status === 403) {
        enqueueSnackbar("You don't have permission to edit this event.", { variant: 'error' });
      } else if (err.response?.status === 404) {
        enqueueSnackbar('Event not found.', { variant: 'error' });
      } else {
        enqueueSnackbar('Failed to update event. Please try again.', { variant: 'error' });
      }
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    if (isDirty) {
      setShowCancelDialog(true);
    } else {
      navigate('/my-events');
    }
  };

  const confirmCancel = () => {
    setShowCancelDialog(false);
    setIsDirty(false);
    navigate('/my-events');
  };

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ py: 4, display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
        <Button variant="outlined" onClick={() => navigate('/my-events')}>
          Back to My Events
        </Button>
      </Container>
    );
  }

  if (!event) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">Event not found</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" component="h1" fontWeight="bold" gutterBottom>
            Edit Event
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Event ID: {eventId}
          </Typography>
          {event.ticketsSold && event.ticketsSold > 0 && (
            <Alert severity="info" sx={{ mt: 2 }}>
              This event has {event.ticketsSold} registered attendee{event.ticketsSold > 1 ? 's' : ''}. 
              Changes may affect them.
            </Alert>
          )}
        </Box>

        <form onSubmit={handleSubmit}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            {/* Event Title */}
            <TextField
              fullWidth
              required
              label="Event Title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="e.g., Spring Music Festival"
              inputProps={{ maxLength: 100 }}
              helperText={`${formData.title.length}/100 characters`}
            />

            {/* Description */}
            <TextField
              fullWidth
              required
              multiline
              rows={4}
              label="Description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Provide a detailed description of your event..."
              inputProps={{ maxLength: 1000 }}
              helperText={`${formData.description.length}/1000 characters`}
            />

            {/* Event Type / Category and Location */}
            <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
              <TextField
                fullWidth
                required
                label="Event Category"
                name="eventType"
                value={formData.eventType}
                onChange={handleChange}
                placeholder="e.g., Academic, Cultural, Sports, Social"
              />

              <TextField
                fullWidth
                required
                label="Location"
                name="location"
                value={formData.location}
                onChange={handleChange}
                placeholder="e.g., Campus Quad, Main Hall"
              />
            </Box>

            {/* Price and Capacity */}
            <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
              <TextField
                fullWidth
                required
                type="number"
                label="Ticket Price"
                name="price"
                value={formData.price}
                onChange={handleChange}
                inputProps={{ min: 0, step: 0.01 }}
                helperText="Enter 0 for free events"
              />

              <TextField
                fullWidth
                required
                type="number"
                label="Capacity"
                name="capacity"
                value={formData.capacity}
                onChange={handleChange}
                inputProps={{ min: event.ticketsSold || 1 }}
                helperText={
                  event.ticketsSold 
                    ? `Minimum: ${event.ticketsSold} (current ticket sales)` 
                    : 'Maximum number of attendees'
                }
              />
            </Box>

            {/* Image URL */}
            <TextField
              fullWidth
              label="Image URL (Optional)"
              name="imageUrl"
              value={formData.imageUrl}
              onChange={handleChange}
              placeholder="https://example.com/image.jpg"
              helperText="Provide a link to an image for your event"
            />

            {/* Start and End Date/Time */}
            <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
              <TextField
                fullWidth
                required
                type="datetime-local"
                label="Start Date & Time"
                name="startDateTime"
                value={formData.startDateTime}
                onChange={handleChange}
                InputLabelProps={{ shrink: true }}
              />

              <TextField
                fullWidth
                required
                type="datetime-local"
                label="End Date & Time"
                name="endDateTime"
                value={formData.endDateTime}
                onChange={handleChange}
                InputLabelProps={{ shrink: true }}
              />
            </Box>

            {/* Action Buttons */}
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', pt: 2 }}>
              <Button
                variant="outlined"
                size="large"
                onClick={handleCancel}
                disabled={saving}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={saving}
              >
                {saving ? 'Saving...' : 'Save Changes'}
              </Button>
            </Box>
          </Box>
        </form>
      </Paper>

      {/* Warning Dialog for events with registrations */}
      <Dialog open={showWarningDialog} onClose={() => setShowWarningDialog(false)}>
        <DialogTitle>Confirm Changes</DialogTitle>
        <DialogContent>
          <DialogContentText>
            This event has {event.ticketsSold} registered attendee{event.ticketsSold! > 1 ? 's' : ''}. 
            Your changes may affect them. Are you sure you want to continue?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowWarningDialog(false)}>Cancel</Button>
          <Button onClick={saveEvent} variant="contained" autoFocus>
            Confirm Changes
          </Button>
        </DialogActions>
      </Dialog>

      {/* Cancel Confirmation Dialog */}
      <Dialog open={showCancelDialog} onClose={() => setShowCancelDialog(false)}>
        <DialogTitle>Unsaved Changes</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You have unsaved changes. Are you sure you want to leave without saving?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowCancelDialog(false)}>Stay</Button>
          <Button onClick={confirmCancel} color="error" autoFocus>
            Leave Without Saving
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default EditEventPage;
