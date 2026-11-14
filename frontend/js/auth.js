// Elegxei an o xristis einai sindedemenos
function isLoggedIn() {
    return localStorage.getItem('token') !== null;
}

// Apothikeuei to session sto localStorage
function saveSession(token, user) {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
}

// Aposindesi tou xristi
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

// Enimerwnei to menu analoga me to an o xristis einai sindedemenos
function updateNavbar() {
    const navLogin = document.getElementById('nav-login');
    const navLogout = document.getElementById('nav-logout');
    const navBookings = document.getElementById('nav-bookings');
    const userNameSpan = document.getElementById('user-name');

    if (isLoggedIn()) {
        const user = JSON.parse(localStorage.getItem('user'));
        if (navLogin) navLogin.style.display = 'none';
        if (navLogout) navLogout.style.display = 'block';
        if (navBookings) navBookings.style.display = 'block';
        if (userNameSpan && user) userNameSpan.textContent = user.name;
    } else {
        if (navLogin) navLogin.style.display = 'block';
        if (navLogout) navLogout.style.display = 'none';
        if (navBookings) navBookings.style.display = 'none';
    }
}

// Otan o xristis patisei to logout, kalw tin logout synartisi
document.addEventListener('DOMContentLoaded', updateNavbar);