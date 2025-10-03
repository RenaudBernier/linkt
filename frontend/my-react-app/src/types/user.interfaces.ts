interface User {
    userID : number;
    firstName : string;
    lastName : string;
    email : string;
    phoneNumber : string;

    login(email: string, password: string) : boolean;
    logout(): void
}

interface Ticket{
    ticketID : number;
    eventID : number;
    qrCode : string;

    generateQRCode() : string;
    validateQRCode() : boolean;
}



