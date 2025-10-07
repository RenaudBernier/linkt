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


                <div className="row">
                    <div className="row-item">
                        <label>Expiration</label>
                        <input
                            required
                            id="expiration-box"
                            placeholder="05/2028"
                            maxLength={7}
                        />
                    </div>

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
                        />
                    </div>
                </div>



            </form>

        </div>

    )

}

export default CheckoutDetails