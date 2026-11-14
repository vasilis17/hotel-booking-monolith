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
        
        if (!response.ok) {
            throw new Error('Σφάλμα φόρτωσης κρατήσεων');
        }

        const bookings = await response.json();

        if (bookings.length === 0) {
            container.innerHTML = '<div class="alert alert-info">Δεν έχετε κρατήσεις ακόμα.</div>';
            return;
        }

        let html = '<table class="table table-striped"><thead><tr><th>ID</th><th>Δωμάτιο</th><th>Check-in</th><th>Check-out</th><th>Σύνολο</th><th>Status</th></tr></thead><tbody>';
        bookings.forEach(b => {
            html += `<tr>
                <td>${b.id}</td>
                <td>${b.room.roomNumber}</td>
                <td>${b.checkInDate}</td>
                <td>${b.checkoutDate}</td>
                <td>€${b.totalPrice.toFixed(2)}</td>
                <td><span class="badge bg-success">${b.status}</span></td>
            </tr>`;
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

document.addEventListener('DOMContentLoaded', loadBookings);