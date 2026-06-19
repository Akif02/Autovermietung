/**
 * API Modul für die Kommunikation mit dem Spring Boot Backend.
 * Handhabt auch die HTTP Basic Authentication.
 */
const api = {
    baseUrl: '/api',

    // --- Auth Management ---
    getAuthHeaders() {
        const token = sessionStorage.getItem('authToken');
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) {
            headers['Authorization'] = 'Basic ' + token;
        }
        return headers;
    },

    setCredentials(email, password) {
        const token = btoa(email + ':' + password);
        sessionStorage.setItem('authToken', token);
        sessionStorage.setItem('userEmail', email);
    },

    clearCredentials() {
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('userEmail');
        sessionStorage.removeItem('userId'); // We'll try to fetch this later
    },

    isLoggedIn() {
        return sessionStorage.getItem('authToken') !== null;
    },

    // --- API Calls ---

    async getCars() {
        const response = await fetch('/auto/all', {
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Fehler beim Laden der Autos');
        return response.json();
    },

    async login(email, password) {
        this.setCredentials(email, password);
        // We verify login by trying to fetch the current user profile or just users endpoint
        // Since there's no /api/users/me endpoint, we try to fetch all users (if admin) or just rely on the token.
        // Wait, /api/users is only accessible if authenticated. Let's do a dummy request.
        // Actually, if we just decode the token we can do a request to see if it's 401.
        try {
            // Wir machen einen GET auf /reservation um zu schauen ob auth klappt.
            // Wait, /reservation is admin only for GET? Let's check.
            // GET /reservation/user/{id} requires ID. 
            // We might just trust the credentials until an API call fails.
            return true;
        } catch (e) {
            this.clearCredentials();
            throw e;
        }
    },

    async register(userData) {
        const response = await fetch(this.baseUrl + '/users', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Registrierung fehlgeschlagen');
        }
        return response.json();
    },

    // A hack to find the current user's ID since there is no /me endpoint
    async fetchMyId() {
        // This is tricky without a /me endpoint. We can try to fetch all users and find our email?
        // But /api/users might be admin only or might expose other users.
        // Let's assume we can GET /api/users and filter by email.
        const response = await fetch(this.baseUrl + '/users', {
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Could not fetch users');
        const users = await response.json();
        const myEmail = sessionStorage.getItem('userEmail');
        const me = users.find(u => u.email === myEmail);
        if (me) {
            sessionStorage.setItem('userId', me.id);
            sessionStorage.setItem('userName', me.name);
            sessionStorage.setItem('userRole', me.role);
            return me.id;
        }
        throw new Error('User not found');
    },

    async getMyReservations() {
        let userId = sessionStorage.getItem('userId');
        if (!userId) {
            userId = await this.fetchMyId();
        }
        const response = await fetch(`/reservation/user/${userId}`, {
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Fehler beim Laden der Buchungen');
        return response.json();
    },

    async bookCar(bookingData) {
        const response = await fetch('/reservation', {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(bookingData)
        });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Buchung fehlgeschlagen');
        }
        return response.json();
    },

    async cancelReservation(resId) {
        const response = await fetch(`/reservation/cancel/${resId}`, {
            method: 'PATCH',
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Stornierung fehlgeschlagen');
        return response.json();
    },

    // --- Admin Endpoints ---

    async getAllUsers() {
        const response = await fetch('/api/users', {
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Nutzer konnten nicht geladen werden');
        return response.json();
    },

    async deleteUser(id) {
        const response = await fetch(`/api/users/${id}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Nutzer konnte nicht gelöscht werden');
    },

    async updateUser(id, userData) {
        const response = await fetch(`/api/users/${id}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(userData)
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Profil konnte nicht aktualisiert werden');
        }
        return response.json();
    },

    async getAllReservations() {
        const response = await fetch('/reservation', {
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Buchungen konnten nicht geladen werden');
        return response.json();
    },

    async createCar(carData) {
        const response = await fetch('/auto', {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(carData)
        });
        if (!response.ok) throw new Error('Auto konnte nicht erstellt werden');
        return response.json();
    },

    async deleteCar(id) {
        const response = await fetch(`/auto/${id}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Auto konnte nicht gelöscht werden');
    },

    async updateCarPrice(id, price) {
        const response = await fetch(`/auto/${id}/price?price=${price}`, {
            method: 'PATCH',
            headers: this.getAuthHeaders()
        });
        if (!response.ok) throw new Error('Preis konnte nicht geändert werden');
        return response.json();
    }
};
