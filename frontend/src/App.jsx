import { BrowserRouter, Routes, Route } from "react-router-dom";
import Documents from "./pages/Documents";
import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";

import Dashboard from "./pages/Dashboard";

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

                        <Route
                            path="/"
                            element={<Dashboard />}
                        />

                        <Route
                            path="/documents"
                            element={<Documents />}
                        />

                        <Route
                            path="/chat"
                            element={
                                <PlaceholderPage
                                    title="RAG Chat"
                                />
                            }
                        />

                        <Route
                            path="/costs"
                            element={
                                <PlaceholderPage
                                    title="Cloud Costs"
                                />
                            }
                        />

                        <Route
                            path="/recommendations"
                            element={
                                <PlaceholderPage
                                    title="AI Recommendations"
                                />
                            }
                        />

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