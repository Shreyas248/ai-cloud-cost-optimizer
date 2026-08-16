import { useEffect, useRef, useState } from "react";
import { Send, Bot, User, Loader2, FileText } from "lucide-react";
import api from "../services/api";
import "./Chat.css";

function Chat() {

    const [messages, setMessages] = useState([
        {
            role: "assistant",
            content:
                "Hello! I'm your Cloud Cost AI Assistant. Ask me anything about the cloud cost documents you've uploaded.",
            sources: []
        }
    ]);

    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);

    const messagesEndRef = useRef(null);

    // ==========================================
    // AUTO SCROLL
    // ==========================================

    useEffect(() => {

        messagesEndRef.current?.scrollIntoView({
            behavior: "smooth"
        });

    }, [messages, loading]);


    // ==========================================
    // SEND MESSAGE
    // ==========================================

    const sendMessage = async () => {

        const question = input.trim();

        if (!question || loading) {
            return;
        }

        // Add user message immediately
        setMessages((previous) => [
            ...previous,
            {
                role: "user",
                content: question,
                sources: []
            }
        ]);

        setInput("");
        setLoading(true);

        try {

            const response = await api.post(
                "/search",
                {
                    query: question
                }
            );

            console.log(
                "RAG response:",
                response.data
            );

            setMessages((previous) => [
                ...previous,
                {
                    role: "assistant",
                    content:
                        response.data.answer ||
                        "I couldn't generate an answer.",
                    sources:
                        response.data.sources || []
                }
            ]);

        } catch (error) {

    console.error("========== CHAT ERROR ==========");
    console.error("Full error:", error);
    console.error("Message:", error.message);
    console.error("Status:", error.response?.status);
    console.error("Response:", error.response?.data);
    console.error("URL:", error.config?.url);
    console.error("Base URL:", error.config?.baseURL);
    console.error("================================");

    const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        "Unknown error";

    setMessages((prev) => [
        ...prev,
        {
            role: "assistant",
            content: `RAG Error: ${errorMessage}`
        }
    ]);
}finally {

            setLoading(false);

        }
    };


    // ==========================================
    // ENTER KEY
    // ==========================================

    const handleKeyDown = (event) => {

        if (
            event.key === "Enter" &&
            !event.shiftKey
        ) {

            event.preventDefault();

            sendMessage();
        }
    };


    return (

        <div className="chat-page">

            {/* =====================================
                HEADER
            ===================================== */}

            <div className="chat-header">

                <div className="chat-header-icon">
                    <Bot size={26} />
                </div>

                <div>

                    <h1>
                        RAG Chat
                    </h1>

                    <p>
                        Ask questions about your
                        cloud cost documents
                    </p>

                </div>

            </div>


            {/* =====================================
                CHAT AREA
            ===================================== */}

            <div className="chat-container">

                <div className="messages-container">

                    {messages.map(
                        (message, index) => (

                            <div
                                key={index}
                                className={`message-row ${
                                    message.role
                                }`}
                            >

                                <div className="message-avatar">

                                    {message.role === "assistant"
                                        ? <Bot size={18} />
                                        : <User size={18} />
                                    }

                                </div>


                                <div className="message-content">

                                    <div className="message-bubble">

                                        {message.content}

                                    </div>


                                    {/* =================================
                                        SOURCES
                                    ================================= */}

                                    {message.role === "assistant" &&
                                        message.sources &&
                                        message.sources.length > 0 && (

                                            <div className="sources-section">

                                                <div className="sources-title">

                                                    <FileText size={15} />

                                                    Sources

                                                </div>


                                                {message.sources.map(
                                                    (source, sourceIndex) => (

                                                        <div
                                                            className="source-card"
                                                            key={sourceIndex}
                                                        >

                                                            <div className="source-header">

                                                                <span>
                                                                    {source.filename ||
                                                                        "Document"}
                                                                </span>

                                                                {source.chunkIndex !== undefined && (

                                                                    <span>
                                                                        Chunk {
                                                                            source.chunkIndex
                                                                        }
                                                                    </span>

                                                                )}

                                                            </div>


                                                            <p>
                                                                {source.content}
                                                            </p>


                                                            {source.score !== undefined && (

                                                                <small>
                                                                    Relevance:{" "}
                                                                    {(
                                                                        source.score *
                                                                        100
                                                                    ).toFixed(1)}
                                                                    %
                                                                </small>

                                                            )}

                                                        </div>

                                                    )
                                                )}

                                            </div>

                                        )}

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

                                <Bot size={18} />

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

                    <div ref={messagesEndRef} />

                </div>


                {/* =====================================
                    INPUT
                ===================================== */}

                <div className="chat-input-container">

                    <textarea
                        value={input}
                        onChange={(event) =>
                            setInput(event.target.value)
                        }
                        onKeyDown={handleKeyDown}
                        placeholder="Ask about your cloud costs..."
                        rows={1}
                        disabled={loading}
                    />

                    <button
                        onClick={sendMessage}
                        disabled={
                            loading ||
                            !input.trim()
                        }
                        title="Send message"
                    >

                        {loading
                            ? <Loader2
                                size={20}
                                className="loading-icon"
                            />
                            : <Send size={20} />
                        }

                    </button>

                </div>


                <div className="chat-footer">

                    Answers are generated using your
                    uploaded documents.

                </div>

            </div>

        </div>

    );
}

export default Chat;