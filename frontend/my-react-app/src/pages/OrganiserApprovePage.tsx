import React, { useState, useEffect } from 'react';
import { getPendingOrganizers, approveOrganizer } from '../api/users.api';
import type { User } from '../types/user.interfaces';
import {
    Container,
    Typography,
    Alert,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Button,
    Paper
} from '@mui/material';

const OrganiserApprovePage: React.FC = () => {
    const [organizers, setOrganizers] = useState<User[]>([]);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchPendingOrganizers = async () => {
            try {
                const response = await getPendingOrganizers();
                if (Array.isArray(response.data)) {
                    setOrganizers(response.data);
                }
            } catch (err) {
                setError('There was an error fetching the pending organizers.');
            }
        };

        fetchPendingOrganizers();
    }, []);

    const handleApprove = async (userId: number) => {
        try {
            await approveOrganizer(userId);
            setOrganizers(organizers.filter(org => org.userId !== userId));
        } catch (err) {
            setError('There was an error approving the organizer.');
        }
    };

    return (
        <Container sx={{ mt: 5 }}>
            <Typography variant="h4" component="h2" gutterBottom>
                Pending Organizer Approvals
            </Typography>
            {error && <Alert severity="error">{error}</Alert>}
            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Email</TableCell>
                            <TableCell>Action</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {organizers.filter(org => org.userId !== undefined).map(organizer => (
                            <TableRow key={organizer.userId}>
                                <TableCell>{organizer.firstName} {organizer.lastName}</TableCell>
                                <TableCell>{organizer.email}</TableCell>
                                <TableCell>
                                    <Button variant="contained" color="primary" onClick={() => organizer.userId && handleApprove(organizer.userId)}>
                                        Approve
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    );
};

export default OrganiserApprovePage;
