import { useState } from "react";


function CheckoutDetails() {
    const [expirationDate, setExpirationDate] = useState("");
    const [error, setError] = useState("");

    // Reset all inputs
    const handleCancel = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.currentTarget.form?.reset();
    };

    // Preventing the user from writing characters in the input box
    const preventCharacter = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (!/[0-9]/.test(e.key)) e.preventDefault();
    };

    const preventNumeric = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (!/^[a-z A-Z]$/.test(e.key)) e.preventDefault();
    };

    // handling the date format and validation
    const handleDate = (e: React.FormEvent<HTMLInputElement>) => {
        let value = e.currentTarget.value.replace(/\D/g, "");
        if (value.length > 2) value = value.slice(0, 2) + "/" + value.slice(2, 6);
        setExpirationDate(value);

        // Validation
        if (value.length === 7) {
            const [monthStr, yearStr] = value.split("/");
            const month = parseInt(monthStr);
            const year = parseInt(yearStr);
            const today = new Date();
            const expDate = new Date(year, month - 1, 1);
            expDate.setMonth(expDate.getMonth() + 1);

            if (month < 1 || month > 12 ||
                year < today.getFullYear() || year > today.getFullYear() + 6
            ) {
                setError("Invalid month or year");
                return;
            }

            if (month < 1 || month > 12) {
                setError("Invalid month");
                return;
            }

            if (expDate < today) setError("Card expired");

            if (year > (today.getFullYear() + 6) ) {
                setError("Invalid Year should be less than " + (today.getFullYear() + 10));
                return;
            }

            if (year > (today.getFullYear() + 6) ) {
                setError("Invalid Year should be less than " + (today.getFullYear() + 10));
                return;
            }

            else setError("");
        } else {
            setError("");
        }
    };

    return (
        // Checkout Info
        <div className="checkoutPage-form">
            <h2>Checkout Information</h2>

            <form>
                <label className="checkoutPage-label">Name on Card</label>
                <input
                    className="checkoutPage-input"
                    required
                    type="text"
                    placeholder="John Smith"
                    onKeyPress={preventNumeric}
                />

                <label className="checkoutPage-label">Credit Card Number</label>
                <input
                    className="checkoutPage-input"
                    required
                    type="text"
                    inputMode="numeric"
                    placeholder="1111222233334444"
                    pattern="\d{16}"
                    maxLength={16}
                    onKeyPress={preventCharacter}
                />

                {/* Row containing expiration and CVV */}
                <div className="checkoutPage-row">
                    {/* Expiration */}
                    <div className="checkoutPage-rowItem">
                        <label className="checkoutPage-label">Expiration</label>
                        <input
                            className="checkoutPage-input"
                            required
                            placeholder="05/2028"
                            maxLength={7}
                            value={expirationDate}
                            onChange={handleDate}
                            onKeyPress={preventCharacter}
                            style={{ borderColor: error ? "red" : "" }}
                        />
                        {error && (
                            <p style={{ color: "red", fontSize: "0.85rem" }}>{error}</p>
                        )}
                    </div>

                    {/* CVV */}
                    <div className="checkoutPage-rowItem">
                        <label className="checkoutPage-label">CVV</label>
                        <input
                            className="checkoutPage-input checkoutPage-cvv"
                            required
                            type="text"
                            inputMode="numeric"
                            pattern="\d{3}"
                            maxLength={3}
                            placeholder="111"
                            onKeyPress={preventCharacter}
                        />
                    </div>
                </div>

                {/* Action buttons */}
                <div className="checkoutPage-actions">
                    <button className="checkoutPage-submit" type="submit">Submit</button>
                    <button className="checkoutPage-cancel" type="button" onClick={handleCancel}>
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}

export default CheckoutDetails;
