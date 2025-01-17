import React, { useState, useEffect } from 'react';
import userService from '../../services/userService';

function AdminDashboard() {
    const [users, setUsers] = useState([]);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const users = await userService.getAllUsers();
                setUsers(users);
            } catch (error) {
                console.error('Failed to fetch users', error);
            }
        };
        fetchUsers();
    }, []);

    return (
        <div>
            <h1>Admin Dashboard</h1>
            <ul>
                {users.map((user) => (
                    <li key={user.id}>
                        {user.username} - {user.roles.join(', ')}
                        {/* Add buttons for updating roles or deleting users */}
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default AdminDashboard;
