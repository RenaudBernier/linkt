import * as React from "react";

function CheckoutDetails() {


    return (
        /*Checkout Info*/
        <div className="checkout">
            <h2>Checkout Information</h2>

            <form>
                <label>Name on Card</label>
                <input
                    required
                    type="text"
                    placeholder="John Smith"/>

                <label> Credit card number</label>
                <input
                    required
                    type="text"
                    inputMode="numeric"
                    placeholder="1111-2222-3333-4444"
                    pattern="\d{16}"
                    maxLength={16}
                />


            </form>

        </div>

    )

}

export default CheckoutDetails