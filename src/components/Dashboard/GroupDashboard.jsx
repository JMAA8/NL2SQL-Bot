import React, { useState, useEffect } from 'react';
import {jwtDecode} from "jwt-decode";
import GroupService from '../../services/groupService';
import UserService from '../../services/userService';
import DocumentService from '../../services/documentService';

const GroupDashboard = ({ groupId }) => {
    const [groupName, setGroupName] = useState('');
    const [groupUsers, setGroupUsers] = useState([]);
    const [groupDocuments, setGroupDocuments] = useState([]);
    const [selectedFile, setSelectedFile] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [isOwner, setIsOwner] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        fetchGroupDetails();
        fetchGroupUsers();
        fetchGroupDocuments();
        fetchCurrentUser();
    }, []);

    const fetchCurrentUser = async () => {
        const token = sessionStorage.getItem('token');
        const decoded = jwtDecode(token);
        console.log('Decoded Token: ', decoded);
        const groups = decoded.groups || [];
        console.log('Groups: ', groups);
        if (!token) return;

        try {
            if (groups.includes("ADMIN")){
                setIsOwner(true);
                console.log('Current User ist ADMIN');
            }else{
                checkIfOwner(decoded.userId);
            }
        } catch (error) {
            console.error('Fehler beim Abrufen der Benutzerinformationen:', error);
        }
    };

    const checkIfOwner = async (userId) => {
        try {
            const response = await GroupService.checkIfUserIsOwner(groupId, userId);
            setIsOwner(response.data.isOwner);
        } catch (error) {
            console.error('Fehler beim Überprüfen des Owner-Status:', error);
        }
    };

    const fetchGroupDetails = async () => {
        try {
            const response = await GroupService.getGroupById(groupId);
            setGroupName(response.data.name);
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppendetails:', error);
        }
    };

    const fetchGroupUsers = async () => {
        try {
            const response = await GroupService.getUsersByGroupId(groupId);
            setGroupUsers(response.data);
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppenmitglieder:', error);
        }
    };

    const handleSearch = async () => {
        try {
            const response = await UserService.searchUsers(searchQuery);
            setSearchResults(response.data);
        } catch (error) {
            console.error('Fehler bei der Benutzersuche:', error);
        }
    };

    const handleAddUser = async (userId) => {
        try {
            await GroupService.addUserToGroup(groupId, userId);
            fetchGroupUsers();
        } catch (error) {
            console.error('Fehler beim Hinzufügen des Benutzers zur Gruppe:', error);
        }
    };

    const handleRemoveUser = async (userId) => {
        try {
            await GroupService.removeUserFromGroup(groupId, userId);
            fetchGroupUsers();
        } catch (error) {
            console.error('Fehler beim Entfernen des Benutzers aus der Gruppe:', error);
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
                            <li key={user.id}>{user.username} {isOwner && <button onClick={() => handleRemoveUser(user.id)}>Entfernen</button>}</li>
                        ))}
                    </ul>
                    {isOwner && (
                        <div>
                            <h2>Benutzer hinzufügen</h2>
                            <input
                                type="text"
                                placeholder="ID, Name oder E-Mail"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                            <button onClick={handleSearch}>Suchen</button>
                            <ul>
                                {searchResults.map((user) => (
                                    <li key={user.id}>
                                        {user.username} ({user.email})
                                        <button onClick={() => handleAddUser(user.id)}>Hinzufügen</button>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </section>
            </div>
            <div style={{ flex: 1 }}>
                <section>
                    <h2>Gruppendokumente</h2>
                    <ul>
                        {groupDocuments.map((doc) => (
                            <li key={doc.id}>
                                <a href={doc.url} target="_blank" rel="noopener noreferrer">{doc.name}</a>
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
