import type { Event } from "../types/event.interface";

const mockEvent: Event = {
    eventID: 1,
    title: "Tech Talk",
    description: "Shaping the Future of Technology",
    category: "Technology",
    image: ["../public/mock-event-image.webp"],
    price: 10,
    startDate: new Date("2025-10-15T17:00:00"),
    endDate: new Date("2025-10-15T20:00:00"),
    location: "123 Rue Guy, Montreal, QC H1A 1A1, Canada",
    capacity: 100
};

function TicketDetails() {
    return (
        <div className="checkoutPage-ticket">
            <h2>Ticket Details</h2>

            <div className="checkoutPage-ticketContent">
                <img
                    className="checkoutPage-img"
                    src={mockEvent.image[0]}
                    alt={mockEvent.title}
                />
                <h3 className="checkoutPage-eventName">{mockEvent.title}</h3>
                <p className="checkoutPage-eventDescription">{mockEvent.description}</p>
                <p className="checkoutPage-price">
                    {mockEvent.price === 0 ? "Free" : "$" + mockEvent.price}
                </p>
                <p className="checkoutPage-eventDate">
                    {mockEvent.startDate.toLocaleString("en-US", {
                        month: "short",
                        day: "numeric",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit"
                    })}
                </p>
                <p className="checkoutPage-eventLocation">{mockEvent.location}</p>
            </div>
        </div>
    );
}

export default TicketDetails;
