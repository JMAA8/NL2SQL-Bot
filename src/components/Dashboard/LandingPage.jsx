import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../../App.css'; // Nutzt die Styles aus deiner bestehenden App.css

const LandingPage = () => {
    const navigate = useNavigate();

    return (
        /*<div className="form-container">
        <h1>Willkommen beim Chatbot</h1>
        <p>Bitte wähle eine Option, um fortzufahren:</p>
        <div className="button-container">
            <button className="form-button" onClick={() => navigate('/login')}>
                Login
            </button>
            <button className="form-button" onClick={() => navigate('/register')}>
                Registrieren
            </button>
        </div>
    </div>

         */
        <div
            style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '26vh',
                backgroundColor: '#f5f5f5', // Heller Hintergrund
                padding: '20px',
                textAlign: 'center',
            }}
        >
            <h1 style={{ fontSize: '2.5rem', marginBottom: '10px', color: '#333' }}>
                Willkommen beim Chatbot
            </h1>
            <p style={{ fontSize: '1.2rem', marginBottom: '20px', color: '#666' }}>
                Bitte wähle eine Option, um fortzufahren:
            </p>
            <div
                style={{
                    display: 'flex',
                    gap: '15px', // Abstand zwischen den Buttons
                }}
            >
                <button
                    style={{
                        backgroundColor: '#007bff', // Blaue Hintergrundfarbe
                        color: 'white',
                        padding: '10px 20px',
                        fontSize: '1rem',
                        border: 'none',
                        borderRadius: '8px', // Abgerundete Ecken
                        cursor: 'pointer',
                        transition: 'background-color 0.3s ease, transform 0.2s ease',
                    }}
                    onMouseOver={(e) => (e.target.style.backgroundColor = '#0056b3')}
                    onMouseOut={(e) => (e.target.style.backgroundColor = '#007bff')}
                    onClick={() => navigate('/login')}
                >
                    Login
                </button>
                <button
                    style={{
                        backgroundColor: '#007bff',
                        color: 'white',
                        padding: '10px 20px',
                        fontSize: '1rem',
                        border: 'none',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        transition: 'background-color 0.3s ease, transform 0.2s ease',
                    }}
                    onMouseOver={(e) => (e.target.style.backgroundColor = '#0056b3')}
                    onMouseOut={(e) => (e.target.style.backgroundColor = '#007bff')}
                    onClick={() => navigate('/register')}
                >
                    Registrieren
                </button>
            </div>
        </div>
    );
};

export default LandingPage;
