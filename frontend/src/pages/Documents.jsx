import { useState } from "react";

import {
    Upload,
    FileText,
    CheckCircle,
    AlertCircle,
    Loader2
} from "lucide-react";

import api from "../services/api";

import "./Documents.css";


function Documents() {

    // ==========================================
    // STATE
    // ==========================================

    const [selectedFile, setSelectedFile] =
        useState(null);

    const [uploading, setUploading] =
        useState(false);

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");


    // ==========================================
    // GET JWT TOKEN
    // ==========================================

    const getToken = () => {

        /*
         * Try the common token keys.
         *
         * Your login page may use one of these.
         */

        const token =
            localStorage.getItem("token") ||

            localStorage.getItem("jwtToken") ||

            localStorage.getItem("accessToken") ||

            localStorage.getItem("jwt") ||

            localStorage.getItem("authToken");


        return token;
    };


    // ==========================================
    // FILE SELECTION
    // ==========================================

    const handleFileChange = (event) => {

        const file =
            event.target.files[0];


        setMessage("");

        setError("");


        if (!file) {

            setSelectedFile(null);

            return;
        }


        const fileName =
            file.name.toLowerCase();


        // ==========================================
        // FILE TYPE
        // ==========================================

        if (
            !fileName.endsWith(".csv") &&
            !fileName.endsWith(".pdf")
        ) {

            setSelectedFile(null);

            setError(
                "Only CSV and PDF files are supported."
            );

            event.target.value = "";

            return;
        }


        // ==========================================
        // FILE SIZE
        // ==========================================

        if (
            file.size >
            20 * 1024 * 1024
        ) {

            setSelectedFile(null);

            setError(
                "File size must be less than 20 MB."
            );

            event.target.value = "";

            return;
        }


        setSelectedFile(file);
    };


    // ==========================================
    // UPLOAD FILE
    // ==========================================

    const handleUpload = async () => {

        if (!selectedFile) {

            setError(
                "Please select a file first."
            );

            return;
        }


        setUploading(true);

        setMessage("");

        setError("");


        try {

            // ======================================
            // GET TOKEN
            // ======================================

            const token =
                getToken();


            console.log(
                "================================="
            );

            console.log(
                "DOCUMENT UPLOAD"
            );

            console.log(
                "Token exists:",
                !!token
            );

            if (token) {

                console.log(
                    "Token length:",
                    token.length
                );

            }

            console.log(
                "================================="
            );


            // ======================================
            // NO TOKEN
            // ======================================

            if (!token) {

                setError(
                    "You are not logged in. Please log in again."
                );

                return;
            }


            // ======================================
            // CREATE FORM DATA
            // ======================================

            const formData =
                new FormData();


            formData.append(
                "file",
                selectedFile
            );


            // ======================================
            // UPLOAD
            //
            // IMPORTANT:
            //
            // We explicitly attach:
            //
            // Authorization: Bearer <JWT>
            //
            // This fixes the 401 problem when
            // api.js does not attach the token.
            // ======================================

            const response =
                await api.post(
                    "/documents/upload",
                    formData,
                    {
                        headers: {

                            Authorization:
                                `Bearer ${token}`

                        }
                    }
                );


            // ======================================
            // SUCCESS
            // ======================================

            console.log(
                "Upload response:",
                response.data
            );


            setMessage(

                response.data?.message ||

                "File uploaded successfully."

            );


            // ======================================
            // RESET FILE
            // ======================================

            setSelectedFile(null);


            const fileInput =
                document.getElementById(
                    "file-input"
                );


            if (fileInput) {

                fileInput.value = "";

            }


        } catch (err) {

            console.error(
                "================================="
            );

            console.error(
                "DOCUMENT UPLOAD ERROR"
            );

            console.error(
                "Status:",
                err.response?.status
            );

            console.error(
                "Response:",
                err.response?.data
            );

            console.error(
                "Message:",
                err.message
            );

            console.error(
                "================================="
            );


            // ======================================
            // 401
            // ======================================

            if (
                err.response?.status === 401
            ) {

                setError(
                    "Authentication failed. Please log out and log in again."
                );

                return;
            }


            // ======================================
            // 403
            // ======================================

            if (
                err.response?.status === 403
            ) {

                setError(
                    "Access denied. Your authentication token may be invalid."
                );

                return;
            }


            // ======================================
            // BACKEND ERROR
            // ======================================

            const backendError =

                err.response?.data?.error ||

                err.response?.data?.message;


            if (backendError) {

                setError(
                    backendError
                );

                return;
            }


            // ======================================
            // NETWORK ERROR
            // ======================================

            if (
                err.code === "ERR_NETWORK"
            ) {

                setError(
                    "Unable to connect to the backend. Make sure Spring Boot is running on port 8080."
                );

                return;
            }


            // ======================================
            // DEFAULT ERROR
            // ======================================

            setError(
                "Unable to upload file. Please try again."
            );

        } finally {

            setUploading(false);

        }
    };


    // ==========================================
    // RENDER
    // ==========================================

    return (

        <div className="documents-page">


            {/* ======================================
                HEADER
            ====================================== */}

            <div className="documents-header">

                <div>

                    <h1>
                        Documents
                    </h1>

                    <p>

                        Upload cloud cost reports and
                        infrastructure documents for AI
                        analysis.

                    </p>

                </div>

            </div>


            {/* ======================================
                UPLOAD CARD
            ====================================== */}

            <div className="upload-card">


                {/* ==================================
                    ICON
                ================================== */}

                <div className="upload-icon">

                    <Upload
                        size={32}
                    />

                </div>


                {/* ==================================
                    TITLE
                ================================== */}

                <h2>
                    Upload a document
                </h2>


                <p className="upload-description">

                    Supported formats: PDF and CSV

                    <br />

                    Maximum file size: 20 MB

                </p>


                {/* ==================================
                    CHOOSE FILE
                ================================== */}

                <label

                    htmlFor="file-input"

                    className="choose-file-button"

                >

                    <FileText
                        size={20}
                    />

                    <span>
                        Choose a file
                    </span>

                </label>


                <input

                    id="file-input"

                    type="file"

                    accept=".csv,.pdf"

                    onChange={
                        handleFileChange
                    }

                    hidden

                />


                {/* ==================================
                    SELECTED FILE
                ================================== */}

                {selectedFile && (

                    <div className="selected-file">

                        <FileText
                            size={20}
                        />


                        <div className="selected-file-info">

                            <strong>

                                {selectedFile.name}

                            </strong>


                            <span>

                                {(
                                    selectedFile.size /
                                    1024
                                ).toFixed(1)} KB

                            </span>

                        </div>


                        <CheckCircle

                            size={20}

                            className="file-success-icon"

                        />

                    </div>

                )}


                {/* ==================================
                    UPLOAD BUTTON
                ================================== */}

                <button

                    className="upload-button"

                    onClick={
                        handleUpload
                    }

                    disabled={
                        !selectedFile ||
                        uploading
                    }

                >

                    {uploading ? (

                        <>

                            <Loader2

                                size={20}

                                className="loading-icon"

                            />

                            Uploading...

                        </>

                    ) : (

                        <>

                            <Upload
                                size={20}
                            />

                            Upload Document

                        </>

                    )}

                </button>


                {/* ==================================
                    SUCCESS
                ================================== */}

                {message && (

                    <div className="upload-success">

                        <CheckCircle
                            size={20}
                        />

                        <span>

                            {message}

                        </span>

                    </div>

                )}


                {/* ==================================
                    ERROR
                ================================== */}

                {error && (

                    <div className="upload-error">

                        <AlertCircle
                            size={20}
                        />

                        <span>

                            {error}

                        </span>

                    </div>

                )}

            </div>

        </div>

    );

}


export default Documents;