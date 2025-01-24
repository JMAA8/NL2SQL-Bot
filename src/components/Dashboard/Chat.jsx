import React, { useState, useEffect } from 'react';
import chatService from '../../services/chatService';

function Chat() {
    const [chats, setChats] = useState([]); // Liste aller Chats
    const [messages, setMessages] = useState([]); // Nachrichten im aktuellen Chat
    const [currentChatId, setCurrentChatId] = useState(null); // Aktiver Chat
    const [currentMessage, setCurrentMessage] = useState(''); // Nachricht im Eingabefeld

    useEffect(() => {
        const fetchUserChats = async () => {
            try {
                const data = await chatService.getUserChats(); // Promise korrekt auflösen
                setChats(data); // Chats setzen
            } catch (error) {
                console.error('Fehler beim Abrufen der Benutzer-Chats:', error);
            }
        };

        // Promise korrekt behandeln
        fetchUserChats().catch((error) => {
            console.error('Unhandled promise rejection:', error);
        });
    }, []);


    // Nachrichten eines spezifischen Chats laden
    const handleChatSelect = async (chatId) => {
        try {
            setCurrentChatId(chatId);
            const data = await chatService.getChatMessages(chatId);
            setMessages(data);
        } catch (error) {
            console.error('Fehler beim Laden des Chatverlaufs:', error);
        }
    };

    // Neuer Chat starten
    const handleNewChat = () => {
        setCurrentChatId(null); // Setze aktuelle Chat-ID zurück
        setMessages([]); // Leere die Nachrichten
    };

    // Nachricht senden
    const handleSendMessage = async () => {
        if (!currentMessage.trim()) return;

        try {
            const response = await chatService.sendMessage(currentMessage, currentChatId);
            setMessages((prev) => [...prev, response]); // Nachricht zum Chat hinzufügen
            setCurrentMessage(''); // Eingabefeld leeren
            if (!currentChatId) {
                // Wenn ein neuer Chat erstellt wurde, aktualisiere die Chats-Liste
                setCurrentChatId(response.chatId);
                setChats((prev) => [...prev, { id: response.chatId, name: `Chat ${response.chatId}` }]);
            }
        } catch (error) {
            console.error('Fehler beim Senden der Nachricht:', error);
        }
    };

    return (
        <div style={styles.container}>
            {/* Seitenleiste mit Chats */}
            <div style={styles.sidebar}>
                <h3>Alte Chats</h3>
                <button onClick={handleNewChat} style={styles.newChatButton}>
                    Neuer Chat
                </button>
                <ul style={styles.chatList}>
                    {chats.map((chat) => (
                        <li
                            key={chat.id}
                            onClick={() => handleChatSelect(chat.id)}
                            style={{
                                ...styles.chatItem,
                                backgroundColor: currentChatId === chat.id ? '#ddd' : 'transparent',
                            }}
                        >
                            {chat.name}
                        </li>
                    ))}
                </ul>
            </div>

            {/* Chatfenster */}
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
                    <button onClick={handleSendMessage} style={styles.button}>
                        Senden
                    </button>
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
