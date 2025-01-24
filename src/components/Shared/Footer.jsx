import React from 'react';
import { Link } from 'react-router-dom';

function Footer() {
    return (
        <div style={styles.container}>
            <footer style={styles.footer}>
                <div style={styles.footerContent}>
                    <p style={styles.footerText}>
                        © {new Date().getFullYear()} Chatbot Dashboard. Alle Rechte vorbehalten.
                    </p>
                    <nav style={styles.footerNav}>
                        <Link to="/impressum" style={styles.footerLink}>
                            Impressum
                        </Link>
                        <Link to="/datenschutz" style={styles.footerLink}>
                            Datenschutz
                        </Link>
                        <Link to="/kontakt" style={styles.footerLink}>
                            Kontakt
                        </Link>
                    </nav>
                </div>
            </footer>
        </div>
    );
}
const styles = {
    container: {
        display: 'flex',
        bottom: 0,
        flexDirection: 'column',
        height: '62vh', // Höhe des Viewports
        margin: 0, // Entfernt Standard-Abstand
    },
    footer: {
        backgroundColor: '#333',
        color: 'white',
        textAlign: 'center',
        padding: '20px',
        position: 'relative',
        width: '100%',
        marginTop: 'auto', // Sorgt dafür, dass der Footer am unteren Rand bleibt
    },
    footerContent: {
        maxWidth: '1200px',
        margin: '0 auto', // Zentriert den Inhalt
    },
    footerText: {
        margin: 0,
    },
    footerNav: {
        marginTop: '10px',
    },
    footerLink: {
        color: 'white',
        textDecoration: 'none',
        margin: '0 10px',
    },
};

export default Footer;
