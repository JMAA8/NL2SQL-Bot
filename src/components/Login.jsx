import { jwtDecode } from 'jwt-decode';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import authService from '../services/authService';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        console.log('Login.jsx, handleLogin anfang');
        e.preventDefault();
        try {
            console.log('Login.jsx, handleLogin try');
            const response = await authService.login(username, password);
            console.log('Login.js: Erhaltenes Token:', response.token);
            const decoded = jwtDecode(response.token);
            const userRole = decoded.groups?.[0]; // Zugriff auf groups statt roles
            if (userRole === 'ADMIN') navigate('/admin');
            else if (userRole === 'ADVANCED_USER') navigate('/advanced-user');
            else if (userRole === 'BASIC_USER') navigate('/basic-user');
            else throw new Error('Unbekannte Rolle');
        } catch (error) {
            console.log('Login.jsx, handleLogin error');
            setErrorMessage(error.response?.data || 'Login fehlgeschlagen.');
        }
    };

    return (
        <form onSubmit={handleLogin}>
            <input
                type="text"
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />
            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            {errorMessage && <p>{errorMessage}</p>}
            <button type="submit">Login</button>
        </form>
    );
};

export default Login;
