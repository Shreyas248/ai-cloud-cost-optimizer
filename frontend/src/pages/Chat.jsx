import { useEffect, useRef, useState } from "react";
import {
    Send,
    Bot,
    User,
    Loader2,
    Trash2
} from "lucide-react";

import api from "../services/api";
import "./Chat.css";


// ==========================================
// LOCAL STORAGE KEY
// ==========================================

const STORAGE_KEY =
    "cloud_optimizer_rag_chat_history";


// ==========================================
// DEFAULT MESSAGE
// ==========================================

const defaultMessage = {
    role: "assistant",

    content:
        "Hello! I'm your Cloud Cost AI Assistant. Ask me anything about the cloud cost documents you've uploaded.",

    sources: []
};


// ==========================================
// CHAT COMPONENT
// ==========================================

function Chat() {


    // ==========================================
    // LOAD CHAT HISTORY
    // ==========================================

    const [messages, setMessages] = useState(() => {

        try {

            const savedMessages =
                localStorage.getItem(
                    STORAGE_KEY
                );


            if (savedMessages) {

                const parsedMessages =
                    JSON.parse(
                        savedMessages
                    );


                if (
                    Array.isArray(
                        parsedMessages
                    ) &&
                    parsedMessages.length > 0
                ) {

                    return parsedMessages;

                }

            }

        } catch (error) {

            console.error(
                "Failed to load chat history:",
                error
            );

        }


        return [defaultMessage];

    });


    // ==========================================
    // INPUT
    // ==========================================

    const [input, setInput] =
        useState("");


    // ==========================================
    // LOADING
    // ==========================================

    const [loading, setLoading] =
        useState(false);


    // ==========================================
    // MESSAGE END REFERENCE
    // ==========================================

    const messagesEndRef =
        useRef(null);


    // ==========================================
    // SAVE CHAT HISTORY
    // ==========================================

    useEffect(() => {

        try {

            localStorage.setItem(
                STORAGE_KEY,
                JSON.stringify(messages)
            );

        } catch (error) {

            console.error(
                "Failed to save chat history:",
                error
            );

        }

    }, [messages]);


    // ==========================================
    // AUTO SCROLL
    // ==========================================

    useEffect(() => {

        messagesEndRef.current?.scrollIntoView({
            behavior: "smooth"
        });

    }, [
        messages,
        loading
    ]);


    // ==========================================
    // SEND MESSAGE
    // ==========================================

    const sendMessage = async () => {

        const question =
            input.trim();


        // Do nothing if empty
        // or currently loading

        if (
            !question ||
            loading
        ) {

            return;

        }


        // ======================================
        // ADD USER MESSAGE
        // ======================================

        const userMessage = {

            role: "user",

            content: question,

            sources: []

        };


        setMessages(
            (previous) => [
                ...previous,
                userMessage
            ]
        );


        // Clear input

        setInput("");


        // Start loading

        setLoading(true);


        try {

            // ==================================
            // CALL RAG BACKEND
            // ==================================

            const response =
                await api.post(
                    "/search",
                    {
                        query: question
                    }
                );


            console.log(
                "RAG response:",
                response.data
            );


            // ==================================
            // ASSISTANT MESSAGE
            // ==================================

            const assistantMessage = {

                role: "assistant",

                content:
                    response.data.answer ||
                    "I couldn't generate an answer.",

                // Keep sources internally.
                // They are NOT displayed in UI.

                sources:
                    response.data.sources ||
                    []

            };


            setMessages(
                (previous) => [
                    ...previous,
                    assistantMessage
                ]
            );


        } catch (error) {

            // ==================================
            // ERROR LOGGING
            // ==================================

            console.error(
                "========== CHAT ERROR =========="
            );

            console.error(
                "Full error:",
                error
            );

            console.error(
                "Message:",
                error.message
            );

            console.error(
                "Status:",
                error.response?.status
            );

            console.error(
                "Response:",
                error.response?.data
            );

            console.error(
                "URL:",
                error.config?.url
            );

            console.error(
                "Base URL:",
                error.config?.baseURL
            );

            console.error(
                "================================"
            );


            const errorMessage =
                error.response?.data?.message ||
                error.response?.data?.error ||
                error.message ||
                "Unknown error";


            // ==================================
            // DISPLAY ERROR
            // ==================================

            setMessages(
                (previous) => [
                    ...previous,

                    {
                        role: "assistant",

                        content:
                            `RAG Error: ${errorMessage}`,

                        sources: []

                    }
                ]
            );

        } finally {

            setLoading(false);

        }

    };


    // ==========================================
    // ENTER KEY
    // ==========================================

    const handleKeyDown = (
        event
    ) => {

        if (
            event.key === "Enter" &&
            !event.shiftKey
        ) {

            event.preventDefault();

            sendMessage();

        }

    };


    // ==========================================
    // CLEAR CHAT
    // ==========================================

    const clearChat = () => {

        const confirmed =
            window.confirm(
                "Are you sure you want to clear your chat history?"
            );


        if (!confirmed) {

            return;

        }


        // Remove saved history

        localStorage.removeItem(
            STORAGE_KEY
        );


        // Reset chat

        setMessages([
            defaultMessage
        ]);

    };


    // ==========================================
    // UI
    // ==========================================

    return (

        <div className="chat-page">


            {/* =====================================
                HEADER
            ===================================== */}

            <div className="chat-header">


                {/* HEADER ICON */}

                <div className="chat-header-icon">

                    <Bot size={26} />

                </div>


                {/* HEADER TEXT */}

                <div className="chat-header-text">

                    <h1>
                        RAG Chat
                    </h1>

                    <p>
                        Ask questions about your
                        cloud cost documents
                    </p>

                </div>


                {/* CLEAR CHAT */}

                {messages.length > 1 && (

                    <button
                        className="clear-chat-button"
                        onClick={clearChat}
                        type="button"
                        title="Clear conversation"
                    >

                        <Trash2
                            size={16}
                        />

                        <span>
                            Clear Chat
                        </span>

                    </button>

                )}

            </div>



            {/* =====================================
                CHAT CONTAINER
            ===================================== */}

            <div className="chat-container">


                {/* =================================
                    MESSAGES
                ================================= */}

                <div className="messages-container">


                    {messages.map(
                        (
                            message,
                            index
                        ) => (

                            <div
                                key={index}
                                className={`message-row ${message.role}`}
                            >


                                {/* =========================
                                    AVATAR
                                ========================= */}

                                <div className="message-avatar">

                                    {message.role ===
                                    "assistant"
                                        ? (
                                            <Bot
                                                size={18}
                                            />
                                        )
                                        : (
                                            <User
                                                size={18}
                                            />
                                        )}

                                </div>



                                {/* =========================
                                    MESSAGE
                                ========================= */}

                                <div className="message-content">


                                    <div className="message-bubble">

                                        {message.content}

                                    </div>


                                    {/* =========================
                                        SOURCES REMOVED
                                    ========================= */}

                                </div>

                            </div>

                        )
                    )}



                    {/* =================================
                        LOADING
                    ================================= */}

                    {loading && (

                        <div className="message-row assistant">


                            <div className="message-avatar">

                                <Bot
                                    size={18}
                                />

                            </div>


                            <div className="message-bubble loading-bubble">

                                <Loader2
                                    size={18}
                                    className="loading-icon"
                                />

                                Thinking...

                            </div>

                        </div>

                    )}


                    {/* SCROLL TARGET */}

                    <div
                        ref={messagesEndRef}
                    />

                </div>



                {/* =====================================
                    INPUT
                ===================================== */}

                <div className="chat-input-container">


                    <textarea
                        value={input}
                        onChange={(event) =>
                            setInput(
                                event.target.value
                            )
                        }
                        onKeyDown={
                            handleKeyDown
                        }
                        placeholder="Ask about your cloud costs..."
                        rows={1}
                        disabled={loading}
                    />


                    <button
                        onClick={
                            sendMessage
                        }
                        disabled={
                            loading ||
                            !input.trim()
                        }
                        title="Send message"
                    >

                        {loading
                            ? (
                                <Loader2
                                    size={20}
                                    className="loading-icon"
                                />
                            )
                            : (
                                <Send
                                    size={20}
                                />
                            )}

                    </button>

                </div>



                {/* =====================================
                    FOOTER
                ===================================== */}

                <div className="chat-footer">

                    Answers are generated using your
                    uploaded documents.

                </div>


            </div>

        </div>

    );

}


export default Chat;