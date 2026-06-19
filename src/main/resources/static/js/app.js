/**
 * Main Application Logic
 */

const app = {
    isLoginMode: true,
    allCars: [],

    init() {
        this.updateNav();
        this.loadCars();
        // Setup simple router
        window.addEventListener('hashchange', () => this.handleRoute());
        this.handleRoute();
    },

    handleRoute() {
        const hash = window.location.hash;
        if (hash === '#dashboard' && api.isLoggedIn()) {
            this.showDashboard();
        } else {
            this.showHome();
        }
    },

    // --- Navigation & UI State ---
    
    updateNav() {
        const navContainer = document.getElementById('nav-auth-container');
        if (api.isLoggedIn()) {
            const userName = sessionStorage.getItem('userName') || sessionStorage.getItem('userEmail');
            const isAdmin = sessionStorage.getItem('userRole') === 'ADMIN';
            
            navContainer.innerHTML = `
                <div class="user-greeting">
                    ${isAdmin ? `<button class="btn btn-primary" onclick="app.openAdminCarModal()">+ Auto</button>` : ''}
                    <span onclick="window.location.hash='#dashboard'">Hi, ${userName.split('@')[0]}</span>
                    <button class="btn btn-outline" onclick="app.logout()">Logout</button>
                </div>
            `;
        } else {
            navContainer.innerHTML = `
                <button class="btn btn-outline" onclick="app.openLoginModal()">Login</button>
            `;
        }
    },

    showHome() {
        document.getElementById('page-home').classList.add('active');
        document.getElementById('page-dashboard').classList.remove('active');
        window.location.hash = '';
    },

    showDashboard() {
        if (!api.isLoggedIn()) {
            this.openLoginModal();
            return;
        }
        document.getElementById('page-home').classList.remove('active');
        document.getElementById('page-dashboard').classList.add('active');
        this.loadDashboardData();
    },

    logout() {
        api.clearCredentials();
        sessionStorage.removeItem('userRole');
        this.updateNav();
        this.showHome();
        this.loadCars(); // refresh grid to remove admin buttons
    },

    // --- Modals ---

    openLoginModal() {
        document.getElementById('modal-login').classList.add('active');
        this.isLoginMode = true;
        this.renderAuthForm();
    },

    openBookingModal(carId, brand, price, category) {
        if (!api.isLoggedIn()) {
            this.openLoginModal();
            return;
        }
        document.getElementById('modal-booking').classList.add('active');
        
        document.getElementById('book-car-id').value = carId;
        document.getElementById('book-car-price').value = price;
        
        document.getElementById('booking-car-info').innerHTML = `
            <h3>${brand}</h3>
            <p>${category} | ${price} € / Tag</p>
        `;

        // Reset form
        document.getElementById('form-booking').reset();
        document.getElementById('calc-days').innerText = '0';
        document.getElementById('calc-price-day').innerText = price + ' €';
        document.getElementById('calc-total').innerText = '0 €';
        document.getElementById('booking-error').innerText = '';
        document.getElementById('booking-success').innerText = '';
    },

    openAdminCarModal() {
        document.getElementById('modal-admin-car').classList.add('active');
        document.getElementById('form-admin-car').reset();
    },

    closeModals() {
        document.querySelectorAll('.modal').forEach(m => m.classList.remove('active'));
    },

    // --- Authentication Flow ---

    toggleAuthMode() {
        this.isLoginMode = !this.isLoginMode;
        this.renderAuthForm();
    },

    renderAuthForm() {
        const title = document.getElementById('auth-title');
        const regFields = document.getElementById('register-fields');
        const btn = document.getElementById('btn-submit-auth');
        const switchText = document.getElementById('auth-switch-text');
        const switchLink = document.querySelector('.auth-switch a');
        
        document.getElementById('auth-error').innerText = '';

        if (this.isLoginMode) {
            title.innerText = 'Willkommen zurück';
            regFields.style.display = 'none';
            btn.innerText = 'Einloggen';
            switchText.innerText = 'Neu hier?';
            switchLink.innerText = 'Registrieren';
            
            // remove required from reg fields
            document.getElementById('auth-name').removeAttribute('required');
        } else {
            title.innerText = 'Konto erstellen';
            regFields.style.display = 'block';
            btn.innerText = 'Registrieren';
            switchText.innerText = 'Bereits ein Konto?';
            switchLink.innerText = 'Einloggen';
            
            // add required
            document.getElementById('auth-name').setAttribute('required', 'true');
        }
    },

    async handleAuth(e) {
        e.preventDefault();
        const email = document.getElementById('auth-email').value;
        const password = document.getElementById('auth-password').value;
        const errorDiv = document.getElementById('auth-error');
        
        try {
            if (this.isLoginMode) {
                // Login
                api.setCredentials(email, password);
                await api.fetchMyId(); // Verify and fetch ID
                this.closeModals();
                this.updateNav();
                this.loadCars();
            } else {
                // Register
                const userData = {
                    name: document.getElementById('auth-name').value,
                    email: email,
                    password: password,
                    phone: document.getElementById('auth-phone').value,
                    address: document.getElementById('auth-address').value,
                    birthDate: document.getElementById('auth-birthdate').value,
                    hasDriverLicense: document.getElementById('auth-license').checked
                };
                await api.register(userData);
                
                // Auto login after register
                api.setCredentials(email, password);
                await api.fetchMyId();
                this.closeModals();
                this.updateNav();
                this.loadCars();
            }
        } catch (err) {
            errorDiv.innerText = err.message || 'Authentifizierung fehlgeschlagen.';
            api.clearCredentials();
        }
    },

    // --- Cars / Flotte ---

    async loadCars() {
        const grid = document.getElementById('car-grid');
        grid.innerHTML = '<div class="loader"></div>';
        try {
            this.allCars = await api.getCars();
            this.applyFilters();
        } catch (e) {
            console.error(e);
            grid.innerHTML = '<p style="color:var(--danger)">Fehler beim Laden der Flotte. Bitte prüfe die Backend-Verbindung.</p>';
        }
    },

    applyFilters() {
        if (!this.allCars) return;
        
        const catFilter = document.getElementById('filter-category').value;
        const fuelFilter = document.getElementById('filter-fuel').value;
        
        let filtered = this.allCars;
        
        if (catFilter !== 'ALL') {
            filtered = filtered.filter(c => c.category === catFilter);
        }
        if (fuelFilter !== 'ALL') {
            filtered = filtered.filter(c => c.fuel === fuelFilter);
        }
        
        this.renderCars(filtered);
    },

    renderCars(carsToRender) {
        const grid = document.getElementById('car-grid');
        const isAdmin = sessionStorage.getItem('userRole') === 'ADMIN';
        grid.innerHTML = '';
        
        if(carsToRender.length === 0) {
            grid.innerHTML = '<p style="color:var(--text-muted)">Keine Fahrzeuge entsprechen den Filterkriterien.</p>';
            return;
        }

        carsToRender.forEach(car => {
            // Map local images based on category or fallback
            let imgPath = 'images/sport_car.png'; // fallback
            if(car.category === 'SUV') imgPath = 'images/luxury_suv.png';
            if(car.fuel === 'ELEKTRO' || car.category === 'LIMOUSINE') imgPath = 'images/electric_car.png';
            if(car.category === 'SPORTWAGEN') imgPath = 'images/sport_car.png';

            const html = `
                <div class="car-card">
                    <div class="car-img-wrapper">
                        <span class="car-status status-${car.status}">${car.status}</span>
                        <img src="${imgPath}" alt="${car.brand}">
                    </div>
                    <div class="car-info">
                        <div class="car-brand">${car.brand}</div>
                        <div class="car-meta">
                            <span>${car.category}</span> • 
                            <span>${car.transmission}</span> • 
                            <span>${car.seats} Sitze</span>
                        </div>
                        <div class="car-price">
                            <div class="price-val">${car.pricePerDay} <span>€ / Tag</span></div>
                            <button class="btn btn-outline" 
                                ${car.status !== 'AVAILABLE' ? 'disabled' : ''}
                                onclick="app.openBookingModal(${car.id}, '${car.brand}', ${car.pricePerDay}, '${car.category}')">
                                ${car.status === 'AVAILABLE' ? 'Buchen' : 'Belegt'}
                            </button>
                        </div>
                        ${isAdmin ? `
                            <div class="admin-actions">
                                <button class="btn btn-outline" onclick="app.adminUpdatePrice(${car.id})">Preis</button>
                                <button class="btn btn-danger" onclick="app.adminDeleteCar(${car.id})">Löschen</button>
                            </div>
                        ` : ''}
                    </div>
                </div>
            `;
            grid.insertAdjacentHTML('beforeend', html);
        });
    },

    // --- Admin Functions ---
    async handleAdminCar(e) {
        e.preventDefault();
        const data = {
            brand: document.getElementById('admin-car-brand').value,
            category: document.getElementById('admin-car-cat').value,
            fuel: document.getElementById('admin-car-fuel').value,
            pricePerDay: document.getElementById('admin-car-price').value,
            seats: document.getElementById('admin-car-seats').value,
            transmission: document.getElementById('admin-car-trans').value,
            licensePlate: document.getElementById('admin-car-license').value,
            status: 'AVAILABLE'
        };

        try {
            await api.createCar(data);
            this.closeModals();
            this.loadCars();
        } catch(err) {
            alert(err.message);
        }
    },

    async adminDeleteCar(id) {
        if(!confirm('Fahrzeug wirklich löschen?')) return;
        try {
            await api.deleteCar(id);
            this.loadCars();
        } catch(e) {
            alert(e.message);
        }
    },

    async adminUpdatePrice(id) {
        const p = prompt('Neuer Preis pro Tag (€):');
        if(!p) return;
        try {
            await api.updateCarPrice(id, parseFloat(p));
            this.loadCars();
        } catch(e) {
            alert(e.message);
        }
    },

    // --- Booking Logic ---

    calculatePrice() {
        const start = document.getElementById('book-start').value;
        const end = document.getElementById('book-end').value;
        const pricePerDay = parseFloat(document.getElementById('book-car-price').value);
        
        if (start && end) {
            const sDate = new Date(start);
            const eDate = new Date(end);
            
            // Calculate difference in days (ignoring time)
            sDate.setHours(0,0,0,0);
            eDate.setHours(0,0,0,0);
            
            const diffTime = eDate - sDate;
            let diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            if (diffDays <= 0) diffDays = 1; // Same day booking counts as 1 day

            document.getElementById('calc-days').innerText = diffDays;
            document.getElementById('calc-total').innerText = (diffDays * pricePerDay).toFixed(2) + ' €';
        }
    },

    async handleBooking(e) {
        e.preventDefault();
        const errDiv = document.getElementById('booking-error');
        const sucDiv = document.getElementById('booking-success');
        const btn = document.getElementById('btn-submit-booking');
        
        errDiv.innerText = '';
        sucDiv.innerText = '';
        btn.disabled = true;

        const formatForBackend = (dateString) => {
            const [y, m, d] = dateString.split('-');
            return `${d}.${m}.${y} 10:00`;
        };

        const bookingData = {
            autoId: parseInt(document.getElementById('book-car-id').value),
            startDateTime: formatForBackend(document.getElementById('book-start').value),
            endDateTime: formatForBackend(document.getElementById('book-end').value)
        };

        try {
            await api.bookCar(bookingData);
            sucDiv.innerText = 'Buchung erfolgreich!';
            setTimeout(() => {
                this.closeModals();
                this.loadCars(); // refresh status
                window.location.hash = '#dashboard';
                this.showDashboard();
            }, 1500);
        } catch (e) {
            errDiv.innerText = e.message;
            btn.disabled = false;
        }
    },

    // --- Dashboard ---

    async loadDashboardData() {
        const isAdmin = sessionStorage.getItem('userRole') === 'ADMIN';
        
        document.getElementById('dashboard-user-info').innerHTML = `
            <p>${sessionStorage.getItem('userName')} | ${sessionStorage.getItem('userEmail')}</p>
        `;

        this.loadProfileForm();
        this.loadMyReservations();

        const adminPanels = document.getElementById('admin-panels');
        if (isAdmin) {
            adminPanels.style.display = 'block';
            this.loadAdminUsers();
            this.loadAdminReservations();
        } else {
            adminPanels.style.display = 'none';
        }
    },

    async loadProfileForm() {
        try {
            const userId = sessionStorage.getItem('userId');
            // Fetch all users to find my data (since no direct GET /me)
            const users = await api.getAllUsers();
            const me = users.find(u => u.id == userId);
            if (me) {
                document.getElementById('profile-name').value = me.name || '';
                document.getElementById('profile-email').value = me.email || '';
                document.getElementById('profile-phone').value = me.phone || '';
                document.getElementById('profile-address').value = me.address || '';
                document.getElementById('profile-birthdate').value = me.birthDate || '';
                document.getElementById('profile-license').checked = me.hasDriverLicense || false;
            }
        } catch (e) {
            console.error('Konnte Profil nicht laden', e);
        }
    },

    async handleProfileUpdate(e) {
        e.preventDefault();
        const userId = sessionStorage.getItem('userId');
        const errorDiv = document.getElementById('profile-error');
        const successDiv = document.getElementById('profile-success');
        errorDiv.innerText = '';
        successDiv.innerText = '';

        const userData = {
            name: document.getElementById('profile-name').value,
            email: document.getElementById('profile-email').value,
            phone: document.getElementById('profile-phone').value,
            address: document.getElementById('profile-address').value,
            birthDate: document.getElementById('profile-birthdate').value,
            hasDriverLicense: document.getElementById('profile-license').checked
        };

        try {
            await api.updateUser(userId, userData);
            sessionStorage.setItem('userName', userData.name);
            sessionStorage.setItem('userEmail', userData.email);
            document.getElementById('dashboard-user-info').innerHTML = `
                <p>${userData.name} | ${userData.email}</p>
            `;
            this.updateNav(); // update top right name
            successDiv.innerText = 'Profil erfolgreich aktualisiert!';
            setTimeout(() => successDiv.innerText = '', 3000);
        } catch (err) {
            errorDiv.innerText = err.message;
        }
    },

    async loadMyReservations() {
        const list = document.getElementById('reservations-list');
        list.innerHTML = '<div class="loader"></div>';
        try {
            const reservations = await api.getMyReservations();
            list.innerHTML = '';
            
            if (reservations.length === 0) {
                list.innerHTML = '<p style="color:var(--text-muted)">Du hast noch keine Buchungen.</p>';
                return;
            }

            reservations.forEach(res => {
                const isCancellable = res.status === 'ACTIVE';
                const html = `
                    <div class="reservation-card">
                        <div class="res-details">
                            <h4>Buchung #${res.id}</h4>
                            <div class="res-meta">
                                ${new Date(res.startTime).toLocaleDateString()} bis ${new Date(res.endTime).toLocaleDateString()}
                            </div>
                            <span class="res-status ${res.status}">${res.status}</span>
                            <div class="res-price">Gesamt: ${res.totalPrice} €</div>
                        </div>
                        <div>
                            ${isCancellable ? `<button class="btn btn-danger" onclick="app.cancelReservation(${res.id})">Stornieren</button>` : ''}
                        </div>
                    </div>
                `;
                list.insertAdjacentHTML('beforeend', html);
            });
        } catch (e) {
            console.error(e);
            list.innerHTML = '<p style="color:var(--danger)">Fehler beim Laden der Buchungen.</p>';
        }
    },

    async cancelReservation(id) {
        if(!confirm('Bist du sicher, dass du diese Buchung stornieren möchtest?')) return;
        try {
            await api.cancelReservation(id);
            this.loadMyReservations();
            this.loadCars(); // refresh cars
            if (sessionStorage.getItem('userRole') === 'ADMIN') {
                this.loadAdminReservations();
            }
        } catch (e) {
            alert(e.message);
        }
    },

    // --- Admin Dashboard Methods ---

    async loadAdminUsers() {
        const tbody = document.getElementById('admin-users-list');
        tbody.innerHTML = '<tr><td colspan="6">Lade Nutzer...</td></tr>';
        try {
            const users = await api.getAllUsers();
            tbody.innerHTML = '';
            users.forEach(u => {
                const isMe = u.id == sessionStorage.getItem('userId');
                tbody.innerHTML += `
                    <tr style="border-bottom: 1px solid #333;">
                        <td style="padding: 1rem;">${u.id}</td>
                        <td style="padding: 1rem;">${u.name}</td>
                        <td style="padding: 1rem;">${u.email}</td>
                        <td style="padding: 1rem;">${u.phone}</td>
                        <td style="padding: 1rem;">${u.role}</td>
                        <td style="padding: 1rem;">
                            ${!isMe ? `<button class="btn btn-danger btn-sm" onclick="app.deleteUser(${u.id})">Löschen</button>` : '<span style="color:#666">Du</span>'}
                        </td>
                    </tr>
                `;
            });
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="6" style="color:red">${e.message}</td></tr>`;
        }
    },

    async deleteUser(id) {
        if(!confirm('Nutzer wirklich löschen?')) return;
        try {
            await api.deleteUser(id);
            this.loadAdminUsers();
        } catch (e) {
            alert(e.message);
        }
    },

    async loadAdminReservations() {
        const tbody = document.getElementById('admin-reservations-list');
        tbody.innerHTML = '<tr><td colspan="6">Lade alle Buchungen...</td></tr>';
        try {
            const res = await api.getAllReservations();
            tbody.innerHTML = '';
            res.forEach(r => {
                const isCancellable = r.status === 'ACTIVE';
                tbody.innerHTML += `
                    <tr style="border-bottom: 1px solid #333;">
                        <td style="padding: 1rem;">${r.id}</td>
                        <td style="padding: 1rem;">Auto ID: ${r.autoId}</td>
                        <td style="padding: 1rem;">User ID: ${r.userId}</td>
                        <td style="padding: 1rem;">${new Date(r.startTime).toLocaleDateString()} - ${new Date(r.endTime).toLocaleDateString()}</td>
                        <td style="padding: 1rem;">${r.totalPrice} €</td>
                        <td style="padding: 1rem;">
                            <span class="res-status ${r.status}" style="font-size:0.8rem; padding:2px 6px">${r.status}</span>
                            ${isCancellable ? `<button class="btn btn-danger btn-sm" style="margin-left:10px" onclick="app.cancelReservation(${r.id})">Storno</button>` : ''}
                        </td>
                    </tr>
                `;
            });
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="6" style="color:red">${e.message}</td></tr>`;
        }
    }
};

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    app.init();
});
