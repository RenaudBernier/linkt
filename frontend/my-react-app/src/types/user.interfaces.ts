import type {Ticket} from "./ticket.interface";

interface User {
    userID : number;
    firstName : string;
    lastName : string;
    email : string;
    phoneNumber : string;

    login(email: string, password: string) : boolean;
    logout(): void
}



interface Student extends User {

    // to be discussed list of ID or tickets
    tickets : Ticket[];
    savedEvents : Event[];

    savedEvent() : boolean;
    claimTicket(): Ticket ;
    viewTickets() : Ticket[] ;

    //viewEvents
}
