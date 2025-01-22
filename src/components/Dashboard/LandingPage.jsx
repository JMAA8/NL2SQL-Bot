import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../../App.css'; // Nutzt die Styles aus deiner bestehenden App.css

const LandingPage = () => {
    const navigate = useNavigate();

    return (
        <div className="container">
            <h1>Willkommen beim Chatbot</h1>
            <p>Bitte wähle eine Option, um fortzufahren:</p>
            <div className="button-container">
                <button className="btn" onClick={() => navigate('/login')}>
                    Login
                </button>
                <button className="btn" onClick={() => navigate('/register')}>
                    Registrieren
                </button>
            </div>
        </div>
    );
};

export default LandingPage;
