import React, { useState, useEffect } from 'react';
import userService from '../../services/userService';
import groupService from '../../services/groupService';
import documentService from '../../services/documentService';

function AdminDashboard() {
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
    const [newGroupName, setNewGroupName] = useState('');
    const [newGroupPassword, setNewGroupPassword] = useState('');

    const [users, setUsers] = useState([]);
    const [userSearch, setUserSearch] = useState('');
    const [newUser, setNewUser] = useState({
        username: '',
        password: '',
        email: '',
        role: 'BASIC_USER'
    });

    useEffect(() => {
        fetchUserData();
        fetchUserDocuments();
        fetchAllGroups();
        fetchAllUsers();
    }, []);

    // Benutzerdaten abrufen
    const fetchUserData = async () => {
        try {
            const user = await userService.getUserProfile();
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

    //Eigene Dokumente abrufen
    const fetchUserDocuments = async () => {
        try {
            const docs = await documentService.getUserDocuments();
            console.log("Docs: ", docs);

            if (!docs || docs.length === 0) {
                console.log("Keine Dokumente vorhanden");
                setDocuments([{ id: "no-documents", documentName: "Noch keine Documents hochgeladen" }]);
            } else {
                setDocuments(docs.map(doc => ({
                    id: doc.id || "unknown",
                    documentName: doc.documentName?.toString() || "Unbenanntes Dokument"
                })));
            }
        } catch (error) {
            console.error('Fehler beim Abrufen der Dokumente:', error);
        }
    };

    // Alle Gruppen abrufen
    const fetchAllGroups = async () => {
        try {
            const allGroups = await groupService.getAllGroups();
            setGroups(allGroups);
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppen:', error);
        }
    };

    // Alle Benutzer abrufen
    const fetchAllUsers = async () => {
        try {
            const allUsers = await userService.getAllUsers();
            setUsers(allUsers);
        } catch (error) {
            console.error('Fehler beim Abrufen der Benutzer:', error);
        }
    };

    // Neue Gruppe erstellen
    const handleCreateGroup = async () => {
        try {
            await groupService.createGroup(newGroupName, newGroupPassword);
            fetchAllGroups();
            setNewGroupName('');
            setNewGroupPassword('');
        } catch (error) {
            console.error('Fehler beim Erstellen der Gruppe:', error);
        }
    };

    // Neuen Benutzer erstellen
    const handleCreateUser = async () => {
        try {
            await userService.createUser(newUser);
            fetchAllUsers();
            setNewUser({ username: '', password: '', email: '', role: 'BASIC_USER' });
        } catch (error) {
            console.error('Fehler beim Erstellen des Benutzers:', error);
        }
    };

    // Dokument löschen
    const handleDeleteDocument = async (documentId) => {
        try {
            await documentService.deleteDocument(documentId);
            console.log("AdminDashboard - Document ist erfolgreich gelöscht")
            fetchUserDocuments();
        } catch (error) {
            console.error('Fehler beim Löschen des Dokuments:', error);
        }
    };

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

            {/* Eigene Dokumente */}
            <div style={styles.section}>
                <h2>Personal Documents:</h2>
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

                    {documents
                        .filter((doc) => doc.documentName.toLowerCase().includes(documentSearch.toLowerCase()))
                        .map((doc) => (
                            <li key={doc.id}>
                                {doc.documentName}

                                <button onClick={() => handleDeleteDocument(doc.id)} style={styles.deleteButton}>🗑️</button>
                            </li>
                        ))}
                </ul>
            </div>

            {/* Gruppenverwaltung */}
            <div style={styles.section}>
                <h2>Groups:</h2>
                <input
                    type="text"
                    placeholder="Search..."
                    value={groupSearch}
                    onChange={(e) => setGroupSearch(e.target.value)}
                    style={styles.input}
                />
                <ul>
                    {groups
                        .filter((group) => group.groupName.toLowerCase().includes(groupSearch.toLowerCase()))
                        .map((group) => (
                            <li key={group.id}>{group.groupName}</li>
                        ))}
                </ul>
                <input
                    type="text"
                    placeholder="New Group Name"
                    value={newGroupName}
                    onChange={(e) => setNewGroupName(e.target.value)}
                    style={styles.input}
                />
                <input
                    type="password"
                    placeholder="Group Password"
                    value={newGroupPassword}
                    onChange={(e) => setNewGroupPassword(e.target.value)}
                    style={styles.input}
                />
                <button onClick={handleCreateGroup} style={styles.button}>Create</button>
            </div>

            {/* Benutzerverwaltung */}
            <div style={styles.section}>
                <h2>Users:</h2>
                <input
                    type="text"
                    placeholder="Search..."
                    value={userSearch}
                    onChange={(e) => setUserSearch(e.target.value)}
                    style={styles.input}
                />
                <ul>
                    {users
                        .filter((user) =>
                            user.username.toLowerCase().includes(userSearch.toLowerCase()) ||
                            user.email.toLowerCase().includes(userSearch.toLowerCase())
                        )
                        .map((user) => (
                            <li key={user.id}>{user.username} ({user.email})</li>
                        ))}
                </ul>
                <input
                    type="text"
                    placeholder="Username"
                    value={newUser.username}
                    onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                    style={styles.input}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={newUser.password}
                    onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                    style={styles.input}
                />
                <input
                    type="email"
                    placeholder="Email"
                    value={newUser.email}
                    onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                    style={styles.input}
                />
                <button onClick={handleCreateUser} style={styles.button}>Create</button>
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
        width: '95%',
        padding: '8px',
        marginBottom: '10px',
    },
    button: {
        padding: '10px',
        backgroundColor: '#007bff',
        color: 'white',
        border: 'none',
        cursor: 'pointer',
        borderRadius: '5px',
    },
    deleteButton: {
        marginLeft: '10px',
        backgroundColor: '#ff4d4d',
        color: 'white',
        border: 'none',
        cursor: 'pointer',
        borderRadius: '5px',
    }
};

export default AdminDashboard;
