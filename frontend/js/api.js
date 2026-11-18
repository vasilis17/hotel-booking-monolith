// --- SISTIMA EIDOPOIISEON (NOTIFICATIONS) ---

// Synartisi gia na emfanizoume minimata epityxias i lathous
function showNotification(message, type = 'success') {
    // Dimiourgoume to container an den yparxei idi
    let area = document.getElementById('notificationArea');
    if (!area) {
        area = document.createElement('div');
        area.id = 'notificationArea';
        area.style.position = 'fixed';
        area.style.top = '80px';
        area.style.right = '20px';
        area.style.zIndex = '9999';
        area.style.minWidth = '300px';
        document.body.appendChild(area);
    }

    const alertDiv = document.createElement('div');
    // Xrisimopoioume ta Bootstrap classes
    alertDiv.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show shadow`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    area.appendChild(alertDiv);

    // Aftomato kleisimo meta apo 4 deuterolepta
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.classList.remove('show');
            setTimeout(() => alertDiv.remove(), 150);
        }
    }, 4000);
}

// --- PROSARMOSMENO MODAL EPIVEVAIOSIS (ANTI GIA CONFIRM) ---

function showConfirmModal(message, onConfirm) {
    // Dimiourgia tou Modal HTML an den yparxei
    let modalElement = document.getElementById('customConfirmModal');
    if (!modalElement) {
        modalElement = document.createElement('div');
        modalElement.id = 'customConfirmModal';
        modalElement.className = 'modal fade';
        modalElement.innerHTML = `
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Επιβεβαίωση</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body" id="customConfirmMessage"></div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Άκυρο</button>
                        <button type="button" class="btn btn-danger" id="customConfirmBtn">Ναι, Συνέχεια</button>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modalElement);
    }
    
    // Thetoume to minima
    document.getElementById('customConfirmMessage').innerText = message;
    
    // Diaxeirisi tou koumpiou epivevaiosis
    const confirmBtn = document.getElementById('customConfirmBtn');
    // Afairoume palia events gia na min kalitai polles fores
    const newConfirmBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
    
    newConfirmBtn.addEventListener('click', () => {
        // Kleisimo tou modal
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();
        // Ektelesi tis synartisis
        onConfirm();
    });

    // Anoigma tou modal
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

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