import React, { useState, useEffect } from 'react';
import chatService, { getUserChats } from '../../services/chatService';

function Chat() {
    const [chats, setChats] = useState([]); // Liste aller Chats
    const [messages, setMessages] = useState([]); // Nachrichten im aktuellen Chat
    const [currentChatId, setCurrentChatId] = useState(null); // Aktiver Chat
    const [currentMessage, setCurrentMessage] = useState(''); // Nachricht im Eingabefeld
    const [editingTitle, setEditingTitle] = useState(null); // Bearbeiteter Chat-Titel
    const [newTitle, setNewTitle] = useState(''); // Neuer Titel

    // Funktion, um den Titel des aktuellen Chats zu erhalten
    const getCurrentChatTitle = () => {
        const currentChat = chats.find((chat) => chat.id === currentChatId);
        return currentChat ? currentChat.title : 'Neuer Chat';
    };

    useEffect(() => {
        fetchUserChats();
    }, []);

    const fetchUserChats = async () => {
        try {
            const data = await chatService.getUserChats(); // Abruf aller Benutzer-Chats
            const formattedChats = data.map((chat) => ({
                id: chat.chatId,
                title: chat.title || `Chat ${chat.chatId} Kein Titel erkannt`, // Fallback-Titel, falls kein Titel vorhanden ist
            }));
            setChats(formattedChats); // Chats in den State setzen
        } catch (error) {
            alert('Fehler beim Abrufen der Chats: ' + error);
            console.error('Fehler beim Abrufen der Benutzer-Chats:', error);
        }
    };



    const handleChatSelect = async (chatId) => {
        try {
            setCurrentChatId(chatId);
            const data = await chatService.getChatMessages(chatId);

            // Hier die Struktur für prompt und response anpassen
            const formattedMessages = data.flatMap((message) => [
                { sender: 'User', text: message.prompt },
                { sender: 'Bot', text: message.response },
            ]);

            setMessages(formattedMessages);
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

            // Nachricht strukturieren und hinzufügen
            const newMessages = response.messages.flatMap((messages) => [
                { sender: 'User', text: messages.prompt },
                { sender: 'Bot', text: messages.response },
            ]);

            setMessages((prev) => [...prev, ...newMessages]);
            setCurrentMessage('');

            if (!currentChatId) {
                console.log('Neuer Chat wird erstellt mit ID:', response.chatId);
                setCurrentChatId(response.chatId);
                await fetchUserChats();

                /*
                // Neuen Chat manuell zur Liste hinzufügen
                setChats((prevChats) => [
                    ...prevChats,
                    {
                        id: response.chatId,
                        title: response.title || `Chat ${response.chatId}`,
                    },
                ]);

                 */

                // Kein zusätzlicher Abruf nötig
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
            console.log('Chat - handleSaveTitle - nach ausführen von ChatService');
            setChats((prev) => prev.map((chat) => (chat.id === editingTitle ? { ...chat, title: newTitle } : chat)));
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
                                    {/* Titel anzeigen und `handleChatSelect` hinzufügen */}
                                    <span
                                        onClick={() => handleChatSelect(chat.id)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        {chat.title || `Chat ${chat.id}`} {/* Fallback-Titel */}
                                    </span>
                                    <button onClick={() => handleEditTitle(chat.id, chat.title)}>Bearbeiten</button>
                                    <button onClick={() => handleDeleteChat(chat.id)}>Löschen</button>
                                </div>
                            )}
                        </li>
                    ))}
                </ul>
            </div>

            <div style={styles.chatWindow}>
                <h3>{getCurrentChatTitle()}</h3>
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
