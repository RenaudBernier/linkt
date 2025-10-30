import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { addEvent } from './api/events.api.ts';
import { useAuth } from './contexts/AuthContext';

export default function AddEvent() {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [eventType, setEventType] = useState('');
    const [price, setPrice] = useState('');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');
    const [location, setLocation] = useState('');
    const [capacity, setCapacity] = useState('');
    //const [userType, setUserType] = useState<'student' | 'org'>('student');
    const navigate = useNavigate();
    const { user } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const response = await addEvent({
                eventID: null,
                title: title,
                description: description,
                category: eventType,
                image: [],
                price: Number(price),
                startDate: new Date(startTime),
                endDate: new Date (endTime),
                location: location,
                capacity: Number(capacity)
            });


            navigate('/');
        } catch (error) {
            console.error('New Event Creation failed!', error);
            // TODO: Show error message to user
        }
    };

    return (
        <div style={{maxWidth: 400, margin: 'auto', padding: 20}}>
            <h2>ADD EVENT</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Name Of Event: </label><br/>
                    <input
                        type="text"
                        value={title}
                        onChange={e => setTitle(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10}}>
                    <label>Description: </label><br/>
                    <textarea 
                        value={description}
                        onChange={e => setDescription(e.target.value)}
                        required
                    ></textarea>
                </div>
                <div style={{marginTop: 10}}>
                    <label>Event Type: </label><br/>
                    <input
                        type="text"
                        value={eventType}
                        onChange={e => setEventType(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10}}>
                    <label>Price: </label><br/>
                    <input
                        type="number"
                        value={price}
                        onChange={e => setPrice(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10}}>
                    <label>Capacity: </label><br/>
                    <input
                        type="number"
                        value={capacity}
                        onChange={e => setCapacity(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10, marginBottom: 20}}>
                    <label>Start Date Time:</label><br/>
                    <input
                        type="datetime-local"
                        value={startTime}
                        onChange={e => setStartTime(e.target.value)}
                        required
                    />
                </div>
                <div style={{marginTop: 10, marginBottom: 20}}>
                    <label>End Date Time:</label><br/>
                    <input
                        type="datetime-local"
                        value={endTime}
                        onChange={e => setEndTime(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" style={{marginTop: 20}}>Add Event!</button>
            </form>
            <button style={{marginTop: 20}} onClick={() => navigate('/')}>
                Back To Home
            </button>
        </div>

    );


}