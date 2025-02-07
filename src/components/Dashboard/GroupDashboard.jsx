import React, { useState, useEffect } from 'react';
import GroupService from '../services/GroupService';
import UserService from '../services/UserService';
import DocumentService from '../services/DocumentService';

const GroupDashboard = ({ groupId }) => {
    const [groupName, setGroupName] = useState('');
    const [groupUsers, setGroupUsers] = useState([]);
    const [allUsers, setAllUsers] = useState([]);
    const [userSearch, setUserSearch] = useState('');
    const [groupDocuments, setGroupDocuments] = useState([]);
    const [selectedFile, setSelectedFile] = useState(null);

    useEffect(() => {
        fetchGroupDetails();
        fetchGroupUsers();
        fetchGroupDocuments();
    }, []);

    const fetchGroupDetails = async () => {
        try {
            const group = await GroupService.getGroupById(groupId);
            setGroupName(group.name);
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppendetails:', error);
        }
    };

    const fetchGroupUsers = async () => {
        try {
            const users = await GroupService.getUsersByGroupId(groupId);
            setGroupUsers(users);
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppenmitglieder:', error);
        }
    };

    const fetchGroupDocuments = async () => {
        try {
            const documents = await DocumentService.getDocumentsByGroupId(groupId);
            setGroupDocuments(documents);
        } catch (error) {
            console.error('Fehler beim Abrufen der Dokumente:', error);
        }
    };

    const fetchAllUsers = async () => {
        try {
            const users = await UserService.getAllUsers();
            setAllUsers(users);
        } catch (error) {
            console.error('Fehler beim Abrufen aller Benutzer:', error);
        }
    };

    useEffect(() => {
        if (userSearch) {
            fetchAllUsers();
        }
    }, [userSearch]);

    const addUserToGroup = async (userId) => {
        try {
            await GroupService.addUserToGroup(groupId, userId);
            fetchGroupUsers();
        } catch (error) {
            console.error('Fehler beim Hinzufügen des Benutzers zur Gruppe:', error);
        }
    };

    const handleFileChange = (event) => {
        setSelectedFile(event.target.files[0]);
    };

    const uploadDocument = async () => {
        if (!selectedFile) return;
        try {
            await DocumentService.uploadDocument(groupId, selectedFile);
            fetchGroupDocuments();
            setSelectedFile(null);
        } catch (error) {
            console.error('Fehler beim Hochladen des Dokuments:', error);
        }
    };

    const deleteDocument = async (documentId) => {
        try {
            await DocumentService.deleteDocument(documentId);
            fetchGroupDocuments();
        } catch (error) {
            console.error('Fehler beim Löschen des Dokuments:', error);
        }
    };

    return (
        <div style={{ display: 'flex', gap: '20px' }}>
            <div style={{ flex: 1 }}>
                <h1>{groupName}</h1>
                <section>
                    <h2>Gruppenmitglieder</h2>
                    <ul>
                        {groupUsers.map((user) => (
                            <li key={user.id}>{user.username}</li>
                        ))}
                    </ul>
                    <input
                        type="text"
                        placeholder="Benutzer suchen (ID, Name, E-Mail)"
                        value={userSearch}
                        onChange={(e) => setUserSearch(e.target.value)}
                    />
                    {userSearch && (
                        <ul>
                            {allUsers
                                .filter((user) =>
                                    user.username.toLowerCase().includes(userSearch.toLowerCase()) ||
                                    user.id.toString().includes(userSearch) ||
                                    (user.email && user.email.toLowerCase().includes(userSearch.toLowerCase()))
                                )
                                .map((user) => (
                                    <li key={user.id}>
                                        {user.username}{' '}
                                        <button onClick={() => addUserToGroup(user.id)}>Hinzufügen</button>
                                    </li>
                                ))}
                        </ul>
                    )}
                </section>
            </div>
            <div style={{ flex: 1 }}>
                <section>
                    <h2>Gruppendokumente</h2>
                    <ul>
                        {groupDocuments.map((doc) => (
                            <li key={doc.id}>
                                <a href={doc.url} target="_blank" rel="noopener noreferrer">
                                    {doc.name}
                                </a>
                                <button onClick={() => deleteDocument(doc.id)}>Löschen</button>
                            </li>
                        ))}
                    </ul>
                    <input type="file" onChange={handleFileChange} />
                    <button onClick={uploadDocument}>Dokument hochladen</button>
                </section>
            </div>
        </div>
    );
};

export default GroupDashboard;
