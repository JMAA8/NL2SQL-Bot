import React, { useState, useEffect } from 'react';
import userService from '../../services/userService';
import groupService from '../../services/groupService';
import documentService from '../../services/documentService';

function AdvancedUserDashboard() {
    const [userData, setUserData] = useState({
        id: '',
        username: '',
        password: '',
        email: 'Nicht verfügbar',
        role: 'Keine Rolle'
    });

    const [groups, setGroups] = useState([]);
    const [groupSearch, setGroupSearch] = useState('');

    const [documents, setDocuments] = useState([]);
    const [documentSearch, setDocumentSearch] = useState('');
    const [newDocument, setNewDocument] = useState(null);

    // State für das Popup-Fenster zur Gruppenerstellung
    const [showCreateGroupPopup, setShowCreateGroupPopup] = useState(false);
    const [newGroupName, setNewGroupName] = useState('');
    const [newGroupPassword, setNewGroupPassword] = useState('');

    useEffect(() => {
        fetchUserData();
        fetchUserGroups();
        fetchUserDocuments();
    }, []);

    // Benutzerdaten abrufen
    const fetchUserData = async () => {
        try {
            const user = await userService.getUserProfile();
            console.log("User Service - getUserProfile - Response:", user);

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

    // Gruppen abrufen
    const fetchUserGroups = async () => {
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

    // Gruppen-Suche
    const handleGroupSearch = async () => {
        try {
            const searchResults = await groupService.searchGroups(groupSearch);
            setGroups(searchResults);
        } catch (error) {
            console.error('Fehler bei der Gruppensuche:', error);
        }
    };

    // Neue Gruppe erstellen
    const handleCreateGroup = async () => {
        if (!newGroupName || !newGroupPassword) {
            alert("Bitte Gruppenname und Passwort eingeben!");
            return;
        }

        try {
            await groupService.createGroup({
                groupName: newGroupName,
                password: newGroupPassword
            });

            alert("Gruppe erfolgreich erstellt!");
            setShowCreateGroupPopup(false);
            setNewGroupName('');
            setNewGroupPassword('');
            fetchUserGroups();
        } catch (error) {
            console.error('Fehler beim Erstellen der Gruppe:', error);
            alert("Fehler beim Erstellen der Gruppe.");
        }
    };

    // Dokumente abrufen
    const fetchUserDocuments = async () => {
        try {
            const docs = await documentService.getUserDocuments();
            setDocuments(docs);
        } catch (error) {
            console.error('Fehler beim Abrufen der Dokumente:', error);
        }
    };

    // Dokumentensuche
    const filteredDocuments = documents.filter(doc =>
        doc.name.toLowerCase().includes(documentSearch.toLowerCase())
    );

    // Datei-Upload
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
                <button onClick={() => setShowCreateGroupPopup(true)} style={styles.createButton}>+ Neue Gruppe</button>

                <ul>
                    {groups.map((group) => (
                        <li key={group.id}>
                            {group.groupName}
                        </li>
                    ))}
                </ul>
            </div>

            {/* PopUp-Fenster zur Gruppenerstellung */}
            {showCreateGroupPopup && (
                <div style={styles.popupOverlay}>
                    <div style={styles.popup}>
                        <h2>Neue Gruppe erstellen</h2>
                        <input
                            type="text"
                            placeholder="Gruppenname"
                            value={newGroupName}
                            onChange={(e) => setNewGroupName(e.target.value)}
                            style={styles.input}
                        />
                        <input
                            type="password"
                            placeholder="Passwort"
                            value={newGroupPassword}
                            onChange={(e) => setNewGroupPassword(e.target.value)}
                            style={styles.input}
                        />
                        <div style={styles.popupButtons}>
                            <button onClick={handleCreateGroup} style={styles.button}>Erstellen</button>
                            <button onClick={() => setShowCreateGroupPopup(false)} style={styles.cancelButton}>Abbrechen</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

const styles = {
    // Gleiche Styles wie in BasicUserDashboard
};

export default AdvancedUserDashboard;
