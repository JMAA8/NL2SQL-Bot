import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/chat'; // Basis-URL für Chat-Endpoints

// Alle Chats eines Benutzers abrufen
export const getUserChats = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/user`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Liste der vom Benutzer erstellten Chats
    } catch (error) {
        console.error('Fehler beim Abrufen der Benutzer-Chats:', error);
        throw error.response?.data || 'Fehler beim Abrufen der Benutzer-Chats.';
    }
};



// Nachrichten eines spezifischen Chats abrufen
export const getChatMessages = async (chatId) => {
    try {
        const response = await axios.get(`${API_BASE_URL}/${chatId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
        return response.data; // Liste der Nachrichten des Chats
    } catch (error) {
        console.error(`Fehler beim Abrufen der Nachrichten für Chat ${chatId}:`, error);
        throw error.response?.data || 'Fehler beim Abrufen der Nachrichten.';
    }
};

// Nachricht senden (für neuen oder bestehenden Chat)
export const sendMessage = async (text, chatId) => {
    try {
        const response = await axios.post(
            API_BASE_URL,
            { text, chatId }, // Text und optionale Chat-ID senden
            {
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            }
        );
        return response.data; // Neue Nachricht und ggf. Chat-Details (z. B. neue chatId)
    } catch (error) {
        console.error('Fehler beim Senden der Nachricht:', error);
        throw error.response?.data || 'Fehler beim Senden der Nachricht.';
    }
};

export default {
    getUserChats,
    getChatMessages,
    sendMessage,
};
