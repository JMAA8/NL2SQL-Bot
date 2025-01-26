import axios from 'axios';
import {jwtDecode} from "jwt-decode";

const API_BASE_URL = 'http://localhost:8080/chat'; // Basis-URL für Chat-Endpoints

// Hilfsfunktion zum Dekodieren des Tokens und Abrufen der Benutzer-ID
const getUserIdFromToken = () => {
    const token = localStorage.getItem('token');
    if (!token) {
        throw new Error('Token nicht gefunden. Bitte melden Sie sich erneut an.');
    }
    const decoded = jwtDecode(token);
    return decoded.userId; // Angenommen, die Benutzer-ID ist als `userId` im Token enthalten
};

// Abruf aller Chats eines Benutzers
export const getUserChats = async () => {
    try {
        const userId = getUserIdFromToken();
       console.error('ChatService - UserID:', userId);
        const response = await axios.get(`${API_BASE_URL}/user/${userId}`, {
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

export const sendMessage = async (chatId, prompt) => {
  /*  if (!chatId || !prompt) {
     console.error('Chat-ID oder Prompt fehlen.');
     return;
    }*/
    try {
        const userId = getUserIdFromToken();
        console.log('Sende Daten:', { userId, chatId, prompt });
        const response = await axios.post(
            `${API_BASE_URL}/message`,
            { userId, chatId, prompt },
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

// Titel eines Chats ändern
export const updateChatTitle = async (chatId, newTitle) => {
    try {
        const response = await axios.put(
            `${API_BASE_URL}/${chatId}/title`,
            null,
            {
                params: { newTitle },
                headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
            }
        );
        return response.data; // Aktualisierte Chat-Daten
    } catch (error) {
        console.error(`Fehler beim Aktualisieren des Titels für Chat ${chatId}:`, error);
        throw error.response?.data || 'Fehler beim Aktualisieren des Chat-Titels.';
    }
};

// Chat löschen
export const deleteChat = async (chatId) => {
    try {
        await axios.delete(`${API_BASE_URL}/${chatId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
        });
    } catch (error) {
        console.error(`Fehler beim Löschen des Chats ${chatId}:`, error);
        throw error.response?.data || 'Fehler beim Löschen des Chats.';
    }
};
const chatService = {
    getUserChats,
    getChatMessages,
    sendMessage,
    updateChatTitle,
    deleteChat,
};

export default chatService;