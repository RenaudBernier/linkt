import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { addEvent } from './api/events.api.ts';
import { useAuth } from './contexts/AuthContext';
import { useSnackbar } from 'notistack';
import './CreateData.css';

export default function AddEvent() {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [eventType, setEventType] = useState('');
    const [image, setImage] = useState('');
    const [price, setPrice] = useState('0');
    const [startTime, setStartTime] = useState('');
    const [endTime, setEndTime] = useState('');
    const [location, setLocation] = useState('');
    const [capacity, setCapacity] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { user } = useAuth();
    const { enqueueSnackbar } = useSnackbar();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        const eventData = {
            title,
            description,
            eventType,
            price,
            startDateTime: startTime,
            endDateTime: endTime,
            location,
            capacity,
            image
        };

        try {
            await addEvent(eventData);
            enqueueSnackbar('Event created successfully!', { variant: 'success' });
            // Navigate to My Events dashboard if user is organizer, otherwise home
            if (user?.userType === 'organizer') {
                navigate('/my-events');
            } else {
                navigate('/');
            }
        }
        catch (error) {
            console.error('New Event Creation failed!', error);
            enqueueSnackbar('Failed to create event. Please try again.', { variant: 'error' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{maxWidth: 500, margin: 'auto', padding: 15}}>
            <h2 className='createtitle'>CREATE EVENT</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Name Of Event: <span style={{color: 'red'}}>*</span></label><br/>
                    <input
                        type="text"
                        value={title}
                        onChange={e => setTitle(e.target.value)}
                        required
                        style={{width: '100%', padding: 8, marginTop: 5}}
                        placeholder="e.g., Spring Music Festival"
                    />
                </div>
                <div style={{marginTop: 15}}>
                    <label>Description: <span style={{color: 'red'}}>*</span></label><br/>
                    <textarea 
                        value={description}
                        onChange={e => setDescription(e.target.value)}
                        required
                        style={{width: '100%', padding: 8, marginTop: 5, minHeight: 80}}
                        placeholder="Provide a detailed description of your event..."
                    ></textarea>
                </div>
                <div style={{marginTop: 15}}>
                    <label>Event Category: <span style={{color: 'red'}}>*</span></label><br/>
                    <input
                        type="text"
                        value={eventType}
                        onChange={e => setEventType(e.target.value)}
                        required
                        style={{width: '100%', padding: 8, marginTop: 5}}
                        placeholder="e.g., Academic, Cultural, Sports, Social"
                    />
                </div>
                <div style={{marginTop: 15}}>
                    <label>Location: <span style={{color: 'red'}}>*</span></label><br/>
                    <input
                        type="text"
                        value={location}
                        onChange={e => setLocation(e.target.value)}
                        required
                        style={{width: '100%', padding: 8, marginTop: 5}}
                        placeholder="e.g., Campus Quad, Main Hall"
                    />
                </div>
                <div style={{marginTop: 15}}>
                    <label>Ticket Price: <span style={{color: 'red'}}>*</span></label><br/>
                    <input
                        type="number"
                        value={price}
                        onChange={e => setPrice(e.target.value)}
                        required
                        min="0"
                        step="0.01"
                        style={{width: '100%', padding: 8, marginTop: 5}}
                        placeholder="Enter 0 for free events"
                    />
                </div>
                <div style={{marginTop: 15}}>
                    <label>Capacity: <span style={{color: 'red'}}>*</span></label><br/>
                    <input
                        type="number"
                        value={capacity}
                        onChange={e => setCapacity(e.target.value)}
                        required
                        min="1"
                        style={{width: '100%', padding: 8, marginTop: 5}}
                        placeholder="Maximum number of attendees"
                    />
                </div>
                <div style={{marginTop: 15}}>
                    <label>Image URL: (Optional)</label><br/>
                    <small style={{color: '#666'}}>Please enter an image link online for your event!</small><br/>
                    <input
                        type="text"
                        value={image}
                        onChange={e => setImage(e.target.value)}
                        style={{width: '100%', padding: 8, marginTop: 5}}
                        placeholder="https://example.com/image.jpg"
                    />
                </div>
                <div style={{marginTop: 15}}>
                    <label>Start Date & Time: <span style={{color: 'red'}}>*</span></label><br/>
                    <input
                        type="datetime-local"
                        value={startTime}
                        onChange={e => setStartTime(e.target.value)}
                        required
                        style={{width: '100%', padding: 8, marginTop: 5}}
                    />
                </div>
                <div style={{marginTop: 15}}>
                    <label>End Date & Time: <span style={{color: 'red'}}>*</span></label><br/>
                    <input
                        type="datetime-local"
                        value={endTime}
                        onChange={e => setEndTime(e.target.value)}
                        required
                        style={{width: '100%', padding: 8, marginTop: 5}}
                    />
                </div>
                <div style={{marginTop: 25, display: 'flex', gap: 10}}>
                    <button 
                        type="button" 
                        onClick={() => user?.userType === 'organizer' ? navigate('/my-events') : navigate('/')}
                        disabled={loading}
                        style={{
                            padding: '10px 20px',
                            background: 'black',
                            color: 'white',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            borderRadius: 4,
                            fontWeight: 'bold'
                        }}
                    >
                        Cancel
                    </button>
                    <button 
                        type="submit" 
                        disabled={loading}
                        style={{
                            padding: '10px 20px',
                            background: loading ? '#ccc' : '#2563eb',
                            color: 'white',
                            border: 'none',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            borderRadius: 4,
                            fontWeight: 'bold'
                        }}
                    >
                        {loading ? 'Creating...' : 'Create Event'}
                    </button>
                </div>
            </form>
        </div>
    );
}