import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { QRCodeSVG } from 'qrcode.react';
import { getUserTickets, type Ticket as TicketData } from "./api/tickets.api";
import "./App.css";

const MyTickets: React.FC = () => {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState<TicketData[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedTicket, setSelectedTicket] = useState<TicketData | null>(null);

  useEffect(() => {
    const fetchTickets = async () => {
      if (!localStorage.getItem('token')) {
        navigate('/login');
        return;
      }

      try {
        const ticketData = await getUserTickets();
        setTickets(ticketData);
        setLoading(false);
      } catch (error: any) {
        if (error.response?.status === 401) {
          navigate('/login');
        } else {
          console.error('Error fetching tickets:', error);
          setLoading(false);
        }
      }
    };

    fetchTickets();
  }, [navigate]);

  // Add event to calendar (.ics file download)
  const handleAddToCalendar = (ticket: TicketData) => {
    const event = ticket.event;
    const startDate = new Date(event.startDateTime);
    const endDate = new Date(event.endDateTime);
    try {

      // Format date for ICS (YYYYMMDDTHHMMSSZ)
      const formatDate = (date: Date) =>
        date.toISOString().replace(/[-:]/g, "").split(".")[0] + "Z";

      // ICS event structure
      const icsContent = `BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//MyTicketsApp//EN
BEGIN:VEVENT
UID:${ticket.qrCode}@myticketsapp
DTSTAMP:${formatDate(new Date())}
DTSTART:${formatDate(startDate)}
DTEND:${formatDate(endDate)}
SUMMARY:${event.title}
LOCATION:${event.location}
DESCRIPTION:Your ticket (${ticket.qrCode}) for ${event.title}.
END:VEVENT
END:VCALENDAR`;

      // Create and download the ICS file
      const blob = new Blob([icsContent], {
        type: "text/calendar;charset=utf-8",
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `${event.title.replace(/\s+/g, "_")}.ics`;
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

  if (loading) {
    return (
      <div className="card">
        <h1>My Tickets</h1>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="card">
      {/* Back button */}
      <button style = {{fontSize: '20px'}} className="back-btn" onClick={handleGoBack}>
        Back
      </button>

      <h1 style = {{fontFamily: 'Montserrat', fontWeight: '700', fontStyle: 'italic'}}>My Tickets</h1>

      {tickets.length === 0 ? (
        <p style={{fontFamily: 'Montserrat'}}>No tickets found. Browse events to purchase tickets!</p>
      ) : (
        <div className="tickets-container">
          {tickets.map((ticket) => (
            <div key={ticket.ticketId} className="ticket-card">
              <div className="ticket-content">
                <img
                  src={ticket.event.imageUrl || '/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'}
                  alt={ticket.event.title}
                  className="ticket-image"
                />
                <div className="ticket-details">
                  <h3>{ticket.event.title}</h3>
                  <p>{new Date(ticket.event.startDateTime).toLocaleString('en-US', {
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}</p>
                  <p>{ticket.event.location}</p>
                  <p>Ticket Code: {ticket.qrCode.substring(0, 8)}...</p>

                  <div className="ticket-buttons">
                    <button
                      className="view-ticket-btn"
                      onClick={() => setSelectedTicket(ticket)}
                    >
                      View Ticket
                    </button>
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
      )}

      {/* QR Code Modal */}
      {selectedTicket && (
        <div
          className="modal"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            zIndex: 1000,
          }}
          onClick={() => setSelectedTicket(null)}
        >
          <div
            className="modal-content"
            style={{
              backgroundColor: 'white',
              padding: '2rem',
              borderRadius: '8px',
              maxWidth: '500px',
              textAlign: 'center',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2>{selectedTicket.event.title}</h2>
            <p>{new Date(selectedTicket.event.startDateTime).toLocaleString()}</p>
            <p>{selectedTicket.event.location}</p>
            <div style={{ marginTop: '1rem', marginBottom: '1rem' }}>
              <QRCodeSVG value={selectedTicket.qrCode} size={256} />
            </div>
            <p style={{ fontSize: '0.8rem', wordBreak: 'break-all' }}>
              {selectedTicket.qrCode}
            </p>
            <button
              onClick={() => setSelectedTicket(null)}
              style={{
                marginTop: '1rem',
                padding: '0.5rem 2rem',
                cursor: 'pointer',
              }}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyTickets;
