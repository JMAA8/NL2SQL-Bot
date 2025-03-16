import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import userService from '../../services/userService';
import groupService from '../../services/groupService';
import documentService from '../../services/documentService';
import adminService from '../../services/adminService';
import GroupDashboard from '../Dashboard/GroupDashboard';

function AdminDashboard() {
    const navigate = useNavigate();
    const [selectedGroupId, setSelectedGroupId] = useState(null);

    const [userData, setUserData] = useState({
        id: '',
        username: '',
        password: '',
        email: 'Nicht verfügbar',
        role: 'Keine Rolle'
    });

    const [selectedUserId, setSelectedUserId] = useState(null);
    const [selectedRole, setSelectedRole] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);

    const roles = ['ADMIN', 'BASIC_USER', 'ADVANCED_USER'];

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
    });

    useEffect(() => {
        fetchUserData();
        fetchUserDocuments();
        fetchAllGroups();
        fetchAllUsers();
    }, []);

    const fetchUserData = async () => {
        try {
            const user = await userService.getUserProfile();
            setUserData({
                id: user.id,
                username: user.username || "Unbekannt",
                password: user.password || "Nicht verfügbar",
                email: user.email || "Keine E-Mail hinterlegt",
                role: user.roles?.[0]?.roleName || "Keine Rolle"
            });
        } catch (error) {
            console.error('Fehler beim Abrufen der Benutzerdaten:', error);
        }
    };

    const fetchUserDocuments = async () => {
        try {
            const docs = await documentService.getUserDocuments();
            setDocuments(docs.length > 0 ? docs : [{ id: "no-documents", documentName: "Noch keine Dokumente hochgeladen" }]);
        } catch (error) {
            console.error('Fehler beim Abrufen der Dokumente:', error);
        }
    };

    const fetchAllGroups = async () => {
        try {
            const allGroups = await groupService.getAllGroups();
            setGroups(allGroups);
        } catch (error) {
            console.error('Fehler beim Abrufen der Gruppen:', error);
        }
    };

    const fetchAllUsers = async () => {
        try {
            const allUsers = await adminService.getAllUsers();
            setUsers(allUsers);
        } catch (error) {
            console.error('Fehler beim Abrufen der Benutzer:', error);
        }
    };

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

    const handleCreateUser = async () => {
        try {
            await adminService.createUser(newUser.username, newUser.password);
            fetchAllUsers();
            setNewUser({ username: '', password: '' });
        } catch (error) {
            console.error('Fehler beim Erstellen des Benutzers:', error);
        }
    };

    const handleDeleteDocument = async (documentId) => {
        try {
            await documentService.deleteDocument(documentId);
            fetchUserDocuments();
        } catch (error) {
            console.error('Fehler beim Löschen des Dokuments:', error);
        }
    };

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

    const openModal = (userId) => {
        setSelectedUserId(userId);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setSelectedUserId(null);
        setSelectedRole('');
    };

    const handleRoleChange = async () => {
        if (selectedUserId && selectedRole) {
            await userService.changeRole(selectedUserId, selectedRole);
            closeModal();
        }
    };
    const openGroupDashboard = (groupId) => {
        setSelectedGroupId(groupId);
    };

    const [hoveredGroup, setHoveredGroup] = useState(null);

    return (
        <div style={styles.container}>
            {selectedGroupId ? (
                <>
                    <button onClick={() => setSelectedGroupId(null)} style={styles.backbutton}>
                        ⬅ Zurück zur Gruppenübersicht
                    </button>
                    <GroupDashboard groupId={selectedGroupId} />
                </>
            ) : (
                <>
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
                        <input type="text" placeholder="Search..." value={documentSearch} onChange={(e) => setDocumentSearch(e.target.value)} style={styles.input} />
                        <input type="file" onChange={(e) => setNewDocument(e.target.files[0])} style={styles.uploadInput} />
                        <button onClick={handleFileUpload} style={styles.button}>Upload +</button>
                        <ul>
                            {documents.map((doc) => (
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
                        <input type="text" placeholder="Search..." value={groupSearch} onChange={(e) => setGroupSearch(e.target.value)} style={styles.input} />
                        <ul>
                            {groups.map((group) => (
                                <li key={group.id}>
                                    <button style={styles.groupButton(hoveredGroup === group.id)}
                                            onClick={() => openGroupDashboard(group.id)}
                                            onMouseEnter={() => setHoveredGroup(group.id)}
                                            onMouseLeave={() => setHoveredGroup(null)}>
                                        {group.groupName}
                                    </button>
                                </li>
                            ))}
                        </ul>
                        <input type="text" placeholder="New Group Name" value={newGroupName} onChange={(e) => setNewGroupName(e.target.value)} style={styles.input} />
                        <input type="password" placeholder="Group Password" value={newGroupPassword} onChange={(e) => setNewGroupPassword(e.target.value)} style={styles.input} />
                        <button onClick={handleCreateGroup} style={styles.button}>Create</button>
                    </div>

                    {/* Benutzerverwaltung */}
                    <div style={styles.section}>
                        <h2>Users:</h2>
                        <input type="text" placeholder="Search..." value={userSearch} onChange={(e) => setUserSearch(e.target.value)} style={styles.input} />
                        <ul>
                            {users.map((user) => (
                                <li key={user.id}>
                                    {user.username}
                                    <button onClick={() => openModal(user.id)} style={styles.button}>
                                        Rolle ändern
                                    </button>
                                </li>
                            ))}
                        </ul>
                        <input type="text" placeholder="Username" value={newUser.username} onChange={(e) => setNewUser({ ...newUser, username: e.target.value })} style={styles.input} />
                        <input type="password" placeholder="Password" value={newUser.password} onChange={(e) => setNewUser({ ...newUser, password: e.target.value })} style={styles.input} />
                        <button onClick={handleCreateUser} style={styles.button}>Create</button>
                    </div>

                    {/* Modal-Fenster für die Rollenänderung */}
                    {isModalOpen && (
                        <div style={styles.modal}>
                            <h3>Neue Rolle auswählen</h3>
                            <select value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)} style={styles.select}>
                                <option value="">Rolle wählen...</option>
                                {roles.map((role) => (
                                    <option key={role} value={role}>
                                        {role}
                                    </option>
                                ))}
                            </select>
                            <div style={styles.buttonContainer}>
                                <button onClick={handleRoleChange} disabled={!selectedRole} style={styles.saveButton}>
                                    Speichern
                                </button>
                                <button onClick={closeModal} style={styles.cancelButton}>
                                    Abbrechen
                                </button>
                            </div>
                        </div>
                    )}

                </>
            )}
        </div>
    );
}

const styles = {
    container: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', padding: '20px' },
    section: { border: '1px solid #ccc', padding: '15px', borderRadius: '8px' },
    input: { width: '95%', padding: '8px', marginBottom: '10px' },
    button: { padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', cursor: 'pointer', borderRadius: '5px' },
    deleteButton: { marginLeft: '10px', backgroundColor: '#ff4d4d', color: 'white', border: 'none', cursor: 'pointer', borderRadius: '5px' },
    backbutton: { position: 'absolute', top: '100px',width: '200px',height: '50px',backgroundColor: '#007bff',color: 'white',border: 'none',borderRadius: '5px',cursor: 'pointer'},
    groupButton: (isHovered) => ({ padding: '10px', backgroundColor: isHovered ? '#007bff' : '#ffffff',  color: isHovered ? 'white' : 'black', border: '1px solid #ddd', cursor: 'pointer', borderRadius: '5px', width: '100%', textAlign: 'left' }),
    modal: {
        position: 'fixed',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        backgroundColor: '#fff',
        padding: '20px',
        borderRadius: '8px',
        boxShadow: '0px 4px 6px rgba(0,0,0,0.1)',
        zIndex: 1000
    },
    select: { width: '100%', padding: '8px', marginBottom: '10px' },
    buttonContainer: { marginTop: '10px', display: 'flex', justifyContent: 'space-between' },
    saveButton: { backgroundColor: '#4CAF50', color: 'white', padding: '5px 10px', border: 'none', cursor: 'pointer' },
    cancelButton: { backgroundColor: '#f44336', color: 'white', padding: '5px 10px', border: 'none', cursor: 'pointer' },
};

export default AdminDashboard;
