import type { Event } from "../types/event.interface";
import eventImg1 from "../assets/event1.png";
import eventImg2 from "../assets/event2.png";

const events: Event[] = [
    {
        eventID: 1,
        title: "Event 1",
        description: "event 1 description",
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
        description: "event 2 description",
        category: "------",
        image: [eventImg2],
        price: 20,
        startDate: new Date("2025-10-22T14:00:00"),
        endDate: new Date("2025-10-22T18:00:00"),
        location: "EV Building, Concordia University, Montreal, QC",
        capacity: 100
    }
];

function TicketDetails({ eventId }: { eventId?: string }) {
    const event = events.find(e => e.eventID === Number(eventId));

    if (!event) {
        return (
            <div className="checkoutPage-ticket">
                <h2>Ticket Details</h2>
                <p>Event not found</p>
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
