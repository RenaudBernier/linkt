import axios from 'axios';

export interface StudentRegistration {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  ticketId: number;
  qrCode: string;
  isScanned: boolean;
  scannedAt: string | null;
}

const API_URL = 'http://localhost:8080/api/events';

/**
 * Fetch registered students for a specific event
 * Only organizers can call this endpoint
 */
export const getRegisteredStudents = async (
  eventId: number,
  token: string
): Promise<StudentRegistration[]> => {
  try {
    const response = await axios.get(
      `${API_URL}/${eventId}/registered-students`,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      }
    );
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response?.status === 401) {
        throw new Error('Not authenticated. Please log in again.');
      } else if (error.response?.status === 403) {
        throw new Error('You do not have permission to view this event\'s students.');
      } else if (error.response?.status === 404) {
        throw new Error('Event not found.');
      }
      throw new Error(error.response?.data || 'Failed to fetch registered students');
    }
    throw error;
  }
};
