import React from 'react';
import { Link } from 'react-router-dom';

function Footer() {
    return (
        <footer className="footer">
            <div className="container">
                <p className="footer-text">
                    © {new Date().getFullYear()} Chatbot Fra Uas. Alle Rechte vorbehalten.
                </p>
                <nav className="footer-nav">
                    <Link to="/impressum" className="footer-link">
                        Impressum
                    </Link>
                    <Link to="/datenschutz" className="footer-link">
                        Datenschutz
                    </Link>
                    <Link to="/kontakt" className="footer-link">
                        Kontakt
                    </Link>
                </nav>
            </div>
        </footer>
    );
}

export default Footer;
