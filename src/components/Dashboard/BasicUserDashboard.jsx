import React, { useState, useEffect } from 'react';
import userService from '../../services/userService';
import groupService from '../../services/groupService';
import documentService from '../../services/documentService';

function BasicUserDashboard() {
    const [userData, setUserData] = useState({
        id: '',
        username: '',
        password: '',
        email: 'Nicht verfügbar',
        role: 'Keine Rolle'
    });
    const [documents, setDocuments] = useState([]);
    const [documentSearch, setDocumentSearch] = useState('');
    const [newDocument, setNewDocument] = useState(null);

    const [groups, setGroups] = useState([]);
    const [groupSearch, setGroupSearch] = useState('');
    const [groupPassword, setGroupPassword] = useState('');
    const [selectedGroup, setSelectedGroup] = useState(null);

    useEffect(() => {
        fetchUserData();
        fetchUserDocuments();
        fetchJoinedGroups();
    }, []);

// Benutzerdaten abrufen
    const fetchUserData = async (userId) => {
        try {
            const user = await userService.getUserProfile(userId);
            console.log("User Service - getUserProfile - Response:", user);
            console.log("Role: ",user.roles);

            const userData = {
                id: user.id,
                username: user.username || "Unbekannt",
                password: user.password || "Nicht verfügbar",
                email: user.email || "Keine E-Mail hinterlegt",
                role: user.roles?.[0]?.roleName || "Keine Rolle"
            };

            setUserData(userData);
        } catch (error) {
            console.error('Fehler beim Abrufen der Benutzerdaten:', error);
        }
    };


    //Dokumente abrufen
    const fetchUserDocuments = async () => {
        try {
            const docs = await documentService.getUserDocuments();
            setDocuments(docs);
        } catch (error) {
            console.error('Fehler beim Abrufen der Dokumente:', error);
        }
    };

    //Gruppen abrufen
    const fetchJoinedGroups = async () => {
        try {
            const userGroups = await groupService.getJoinedGroups();
            console.log("userGroups: ", userGroups);

            if (!userGroups || userGroups.length === 0) {
                setGroups([{ id: "no-groups", groupName: "Noch keiner Gruppe beigetreten" }]);
            } else {
                setGroups(userGroups);
            }
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppen:', error);
        }
    };

    //Dokumentensuche
    const filteredDocuments = documents.filter(doc =>
        doc.name.toLowerCase().includes(documentSearch.toLowerCase())
    );

    //Datei-Upload
    const handleFileUpload = async () => {
        if (!newDocument) return;
        try {
            await documentService.uploadDocument(newDocument);
            setNewDocument(null);
            fetchUserDocuments();
        } catch (error) {
            console.error('Fehler beim Hochladen des Dokuments:', error);
        }
    };

    //Gruppen-Suche
    const handleGroupSearch = async () => {
        try {
            const searchResults = await groupService.searchGroups(groupSearch);
            setGroups(searchResults);
        } catch (error) {
            console.error('Fehler bei der Gruppensuche:', error);
        }
    };

    //Gruppe beitreten
    const handleJoinGroup = async () => {
        if (!selectedGroup || !groupPassword) return;
        try {
            await groupService.joinGroup(selectedGroup.id, groupPassword);
            fetchJoinedGroups();
            setGroupPassword('');
        } catch (error) {
            console.error('Fehler beim Beitritt zur Gruppe:', error);
        }
    };

    return (
        <div style={styles.container}>
            {/* Persönliche Daten */}
            <div style={styles.section}>
                <h2>Personal Data:</h2>
                <p><strong>Name:</strong> {userData.username}</p>
                <p><strong>Password:</strong> {userData.password}</p>
                <p><strong>Role:</strong> {userData.role}</p>
                <p><strong>E-Mail:</strong> {userData.email}</p>
            </div>

            {/* Dokumente */}
            <div style={styles.section}>
                <h2>Documents:</h2>
                <input
                    type="text"
                    placeholder="Search..."
                    value={documentSearch}
                    onChange={(e) => setDocumentSearch(e.target.value)}
                    style={styles.input}
                />
                <input
                    type="file"
                    onChange={(e) => setNewDocument(e.target.files[0])}
                    style={styles.uploadInput}
                />
                <button onClick={handleFileUpload} style={styles.button}>Upload +</button>

                <ul>
                    {filteredDocuments.map((doc) => (
                        <li key={doc.id}>{doc.name}</li>
                    ))}
                </ul>
            </div>

            {/* Gruppen */}
            <div style={styles.section}>
                <h2>Groups:</h2>
                <input
                    type="text"
                    placeholder="Search..."
                    value={groupSearch}
                    onChange={(e) => setGroupSearch(e.target.value)}
                    style={styles.input}
                />
                <button onClick={handleGroupSearch} style={styles.button}>🔍</button>
                <ul>
                    {groups.map((group) => (
                        <li key={group.id} onClick={() => setSelectedGroup(group)}>
                            {group.groupName}
                        </li>
                    ))}
                </ul>

                {/* Gruppenbeitritt */}
                {selectedGroup && (
                    <div style={styles.joinGroupContainer}>
                        <h3>Join {selectedGroup.groupName}</h3>
                        <input
                            type="password"
                            placeholder="Group Password"
                            value={groupPassword}
                            onChange={(e) => setGroupPassword(e.target.value)}
                            style={styles.input}
                        />
                        <button onClick={handleJoinGroup} style={styles.button}>Join</button>
                    </div>
                )}
            </div>
        </div>
    );
}

const styles = {
    container: {
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '20px',
        padding: '20px',
    },
    section: {
        border: '1px solid #ccc',
        padding: '15px',
        borderRadius: '8px',
    },
    input: {
        width: '100%',
        padding: '8px',
        marginBottom: '10px',
    },
    uploadInput: {
        marginRight: '10px',
    },
    button: {
        padding: '10px',
        backgroundColor: '#007bff',
        color: 'white',
        border: 'none',
        cursor: 'pointer',
        borderRadius: '5px',
    },
    joinGroupContainer: {
        marginTop: '10px',
    },
};

export default BasicUserDashboard;
