import { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Chip,
  Stack,
  Divider,
  Slider,
  Button,
} from '@mui/material';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import type { SelectChangeEvent } from '@mui/material';
import type { Event } from '../types/event.interface';
import { saveEvent, checkIfSaved } from '../api/savedEvents.api';
import { useSnackbar } from 'notistack';

function EventsPage() {
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [savingEventId, setSavingEventId] = useState<number | null>(null);
  const [savedEventIds, setSavedEventIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/events');
        if (!response.ok) {
          throw new Error('Failed to fetch events');
        }
        const data = await response.json();

        // Transform backend data to match frontend interface
        const transformedEvents: Event[] = data.map((event: any) => ({
          eventID: event.eventId,
          title: event.title,
          description: event.description,
          category: event.eventType,
          image: event.imageUrl ? [event.imageUrl] : ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
          price: event.price || 0,
          startDate: new Date(event.startDateTime),
          endDate: new Date(event.endDateTime),
          location: event.location,
          capacity: event.capacity,
        }));

        setEvents(transformedEvents);
        setLoading(false);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred');
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  useEffect(() => {
    const checkSavedEvents = async () => {
      if (!localStorage.getItem('token')) return;

      const savedIds = new Set<number>();
      for (const event of events) {
        try {
          const isSaved = await checkIfSaved(event.eventID);
          if (isSaved) {
            savedIds.add(event.eventID);
          }
        } catch (error) {
          // Ignore errors for individual checks
        }
      }
      setSavedEventIds(savedIds);
    };

    if (events.length > 0) {
      checkSavedEvents();
    }
  }, [events]);

  const handleAddToFavorites = async (eventId: number) => {
    if (!localStorage.getItem('token')) {
      navigate('/login');
      return;
    }

    setSavingEventId(eventId);
    try {
      await saveEvent(eventId);
      setSavedEventIds(prev => new Set(prev).add(eventId));
      enqueueSnackbar('Event saved!', { variant: 'success' });
    } catch (error: any) {
      if (error.response?.status === 401) {
        navigate('/login');
      } else if (error.response?.status === 409) {
        enqueueSnackbar('Event already saved', { variant: 'info' });
      } else {
        console.error('Error saving event:', error);
        enqueueSnackbar('Failed to save event. Please try again.', { variant: 'error' });
      }
    } finally {
      setSavingEventId(null);
    }
  };

  const [sortBy, setSortBy] = useState<string>('date');
  const [filterCategory, setFilterCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Get min and max prices from events
  const { minPrice, maxPrice } = useMemo(() => {
    if (events.length === 0) return { minPrice: 0, maxPrice: 100 };
    const prices = events.map(event => event.price);
    return {
      minPrice: Math.min(...prices),
      maxPrice: Math.max(...prices),
    };
  }, [events]);

  const [priceRange, setPriceRange] = useState<number[]>([minPrice, maxPrice]);

  // Get unique categories
  const categories = useMemo(() => {
    const cats = new Set(events.map(event => event.category));
    return ['all', ...Array.from(cats)];
  }, [events]);

  // Filter and sort events
  const filteredAndSortedEvents = useMemo(() => {
    let filtered = events;

    // Filter by category
    if (filterCategory !== 'all') {
      filtered = filtered.filter(event => event.category === filterCategory);
    }

    // Filter by search query
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(event =>
        event.title.toLowerCase().includes(query) ||
        event.description.toLowerCase().includes(query) ||
        event.location.toLowerCase().includes(query)
      );
    }

    // Filter by price range
    filtered = filtered.filter(
      event => event.price >= priceRange[0] && event.price <= priceRange[1]
    );

    // Sort events
    const sorted = [...filtered].sort((a, b) => {
      switch (sortBy) {
        case 'date':
          return a.startDate.getTime() - b.startDate.getTime();
        case 'title':
          return a.title.localeCompare(b.title);
        case 'price':
          return a.price - b.price;
        case 'location':
          return a.location.localeCompare(b.location);
        default:
          return 0;
      }
    });

    return sorted;
  }, [events, filterCategory, searchQuery, sortBy, priceRange]);

  const handleSortChange = (event: SelectChangeEvent) => {
    setSortBy(event.target.value);
  };

  const handleCategoryChange = (event: SelectChangeEvent) => {
    setFilterCategory(event.target.value);
  };

  const handlePriceChange = (_event: any, newValue: number | number[]) => {
    setPriceRange(newValue as number[]);
  };

  const valuetext = (value: number) => {
    return value === 0 ? 'Free' : `$${value}`;
  };

  const formatDate = (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    }).format(date);
  };

  const formatTime = (date: Date) => {
    return new Intl.DateTimeFormat('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    }).format(date);
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Typography variant="h5" textAlign="center">Loading events...</Typography>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Typography variant="h5" textAlign="center" color="error">
          Error loading events: {error}
        </Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" component="h1" gutterBottom fontWeight="bold">
          Browse Events
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Discover and explore upcoming campus events
        </Typography>
      </Box>

      {/* Filters and Search */}
      <Box sx={{ mb: 4 }}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              label="Search events"
              variant="outlined"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by title, description, or location..."
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <FormControl fullWidth>
              <InputLabel>Category</InputLabel>
              <Select
                value={filterCategory}
                label="Category"
                onChange={handleCategoryChange}
              >
                {categories.map((category) => (
                  <MenuItem key={category} value={category}>
                    {category.charAt(0).toUpperCase() + category.slice(1)}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <FormControl fullWidth>
              <InputLabel>Sort By</InputLabel>
              <Select
                value={sortBy}
                label="Sort By"
                onChange={handleSortChange}
              >
                <MenuItem value="date">Date</MenuItem>
                <MenuItem value="title">Title</MenuItem>
                <MenuItem value="price">Price</MenuItem>
                <MenuItem value="location">Location</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <Box sx={{ px: 2 }}>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Price Range: {priceRange[0] === 0 ? 'Free' : `$${priceRange[0]}`} - {priceRange[1] === 0 ? 'Free' : `$${priceRange[1]}`}
              </Typography>
              <Slider
                value={priceRange}
                onChange={handlePriceChange}
                valueLabelDisplay="auto"
                getAriaValueText={valuetext}
                valueLabelFormat={valuetext}
                min={minPrice}
                max={maxPrice}
                sx={{
                  '& .MuiSlider-thumb': {
                    width: 20,
                    height: 20,
                  },
                }}
              />
            </Box>
          </Grid>
        </Grid>
      </Box>

      {/* Results count */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="body2" color="text.secondary">
          Showing {filteredAndSortedEvents.length} event{filteredAndSortedEvents.length !== 1 ? 's' : ''}
        </Typography>
      </Box>

      {/* Events Grid */}
      <Grid container spacing={3}>
        {filteredAndSortedEvents.map((event) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={event.eventID}>
            <Card
              sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                borderRadius: 3,
                overflow: 'hidden',
                transition: 'all 0.3s ease-in-out',
                '&:hover': {
                  transform: 'translateY(-8px)',
                  boxShadow: '0 12px 24px rgba(0,0,0,0.15)',
                },
              }}
            >
              <CardMedia
                component="img"
                height="220"
                image={event.image[0]}
                alt={event.title}
                sx={{ objectFit: 'cover' }}
              />
              <CardContent sx={{ flexGrow: 1, p: 2.5 }}>
                  <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
                    <Chip
                      label={event.category}
                      size="small"
                      color="primary"
                      sx={{
                        fontWeight: 600,
                        borderRadius: 2,
                      }}
                    />
                    {event.price === 0 ? (
                      <Chip
                        label="Free"
                        size="small"
                        color="success"
                        sx={{
                          fontWeight: 600,
                          borderRadius: 2,
                        }}
                      />
                    ) : (
                      <Chip
                        label={`$${event.price}`}
                        size="small"
                        sx={{
                          fontWeight: 600,
                          borderRadius: 2,
                        }}
                      />
                    )}
                  </Stack>

                  <Typography
                    gutterBottom
                    variant="h6"
                    component="h2"
                    fontWeight="bold"
                    sx={{ mb: 1.5 }}
                  >
                    {event.title}
                  </Typography>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                      mb: 2,
                      display: '-webkit-box',
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden',
                      lineHeight: 1.6,
                    }}
                  >
                    {event.description}
                  </Typography>

                  <Divider sx={{ my: 2 }} />

                  <Stack spacing={1.5}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                      <CalendarTodayIcon
                        sx={{
                          fontSize: 18,
                          color: 'primary.main',
                        }}
                      />
                      <Typography variant="body2" color="text.primary" fontWeight="medium">
                        {formatDate(event.startDate)}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                      <AccessTimeIcon
                        sx={{
                          fontSize: 18,
                          color: 'primary.main',
                        }}
                      />
                      <Typography variant="body2" color="text.secondary">
                        {formatTime(event.startDate)} - {formatTime(event.endDate)}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                      <LocationOnIcon
                        sx={{
                          fontSize: 18,
                          color: 'primary.main',
                        }}
                      />
                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {event.location}
                      </Typography>
                    </Box>
                  </Stack>
              </CardContent>
              <CardActions sx={{ px: 2.5, pb: 2.5, pt: 0, gap: 1 }}>
                <Button
                  onClick={() => navigate(`/checkout/${event.eventID}`)}
                  variant="contained"
                  size="small"
                >
                  Buy ticket
                </Button>
                <Button
                  onClick={() => handleAddToFavorites(event.eventID)}
                  variant={savedEventIds.has(event.eventID) ? "contained" : "outlined"}
                  size="small"
                  disabled={savingEventId === event.eventID}
                >
                  {savedEventIds.has(event.eventID) ? 'Saved' : 'Save event'}
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* No results message */}
      {filteredAndSortedEvents.length === 0 && (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary">
            No events found matching your criteria
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Try adjusting your filters or search query
          </Typography>
        </Box>
      )}
    </Container>
  );
}

export default EventsPage;
