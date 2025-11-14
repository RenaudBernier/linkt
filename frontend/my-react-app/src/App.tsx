// src/App.tsx
import {Routes, Route, useNavigate, Outlet} from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import { useState, useEffect } from 'react';
import {
  Toolbar,
  Box,
  Typography,
  Card,
  CardMedia,
  CardContent,
  Button,
  CircularProgress,
} from "@mui/material";
//import '@fontsource-variable/cabin';
import './App.css';
import SignUp from './SignUp';
import Login from './Login';
import CreateData from './CreateData';
import CheckoutPage from "./components/CheckoutPage.tsx";
import Header from "./components/Header.tsx";
import Footer from "./components/Footer";
import EventsPage from "./pages/EventsPage.tsx";
import MyTickets from './mytickets.tsx';
import Settings from "./components/Settings";
import SavedTickets from "./SavedTickets.tsx";
import OrganiserApprovePage from "./pages/OrganiserApprovePage.tsx";
import RegisteredStudentsPage from "./pages/RegisteredStudentsPage.tsx";
import MyEventsPage from "./pages/MyEventsPage.tsx";
import EditEventPage from "./pages/EditEventPage.tsx";
import ScanTicketPage from "./pages/ScanTicketPage.tsx";
import AdminDashboard from "./pages/AdminDashboard.tsx";
import { getTopEvents } from './api/events.api';
import type { Event } from './types/event.interface';
function MainLayout() {
    return (
        <>
            <Header/>
            <Toolbar/>

            <Outlet />
            <Footer />
        </>
    );
}

function BlankLayout() {
    return (
        <main>
            <Outlet />
        </main>
    );
}


function Home() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [topEvents, setTopEvents] = useState<Event[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchTopEvents = async () => {
            try {
                const events = await getTopEvents();
                setTopEvents(events);
            } catch (error) {
                console.error('Failed to fetch top events:', error);
            } finally {
                setLoading(false);
            }
        };
        
        fetchTopEvents();
    }, []);

    const handleEventClick = (eventId: number) => {
        if (!user) {
            // User not logged in, redirect to login
            navigate('/login');
        } else {
            // User is logged in, proceed to checkout
            navigate(`/checkout/${eventId}`);
        }
    };

    return (
        <>
            
      <Box component = "section" sx = {{p: 2, width: '100%', bgcolor: '#008dd5', color: 'white', border: '5px white'}}>
        <br></br>
        <Typography variant = "h2"> Linkt </Typography>
        <Typography variant = "body1"> Welcome to our comprehensive campus Events & Ticketing service, designed to streamline event management and boost student engagement for students! 
          We allow students to easily discover and search for events using comprehensive filters, save them to their personal calendar, and claim digital, QR-coded
          tickets (free or mock paid) for check-in! If you are an organizer, then you are welcome too! Organizers benefit from the ability to create, manage, 
          and track attendance for their events. They can also gain valuable insights for their events via our analytics dashboards. We are also welcoming 
          campus administrators, who can oversee organizations and moderate all content. Linkt is our brand-new system that connects students with campus life 
          while providing essential tools for hosting and administrating events! </Typography> 
        {user?.userType == 'organizer' && ( <br></br> )}
        {user?.userType == 'organizer' && ( <br></br> )}

        {user?.userType == 'organizer' && (
        <Typography variant = "h5">
            Hey! We noticed that you're an organizer! Feel free to add your event to our page!
        </Typography>)}

      {user?.userType == 'organizer' && (
        <button onClick={() => navigate('/CreateData')}>
        Create an Event!
        </button>)}

      </Box>




      <Box component = "section" sx = {{py: 6, px: 3, width: '100%', bgcolor: '#373f51', color: 'white'}}>
        <Box sx={{ maxWidth: '1600px', mx: 'auto' }}>
          <Typography variant = "h2" sx={{ mb: 4 }}> Top Events </Typography>
          
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress sx={{ color: 'white' }} />
            </Box>
          ) : topEvents.length === 0 ? (
            <Typography variant = "h5"> No events available yet. Check back soon! </Typography>
          ) : (
            <Box 
              sx={{ 
                display: 'grid',
                gridTemplateColumns: {
                  xs: '1fr',
                  sm: 'repeat(2, 1fr)',
                  md: 'repeat(3, 1fr)'
                },
                gap: 3
              }}
            >
              {topEvents.map((event) => (
                <Card 
                  key={event.eventID}
                  sx={{ 
                    height: '100%', 
                    display: 'flex', 
                    flexDirection: 'column',
                    cursor: 'pointer',
                    transition: 'all 0.3s ease-in-out',
                    borderRadius: 3,
                    overflow: 'hidden',
                    '&:hover': {
                      transform: 'translateY(-8px)',
                      boxShadow: '0 12px 24px rgba(0,0,0,0.15)',
                    }
                  }}
                  onClick={() => handleEventClick(event.eventID)}
                >
                  {event.image && event.image.length > 0 && event.image[0] && (
                    <CardMedia
                      component="img"
                      height="220"
                      image={event.image[0]}
                      alt={event.title}
                      sx={{ objectFit: 'cover' }}
                    />
                  )}
              <CardContent sx={{ flexGrow: 1, p: 3 }}>
                <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
                  {event.title}
                </Typography>
                <Typography 
                  variant="body1" 
                  color="text.secondary"
                  sx={{
                    display: '-webkit-box',
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                    mb: 2,
                    minHeight: '4.5em'
                  }}
                >
                  {event.description}
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                  📍 {event.location}
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 1 }}>
                  🎫 {event.ticketsSold || 0} tickets sold
                </Typography>
                <Typography variant="h6" color="primary.main" fontWeight="bold" sx={{ mt: 2 }}>
                  {event.price === 0 ? 'Free' : `$${event.price}`}
                </Typography>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}
      
      <Box sx={{ mt: 4, textAlign: 'center' }}>
        <Button 
          variant="contained" 
          size="large"
          onClick={() => navigate('/events')}
          sx={{ 
            bgcolor: '#008dd5',
            '&:hover': { bgcolor: '#007bbf' }
          }}
        >
          View All Events
        </Button>
      </Box>
      </Box>
      </Box>


      <Box component = "section" sx = {{p: 2, width: '100%', bgcolor: '#a63a50', color: 'white', border: '5px white'}}>
        <Typography variant = "h3"> Ready To Interact? </Typography>
        {/*
        <button onClick={() => setCount((count) => count + 1)}>
                    count is {count}
                </button>
        <p>
                    Edit <code>src/App.tsx</code> and save to test HMR
                </p>
        */}
                <button onClick={() => navigate('/signup')}>
                    Go to Sign Up
                </button>
                <button onClick={() => navigate('/login')}>
                    Go to Login
                </button>
                <button onClick={() => navigate('/checkout/1')}>
                    Go to Checkout
                </button>
                <button onClick={() => navigate('/savedtickets')}>
                    Saved tickets
                 </button>
                <button onClick={() => navigate('/events')}>
                    Browse events
                </button>
      </Box>
        </>
    );
}


function App() {
    return (

            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<Home />} />
                    <Route path="/events" element={<EventsPage />} />
                    <Route path="/events/create" element={<CreateData />} />
                    <Route path="/events/:eventId/edit" element={<EditEventPage />} />
                    <Route path="/my-events" element={<MyEventsPage />} />
                    <Route path="/my-events/scan/:eventId" element={<ScanTicketPage />} />
                    <Route path="/mytickets" element={<MyTickets />} />
                    <Route path="/settings" element={<Settings/>}></Route>
                    <Route path="/savedtickets" element={<SavedTickets/>} />
                    <Route path="/admin/dashboard" element={<AdminDashboard />} />
                    <Route path="/admin/approve-organizer" element={<OrganiserApprovePage />} />
                    <Route path="/events/:eventId/attendees" element={<RegisteredStudentsPage />} />
                    <Route path="/myevents/scan/:eventId" element={<ScanTicketPage />} />
                </Route>

                <Route element={<BlankLayout/>}>
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/signup" element={<SignUp/>}/>
                    <Route path="/CreateData" element={<CreateData/>}/>
                    <Route path="/checkout/:ticketId" element={<CheckoutPage/>}/>
                </Route>
            </Routes>
    );
}

export default App;
