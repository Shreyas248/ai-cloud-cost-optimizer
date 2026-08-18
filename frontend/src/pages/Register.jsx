import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";

import {
    User,
    Mail,
    Lock,
    UserPlus,
    Loader2,
} from "lucide-react";

import api from "../services/api";

import "./Auth.css";


function Register() {

    const navigate = useNavigate();

    const [name, setName] =
        useState("");

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");


    // ==========================================
    // REGISTER
    // ==========================================

    const handleSubmit =
        async (event) => {

            event.preventDefault();

            setError("");

            setLoading(true);


            try {

                const response =
                    await api.post(
                        "/auth/register",
                        {
                            name,
                            email,
                            password,
                        }
                    );


                // ==================================
                // SAVE JWT
                // ==================================

                localStorage.setItem(
                    "token",
                    response.data.token
                );


                // ==================================
                // SAVE USER
                // ==================================

                localStorage.setItem(
                    "user",
                    JSON.stringify({
                        name: response.data.name,
                        email: response.data.email,
                    })
                );


                // ==================================
                // REDIRECT
                // ==================================

                navigate(
                    "/"
                );


            } catch (err) {

                console.error(
                    "Registration failed:",
                    err
                );


                setError(

                    err.response?.data?.error ||

                    err.response?.data?.message ||

                    "Registration failed. Please try again."

                );

            } finally {

                setLoading(false);

            }

        };


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

                        Create Account

                    </h1>

                    <p>

                        Start optimizing your cloud costs with AI

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
                    onSubmit={handleSubmit}
                    className="auth-form"
                >


                    <label>

                        Full Name

                    </label>


                    <div className="input-wrapper">

                        <User
                            size={19}
                        />

                        <input
                            type="text"
                            placeholder="Your name"
                            value={name}
                            onChange={
                                (event) =>
                                    setName(
                                        event.target.value
                                    )
                            }
                            required
                            disabled={loading}
                        />

                    </div>


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


                    <label>

                        Password

                    </label>


                    <div className="input-wrapper">

                        <Lock
                            size={19}
                        />

                        <input
                            type="password"
                            placeholder="Minimum 6 characters"
                            value={password}
                            onChange={
                                (event) =>
                                    setPassword(
                                        event.target.value
                                    )
                            }
                            minLength="6"
                            required
                            disabled={loading}
                        />

                    </div>


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

                                Creating account...

                            </>

                        ) : (

                            <>
                                <UserPlus
                                    size={19}
                                />

                                Create Account

                            </>

                        )}

                    </button>

                </form>


                {/* ================================
                    FOOTER
                ================================= */}

                <div className="auth-footer">

                    Already have an account?

                    <Link to="/login">

                        Sign in

                    </Link>

                </div>

            </div>

        </div>

    );

}


export default Register;

