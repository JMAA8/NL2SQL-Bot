import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/admin'; // Basis-URL für Auth-Endpoints

// Alle Benutzer abrufen
export const getAllUsers = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/users`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        console.log('Userliste aus Response: ', response.data);
        return response.data; // Liste der Benutzer
    } catch (error) {
        throw error.response?.data || 'Fehler beim Abrufen der Benutzer.';
    }
};

// Neuen Benutzer als Admin erstellen
export const createUser = async (username, password) => {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            throw 'Kein Token gefunden. Bitte erneut anmelden.';
        }

        const response = await axios.post(
            `${API_BASE_URL}/add`,
            { username, password },
            {
                headers: { Authorization: `Bearer ${token}` },
            }
        );

        return response.data;
    } catch (error) {
        throw error.response?.data || 'Registrierung fehlgeschlagen.';
    }
};


export default {
    getAllUsers,
    createUser,

};