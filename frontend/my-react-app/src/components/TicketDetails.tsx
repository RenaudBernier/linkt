import type {Event} from "../types/event.interface";

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
        <div className="ticket-container">
            <h2>Ticket Details</h2>


            <div className="ticket">

                <img
                    id="ticket-img"
                    src={mockEvent.image[0]}>
                </img>
                <h3 id="event-name">{mockEvent.title}</h3>
                <p id="event-description">{mockEvent.description}</p>
                <p id="event-price">{(mockEvent.price == 0 ? "Free" : "$" + mockEvent.price)}</p>
                <p id="event-date">{
                    mockEvent.startDate.toLocaleString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    })
                }</p>
                <p id="event-location">{mockEvent.location}</p>
            </div>
        </div>
    );
}

export default TicketDetails;