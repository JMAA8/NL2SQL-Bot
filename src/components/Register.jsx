import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

function Register() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setErrorMessage('Die Passwörter stimmen nicht überein.');
            return;
        }

        try {
            await authService.register(username, password);
            alert('Registrierung erfolgreich! Bitte melden Sie sich an.');
            navigate('/login');
        } catch (error) {
            console.error('Registrierung fehlgeschlagen:', error);
            setErrorMessage('Ein Fehler ist aufgetreten.');
        }
    };

    return (
        <div className="form-container">
            <h1>Registrieren</h1>
            {errorMessage && <p className="form-error">{errorMessage}</p>}
            <form onSubmit={handleRegister} className="form">
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
                <input
                    type="password"
                    placeholder="Passwort bestätigen"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="form-input"
                    required
                />
                <button type="submit" className="form-button">
                    Registrieren
                </button>
            </form>
        </div>
    );
}

export default Register;
