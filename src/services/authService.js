import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/auth'; // Basis-URL für Auth-Endpoints

// Login-Service
export const login = async (username, password) => {
    try {
        console.log('Methode login im authService try');
        const response = await axios.post(`${API_BASE_URL}/login`, { username, password });
        console.log('authService: Erhaltenes Token:', response.data);
        return response.data; // Enthält das Token
    } catch (error) {
        throw error.response?.data || 'Login fehlgeschlagen.';
    }
};

// Registrierung-Service
export const register = async (username, password) => {
    try {
        const response = await axios.post(`${API_BASE_URL}/register`, { username, password });
        return response.data; // Erfolgreiche Registrierung
    } catch (error) {
        throw error.response?.data || 'Registrierung fehlgeschlagen.';
    }
};

// Token-Validierung (optional, falls im Backend ein entsprechender Endpunkt existiert)
export const validateToken = async (token) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/validate`, {
            headers: { Authorization: `Bearer ${token}` },
        });
        return response.data; // Gültigkeit des Tokens
    } catch (error) {
        throw error.response?.data || 'Token-Validierung fehlgeschlagen.';
    }
};

// Logout-Service (lokal)
export const logout = () => {
    localStorage.removeItem('token'); // Entfernt das Token aus dem lokalen Speicher
};

export default {
    login,
    register,
    validateToken,
    logout,
};
