import React, { useState, useEffect } from 'react';
import { getPendingOrganizers, approveOrganizer } from '../api/users.api';
import { User } from '../types/user.interfaces';

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
        <div className="container mt-5">
            <h2>Pending Organizer Approvals</h2>
            {error && <div className="alert alert-danger">{error}</div>}
            <table className="table table-striped">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    {organizers.map(organizer => (
                        <tr key={organizer.id}>
                            <td>{organizer.name}</td>
                            <td>{organizer.email}</td>
                            <td>
                                <button className="btn btn-primary" onClick={() => handleApprove(organizer.id)}>
                                    Approve
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default OrganiserApprovePage;
