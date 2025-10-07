import "./CheckoutPage.css";
import CheckoutDetails from "./CheckoutDetails";
import TicketDetails from "./TicketDetails";

function CheckoutPage() {
    return (
        <div className="checkout-container">
            <CheckoutDetails />
            <TicketDetails />
        </div>
    );
}

export default CheckoutPage;