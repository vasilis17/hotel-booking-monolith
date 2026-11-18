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

            // Koumpia energeion
            let actionButton = '';
            
            // An einai COMPLETED -> Review
            if (b.status === 'COMPLETED') {
                actionButton += `<button class="btn btn-sm btn-primary shadow-sm me-1" onclick="openReviewModal(${b.id}, '${b.room.roomNumber}')"><i class="bi bi-star-fill me-1"></i>Αξιολόγηση</button>`;
            }
            
            // An einai PENDING -> Pliromi
            if (b.status === 'PENDING') {
                actionButton += `<button class="btn btn-sm btn-success shadow-sm me-1" onclick="openPaymentModal(${b.id}, ${b.totalPrice})"><i class="bi bi-credit-card me-1"></i>Πληρωμή</button>`;
            }
            
            // An einai CONFIRMED -> Check-in
            if (b.status === 'CONFIRMED') {
                actionButton += `<button class="btn btn-sm btn-info text-white shadow-sm me-1" onclick="updateBookingStatus(${b.id}, 'CHECKED_IN')"><i class="bi bi-box-arrow-in-right me-1"></i>Check-in</button>`;
            }
            
            // An einai CHECKED_IN -> Check-out
            if (b.status === 'CHECKED_IN') {
                actionButton += `<button class="btn btn-sm btn-warning text-dark shadow-sm me-1" onclick="updateBookingStatus(${b.id}, 'COMPLETED')"><i class="bi bi-box-arrow-right me-1"></i>Check-out</button>`;
            }
            
            // An den einai CANCELLED i COMPLETED -> Akyrosi
            if (b.status !== 'CANCELLED' && b.status !== 'COMPLETED') {
                actionButton += `<button class="btn btn-sm btn-outline-danger shadow-sm" onclick="cancelBooking(${b.id})"><i class="bi bi-x-circle me-1"></i>Ακύρωση</button>`;
            }
            
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
    try {
        document.getElementById('reviewBookingId').value = bookingId;
        document.getElementById('reviewRoomNumber').innerText = roomNumber;
        document.getElementById('reviewComment').value = '';
        document.getElementById('reviewRating').value = '5';

        const modalElement = document.getElementById('reviewModal');
        if (typeof bootstrap === 'undefined') {
            showNotification('Σφάλμα: Το Bootstrap δεν έχει φορτωθεί σωστά.', 'danger');
            return;
        }
        
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    } catch (error) {
        showNotification('Σφάλμα κατά το άνοιγμα της αξιολόγησης: ' + error.message, 'danger');
        console.error(error);
    }
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

        showNotification('✅ Η αξιολόγηση υποβλήθηκε με επιτυχία!', 'success');
        
        const modalElement = document.getElementById('reviewModal');
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();

        loadBookings();

    } catch (err) {
        showNotification('❌ ' + err.message, 'danger');
    }
}

// Akirosi kratisis me custom modal anti gia confirm()
async function cancelBooking(bookingId) {
    const confirmMessage = 'Είστε σίγουροι ότι θέλετε να ακυρώσετε αυτή την κράτηση; Αν είστε εντός 48 ωρών από το check-in, θα χρεωθείτε το 50%.';
    
    showConfirmModal(confirmMessage, async () => {
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

            showNotification('✅ Η κράτηση ακυρώθηκε επιτυχώς!', 'success');
            loadBookings();

        } catch (err) {
            showNotification('❌ ' + err.message, 'danger');
        }
    });
}

// Anoigma tou Modal Pliromis
function openPaymentModal(bookingId, amount) {
    document.getElementById('paymentBookingId').value = bookingId;
    document.getElementById('paymentAmount').innerText = amount.toFixed(2);
    
    const modalElement = document.getElementById('paymentModal');
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

// Ektelesi pliromis (Prosimiosi)
async function processPayment() {
    const bookingId = document.getElementById('paymentBookingId').value;
    const token = localStorage.getItem('token');

    try {
        // Edw stin pragmatikotita tha ginotan klisi se Stripe/PayPal.
        // Emis apla kanoume to PUT gia na allaksoume to status.
        const response = await fetch(`${API_BASE}/bookings/${bookingId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ status: 'CONFIRMED' })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        // Kleisimo tou modal
        const modalElement = document.getElementById('paymentModal');
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();

        showNotification('✅ Η πληρωμή ολοκληρώθηκε! Η κράτηση επιβεβαιώθηκε.', 'success');
        loadBookings();

    } catch (err) {
        showNotification('❌ Σφάλμα πληρωμής: ' + err.message, 'danger');
    }
}

// Synartisi gia allagi katastasis (Check-in / Check-out)
async function updateBookingStatus(bookingId, newStatus) {
    const token = localStorage.getItem('token');
    
    // Epivevaiosi prin tin allagi
    let msg = newStatus === 'CHECKED_IN' ? 'Είστε σίγουροι ότι θέλετε να κάνετε Check-in;' : 'Είστε σίγουροι ότι θέλετε να κάνετε Check-out και να ολοκληρώσετε τη διαμονή;';
    
    showConfirmModal(msg, async () => {
        try {
            const response = await fetch(`${API_BASE}/bookings/${bookingId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ status: newStatus })
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText);
            }

            const successMsg = newStatus === 'CHECKED_IN' ? '✅ Το Check-in ολοκληρώθηκε!' : '✅ Το Check-out ολοκληρώθηκε! Μπορείτε τώρα να αξιολογήσετε τη διαμονή σας.';
            showNotification(successMsg, 'success');
            loadBookings();

        } catch (err) {
            showNotification('❌ ' + err.message, 'danger');
        }
    });
}

document.addEventListener('DOMContentLoaded', loadBookings);