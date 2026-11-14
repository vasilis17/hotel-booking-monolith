// H diefthinsi tou API pou tha xrisimopoio gia ta aitimata
const API_BASE = 'http://localhost:8080/api';

// LOGIN
async function login(email, password) {
    const response = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });
    if (!response.ok) throw new Error('Λάθος email ή κωδικός');
    return await response.json();
}

// REGISTER
async function register(name, email, password) {
    const response = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password })
    });
    if (!response.ok) throw new Error('Το email χρησιμοποιείται ήδη');
    return await response.text();
}

// GET ROOMS
async function getRooms() {
    const response = await fetch(`${API_BASE}/rooms`);
    if (!response.ok) throw new Error('Σφάλμα φόρτωσης δωματίων');
    return await response.json();
}

// CREATE BOOKING
async function createBooking(bookingData, token) {
    const response = await fetch(`${API_BASE}/bookings`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(bookingData)
    });
    if (!response.ok) {
        const error = await response.text();
        throw new Error(error);
    }
    return await response.json();
}

// GET USER BOOKINGS
async function getUserBookings(userId, token) {
    const response = await fetch(`${API_BASE}/bookings/user/${userId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!response.ok) throw new Error('Σφάλμα φόρτωσης κρατήσεων');
    return await response.json();
}