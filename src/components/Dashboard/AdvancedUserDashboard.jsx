import React, { useState, useEffect } from 'react';
import groupService from '../../services/groupService';
import userService from '../../services/userService';

function AdvancedUserDashboard() {
    const [groups, setGroups] = useState([]);
    const [groupName, setGroupName] = useState('');
    const [selectedGroupId, setSelectedGroupId] = useState(null);
    const [users, setUsers] = useState([]);
    const [selectedUserId, setSelectedUserId] = useState('');

    // Fetch all groups
    useEffect(() => {
        const fetchGroups = async () => {
            try {
                const groups = await groupService.getAllGroups();
                setGroups(groups);
            } catch (error) {
                console.error('Failed to fetch groups', error);
            }
        };
        fetchGroups();
    }, []);

    // Fetch all users
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

    // Create a new group
    const createGroup = async () => {
        try {
            const response = await groupService.createGroup(groupName);
            alert(response);
            setGroupName('');
            setGroups((prevGroups) => [...prevGroups, { id: response.id, groupName }]);
        } catch (error) {
            console.error('Failed to create group', error);
            alert(error);
        }
    };

    // Add a user to a group
    const addUserToGroup = async () => {
        if (!selectedGroupId || !selectedUserId) {
            alert('Please select a group and a user');
            return;
        }

        try {
            const response = await groupService.addUserToGroup(selectedGroupId, selectedUserId);
            alert(response);
        } catch (error) {
            console.error('Failed to add user to group', error);
            alert(error);
        }
    };

    // Delete a group
    const deleteGroup = async (groupId) => {
        try {
            const response = await groupService.deleteGroup(groupId);
            alert(response);
            setGroups((prevGroups) => prevGroups.filter((group) => group.id !== groupId));
        } catch (error) {
            console.error('Failed to delete group', error);
            alert(error);
        }
    };

    return (
        <div>
            <h1>Advanced User Dashboard</h1>

            {/* Section to create a new group */}
            <section>
                <h2>Create Group</h2>
                <input
                    type="text"
                    placeholder="Group Name"
                    value={groupName}
                    onChange={(e) => setGroupName(e.target.value)}
                />
                <button onClick={createGroup}>Create Group</button>
            </section>

            {/* Section to manage groups */}
            <section>
                <h2>Manage Groups</h2>
                <ul>
                    {groups.map((group) => (
                        <li key={group.id}>
                            {group.groupName}
                            <button onClick={() => deleteGroup(group.id)}>Delete</button>
                        </li>
                    ))}
                </ul>
            </section>

            {/* Section to add users to a group */}
            <section>
                <h2>Add User to Group</h2>
                <select
                    value={selectedGroupId}
                    onChange={(e) => setSelectedGroupId(e.target.value)}
                >
                    <option value="">Select Group</option>
                    {groups.map((group) => (
                        <option key={group.id} value={group.id}>
                            {group.groupName}
                        </option>
                    ))}
                </select>

                <select
                    value={selectedUserId}
                    onChange={(e) => setSelectedUserId(e.target.value)}
                >
                    <option value="">Select User</option>
                    {users.map((user) => (
                        <option key={user.id} value={user.id}>
                            {user.username}
                        </option>
                    ))}
                </select>

                <button onClick={addUserToGroup}>Add User</button>
            </section>
        </div>
    );
}

export default AdvancedUserDashboard;
