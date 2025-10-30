export interface Event {
    eventID: number | null; //the "| null might cause errors in the future heads up"
    title: string;
    description: string;
    category: string;
    image : string[];
    price: number;
    startDate: Date;
    endDate: Date;
    location: string;
    capacity: number;

}