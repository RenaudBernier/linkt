import { useState, useMemo } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardMedia,
  CardContent,
  CardActionArea,
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
} from '@mui/material';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import type { SelectChangeEvent } from '@mui/material';
import type { Event } from '../types/event.interface';

// Mock data for demonstration
const mockEvents: Event[] = [
  {
    eventID: 1,
    title: 'Frosh Night',
    description: 'New to school and don\'t know where to start? Have some drinks, play games and meet some new people at the school\'s frosh night!',
    category: 'Social',
    image: ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    price: 0,
    startDate: new Date('2025-10-15T19:00:00'),
    endDate: new Date('2025-10-15T23:00:00'),
    location: 'Student Union Building',
    capacity: 200,
  },
  {
    eventID: 2,
    title: 'DJ Night',
    description: 'The EDM Club is organizing an all-night dance festival on the 22nd of October! Click for more details!',
    category: 'Music',
    image: ['/src/images/swag-slayer-dd2EOQBycJY-unsplash.jpg'],
    price: 15,
    startDate: new Date('2025-10-22T20:00:00'),
    endDate: new Date('2025-10-23T02:00:00'),
    location: 'Campus Arena',
    capacity: 500,
  },
  {
    eventID: 3,
    title: 'Campus Museum Tour',
    description: 'Join us for a tour of the campus museum where you can browse artifacts of some of the school\'s greatest alumni!',
    category: 'Educational',
    image: ['/src/images/willian-justen-de-vasconcellos-_krHI5-8yA4-unsplash.jpg'],
    price: 0,
    startDate: new Date('2025-10-18T14:00:00'),
    endDate: new Date('2025-10-18T16:00:00'),
    location: 'Campus Museum',
    capacity: 30,
  },
  {
    eventID: 4,
    title: 'Tech Career Fair',
    description: 'Connect with leading tech companies and explore internship and job opportunities. Meet recruiters from top firms!',
    category: 'Career',
    image: ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    price: 0,
    startDate: new Date('2025-10-25T10:00:00'),
    endDate: new Date('2025-10-25T16:00:00'),
    location: 'Convention Center',
    capacity: 1000,
  },
  {
    eventID: 5,
    title: 'Open Mic Night',
    description: 'Showcase your talent or enjoy performances from fellow students. Poetry, music, comedy - all welcome!',
    category: 'Arts',
    image: ['/src/images/swag-slayer-dd2EOQBycJY-unsplash.jpg'],
    price: 5,
    startDate: new Date('2025-10-20T18:00:00'),
    endDate: new Date('2025-10-20T22:00:00'),
    location: 'Student Cafe',
    capacity: 80,
  },
  {
    eventID: 6,
    title: 'Hackathon 2025',
    description: '24-hour coding marathon! Build innovative solutions, win prizes, and network with industry professionals.',
    category: 'Technology',
    image: ['/src/images/willian-justen-de-vasconcellos-_krHI5-8yA4-unsplash.jpg'],
    price: 0,
    startDate: new Date('2025-11-01T09:00:00'),
    endDate: new Date('2025-11-02T09:00:00'),
    location: 'Engineering Building',
    capacity: 150,
  },
  {
    eventID: 7,
    title: 'Halloween Costume Party',
    description: 'Get ready for the spookiest night of the year! Costume contest with amazing prizes, DJ, and themed refreshments.',
    category: 'Social',
    image: ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    price: 10,
    startDate: new Date('2025-10-31T20:00:00'),
    endDate: new Date('2025-11-01T01:00:00'),
    location: 'Student Union Ballroom',
    capacity: 300,
  },
  {
    eventID: 8,
    title: 'Yoga & Wellness Workshop',
    description: 'Destress and recharge with guided yoga sessions, meditation, and wellness tips from certified instructors.',
    category: 'Wellness',
    image: ['/src/images/swag-slayer-dd2EOQBycJY-unsplash.jpg'],
    price: 0,
    startDate: new Date('2025-10-28T17:00:00'),
    endDate: new Date('2025-10-28T19:00:00'),
    location: 'Recreation Center',
    capacity: 50,
  },
  {
    eventID: 9,
    title: 'International Food Festival',
    description: 'Taste cuisines from around the world! Student organizations showcase their cultural dishes and traditions.',
    category: 'Cultural',
    image: ['/src/images/willian-justen-de-vasconcellos-_krHI5-8yA4-unsplash.jpg'],
    price: 12,
    startDate: new Date('2025-11-05T12:00:00'),
    endDate: new Date('2025-11-05T18:00:00'),
    location: 'Campus Quad',
    capacity: 500,
  },
  {
    eventID: 10,
    title: 'Guest Speaker: AI & The Future',
    description: 'Renowned AI researcher Dr. Sarah Chen discusses the impact of artificial intelligence on society and careers.',
    category: 'Educational',
    image: ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    price: 0,
    startDate: new Date('2025-11-08T18:30:00'),
    endDate: new Date('2025-11-08T20:00:00'),
    location: 'Auditorium Hall',
    capacity: 400,
  },
  {
    eventID: 11,
    title: 'Basketball Tournament Finals',
    description: 'Cheer for your team at the intramural basketball championship! Exciting finals with live commentary.',
    category: 'Sports',
    image: ['/src/images/swag-slayer-dd2EOQBycJY-unsplash.jpg'],
    price: 0,
    startDate: new Date('2025-11-12T19:00:00'),
    endDate: new Date('2025-11-12T21:00:00'),
    location: 'Sports Complex',
    capacity: 800,
  },
  {
    eventID: 12,
    title: 'Film Festival Screening',
    description: 'Student-produced short films premiere at our annual film festival. Q&A with filmmakers afterwards.',
    category: 'Arts',
    image: ['/src/images/willian-justen-de-vasconcellos-_krHI5-8yA4-unsplash.jpg'],
    price: 5,
    startDate: new Date('2025-11-15T19:00:00'),
    endDate: new Date('2025-11-15T22:00:00'),
    location: 'Media Arts Theater',
    capacity: 120,
  },
];

function EventsPage() {
  const [sortBy, setSortBy] = useState<string>('date');
  const [filterCategory, setFilterCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Get min and max prices from events
  const { minPrice, maxPrice } = useMemo(() => {
    const prices = mockEvents.map(event => event.price);
    return {
      minPrice: Math.min(...prices),
      maxPrice: Math.max(...prices),
    };
  }, []);

  const [priceRange, setPriceRange] = useState<number[]>([minPrice, maxPrice]);

  // Get unique categories
  const categories = useMemo(() => {
    const cats = new Set(mockEvents.map(event => event.category));
    return ['all', ...Array.from(cats)];
  }, []);

  // Filter and sort events
  const filteredAndSortedEvents = useMemo(() => {
    let filtered = mockEvents;

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
  }, [filterCategory, searchQuery, sortBy, priceRange]);

  const handleSortChange = (event: SelectChangeEvent) => {
    setSortBy(event.target.value);
  };

  const handleCategoryChange = (event: SelectChangeEvent) => {
    setFilterCategory(event.target.value);
  };

  const handlePriceChange = (_event: Event, newValue: number | number[], _activeThumb: number) => {
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
              <CardActionArea
                component="a"
                href="https://google.com"
                sx={{
                  flexGrow: 1,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'stretch',
                  height: '100%',
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
              </CardActionArea>
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
