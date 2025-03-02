import axios from 'axios';

const API_BASE_URL = "http://localhost:8080/nextcloud";

export const getLoginUrl = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/login-url`);
        console.log(response.data)
        return response.data;
    } catch (error) {
        console.error("Fehler beim Abrufen der Login-URL:", error);
        return null;
    }
};
export default {
    getLoginUrl,
};