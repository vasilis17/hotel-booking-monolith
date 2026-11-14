// Μεταβλητές για τις ημερομηνίες που επιλέγει ο χρήστης
let selectedCheckIn = null;
let selectedCheckOut = null;

// Εικόνες για κάθε τύπο δωματίου
const roomImages = {
    'SINGLE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    'DOUBLE': 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    'SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
};

// Συνάρτηση που καλείται όταν πατάει "Αναζήτηση"
function searchRooms() {
    const checkIn = document.getElementById('searchCheckIn').value;
    const checkOut = document.getElementById('searchCheckOut').value;

    if (!checkIn || !checkOut) {
        alert('Παρακαλώ επιλέξτε ημερομηνίες Check-in και Check-out.');
        return;
    }

    if (checkIn >= checkOut) {
        alert('Η ημερομηνία Check-out πρέπει να είναι μετά το Check-in.');
        return;
    }

    // Αποθηκεύουμε τις ημερομηνίες
    selectedCheckIn = checkIn;
    selectedCheckOut = checkOut;

    // Κάνουμε scroll στα δωμάτια
    document.getElementById('rooms').scrollIntoView({ behavior: 'smooth' });
}

// Φόρτωση δωματίων
async function loadRooms() {
    const container = document.getElementById('roomsList');
    const noRooms = document.getElementById('noRooms');

    try {
        const rooms = await getRooms();
        if (rooms.length === 0) {
            noRooms.style.display = 'block';
            return;
        }

        rooms.forEach(room => {
            const card = document.createElement('div');
            card.className = 'col-md-4';
            
            const imageUrl = roomImages[room.type] || roomImages['DOUBLE'];
            
            card.innerHTML = `
                <div class="card h-100 shadow-sm border-0 room-card">
                    <img src="${imageUrl}" class="card-img-top" alt="Δωμάτιο ${room.roomNumber}" style="height: 250px; object-fit: cover;">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h5 class="card-title mb-0">Δωμάτιο ${room.roomNumber}</h5>
                            <span class="badge bg-primary">${room.type}</span>
                        </div>
                        <p class="text-muted small mb-3">
                            <i class="fas fa-wifi me-1"></i> Δωρεάν Wi-Fi
                            <i class="fas fa-snowflake ms-2 me-1"></i> A/C
                            <i class="fas fa-tv ms-2 me-1"></i> Smart TV
                        </p>
                        <h4 class="text-primary mb-3">€${room.price} <small class="text-muted fs-6">/ διανυκτέρευση</small></h4>
                        <button class="btn btn-outline-primary w-100" onclick="bookRoom(${room.id}, ${room.price})">Κράτηση</button>
                    </div>
                </div>
            `;
            container.appendChild(card);
        });
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

// Κράτηση
async function bookRoom(roomId, price) {
    if (!isLoggedIn()) {
        alert('Πρέπει να συνδεθείτε για να κάνετε κράτηση!');
        window.location.href = 'login.html';
        return;
    }

    // Έλεγχος αν έχουν επιλεγεί ημερομηνίες
    if (!selectedCheckIn || !selectedCheckOut) {
        alert('Παρακαλώ πρώτα επιλέξτε ημερομηνίες Check-in και Check-out στην αρχική σελίδα.');
        document.getElementById('rooms').scrollIntoView({ behavior: 'smooth' });
        return;
    }

    const bookingData = { 
        roomId: roomId, 
        checkIn: selectedCheckIn, 
        checkOut: selectedCheckOut 
    };

    try {
        const response = await fetch(`${API_BASE}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(bookingData)
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        
        alert('✅ Η κράτηση ολοκληρώθηκε!');
        window.location.href = 'my-bookings.html';
    } catch (err) {
        alert('❌ ' + err.message);
    }
}

document.addEventListener('DOMContentLoaded', loadRooms);