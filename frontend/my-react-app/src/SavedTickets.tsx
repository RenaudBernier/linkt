import type {Event} from "./types/event.interface.ts";
import {useNavigate} from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getSavedEvents, unsaveEvent } from './api/savedEvents.api';
import { useSnackbar } from 'notistack';

import {
    Box,
    Card,
    CardContent,
    CardMedia,
    Typography,
    Button,
    IconButton
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';

function SavedTickets() {
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();
    const [events, setEvents] = useState<Event[]>([]);
    const [loading, setLoading] = useState(true);
    const [removingId, setRemovingId] = useState<number | null>(null);

    useEffect(() => {
        const fetchSavedEvents = async () => {
            if (!localStorage.getItem('token')) {
                navigate('/login');
                return;
            }

            try {
                const savedEvents = await getSavedEvents();
                setEvents(savedEvents);
                setLoading(false);
            } catch (error: any) {
                if (error.response?.status === 401) {
                    navigate('/login');
                } else {
                    console.error('Error fetching saved events:', error);
                    setLoading(false);
                }
            }
        };

        fetchSavedEvents();
    }, [navigate]);

    const handleUnsaveEvent = async (eventId: number) => {
        setRemovingId(eventId);
        try {
            await unsaveEvent(eventId);
            setEvents(events.filter(e => e.eventID !== eventId));
            enqueueSnackbar('Event removed from saved events', { variant: 'success' });
        } catch (error: any) {
            if (error.response?.status === 401) {
                navigate('/login');
            } else {
                console.error('Error removing event:', error);
                enqueueSnackbar('Failed to remove event from saved events', { variant: 'error' });
            }
        } finally {
            setRemovingId(null);
        }
    };

    if (loading) {
        return (
            <Box
                sx={{
                    width: "100vw", minHeight: "100vh",
                    display: "flex", justifyContent: "center", alignItems: "center"
                }}
            >
                <Typography>Loading saved events...</Typography>
            </Box>
        );
    }

    if (events.length === 0) {
        return (
            <Box
                sx={{
                    width: "100vw", minHeight: "100vh",
                    display: "flex", flexDirection: "column",
                    justifyContent: "center", alignItems: "center",
                    padding: "20px"
                }}
            >
                <Typography variant="h5" sx={{ marginBottom: "20px" }}>
                    No saved events yet
                </Typography>
                <Typography variant="body1" sx={{ marginBottom: "20px", color: "#555" }}>
                    Browse events and save the ones you like!
                </Typography>
                <Button
                    onClick={() => navigate('/events')}
                    sx={{
                        backgroundColor: "#2563eb",
                        color: "white",
                        padding: "10px 20px",
                        '&:hover': { backgroundColor: "#1d4ed8" }
                    }}
                >
                    Browse Events
                </Button>
            </Box>
        );
    }

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

                <Box sx={{ display: "flex", gap: "10px", alignItems: "center" }}>
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
                    <IconButton
                        onClick={() => handleUnsaveEvent(mockEvent.eventID)}
                        disabled={removingId === mockEvent.eventID}
                        sx={{
                            color: "#ef4444",
                            '&:hover': {
                                backgroundColor: "#fee2e2"
                            }
                        }}
                    >
                        <DeleteIcon />
                    </IconButton>
                </Box>
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