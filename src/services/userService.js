import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/user'; // Basis-URL für Benutzer-Endpoints

// Alle Benutzer abrufen
export const getAllUsers = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/all`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Liste der Benutzer
    } catch (error) {
        throw error.response?.data || 'Fehler beim Abrufen der Benutzer.';
    }
};

// Benutzerprofil abrufen
export const getUserProfile = async (username) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/profile`, {
            params: { username },
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Benutzerprofil
    } catch (error) {
        throw error.response?.data || 'Fehler beim Abrufen des Benutzerprofils.';
    }
};

// Benutzerprofil aktualisieren
export const updateUserProfile = async (username, updatedUser) => {
    try {
        const response = await axios.put(
            `${API_BASE_URL}/profile`,
            updatedUser,
            {
                params: { username },
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            }
        );
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Aktualisieren des Profils.';
    }
};

// Benutzer löschen
export const deleteUser = async (userId) => {
    try {
        const response = await axios.delete(`${API_BASE_URL}/${userId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Löschen des Benutzers.';
    }
};

// Rolle zu einem Benutzer hinzufügen
export const addRoleToUser = async (userId, roleName) => {
    try {
        const response = await axios.put(
            `${API_BASE_URL}/${userId}/add-role`,
            {},
            {
                params: { roleName },
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            }
        );
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Hinzufügen der Rolle.';
    }
};

// Rolle von einem Benutzer entfernen
export const removeRoleFromUser = async (userId, roleName) => {
    try {
        const response = await axios.delete(`${API_BASE_URL}/${userId}/remove-role`, {
            params: { roleName },
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Entfernen der Rolle.';
    }
};

export default {
    getAllUsers,
    getUserProfile,
    updateUserProfile,
    deleteUser,
    addRoleToUser,
    removeRoleFromUser,
};
