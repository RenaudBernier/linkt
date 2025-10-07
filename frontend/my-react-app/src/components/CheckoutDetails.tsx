import * as React from "react";
import {useState} from "react";


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
            if (month < 1 || month > 12) {
                setError("Invalid month");
                return;
            }

            const today = new Date();
            const expDate = new Date(year, month - 1, 1);
            expDate.setMonth(expDate.getMonth() + 1);

            if (expDate < today) setError("Card expired");
            else setError("");
        } else {
            setError("");
        }
    };

    return (
        /*Checkout Info*/
        <div className="checkout">
            <h2>Checkout Information</h2>

            <form>
                <label>Name on Card</label>
                <input required type="text" placeholder="John Smith"/>

                <label> Credit card number</label>

                <input
                    required
                    type="text"
                    inputMode="numeric"
                    placeholder="1111-2222-3333-4444"
                    pattern="\d{16}"
                    maxLength={16}
                    onKeyPress={preventCharacter}
                />


                // Row Containing the expiration and CVV
                <div className="row">

                    // Row Containing the expiration
                    <div className="row-item">
                        <label>Expiration</label>
                        <input
                            required
                            id="expiration-box"
                            placeholder="05/2028"
                            maxLength={7}
                            value={expirationDate}
                            onChange={handleDate}
                            onKeyPress={preventCharacter}
                            style={{borderColor: error ? "red" : ""}}
                        />
                        {error && (
                            <p style={{color: "red", fontSize: "0.85rem"}}>{error}</p>
                        )}
                    </div>

                    // Row Containing the CVV/
                    <div className="row-item">
                        <label> CVV</label>
                        <input
                            required
                            id="cvv-box"
                            type="text"
                            inputMode="numeric"
                            pattern="\d{3}"
                            maxLength={3}
                            placeholder="111"
                            onKeyPress={preventCharacter}
                        />
                    </div>
                </div>

                // Action buttons
                <div className="actions">
                    <button type="submit">Submit</button>
                    <button type="button" onClick={handleCancel}>Cancel</button>
                </div>
            </form>
        </div>
    )
}
export default CheckoutDetails