import type { Event } from "../types/event.interface";
import { useState, useEffect } from "react";
import { getEventById } from "../api/events.api";

function TicketDetails({ eventId }: { eventId?: string }) {
    const [event, setEvent] = useState<Event | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchEvent = async () => {
            if (!eventId) {
                setError("No event ID provided");
                setLoading(false);
                return;
            }

            try {
                const eventData = await getEventById(Number(eventId));
                setEvent(eventData);
                setLoading(false);
            } catch (err) {
                setError("Failed to load event");
                setLoading(false);
            }
        };

        fetchEvent();
    }, [eventId]);

    if (loading) {
        return (
            <div className="checkoutPage-ticket">
                <h2>Ticket Details</h2>
                <p>Loading...</p>
            </div>
        );
    }

    if (error || !event) {
        return (
            <div className="checkoutPage-ticket">
                <h2>Ticket Details</h2>
                <p>{error || "Event not found"}</p>
            </div>
        );
    }

    return (
        <div className="checkoutPage-ticket">
            <h2>Ticket Details</h2>

            <div className="checkoutPage-ticketContent">
                <img
                    className="checkoutPage-img"
                    src={event.image[0]}
                    alt={event.title}
                />
                <h3 className="checkoutPage-eventName">{event.title}</h3>
                <p className="checkoutPage-price">
                    {event.price === 0 ? "Free" : "$" + event.price}
                </p>
                <p className="checkoutPage-eventDate">
                    {event.startDate.toLocaleString("en-US", {
                        month: "short",
                        day: "numeric",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit"
                    })}
                </p>
                <p className="checkoutPage-eventLocation">{event.location}</p>
            </div>
        </div>
    );
}

export default TicketDetails;
