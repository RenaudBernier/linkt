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
