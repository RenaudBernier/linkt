import React from "react";
import { useNavigate } from "react-router-dom";
import "./App.css";

// Define the Ticket interface
interface Ticket {
  id: number;
  eventName: string;
  date: string;
  location: string;
  ticketCode: string;
  image: string;
}

// Dummy tickets for display
const dummyTickets: Ticket[] = [
  {
    id: 1,
    eventName: "Summer Music Festival",
    date: "July 20, 2025 - 6:00 PM",
    location: "Downtown Park, Toronto",
    ticketCode: "SMF-001",
    image: "src/images/summer.jpg",
  },
  {
    id: 2,
    eventName: "Tech Innovators Expo",
    date: "August 15, 2025 - 10:00 AM",
    location: "Convention Centre, Vancouver",
    ticketCode: "TIE-214",
    image: "src/images/tech.jpg",
  },
  {
    id: 3,
    eventName: "Comedy Night Live",
    date: "September 3, 2025 - 8:30 PM",
    location: "Laugh Lounge, Montreal",
    ticketCode: "CNL-773",
    image: "src/images/comedy.jpg",
  },
];

const MyTickets: React.FC = () => {
  const navigate = useNavigate();

  // Add event to calendar (.ics file download)
  const handleAddToCalendar = (ticket: Ticket) => {
    try {
      // Split date and time safely
      const [dateString, timeString] = ticket.date.split(" - ");
      const start = new Date(`${dateString} ${timeString}`);

      // If date fails to parse, show an alert
      if (isNaN(start.getTime())) {
        alert("Could not parse the event date.");
        return;
      }

      // Default event length: 2 hours
      const end = new Date(start.getTime() + 2 * 60 * 60 * 1000);

      // Format date for ICS (YYYYMMDDTHHMMSSZ)
      const formatDate = (date: Date) =>
        date.toISOString().replace(/[-:]/g, "").split(".")[0] + "Z";

      // ICS event structure
      const icsContent = `BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//MyTicketsApp//EN
BEGIN:VEVENT
UID:${ticket.ticketCode}@myticketsapp
DTSTAMP:${formatDate(new Date())}
DTSTART:${formatDate(start)}
DTEND:${formatDate(end)}
SUMMARY:${ticket.eventName}
LOCATION:${ticket.location}
DESCRIPTION:Your ticket (${ticket.ticketCode}) for ${ticket.eventName}.
END:VEVENT
END:VCALENDAR`;

      // Create and download the ICS file
      const blob = new Blob([icsContent], {
        type: "text/calendar;charset=utf-8",
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `${ticket.eventName.replace(/\s+/g, "_")}.ics`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Error creating calendar file:", error);
    }
  };

  // Back button handler
  const handleGoBack = () => {
    if (window.history.length > 1) {
      navigate(-1); // Go back if there’s history
    } else {
      navigate("/"); // Otherwise go home
    }
  };

  return (
    <div className="card">
      {/* Back button */}
      <button className="back-btn" onClick={handleGoBack}>
        Back
      </button>

      <h1>My Tickets</h1>

      <div className="tickets-container">
        {dummyTickets.map((ticket) => (
          <div key={ticket.id} className="ticket-card">
            <div className="ticket-content">
              <img
                src={ticket.image}
                alt={ticket.eventName}
                className="ticket-image"
              />
              <div className="ticket-details">
                <h3>{ticket.eventName}</h3>
                <p>{ticket.date}</p>
                <p>{ticket.location}</p>
                <p>Ticket Code: {ticket.ticketCode}</p>

                <div className="ticket-buttons">
                  <button className="view-ticket-btn">View Ticket</button>
                  <button
                    className="add-calendar-btn"
                    onClick={() => handleAddToCalendar(ticket)}
                  >
                    Add to Calendar
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MyTickets;
