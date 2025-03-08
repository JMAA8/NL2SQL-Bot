import React, { useState, useEffect } from 'react';
import GroupService from '../../services/groupService';
import DocumentService from '../../services/documentService';
import documentService from "../../services/documentService";

const GroupDashboard = ({ groupId }) => {

    const [groupData, setGroupData] = useState({
        Groupname: '',
        password: '',
        owner: 'Nicht verfügbar'
    });
    const [groupUsers, setGroupUsers] = useState([]);
    const [groupDocuments, setGroupDocuments] = useState([]);
    const [documentSearch, setDocumentSearch] = useState('');
    const [newDocument, setNewDocument] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [isOwner, setIsOwner] = useState(false);
    console.log('GroupDashboard Id: ', groupId);

    useEffect(() => {
        fetchGroupUsers();
        fetchGroupDocuments();
        fetchGroupDetails()
        //fetchCurrentUser();
    }, []);

    /*
    const fetchCurrentUser = async () => {
        const token = sessionStorage.getItem('token');
        if (!token) return;
        const decoded = jwtDecode(token);
        console.log('Decoded Token: ', decoded);

        try {
            if (decoded.groups?.includes("ADMIN")){
                setIsOwner(true);
            } else {
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

     */

    //Groupdetails

    const fetchGroupDetails = async () => {
        try {
            const group = await GroupService.getGroupById(groupId);
            console.log('Group: ', group);

            const groupData = {
                Groupname: group.name,
                password: group.password,
                owner: group.owner
            };

            setGroupData(group);

        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppendetails:', error);
        }
    };


    //Group-User-Management
    const fetchGroupUsers = async () => {
        try {
            const response = await GroupService.getUsersByGroupId(groupId);
            setGroupUsers(response.data || []); // Fallback für leere oder fehlerhafte Antworten
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppenmitglieder:', error);
            setGroupUsers([]);
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


    //Group-Document-Management

    //Dokumente abrufen
    const fetchGroupDocuments = async () => {
        try {
            const docs = await documentService.getDocumentsByGroupId(groupId);
            console.log("Docs: ", docs);

            if (!docs || docs.length === 0) {
                console.log("Keine Dokumente vorhanden");
                setGroupDocuments([{ id: "no-documents", name: "Noch keine Documents hochgeladen" }]);
            } else {
                setGroupDocuments(docs.map(doc => ({
                    id: doc.id || "unknown",
                    name: doc.documentName?.toString() || "Unbenanntes Dokument"
                })));
            }
        } catch (error) {
            console.error('Fehler beim Abrufen der Dokumente:', error);
        }
    };


    //Datei-Upload
    const uploadDocument = async () => {
        if (!newDocument) return;
        try {
            await documentService.uploadDocumentGroup(newDocument, groupId);
            setNewDocument(null);
            fetchGroupDocuments();
        } catch (error) {
            console.error('Fehler beim Hochladen des Dokuments:', error);
        }
    };

    //Datei-Löschen
    const deleteDocument = async (documentId) => {
        try {
            await DocumentService.deleteDocument(documentId);
            fetchGroupDocuments();
        } catch (error) {
            console.error('Fehler beim Löschen des Dokuments:', error);
        }
    };


    //Dokumentensuche
    const filteredDocuments = groupDocuments.filter(doc =>
        doc.name.toLowerCase().includes(documentSearch.toLowerCase())
    );









    return (
        <div style={{ display: 'flex', gap: '20px' }}>
            <div style={{ flex: 1 }}>
                <h1>{groupData.Groupname}</h1>
                <section>
                    <h2>Gruppendetails</h2>
                    <p><strong>Name:</strong> {groupData.Groupname}</p>
                    <p><strong>Password:</strong> {groupData.password}</p>
                </section>
            </div>
            <div style={{ flex: 1 }}>
                <section>
                    <h2>Gruppenmitglieder</h2>
                    <ul>
                        {groupUsers.length > 0 ? (
                            groupUsers.map((user) => (
                                <li key={user.id}>{user.username} {isOwner &&
                                    <button onClick={() => handleRemoveUser(user.id)}>Entfernen</button>}
                                </li>
                            ))
                        ) : (
                            <li>Keine Mitglieder gefunden</li>
                        )}
                    </ul>
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
                    <input type="file" onChange={(e) => setNewDocument(e.target.files[0])} />
                    <button onClick={uploadDocument}>Dokument hochladen</button>
                </section>
            </div>
        </div>
    );
};

export default GroupDashboard;