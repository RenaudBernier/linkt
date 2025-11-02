import axiosInstance from './axiosInstance';
import type { Event } from '../types/event.interface';

export const saveEvent = async (eventId: number): Promise<any> => {
  const response = await axiosInstance.post('/saved-events', { eventId });
  return response.data;
};

export const unsaveEvent = async (eventId: number): Promise<any> => {
  const response = await axiosInstance.delete(`/saved-events/event/${eventId}`);
  return response.data;
};

export const getSavedEvents = async (): Promise<Event[]> => {
  const response = await axiosInstance.get('/saved-events/me');

  // Transform backend data to match frontend interface
  const transformedEvents: Event[] = response.data.map((event: any) => ({
    eventID: event.eventId,
    title: event.title,
    description: event.description,
    category: event.eventType,
    image: event.imageUrl ? [`http://localhost:8080${event.imageUrl}`] : [],
    price: event.price || 0,
    startDate: new Date(event.startDateTime),
    endDate: new Date(event.endDateTime),
    location: event.location,
    capacity: event.capacity,
  }));

  return transformedEvents;
};

export const checkIfSaved = async (eventId: number): Promise<boolean> => {
  const response = await axiosInstance.get(`/saved-events/check/${eventId}`);
  return response.data.isSaved;
};
