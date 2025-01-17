import React, { useState, useEffect } from 'react';
import groupService from '../../services/groupService';

function BasicUserDashboard() {
    const [joinedGroups, setJoinedGroups] = useState([]);

    useEffect(() => {
        const fetchJoinedGroups = async () => {
            try {
                const groups = await groupService.getJoinedGroups();
                setJoinedGroups(groups);
            } catch (error) {
                console.error('Failed to fetch joined groups', error);
            }
        };
        fetchJoinedGroups();
    }, []);

    return (
        <div>
            <h1>Basic User Dashboard</h1>

            {/* Section to view joined groups */}
            <section>
                <h2>My Groups</h2>
                <ul>
                    {joinedGroups.map((group) => (
                        <li key={group.id}>{group.groupName}</li>
                    ))}
                </ul>
            </section>
        </div>
    );
}

export default BasicUserDashboard;
