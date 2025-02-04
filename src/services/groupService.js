import axios from 'axios';
import {jwtDecode} from "jwt-decode";

const API_BASE_URL = 'http://localhost:8080/groups'; // Basis-URL für Gruppen-Endpoints

const getUserIdFromToken = () => {
    const token = localStorage.getItem('token');
    if (!token) {
        throw new Error('Token nicht gefunden. Bitte melden Sie sich erneut an.');
    }
    const decoded = jwtDecode(token);
    return decoded.userId; // Angenommen, die Benutzer-ID ist als `userId` im Token enthalten
};

// Alle Gruppen abrufen
export const getAllGroups = async () => {
    try {
        const response = await axios.get(API_BASE_URL, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Liste der Gruppen
    } catch (error) {
        throw error.response?.data || 'Fehler beim Abrufen der Gruppen.';
    }
};

// Gruppe erstellen
export const createGroup = async (groupNameCreated, groupPasswordCreated) => {
    const ownerIdCreated = getUserIdFromToken();
    console.log("groupService - create - ownerId: ", ownerIdCreated);
    try {
        const response = await axios.post(
            API_BASE_URL,
            { groupNameCreated, ownerIdCreated, groupPasswordCreated },
            {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            }
        );
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Erstellen der Gruppe.';
    }
};

// Benutzer zu Gruppe hinzufügen
export const addUserToGroup = async (groupId, userId) => {
    try {
        const response = await axios.put(
            `${API_BASE_URL}/${groupId}/add-user/${userId}`,
            {},
            {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            }
        );
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Hinzufügen des Benutzers zur Gruppe.';
    }
};

// Benutzer aus Gruppe entfernen
export const removeUserFromGroup = async (groupId, userId) => {
    try {
        const response = await axios.put(
            `${API_BASE_URL}/${groupId}/remove-user/${userId}`,
            {},
            {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            }
        );
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Entfernen des Benutzers aus der Gruppe.';
    }
};

// Gruppe löschen
export const deleteGroup = async (groupId) => {
    try {
        const response = await axios.delete(`${API_BASE_URL}/${groupId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Erfolgsnachricht
    } catch (error) {
        throw error.response?.data || 'Fehler beim Löschen der Gruppe.';
    }
};

// Gruppen, denen der Benutzer angehört (Basic_User spezifisch)
export const getJoinedGroups = async () => {
    const userId = getUserIdFromToken();
    try {
        const response = await axios.get(`${API_BASE_URL}/joined/${userId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Liste der Gruppen, denen der Benutzer angehört
    } catch (error) {
        throw error.response?.data || 'Fehler beim Abrufen der beigetretenen Gruppen.';
    }
};

export default {
    getAllGroups,
    createGroup,
    addUserToGroup,
    removeUserFromGroup,
    deleteGroup,
    getJoinedGroups,
};
