import type {Event} from "./types/event.interface.ts";
import {useNavigate} from 'react-router-dom';
import eventImg1 from "./assets/event1.png";
import eventImg2 from "./assets/event2.png";

import {
    Box,
    Card,
    CardContent,
    CardMedia,
    Typography,
    Button
} from '@mui/material';

const events: Event[] = [
    {
        eventID: 1,
        title: "Event 1",
        description: "------",
        category: "------",
        image: [eventImg1],
        price: 10,
        startDate: new Date("2025-10-15T17:00:00"),
        endDate: new Date("2025-10-15T20:00:00"),
        location: "Hall Building (H-110), Concordia University, Montreal, QC",
        capacity: 120
    },
    {
        eventID: 2,
        title: "Event 2",
        description: "------",
        category: "------",
        image: [eventImg2],
        price: 20,
        startDate: new Date("2025-10-22T14:00:00"),
        endDate: new Date("2025-10-22T18:00:00"),
        location: "Engineering and Computer Science (EV-3.309), Concordia University, Montreal, QC",
        capacity: 100
    }
];

function SavedTickets() {
    const navigate = useNavigate();

    function Ticket({mockEvent}: { mockEvent: Event }) {
        return (
            <Card
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    width: "100%",
                    border: "1px solid #e2e8f0",
                    borderRadius: "15px",
                    padding: "20px",
                    backgroundColor: "#fff",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
                    marginBottom: "10px",
                    textAlign: "left",
                    boxSizing: "border-box",
                    marginTop: "15px",
                    gap: "40px",
                    transition: "all 0.2s ease",
                    '&:hover': {
                        transform: 'scale(1.03)',
                        boxShadow: "0 6px 15px rgba(0, 0, 0, 0.15)"
                    }
                }}
            >
                <Box sx={{display: "flex", alignItems: "center"}}>
                    <CardMedia
                        component="img"
                        image={mockEvent.image[0]}
                        alt={mockEvent.title}
                        sx={{
                            width: "100px",
                            height: "150px",
                            objectFit: "cover",
                            borderRadius: "8px",
                            marginRight: "20px",
                            flexShrink: "0"
                        }}
                    />

                    <CardContent sx={{p: 0}}>
                        <Typography sx={{fontSize: "18px", margin: "0 0 6px 0"}}>
                            {mockEvent.title}
                        </Typography>
                        <Typography sx={{margin: "4px 0", color: "#555"}}>
                            {mockEvent.startDate.toLocaleString("en-US", {
                                month: "short",
                                day: "numeric",
                                year: "numeric",
                                hour: "2-digit",
                                minute: "2-digit"
                            })}
                        </Typography>
                        <Typography sx={{margin: "4px 0", color: "#777"}}>
                            {mockEvent.location}
                        </Typography>
                        <Typography sx={{margin: "4px 0", fontWeight: "bold"}}>
                            {mockEvent.price === 0 ? "Free" : `$${mockEvent.price}`}
                        </Typography>
                    </CardContent>
                </Box>

                <Button
                    onClick={() => navigate(`/checkout/${mockEvent.eventID}`)}
                    sx={{
                        backgroundColor: "#2563eb",
                        color: "white",
                        borderRadius: "6px",
                        padding: "8px 12px",
                        cursor: "pointer",
                        fontSize: "14px",
                        flexShrink: 0,
                        '&:hover': {
                            backgroundColor: "#1d4ed8"
                        }
                    }}
                >
                    Buy Ticket
                </Button>
            </Card>
        );
    }

    function List() {
        return (
            <Box component="ul" sx={{listStyle: "none", padding: 0, margin: 0}}>
                {events.map((mockEvent) => (
                    <Box component="li" key={mockEvent.eventID} sx={{listStyle: "none", padding: 0, margin: 0}}>
                        <Ticket mockEvent={mockEvent}/>
                    </Box>
                ))}
            </Box>
        );
    }

    return (
        <Box
            sx={{
                width: "100vw", minHeight: "100vh",
                margin: "0", padding: 0,
                display: "flex", flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                boxSizing: "border-box"
            }}
        >
            <Box
                sx={{
                    width: "100%", minWidth: "700px",
                    display: "flex", flexDirection: "column",
                    alignItems: "center", justifyContent: "center",
                    padding: "20px",
                }}
            >
                <List/>
            </Box>

            <Button
                onClick={() => navigate('/')}
                sx={{
                    marginTop: "20px", backgroundColor: "#2563eb",
                    color: "white", border: "none",
                    borderRadius: "6px", padding: "10px 16px",
                    cursor: "pointer", fontSize: "14px",
                    '&:hover': {backgroundColor: "#1d4ed8"}
                }}
            >
                Go Back
            </Button>
        </Box>
    );
}
export default SavedTickets;