// src/App.tsx
import {Routes, Route, useNavigate, Outlet} from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import {
  AppBar,
  Toolbar,
  Box,
  TextField,
  InputAdornment,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Typography,
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
import MyEventsPage from "./pages/MyEventsPage.tsx";
import EditEventPage from "./pages/EditEventPage.tsx";
import ScanTicketPage from "./pages/ScanTicketPage.tsx";

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

     

     

      
      <Box component = "section" sx = {{p: 2, width: '100%', bgcolor: '#373f51', color: 'white', border: '5px white'}}>
        {/* 
        Former Logo
              <a href="https://vite.dev" target="_blank">
                <img src={viteLogo} className="logo" alt="Vite logo" />
            </a>
            <a href="https://react.dev" target="_blank">
                <img src={reactLogo} className="logo react" alt="React logo" />
      </a>
        */}
      <Typography variant = "h2"> Top Events </Typography>
      <Typography variant = "h4"> Backend for this hasn't been implemented yet! </Typography>
      <img src = "src\images\samantha-gades-fIHozNWfcvs-unsplash.jpg" alt = "neat college photo!" style = {{maxWidth: '33%', maxHeight: '33%'}}></img>
      <Typography variant = "h3"> Frosh Night </Typography>
      <Typography variant = "body1"> New to school and don't know where to start? Have some drinks, play games and meet some new people at the school's frosh night! </Typography> 
      <br></br>
      <img src = "src\images\swag-slayer-dd2EOQBycJY-unsplash.jpg"  alt = "neat college photo!" style = {{maxWidth: '33%', maxHeight: '33%'}}></img>
      <Typography variant = "h3"> DJ Night </Typography>
      <Typography variant = "body1"> The EDM Club is organizing an all-night dance festival on the 22nd of October! Click for more details! </Typography>
      <br></br>
      <img src = "src\images\willian-justen-de-vasconcellos-_krHI5-8yA4-unsplash.jpg" alt = "neat college photo!" style = {{maxWidth: '33%', maxHeight: '33%'}}></img>
      <Typography variant = "h3"> Campus Museum Tour</Typography>
      <Typography variant = "body1"> Join us for a tour of the campus museum where you can browse artifacts of some of the school's greatest alumni! </Typography>
      {/* With this, the "Top Events" container is forced to extend its height to contain the floated images*/}
      <Box sx={{ clear: 'both' }}></Box>
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
                    <Route path="/admin/approve-organizer" element={<OrganiserApprovePage />} />
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
