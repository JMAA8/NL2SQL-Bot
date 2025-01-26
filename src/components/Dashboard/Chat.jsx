import React, { useState, useEffect } from 'react';
import chatService from '../../services/chatService';

function Chat() {
    const [chats, setChats] = useState([]); // Liste aller Chats
    const [messages, setMessages] = useState([]); // Nachrichten im aktuellen Chat
    const [currentChatId, setCurrentChatId] = useState(null); // Aktiver Chat
    const [currentMessage, setCurrentMessage] = useState(''); // Nachricht im Eingabefeld
    const [editingTitle, setEditingTitle] = useState(null); // Bearbeiteter Chat-Titel
    const [newTitle, setNewTitle] = useState(''); // Neuer Titel

    useEffect(() => {
        const fetchUserChats = async () => {
            try {
                const data = await chatService.getUserChats();
                setChats(data);
            } catch (error) {
                alert('Fehler beim Abrufen der Chats: ' + error);
                console.error('Fehler beim Abrufen der Benutzer-Chats:', error);
            }
        };

        fetchUserChats();
    }, []);

    const handleChatSelect = async (chatId) => {
        try {
            setCurrentChatId(chatId);
            const data = await chatService.getChatMessages(chatId);
            setMessages(data);
        } catch (error) {
            alert('Fehler beim Laden der Chat-Nachrichten: ' + error);
            console.error('Fehler beim Laden der Chat-Nachrichten:', error);
        }
    };

    const handleNewChat = () => {
        setCurrentChatId(null);
        setMessages([]);
    };

    const handleSendMessage = async () => {
        if (!currentMessage.trim()) {
            alert('Nachricht darf nicht leer sein!');
            return;
        }

        try {
            const response = await chatService.sendMessage(currentChatId, currentMessage);
            setMessages((prev) => [...prev, response]);
            setCurrentMessage('');

            if (!currentChatId) {
                setCurrentChatId(response.chatId);
                setChats((prev) => [...prev, { id: response.chatId, name: `Chat ${response.chatId}` }]);
            }
        } catch (error) {
            alert('Fehler beim Senden der Nachricht: ' + error);
            console.error('Fehler beim Senden der Nachricht:', error);
        }
    };

    const handleEditTitle = (chatId, title) => {
        setEditingTitle(chatId);
        setNewTitle(title);
    };

    const handleSaveTitle = async () => {
        try {
            await chatService.updateChatTitle(editingTitle, newTitle);
            setChats((prev) => prev.map((chat) => (chat.id === editingTitle ? { ...chat, name: newTitle } : chat)));
            setEditingTitle(null);
            setNewTitle('');
        } catch (error) {
            alert('Fehler beim Aktualisieren des Titels: ' + error);
            console.error('Fehler beim Aktualisieren des Titels:', error);
        }
    };

    const handleDeleteChat = async (chatId) => {
        if (!window.confirm('Möchten Sie diesen Chat wirklich löschen?')) return;

        try {
            await chatService.deleteChat(chatId);
            setChats((prev) => prev.filter((chat) => chat.id !== chatId));

            if (currentChatId === chatId) {
                handleNewChat();
            }
        } catch (error) {
            alert('Fehler beim Löschen des Chats: ' + error);
            console.error('Fehler beim Löschen des Chats:', error);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.sidebar}>
                <h3>Alte Chats</h3>
                <button onClick={handleNewChat} style={styles.newChatButton}>Neuer Chat</button>
                <ul style={styles.chatList}>
                    {chats.map((chat) => (
                        <li
                            key={chat.id}
                            style={{
                                ...styles.chatItem,
                                backgroundColor: currentChatId === chat.id ? '#ddd' : 'transparent',
                            }}
                        >
                            {editingTitle === chat.id ? (
                                <div style={styles.editContainer}>
                                    <input
                                        type="text"
                                        value={newTitle}
                                        onChange={(e) => setNewTitle(e.target.value)}
                                    />
                                    <button onClick={handleSaveTitle}>Speichern</button>
                                    <button onClick={() => setEditingTitle(null)}>Abbrechen</button>
                                </div>
                            ) : (
                                <div style={styles.chatRow}>
                                    <span onClick={() => handleChatSelect(chat.id)}>{chat.name}</span>
                                    <button onClick={() => handleEditTitle(chat.id, chat.name)}>Bearbeiten</button>
                                    <button onClick={() => handleDeleteChat(chat.id)}>Löschen</button>
                                </div>
                            )}
                        </li>
                    ))}
                </ul>
            </div>
            <div style={styles.chatWindow}>
                <h3>{currentChatId ? `Chat ${currentChatId}` : 'Neuer Chat'}</h3>
                <div style={styles.messages}>
                    {messages.map((message, index) => (
                        <div key={index} style={styles.message}>
                            <strong>{message.sender}:</strong> {message.text}
                        </div>
                    ))}
                </div>
                <div style={styles.inputContainer}>
                    <input
                        type="text"
                        value={currentMessage}
                        onChange={(e) => setCurrentMessage(e.target.value)}
                        placeholder="Nachricht schreiben..."
                        style={styles.input}
                    />
                    <button onClick={handleSendMessage} style={styles.button}>Senden</button>
                </div>
            </div>
        </div>
    );
}

const styles = {
    container: {
        display: 'flex',
        height: '100vh',
    },
    sidebar: {
        width: '25%',
        backgroundColor: '#f0f0f0',
        padding: '10px',
        overflowY: 'auto',
    },
    newChatButton: {
        padding: '10px',
        marginBottom: '10px',
        backgroundColor: '#007bff',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
        width: '100%',
    },
    chatList: {
        listStyleType: 'none',
        padding: 0,
    },
    chatItem: {
        padding: '10px',
        cursor: 'pointer',
        borderBottom: '1px solid #ccc',
    },
    chatRow: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    editContainer: {
        display: 'flex',
        gap: '5px',
    },
    chatWindow: {
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        padding: '10px',
        backgroundColor: '#fff',
    },
    messages: {
        flex: 1,
        overflowY: 'auto',
        marginBottom: '10px',
    },
    inputContainer: {
        display: 'flex',
        gap: '10px',
    },
    input: {
        flex: 1,
        padding: '10px',
        fontSize: '16px',
    },
    button: {
        padding: '10px',
        backgroundColor: '#007bff',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
    },
};

export default Chat;