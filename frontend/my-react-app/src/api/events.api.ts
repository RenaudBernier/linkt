import axiosInstance from './axiosInstance';
import type { Event } from '../types/event.interface';

export const getAllEvents = async (): Promise<Event[]> => {
  const response = await axiosInstance.get('/events');

  // Transform backend data to match frontend interface
  const transformedEvents: Event[] = response.data.map((event: any) => ({
    eventID: event.eventId,
    title: event.title,
    description: event.description,
    category: event.eventType,
    image: event.imageUrl ? [event.imageUrl] : ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    price: event.price || 0,
    startDate: new Date(event.startDateTime),
    endDate: new Date(event.endDateTime),
    location: event.location,
    capacity: event.capacity,
  }));

  return transformedEvents;
};

export const getEventById = async (eventId: number): Promise<Event> => {
  const response = await axiosInstance.get(`/events/${eventId}`);
  const event = response.data;

  return {
    eventID: event.eventId,
    title: event.title,
    description: event.description,
    category: event.eventType,
    image: event.imageUrl ? [event.imageUrl] : ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    price: event.price || 0,
    startDate: new Date(event.startDateTime),
    endDate: new Date(event.endDateTime),
    location: event.location,
    capacity: event.capacity,
  };
};

export const addEvent = async (formData: FormData) => {
  const response = await axiosInstance.post('/events/add', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
} 
