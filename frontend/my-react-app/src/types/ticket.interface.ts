export interface Ticket{
    ticketID : number;
    eventID : number;
    qrCode : string;

    generateQRCode() : string;
    validateQRCode() : boolean;
}