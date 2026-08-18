import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
} from "react-router-dom";

import Documents from "./pages/Documents";
import Dashboard from "./pages/Dashboard";
import Chat from "./pages/Chat";
import CloudCosts from "./pages/CloudCosts";
import Anomalies from "./pages/Anomalies";

import Login from "./pages/Login";
import Register from "./pages/Register";

import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";


// ==========================================
// PROTECTED ROUTE
// ==========================================

function ProtectedRoute({ children }) {

    const token =
        localStorage.getItem("token");

    if (!token) {

        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    return children;

}


// ==========================================
// MAIN APP LAYOUT
// ==========================================

function AppLayout() {

    return (

        <>

            {/* ======================================
                TOP NAVBAR
            ====================================== */}

            <Navbar />


            {/* ======================================
                MAIN APPLICATION LAYOUT
            ====================================== */}

            <div className="application-layout">


                {/* ==================================
                    SIDEBAR
                ================================== */}

                <Sidebar />


                {/* ==================================
                    PAGE CONTENT
                ================================== */}

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
                            path="/cloud-costs"
                            element={<CloudCosts />}
                        />


                        {/* =========================
                            ANOMALIES
                        ========================= */}

                        <Route
                            path="/anomalies"
                            element={<Anomalies />}
                        />


                        {/* =========================
                            FALLBACK
                        ========================= */}

                        <Route
                            path="*"
                            element={
                                <Navigate
                                    to="/"
                                    replace
                                />
                            }
                        />

                    </Routes>

                </main>

            </div>

        </>

    );

}


// ==========================================
// APP
// ==========================================

function App() {

    return (

        <BrowserRouter>

            <Routes>


                {/* ==================================
                    LOGIN
                ================================== */}

                <Route
                    path="/login"
                    element={<Login />}
                />


                {/* ==================================
                    REGISTER
                ================================== */}

                <Route
                    path="/register"
                    element={<Register />}
                />


                {/* ==================================
                    PROTECTED APPLICATION
                ================================== */}

                <Route
                    path="/*"
                    element={

                        <ProtectedRoute>

                            <AppLayout />

                        </ProtectedRoute>

                    }
                />

            </Routes>

        </BrowserRouter>

    );

}


export default App;

