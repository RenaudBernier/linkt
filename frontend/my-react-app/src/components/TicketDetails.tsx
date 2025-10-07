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