import { Cloud, UserCircle, LogOut } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

import "./Navbar.css";


function Navbar() {

    const navigate = useNavigate();

    const [showLogoutModal, setShowLogoutModal] =
        useState(false);


    // ==========================================
    // GET USER DATA
    // ==========================================

    const storedUser =
        localStorage.getItem("user");

    let userName = "User";

    if (storedUser) {

        try {

            const user =
                JSON.parse(storedUser);

            userName =
                user.name || "User";

        } catch (error) {

            console.error(
                "Failed to parse user data:",
                error
            );

        }

    }


    // ==========================================
    // LOGOUT
    // ==========================================

    const handleLogout = () => {

        localStorage.removeItem("token");

        localStorage.removeItem("user");

        setShowLogoutModal(false);

        navigate("/login");

    };


    return (

        <>

            <header className="navbar">


                {/* ==================================
                    BRAND
                ================================== */}

                <div className="navbar-brand">

                    <Cloud size={26} />

                    <span>
                        AI Cloud Cost Optimizer
                    </span>

                </div>


                {/* ==================================
                    USER AREA
                ================================== */}

                <div className="navbar-actions">


                    <div className="navbar-user">

                        <UserCircle size={22} />

                        <span>
                            {userName}
                        </span>

                    </div>


                    <button
                        className="logout-button"
                        onClick={() =>
                            setShowLogoutModal(true)
                        }
                    >

                        <LogOut size={19} />

                        Logout

                    </button>

                </div>

            </header>


            {/* ======================================
                LOGOUT CONFIRMATION MODAL
            ====================================== */}

            {showLogoutModal && (

                <div className="logout-modal-overlay">

                    <div className="logout-modal">


                        <div className="logout-modal-icon">

                            <LogOut size={28} />

                        </div>


                        <h2>
                            Log out?
                        </h2>


                        <p>
                            Are you sure you want to log out
                            of AI Cloud Cost Optimizer?
                        </p>


                        <div className="logout-modal-actions">


                            <button
                                className="cancel-button"
                                onClick={() =>
                                    setShowLogoutModal(false)
                                }
                            >

                                Cancel

                            </button>


                            <button
                                className="confirm-logout-button"
                                onClick={handleLogout}
                            >

                                <LogOut size={18} />

                                Yes, Logout

                            </button>

                        </div>

                    </div>

                </div>

            )}

        </>

    );

}


export default Navbar;