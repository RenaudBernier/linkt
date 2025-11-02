import axiosInstance from './axiosInstance';

export interface Ticket {
  ticketId: number;
  qrCode: string;
  event: {
    eventId: number;
    title: string;
    description: string;
    eventType: string;
    startDateTime: string;
    endDateTime: string;
    location: string;
    imageUrl: string;
    price: number;
  };
}

export const buyTicket = async (eventId: number): Promise<Ticket> => {
  const response = await axiosInstance.post('/tickets', { eventId });
  return response.data;
};

export const getUserTickets = async (): Promise<Ticket[]> => {
  const response = await axiosInstance.get('/tickets/me');
  return response.data;
};

export const getTicket = async (ticketId: number): Promise<Ticket> => {
  const response = await axiosInstance.get(`/tickets/${ticketId}`);
  return response.data;
};

export interface ScanRequest {
  qrCode: string;
}

export interface TicketData {
  ticketId: number;
  studentName: string;
  studentEmail: string;
  eventTitle: string;
  eventStartTime: string;
  ticketType: string;
}

export interface ScanResponse {
  valid: boolean;
  message: string;
  status: 'SUCCESS' | 'ALREADY_SCANNED' | 'INVALID' | 'WRONG_EVENT' | 'ERROR';
  ticketData?: TicketData;
  scannedAt?: string;
  scannedBy?: string;
}

export interface ScanStatsResponse {
  eventId: number;
  eventTitle: string;
  totalTickets: number;
  scannedCount: number;
  remainingCount: number;
}

export const validateTicket = async (eventId: number, qrCode: string): Promise<ScanResponse> => {
  const response = await axiosInstance.post(`/tickets/events/${eventId}/validate`, { qrCode });
  return response.data;
};

export const getScanStats = async (eventId: number): Promise<ScanStatsResponse> => {
  const response = await axiosInstance.get(`/tickets/events/${eventId}/scan-stats`);
  return response.data;
};
