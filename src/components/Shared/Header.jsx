import React from 'react';
import {jwtDecode} from 'jwt-decode';
import { Link, useNavigate } from 'react-router-dom';

function Header() {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token'); // Token löschen
        localStorage.removeItem('role');  // Rolle löschen
        navigate('/login'); // Weiterleitung zur Login-Seite
    };

    const handleProfileClick = () => {

        //Role aus Token extrahieren
        const decoded = jwtDecode(token);
        const role = decoded.groups?.[0];
        console.log('Aktuelle Rolle: ', role)

        if (role === 'ADMIN') {
            navigate('/admin');
        } else if (role === 'ADVANCED_USER') {
            navigate('/advanced-user');
        } else if (role === 'BASIC_USER') {
            navigate('/basic-user');
        } else {
            alert('Ungültige Rolle oder nicht angemeldet.');
        }
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
                    {token && ( // Profil-Link nur anzeigen, wenn ein Token vorhanden ist
                        <button onClick={handleProfileClick} style={styles.linkButton}>
                            Profil
                        </button>
                    )}
                    <Link to="/help" style={styles.link}>
                        Hilfe
                    </Link>
                    {token && ( // Logout-Button nur anzeigen, wenn ein Token vorhanden ist
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
    linkButton: {
        background: 'none',
        border: 'none',
        color: '#fff',
        fontSize: '16px',
        cursor: 'pointer',
        textDecoration: 'underline',
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
