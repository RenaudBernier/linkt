import axiosInstance from './axiosInstance';
import type { Event } from '../types/event.interface';

export const getOrganizerEvents = async (): Promise<Event[]> => {
  const response = await axiosInstance.get('/events/organizer');
  return response.data.map((event: any) => ({
    id: event.eventId,
    eventID: event.eventId,
    title: event.title,
    description: event.description,
    status: event.status || 'published',
    category: event.eventType,
    image: [event.imageUrl || '/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    imageUrl: event.imageUrl || '/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg',
    price: event.price || 0,
    startDate: new Date(event.startDateTime),
    endDate: new Date(event.endDateTime),
    location: event.location,
    capacity: event.capacity,
    ticketsSold: event.ticketsSold || 0,
  }));
};

export const getAllEvents = async (): Promise<Event[]> => {
  const response = await axiosInstance.get('/events');

  // Transform backend data to match frontend interface
  const transformedEvents: Event[] = response.data.map((event: any) => ({
    id: event.eventId,
    eventID: event.eventId,
    title: event.title,
    description: event.description,
    category: event.eventType,
    image: event.imageUrl ? [event.imageUrl] : ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    imageUrl: event.imageUrl || '/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg',
    price: event.price || 0,
    startDate: new Date(event.startDateTime),
    endDate: new Date(event.endDateTime),
    location: event.location,
    capacity: event.capacity,
    status: event.status || 'published',
    ticketsSold: event.ticketsSold || 0,
  }));

  return transformedEvents;
};

export const getEventById = async (eventId: number): Promise<Event> => {
  const response = await axiosInstance.get(`/events/${eventId}`);
  const event = response.data;

  return {
    id: event.eventId,
    eventID: event.eventId,
    title: event.title,
    description: event.description,
    category: event.eventType,
    image: event.imageUrl ? [event.imageUrl] : ['/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg'],
    imageUrl: event.imageUrl || '/src/images/samantha-gades-fIHozNWfcvs-unsplash.jpg',
    price: event.price || 0,
    startDate: new Date(event.startDateTime),
    endDate: new Date(event.endDateTime),
    location: event.location,
    capacity: event.capacity,
    status: event.status || 'published',
    ticketsSold: event.ticketsSold || 0,
  };
};
