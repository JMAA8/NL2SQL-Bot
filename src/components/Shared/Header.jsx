import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

function Header() {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token'); // Entferne das Token aus dem lokalen Speicher
        navigate('/login'); // Weiterleitung zur Login-Seite
    };

    return (
        <header style={styles.header}>
            <div style={styles.container}>
                <h1 style={styles.logo}>
                    <Link to="/" style={styles.logoLink}>
                        Chatbot Dashboard
                    </Link>
                </h1>
                <nav style={styles.nav}>
                    <Link to="/profile" style={styles.link}>
                        Profil
                    </Link>
                    <Link to="/help" style={styles.link}>
                        Hilfe
                    </Link>
                    {token && ( // Nur anzeigen, wenn ein Token vorhanden ist
                        <button onClick={handleLogout} style={styles.logoutButton}>
                            Logout
                        </button>
                    )}
                </nav>
            </div>
        </header>
    );
}

const styles = {
    header: {
        backgroundColor: '#333',
        color: '#fff',
        padding: '15px 0',
        position: 'sticky',
        top: 0,
        zIndex: 1000,
        width: '100%',
    },
    container: {
        maxWidth: '1200px',
        margin: '0 auto',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
    },
    logo: {
        margin: 0,
        fontSize: '24px',
    },
    logoLink: {
        color: '#fff',
        textDecoration: 'none',
    },
    nav: {
        display: 'flex',
        alignItems: 'center',
        gap: '20px',
    },
    link: {
        color: '#fff',
        textDecoration: 'none',
        fontSize: '16px',
    },
    logoutButton: {
        backgroundColor: '#ff4d4d',
        color: '#fff',
        border: 'none',
        padding: '10px 15px',
        borderRadius: '5px',
        cursor: 'pointer',
        fontSize: '16px',
    },
};

export default Header;
