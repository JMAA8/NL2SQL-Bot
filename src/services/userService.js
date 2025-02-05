import axios from 'axios';
import {jwtDecode} from "jwt-decode";

const API_BASE_URL = 'http://localhost:8080/user'; // Basis-URL für user-Endpoints
const API_BASE_URL_Admin = 'http://localhost:8080/admin'; //Basis-URL für admin Endpoints



// Benutzerprofil abrufen
export const getUserProfile = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
        throw new Error('Token nicht gefunden. Bitte melden Sie sich erneut an.');
    }
    const decoded = jwtDecode(token);
    const userId = decoded.userId;
    console.log('userService - getUserProfil - userId: ', userId)
    try {
        const response = await axios.get(`${API_BASE_URL}/profile/${userId}`, {
            headers: {Authorization: `Bearer ${localStorage.getItem('token')}`},
        });
        console.log('userService - getUserProfil - Response: ', response.data);
        return response.data; // Benutzerprofil zurückgeben
    } catch (error) {
        throw error.response?.data || 'Fehler beim Abrufen des Benutzerprofils.';
    }
};


// Benutzerprofil aktualisieren
export const updateUserProfile = async (updatedUser) => {
    const token = localStorage.getItem('token');
    if (!token) {
        throw new Error('Token nicht gefunden. Bitte melden Sie sich erneut an.');
    }
    const decoded = jwtDecode(token);
    const userId = decoded.userId;
    console.log('userService - getUserProfil - userId: ', userId)
    try {
        const response = await axios.put(
            `${API_BASE_URL}/updateProfile/{userId}`,
            updatedUser,
            {
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
        const response = await axios.delete(`${API_BASE_URL_Admin}/${userId}`, {
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
    getUserProfile,
    updateUserProfile,
    deleteUser,
    addRoleToUser,
    removeRoleFromUser,
};
