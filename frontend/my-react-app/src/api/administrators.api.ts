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
