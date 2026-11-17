async function loadBookings() {
    const container = document.getElementById('bookingsList');
    if (!isLoggedIn()) {
        container.innerHTML = '<div class="alert alert-warning">Πρέπει να συνδεθείτε για να δείτε τις κρατήσεις σας.</div>';
        return;
    }

    const token = localStorage.getItem('token');
    try {
        const response = await fetch(`${API_BASE}/bookings/user`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (!response.ok) throw new Error('Σφάλμα φόρτωσης κρατήσεων');

        const bookings = await response.json();

        if (bookings.length === 0) {
            container.innerHTML = '<div class="alert alert-info">Δεν έχετε κρατήσεις ακόμα.</div>';
            return;
        }

        let html = '<table class="table table-striped align-middle"><thead><tr><th>ID</th><th>Δωμάτιο</th><th>Check-in</th><th>Check-out</th><th>Σύνολο</th><th>Status</th><th>Ενέργεια</th></tr></thead><tbody>';
        
        bookings.forEach(b => {

            let badgeClass = 'bg-secondary';
            if (b.status === 'PENDING') badgeClass = 'bg-warning text-dark';
            if (b.status === 'CONFIRMED') badgeClass = 'bg-success';
            if (b.status === 'CHECKED_IN') badgeClass = 'bg-info text-dark';
            if (b.status === 'COMPLETED') badgeClass = 'bg-primary';
            if (b.status === 'CANCELLED') badgeClass = 'bg-danger';

            // koumpia energeies
            let actionButton = '';
            
            // An einai COMPLETED, deixno to koumpi gia na kanei o xristis review
            if (b.status === 'COMPLETED') {
                actionButton += `<button class="btn btn-sm btn-outline-primary me-2" onclick="openReviewModal(${b.id}, '${b.room.roomNumber}')">Αξιολόγηση</button>`;
            }
            
            // An den einai CANCELLED i COMPLETED, deixno to koumpi gia na kanei o xristis cancel
            if (b.status !== 'CANCELLED' && b.status !== 'COMPLETED') {
                actionButton += `<button class="btn btn-sm btn-outline-danger" onclick="cancelBooking(${b.id})">Ακύρωση</button>`;
            }
            
            // An den yparxei action button, deixno ena text-muted small "-"
            if (actionButton === '') {
                actionButton = `<span class="text-muted small">-</span>`;
            }

            html += `<tr>
                <td>${b.id}</td>
                <td>${b.room.roomNumber}</td>
                <td>${b.checkInDate}</td>
                <td>${b.checkoutDate}</td>
                <td>€${b.totalPrice.toFixed(2)}</td>
                <td><span class="badge ${badgeClass}">${b.status}</span></td>
                <td>${actionButton}</td>
            </tr>`;
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

function openReviewModal(bookingId, roomNumber) {
    document.getElementById('reviewBookingId').value = bookingId;
    document.getElementById('reviewRoomNumber').innerText = roomNumber;
    document.getElementById('reviewComment').value = '';
    document.getElementById('reviewRating').value = '5'; // Default 

    const modal = new bootstrap.Modal(document.getElementById('reviewModal'));
    modal.show();
}

async function submitReview() {
    const bookingId = document.getElementById('reviewBookingId').value;
    const rating = parseInt(document.getElementById('reviewRating').value);
    const comment = document.getElementById('reviewComment').value;
    const token = localStorage.getItem('token');

    const reviewData = { bookingId, rating, comment };

    try {
        const response = await fetch(`${API_BASE}/reviews`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(reviewData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        alert('✅ Η αξιολόγηση υποβλήθηκε με επιτυχία!');
        
        const modalElement = document.getElementById('reviewModal');
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();

        loadBookings();

    } catch (err) {
        alert('❌ ' + err.message);
    }
}

// Akirosi kratisis me epivevaiosi kai elegxo gia 48 ores
async function cancelBooking(bookingId) {
    if (!confirm('Είστε σίγουροι ότι θέλετε να ακυρώσετε αυτή την κράτηση; Αν είστε εντός 48 ωρών από το check-in, θα χρεωθείτε το 50%.')) {
        return;
    }

    const token = localStorage.getItem('token');

    try {
        const response = await fetch(`${API_BASE}/bookings/${bookingId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        alert('✅ Η κράτηση ακυρώθηκε επιτυχώς!');
        
        // Epanefere ton pinaka me tis kratisis
        loadBookings();

    } catch (err) {
        alert('❌ ' + err.message);
    }
}

document.addEventListener('DOMContentLoaded', loadBookings);