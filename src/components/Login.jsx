import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {jwtDecode} from 'jwt-decode';
import authService from '../services/authService';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        console.log('Login.jsx, handleLogin gestartet');

        try {
            // Login beim Backend
            const response = await authService.login(username, password);
            console.log('Login.jsx: Erhaltenes Token:', response.token);

            // Token dekodieren
            const decoded = jwtDecode(response.token);
            const userId = decoded.userId; // Benutzer-ID aus dem Token
            const userRole = decoded.groups?.[0]; // Erste Rolle aus dem `groups`-Array

            console.log('Benutzer-ID:', userId);
            console.log('Benutzer-Rolle:', userRole);

            // Token im sessionStorage speichern
            sessionStorage.setItem('token', response.token);

            // Weiterleitung zur Chat-Seite
            navigate('/chat');
        } catch (error) {
            console.error('Login.jsx, handleLogin Fehler:', error);
            setErrorMessage(error.response?.data || 'Login fehlgeschlagen.');
        }
    };

    return (
        <div className="form-container">
            <h1>Login</h1>
            {errorMessage && <p className="form-error">{errorMessage}</p>}
            <form onSubmit={handleLogin} className="form">
                <input
                    type="text"
                    placeholder="Benutzername"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="form-input"
                    required
                />
                <input
                    type="password"
                    placeholder="Passwort"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="form-input"
                    required
                />
                <button type="submit" className="form-button">
                    Login
                </button>
            </form>
        </div>
    );
};

export default Login;
