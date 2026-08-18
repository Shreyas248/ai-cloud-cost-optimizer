import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Mail, Lock, LogIn, Loader2 } from "lucide-react";

import api from "../services/api";

import "./Auth.css";


function Login() {

    const navigate = useNavigate();


    // ==========================================
    // STATE
    // ==========================================

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");


    // ==========================================
    // LOGIN
    // ==========================================

    const handleSubmit =
        async (event) => {

            event.preventDefault();

            setError("");

            setLoading(true);


            try {

                // ==================================
                // CALL LOGIN API
                // ==================================

                const response =
                    await api.post(
                        "/auth/login",
                        {
                            email,
                            password
                        }
                    );


                console.log(
                    "================================="
                );

                console.log(
                    "LOGIN RESPONSE"
                );

                console.log(
                    response.data
                );

                console.log(
                    "Response type:",
                    typeof response.data
                );

                console.log(
                    "================================="
                );


                // ==================================
                // GET JWT
                // ==================================
                //
                // Your backend appears to return:
                //
                // "eyJhbGciOiJIUzUxMiJ9..."
                //
                // rather than:
                //
                // {
                //     token: "eyJ..."
                // }
                //
                // Therefore we support BOTH.
                // ==================================

                let token;


                if (
                    typeof response.data === "string"
                ) {

                    // Backend returns raw JWT string

                    token =
                        response.data;

                } else {

                    // Backend returns JSON object

                    token =
                        response.data?.token ||

                        response.data?.jwt ||

                        response.data?.accessToken;
                }


                // ==================================
                // VALIDATE TOKEN
                // ==================================

                if (
                    !token ||
                    typeof token !== "string"
                ) {

                    console.error(
                        "JWT token was not found in login response."
                    );

                    console.error(
                        "Response:",
                        response.data
                    );

                    throw new Error(
                        "Login succeeded but the server did not return a valid JWT token."
                    );
                }


                // ==================================
                // CLEAN TOKEN
                // ==================================

                token =
                    token.trim();


                // ==================================
                // SAVE JWT
                // ==================================

                localStorage.setItem(
                    "token",
                    token
                );


                // ==================================
                // VERIFY TOKEN WAS SAVED
                // ==================================

                const savedToken =
                    localStorage.getItem(
                        "token"
                    );


                console.log(
                    "JWT saved:",
                    !!savedToken
                );

                console.log(
                    "JWT length:",
                    savedToken
                        ? savedToken.length
                        : 0
                );


                // ==================================
                // SAVE USER
                // ==================================
                //
                // Your JWT contains:
                //
                // sub = email
                // name = user name
                //
                // But we don't need to decode it here.
                //
                // Save the login information if the
                // backend returns it.
                // ==================================

                const userData = {

                    name:
                        response.data?.name ||
                        email.split("@")[0],

                    email:
                        response.data?.email ||
                        email

                };


                localStorage.setItem(
                    "user",
                    JSON.stringify(
                        userData
                    )
                );


                // ==================================
                // REDIRECT
                // ==================================

                navigate(
                    "/"
                );


            } catch (err) {

                console.error(
                    "================================="
                );

                console.error(
                    "LOGIN FAILED"
                );

                console.error(
                    "Error:",
                    err
                );

                console.error(
                    "Response:",
                    err.response?.data
                );

                console.error(
                    "Status:",
                    err.response?.status
                );

                console.error(
                    "================================="
                );


                setError(

                    err.response?.data?.error ||

                    err.response?.data?.message ||

                    err.message ||

                    "Invalid email or password."

                );

            } finally {

                setLoading(false);

            }

        };


    // ==========================================
    // RENDER
    // ==========================================

    return (

        <div className="auth-page">

            <div className="auth-card">


                {/* ================================
                    BRAND
                ================================= */}

                <div className="auth-brand">

                    <div className="auth-logo">

                        ☁

                    </div>


                    <h1>

                        Cloud Optimizer

                    </h1>


                    <p>

                        Sign in to manage your cloud costs

                    </p>

                </div>


                {/* ================================
                    ERROR
                ================================= */}

                {error && (

                    <div className="auth-error">

                        {error}

                    </div>

                )}


                {/* ================================
                    FORM
                ================================= */}

                <form

                    onSubmit={
                        handleSubmit
                    }

                    className="auth-form"

                >


                    {/* ==========================
                        EMAIL
                    =========================== */}

                    <label>

                        Email Address

                    </label>


                    <div className="input-wrapper">

                        <Mail
                            size={19}
                        />


                        <input

                            type="email"

                            placeholder="you@example.com"

                            value={email}

                            onChange={
                                (event) =>
                                    setEmail(
                                        event.target.value
                                    )
                            }

                            required

                            disabled={loading}

                        />

                    </div>


                    {/* ==========================
                        PASSWORD
                    =========================== */}

                    <label>

                        Password

                    </label>


                    <div className="input-wrapper">

                        <Lock
                            size={19}
                        />


                        <input

                            type="password"

                            placeholder="Enter your password"

                            value={password}

                            onChange={
                                (event) =>
                                    setPassword(
                                        event.target.value
                                    )
                            }

                            required

                            disabled={loading}

                        />

                    </div>


                    {/* ==========================
                        LOGIN BUTTON
                    =========================== */}

                    <button

                        type="submit"

                        className="auth-button"

                        disabled={loading}

                    >

                        {loading ? (

                            <>

                                <Loader2

                                    size={19}

                                    className="loading-icon"

                                />

                                Signing in...

                            </>

                        ) : (

                            <>

                                <LogIn
                                    size={19}
                                />

                                Sign In

                            </>

                        )}

                    </button>


                </form>


                {/* ================================
                    FOOTER
                ================================= */}

                <div className="auth-footer">

                    Don't have an account?

                    <Link to="/register">

                        Create one

                    </Link>

                </div>


            </div>

        </div>

    );

}


export default Login;