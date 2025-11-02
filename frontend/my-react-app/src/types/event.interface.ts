export type EventStatus = 'draft' | 'published' | 'cancelled' | 'completed';

export interface Event {
    eventID: number; //the "| null might cause errors in the future heads up"
    title: string;
    description: string;
    category: string;
    image: string[];
    imageUrl?: string;
    price: number;
    startDate: Date;
    endDate: Date;
    location: string;
    capacity: number;
    status?: EventStatus;
    ticketsSold?: number;
}