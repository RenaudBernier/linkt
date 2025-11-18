import React, { useEffect, useState } from 'react';
import { Container, Typography, Alert, CircularProgress, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button } from '@mui/material';
import { getAllEventsAdmin, approveEvent, rejectEvent, type EventData } from '../api/administrators.api';

const ApproveEventsPage: React.FC = () => {
  const [events, setEvents] = useState<EventData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    try {
      setLoading(true);
      const data = await getAllEventsAdmin();
      setEvents(data);
      setError(null);
    } catch (e) {
      console.error(e);
      setError('Failed to load events.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const onApprove = async (eventId: number) => {
    try {
      await approveEvent(eventId);
      await load();
    } catch {
      alert('Failed to approve event.');
    }
  };

  const onReject = async (eventId: number) => {
    try {
      await rejectEvent(eventId);
      await load();
    } catch {
      alert('Failed to reject event.');
    }
  };

  const pending = events.filter(e => e.status === 'pending');

  return (
    <Container sx={{ mt: 5, p: 5}}>
      <Typography variant="h4" component="h2" gutterBottom fontFamily={'Montserrat'} fontWeight={'600'}>
        Pending Event Approvals
      </Typography>
      {error && <Alert severity="error">{error}</Alert>}
      {loading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Event Name</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Location</TableCell>
                <TableCell align="right">Capacity</TableCell>
                <TableCell align="right">Price</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Start Date</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {pending.length > 0 ? (
                pending.map(ev => (
                  <TableRow key={ev.eventId}>
                    <TableCell>{ev.title}</TableCell>
                    <TableCell>{ev.eventType}</TableCell>
                    <TableCell>{ev.location}</TableCell>
                    <TableCell align="right">{ev.capacity}</TableCell>
                    <TableCell align="right">${ev.price.toFixed(2)}</TableCell>
                    <TableCell>{ev.status}</TableCell>
                    <TableCell>{new Date(ev.startDateTime).toLocaleDateString()}</TableCell>
                    <TableCell>
                      <Button variant="contained" color="primary" size="small" sx={{ mr: 1 }} onClick={() => onApprove(ev.eventId)}>Approve</Button>
                      <Button variant="contained" color="error" size="small" onClick={() => onReject(ev.eventId)}>Reject</Button>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} align="center">No pending events</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Container>
  );
};

export default ApproveEventsPage;
