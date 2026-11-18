// Metavlites gia tis imerominies pou epilegei o xristis
let selectedCheckIn = null;
let selectedCheckOut = null;

// Eikones gia kathe typo domatiou
const roomImages = {
    'SINGLE': 'https://images.unsplash.com/photo-1590490360182-c33d57733427?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    'DOUBLE': 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
    'SUITE': 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
};

// Synartisi pou kalite otan pataei "Anazitisi"
function searchRooms() {
    const checkIn = document.getElementById('searchCheckIn').value;
    const checkOut = document.getElementById('searchCheckOut').value;

    if (!checkIn || !checkOut) {
        showNotification('Παρακαλώ επιλέξτε ημερομηνίες Check-in και Check-out.', 'danger');
        return;
    }

    if (checkIn >= checkOut) {
        showNotification('Η ημερομηνία Check-out πρέπει να είναι μετά το Check-in.', 'danger');
        return;
    }

    // Apothikei tis imerominies
    selectedCheckIn = checkIn;
    selectedCheckOut = checkOut;

    // Kano scroll sta domatia
    document.getElementById('rooms').scrollIntoView({ behavior: 'smooth' });
}

// Fortosi domation
// Fortosi domation
async function loadRooms() {
    const container = document.getElementById('roomsList');
    const noRooms = document.getElementById('noRooms');

    try {
        const rooms = await getRooms();
        if (rooms.length === 0) {
            noRooms.style.display = 'block';
            return;
        }

        // Fernoume ola ta reviews gia ola ta domatia mazi
        const roomsWithReviews = await Promise.all(rooms.map(async (room) => {
            try {
                // Kanoume to fetch sto endpoint poy molis kaname permitAll
                const response = await fetch(`${API_BASE}/reviews/room/${room.id}`);
                if (response.ok) {
                    const reviews = await response.json();
                    return { room, reviews };
                } else {
                    console.warn(`Den fortwthikan ta reviews gia to domatio ${room.id}. Status: ${response.status}`);
                }
            } catch (err) {
                console.error("Error loading reviews for room", room.id, err);
            }
            return { room, reviews: [] };
        }));

        // Twra zwgrafizoume tis kartes
        roomsWithReviews.forEach(({ room, reviews }) => {
            let reviewsHtml = '<span class="text-muted small fst-italic">Δεν έχει αξιολογηθεί ακόμα</span>';
            
            if (reviews.length > 0) {
                const sum = reviews.reduce((acc, r) => acc + r.rating, 0);
                const avg = (sum / reviews.length).toFixed(1);
                
                let stars = '';
                const fullStars = Math.floor(avg);
                const hasHalfStar = avg % 1 !== 0;
                
                for (let i = 0; i < 5; i++) {
                    if (i < fullStars) {
                        stars += '<i class="bi bi-star-fill text-warning"></i>';
                    } else if (i === fullStars && hasHalfStar) {
                        stars += '<i class="bi bi-star-half text-warning"></i>';
                    } else {
                        stars += '<i class="bi bi-star text-warning"></i>';
                    }
                }

                reviewsHtml = `
                    <div class="d-flex align-items-center mb-2" style="cursor: pointer;" onclick="openReviewsModal(${room.id}, '${room.roomNumber}')">
                        <span class="me-2">${stars}</span>
                        <span class="fw-bold text-dark">${avg}/5</span>
                        <span class="text-muted small ms-2">(${reviews.length})</span>
                        <i class="bi bi-info-circle text-primary ms-2" title="Δείτε τα σχόλια"></i>
                    </div>
                `;
            }

            const card = document.createElement('div');
            card.className = 'col-md-4';
            const imageUrl = roomImages[room.type] || roomImages['DOUBLE'];
            
            card.innerHTML = `
                <div class="card h-100 shadow-sm border-0 room-card">
                    <img src="${imageUrl}" class="card-img-top" alt="Δωμάτιο ${room.roomNumber}" style="height: 250px; object-fit: cover;">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <h5 class="card-title mb-0">Δωμάτιο ${room.roomNumber}</h5>
                            <span class="badge bg-primary">${room.type}</span>
                        </div>
                        
                        <div class="mb-3">
                            ${reviewsHtml}
                        </div>
                        
                        <p class="text-muted small mb-3">
                            <i class="fas fa-wifi me-1"></i> Wi-Fi
                            <i class="fas fa-snowflake ms-2 me-1"></i> A/C
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

async function openReviewsModal(roomId, roomNumber) {
    const modalBody = document.getElementById('reviewsModalBody');
    const modalTitle = document.getElementById('reviewsModalLabel');
    
    modalTitle.innerText = `Αξιολογήσεις Δωματίου ${roomNumber}`;
    modalBody.innerHTML = '<div class="text-center"><div class="spinner-border text-primary" role="status"></div></div>';

    const modal = new bootstrap.Modal(document.getElementById('reviewsModal'));
    modal.show();

    try {
        const response = await fetch(`${API_BASE}/reviews/room/${roomId}`);
        if (!response.ok) throw new Error('Σφάλμα φόρτωσης αξιολογήσεων');
        
        const reviews = await response.json();
        
        if (reviews.length === 0) {
            modalBody.innerHTML = '<div class="alert alert-info">Δεν υπάρχουν αξιολογήσεις για αυτό το δωμάτιο.</div>';
            return;
        }

        let html = '';
        reviews.forEach(r => {
            const stars = '⭐'.repeat(r.rating);
            const userName = r.booking.user ? r.booking.user.name : 'Επισκέπτης';
            
            html += `
                <div class="card mb-3 border-0 shadow-sm">
                    <div class="card-body">
                        <div class="d-flex justify-content-between">
                            <h6 class="fw-bold mb-1">${userName}</h6>
                            <span class="text-warning">${stars}</span>
                        </div>
                        <p class="text-muted small mb-2">Διαμονή: ${r.booking.checkInDate} έως ${r.booking.checkoutDate}</p>
                        <p class="mb-0">${r.comment ? r.comment : '<em>Χωρίς σχόλιο</em>'}</p>
                    </div>
                </div>
            `;
        });
        modalBody.innerHTML = html;
    } catch (err) {
        modalBody.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

// Kratisi
async function bookRoom(roomId, price) {
    if (!isLoggedIn()) {
        showNotification('Πρέπει να συνδεθείτε για να κάνετε κράτηση!', 'danger');
        setTimeout(() => window.location.href = 'login.html', 2000);
        return;
    }

    // Elegxos an exoun epilegei imerominies
    if (!selectedCheckIn || !selectedCheckOut) {
        showNotification('Παρακαλώ πρώτα επιλέξτε ημερομηνίες Check-in και Check-out στην αρχική σελίδα.', 'danger');
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
        
        showNotification('✅ Η κράτηση ολοκληρώθηκε με επιτυχία!', 'success');
        setTimeout(() => window.location.href = 'my-bookings.html', 2000);
    } catch (err) {
        showNotification('❌ ' + err.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', loadRooms);