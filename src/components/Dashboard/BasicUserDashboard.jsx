import React, { useState, useEffect } from 'react';
import groupService from '../../services/groupService';

console.log('BS_Dashboard');
function BasicUserDashboard() {
    const [joinedGroups, setJoinedGroups] = useState([]);

    useEffect(() => {
        const fetchJoinedGroups = async () => {
            try {
                const groups = await groupService.getJoinedGroups();
                if (!Array.isArray(groups)) {
                    throw new Error('Unerwartete Antwortstruktur vom Server');
                }
                setJoinedGroups(groups);
            } catch (error) {
                console.error('Failed to fetch joined groups:', error);
                //alert('Fehler beim Abrufen der Gruppen.');
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
                {joinedGroups.length > 0 ? (
                    <ul>
                        {joinedGroups.map((group) => (
                            <li key={group.id}>{group.groupName}</li>
                        ))}
                    </ul>
                ) : (
                    <p>Du bist noch keiner Gruppe beigetreten.</p>
                )}
            </section>
        </div>
    );
}

export default BasicUserDashboard;
