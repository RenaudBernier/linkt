import React, { useState, useEffect } from 'react';
import { getPendingOrganizers, approveOrganizer } from '../api/users.api';
import { User } from '../types/user.interfaces';
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
                setOrganizers(response.data);
            } catch (err) {
                setError('There was an error fetching the pending organizers.');
            }
        };

        fetchPendingOrganizers();
    }, []);

    const handleApprove = async (userId: string) => {
        try {
            await approveOrganizer(userId);
            setOrganizers(organizers.filter(org => org.id !== userId));
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
                        {organizers.map(organizer => (
                            <TableRow key={organizer.id}>
                                <TableCell>{organizer.name}</TableCell>
                                <TableCell>{organizer.email}</TableCell>
                                <TableCell>
                                    <Button variant="contained" color="primary" onClick={() => handleApprove(organizer.id)}>
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
