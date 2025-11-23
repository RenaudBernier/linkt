// src/App.tsx
import {Routes, Route, useNavigate, Outlet, Navigate} from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import { useState, useEffect } from 'react';
import {
  Toolbar,
  Box,
  Typography,
  Card,
  CardMedia,
  CardContent,
  CardActions,
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
import { saveEvent, checkIfSaved } from './api/savedEvents.api';
import type { Event } from './types/event.interface';
import { useSnackbar } from 'notistack';
import ApproveEventsPage from "./pages/ApproveEventsPage.tsx";
import EmailVerificationPage from "./pages/EmailVerificationPage.tsx";
import TwoFactorAuthPage from "./pages/TwoFactorAuthPage.tsx";
function MainLayout() {
    return (
        <>
            <Header/>

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
    const { enqueueSnackbar } = useSnackbar();
    const [topEvents, setTopEvents] = useState<Event[]>([]);
    const [loading, setLoading] = useState(true);
    const [savedEventIds, setSavedEventIds] = useState<Set<number>>(new Set());
    const [savingEventId, setSavingEventId] = useState<number | null>(null);

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

    useEffect(() => {
        const checkSavedEvents = async () => {
            if (!localStorage.getItem('token')) return;

            const savedIds = new Set<number>();
            for (const event of topEvents) {
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

        if (topEvents.length > 0) {
            checkSavedEvents();
        }
    }, [topEvents]);

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
            } else {
                enqueueSnackbar('Failed to save event', { variant: 'error' });
            }
        } finally {
            setSavingEventId(null);
        }
    };

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
            
      <Box component = "section" sx = {{width: '100%', bgcolor: '#008dd5', color: 'white'}}>
        <br></br>
        <br></br>
        <Typography variant = "h2" className='title'> Linkt </Typography>
        <br></br>
        <br></br>
        <Typography variant = "body1" fontSize = {'19px'} padding={'2px'}> Welcome to our comprehensive campus Events & Ticketing service, designed to streamline event management and boost student engagement for students! 
          We allow students to easily discover and search for events using comprehensive filters, save them to their personal calendar, and claim digital, QR-coded
          tickets (free or mock paid) for check-in! If you are an organizer, then you are welcome too! Organizers benefit from the ability to create, manage, 
          and track attendance for their events. They can also gain valuable insights for their events via our analytics dashboards. We are also welcoming 
          campus administrators, who can oversee organizations and moderate all content. Linkt is our brand-new system that connects students with campus life 
          while providing essential tools for hosting and administrating events! </Typography> 
        <br></br>
      </Box>

        <Box component = "section" sx = {{width: '100%', bgcolor: '#a63a50', color: 'white'}}>
        <br></br>
        <Typography variant = "h3" className='smallertitle'> Ready To Interact? </Typography><br></br>
            {user?.userType == 'organizer' && (
            <Typography variant = "h5">
                Hey! We noticed that you're an organizer! Feel free to add your event to our page!
            </Typography>)}

            {user?.userType == 'organizer' && (
            <button onClick={() => navigate('/CreateData')}>
            Create an Event!
            </button>)} 
            {user?.userType == 'organizer' && (<br></br>)}
            {user?.userType == 'organizer' && (<br></br>)}

            <button onClick={() => navigate('/events')}>
                Browse Events
            </button> <br></br> <br></br>

            <button onClick={() => navigate('/savedtickets')}>
                Saved Tickets
                </button> <br></br> <br></br>
            
            <button onClick={() => navigate('/signup')}>
                Go to Sign Up
            </button> <br></br><br></br>
            <button onClick={() => navigate('/login')}>
                Go to Login
            </button>
            <br></br>
            <br></br>
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
                    transition: 'all 0.3s ease-in-out',
                    borderRadius: 3,
                    overflow: 'hidden',
                    '&:hover': {
                      transform: 'translateY(-8px)',
                      boxShadow: '0 12px 24px rgba(0,0,0,0.15)',
                    }
                  }}
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
              <CardActions sx={{ p: 2.5, pt: 0, gap: 1 }}>
                <Button
                  onClick={() => handleEventClick(event.eventID)}
                  variant="contained"
                  size="medium"
                  fullWidth
                >
                  Buy Ticket
                </Button>
                <Button
                  onClick={() => handleAddToFavorites(event.eventID)}
                  variant={savedEventIds.has(event.eventID) ? "contained" : "outlined"}
                  size="medium"
                  disabled={savingEventId === event.eventID}
                  fullWidth
                >
                  {savedEventIds.has(event.eventID) ? 'Saved' : 'Save Event'}
                </Button>
              </CardActions>
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

        </>
    );
}


const AdminRoute: React.FC<{ children: React.ReactElement }> = ({ children }) => {
    const { user, isAuthenticated } = useAuth();
    if (!isAuthenticated || user?.userType !== 'administrator') {
        return <Navigate to="/" replace />;
    }
    return children;
};

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
                    <Route path="/admin/dashboard" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
                    <Route path="/admin/approve-organizer" element={<AdminRoute><OrganiserApprovePage /></AdminRoute>} />
                    <Route path="/admin/approve-events" element={<AdminRoute><ApproveEventsPage /></AdminRoute>} />
                    <Route path="/events/:eventId/attendees" element={<RegisteredStudentsPage />} />
                    <Route path="/myevents/scan/:eventId" element={<ScanTicketPage />} />
                </Route>

                <Route element={<BlankLayout/>}>
                    <Route path="/login" element={<Login/>}/>
                    <Route path="/signup" element={<SignUp/>}/>
                    <Route path="/verify-email" element={<EmailVerificationPage/>}/>
                    <Route path="/verify-2fa" element={<TwoFactorAuthPage/>}/>
                    <Route path="/CreateData" element={<CreateData/>}/>
                    <Route path="/checkout/:ticketId" element={<CheckoutPage/>}/>
                </Route>
            </Routes>
    );
}

export default App;
