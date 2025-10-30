import type {Ticket} from "./ticket.interface";

export interface User {
    userId? : number;
    firstName : string;
    lastName : string;
    email : string;
    phoneNumber : string;
    userType?: string;
}

export interface Student extends User {
    // to be discussed list of ID or tickets
    tickets : Ticket[];
    savedEvents : Event[];
}

export interface Organizer extends User {
    isApproved : boolean;
    organizationName?: string; // optional
}