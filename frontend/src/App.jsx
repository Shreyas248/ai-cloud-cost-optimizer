import { BrowserRouter, Routes, Route } from "react-router-dom";

import Documents from "./pages/Documents";
import Dashboard from "./pages/Dashboard";
import Chat from "./pages/Chat";
import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";


function PlaceholderPage({ title }) {
    return (
        <div className="placeholder-page">
            <h1>{title}</h1>

            <p>
                This module will be implemented in a later step.
            </p>
        </div>
    );
}


function App() {

    return (
        <BrowserRouter>

            <Navbar />

            <div className="application-layout">

                <Sidebar />

                <main className="main-content">

                    <Routes>

                        {/* =========================
                            DASHBOARD
                        ========================= */}

                        <Route
                            path="/"
                            element={<Dashboard />}
                        />


                        {/* =========================
                            DOCUMENTS
                        ========================= */}

                        <Route
                            path="/documents"
                            element={<Documents />}
                        />


                        {/* =========================
                            RAG CHAT
                        ========================= */}

                        <Route
                        path="/chat"
                        element={<Chat />}
                        />


                        {/* =========================
                            CLOUD COSTS
                        ========================= */}

                        <Route
                            path="/costs"
                            element={
                                <PlaceholderPage
                                    title="Cloud Costs"
                                />
                            }
                        />


                       

                        {/* =========================
                            ANOMALY DETECTION
                        ========================= */}

                        <Route
                            path="/anomalies"
                            element={
                                <PlaceholderPage
                                    title="Anomaly Detection"
                                />
                            }
                        />

                    </Routes>

                </main>

            </div>

        </BrowserRouter>
    );
}

export default App;