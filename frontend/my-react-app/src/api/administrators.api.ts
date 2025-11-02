import axiosInstance from './axiosInstance';

export interface EventStats {
    eventId: number;
    eventName: string;
    ticketCount: number;
    scannedCount: number;
}

export interface ParticipationTrend {
    date: string;
    ticketsIssued: number;
    ticketsScanned: number;
}

export interface GlobalStatsResponse {
    totalEvents: number;
    totalTickets: number;
    totalScannedTickets: number;
    totalUnscannedTickets: number;
    totalStudents: number;
    totalOrganizers: number;
    scanRate: number;
    topEvents: EventStats[];
    participationTrends: ParticipationTrend[];
}

/**
 * Get global statistics for the platform (admin only)
 * @returns Promise<GlobalStatsResponse> - Global statistics data
 */
export const getGlobalStatistics = async (): Promise<GlobalStatsResponse> => {
    const response = await axiosInstance.get('/administrators/stats/global');
    return response.data;
};

export interface Organizer {
    userId: number;
    firstName: string;
    lastName: string;
    email: string;
    organizationName?: string;
    phoneNumber?: string;
    approvalStatus: string;
}

export interface EventData {
    eventId: number;
    title: string;
    description: string;
    eventType: string;
    location: string;
    startDateTime: string;
    endDateTime: string;
    capacity: number;
    price: number;
    ticketCount: number;
    scannedTicketCount: number;
    organizerId: number;
}

/**
 * Get all organizers (admin only)
 * @returns Promise<Organizer[]> - List of all organizers
 */
export const getAllOrganizers = async (): Promise<Organizer[]> => {
    const response = await axiosInstance.get('/administrators/organizers');
    return response.data;
};

/**
 * Get all events (admin only)
 * @returns Promise<EventData[]> - List of all events
 */
export const getAllEventsAdmin = async (): Promise<EventData[]> => {
    const response = await axiosInstance.get('/administrators/events');
    return response.data;
};
