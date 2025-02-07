import React from 'react';
import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

function ProtectedRoute({ role, children }) {
    const token = localStorage.getItem('token');
    let userRole = null;

    if (token) {
        try {
            const decoded = jwtDecode(token);
            console.log('ProtectedRoute: Decoded Token:', decoded);
            userRole = decoded.groups?.[0]; // Zugriff auf die erste Rolle aus dem `groups`-Array
            console.log('ProtectedRoute: UserRole:', userRole);
        } catch (error) {
            console.error('Invalid token');
        }
    }

    console.log('ProtectedRoute: Token:', token);
    console.log('ProtectedRoute: UserRole:', userRole);
    console.log('ProtectedRoute: Erwartete Rolle:', role);

    if (!token) {
        console.log('ProtectedRoute: Zugriff verweigert (Kein Token)');
        return <Navigate to="/login" />;
    }

    // Anpassung: Unterstützt jetzt mehrere Rollen
    if (role) {
        if (Array.isArray(role)) {
            if (!role.includes(userRole)) {
                console.log('ProtectedRoute: Zugriff verweigert (Nicht in erlaubten Rollen)');
                return <Navigate to="/login" />;
            }
        } else {
            if (userRole !== role) {
                console.log('ProtectedRoute: Zugriff verweigert (Keine Rolle)');
                return <Navigate to="/login" />;
            }
        }
    }

    return children;
}

export default ProtectedRoute;
