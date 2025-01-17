import React from 'react';
import { Navigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode'; // Ohne geschweifte Klammern

function ProtectedRoute({ role, children }) {
    const token = localStorage.getItem('token');
    let userRole = null;

    if (token) {
        try {
            const decoded = jwtDecode(token);
            userRole = decoded.groups?.[0]; // Zugriff auf die erste Rolle aus dem `groups`-Array
        } catch (error) {
            console.error('Invalid token');
        }
    }

    if (!token || userRole !== role) {
        return <Navigate to="/login" />;
    }
    return children;
}

export default ProtectedRoute;
