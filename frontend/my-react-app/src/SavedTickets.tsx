import type {Event} from "./types/event.interface.ts";
import eventImg from "./assets/mock-event-image.png";


const events: Event[] = [
    {
        eventID: 1,
        title: "Event 1",
        description: "------",
        category: "------",
        image: [eventImg],
        price: 10,
        startDate: new Date("2025-10-15T17:00:00"),
        endDate: new Date("2025-10-15T20:00:00"),
        location: "Hall Building (H-110), Concordia University, Montreal, QC",
        capacity: 120
    },
    {
        eventID: 2,
        title: "Event 2",
        description: "------",
        category: "------",
        image: [eventImg],
        price: 20,
        startDate: new Date("2025-10-22T14:00:00"),
        endDate: new Date("2025-10-22T18:00:00"),
        location: "Engineering and Computer Science (EV-3.309), Concordia University, Montreal, QC",
        capacity: 100
    }
];