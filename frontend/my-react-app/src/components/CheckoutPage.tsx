import "./CheckoutPage.css";
import { useParams } from "react-router-dom";
import CheckoutDetails from "./CheckoutDetails";
import TicketDetails from "./TicketDetails";

function CheckoutPage() {
    const { ticketId } = useParams();
    return (
            <div className="checkoutPage-container">
                <CheckoutDetails />
                <TicketDetails eventId={ticketId} />
            </div>
    );
}

export default CheckoutPage;