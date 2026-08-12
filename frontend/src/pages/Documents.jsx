import { useState } from "react";
import { Upload, FileText, CheckCircle, AlertCircle } from "lucide-react";

import api from "../services/api";

function Documents() {
    const [selectedFile, setSelectedFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const handleFileChange = (event) => {
        const file = event.target.files[0];

        setMessage("");
        setError("");

        if (!file) {
            setSelectedFile(null);
            return;
        }

        const allowedTypes = [
            "application/pdf",
            "text/csv",
        ];

        const filename = file.name.toLowerCase();

        const isPdf = filename.endsWith(".pdf");
        const isCsv = filename.endsWith(".csv");

        if (!isPdf && !isCsv) {
            setSelectedFile(null);
            setError("Only PDF and CSV files are supported.");
            return;
        }

        if (!allowedTypes.includes(file.type) && !isPdf && !isCsv) {
            setSelectedFile(null);
            setError("Invalid file type.");
            return;
        }

        if (file.size > 20 * 1024 * 1024) {
            setSelectedFile(null);
            setError("File size must be less than 20 MB.");
            return;
        }

        setSelectedFile(file);
    };

    const handleUpload = async () => {
        if (!selectedFile) {
            setError("Please select a PDF or CSV file.");
            return;
        }

        setUploading(true);
        setMessage("");
        setError("");

        const formData = new FormData();

        formData.append("file", selectedFile);

        try {
            const response = await api.post(
                "/documents/upload",
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data",
                    },
                }
            );

            setMessage(
                `${response.data.filename} uploaded successfully.`
            );

            setSelectedFile(null);

        } catch (err) {

            console.error("Upload failed:", err);

            if (err.response?.data?.error) {
                setError(err.response.data.error);
            } else {
                setError(
                    "Upload failed. Make sure the backend is running."
                );
            }

        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="documents-page">

            <div className="dashboard-header">

                <h1>
                    Documents
                </h1>

                <p>
                    Upload cloud cost reports and infrastructure
                    documents for AI analysis.
                </p>

            </div>

            <div className="upload-card">

                <div className="upload-icon">
                    <Upload size={32} />
                </div>

                <h2>
                    Upload a document
                </h2>

                <p>
                    Supported formats: PDF and CSV
                </p>

                <p>
                    Maximum file size: 20 MB
                </p>

                <label className="file-input-label">

                    <FileText size={18} />

                    <span>
                        {selectedFile
                            ? selectedFile.name
                            : "Choose a file"}
                    </span>

                    <input
                        type="file"
                        accept=".pdf,.csv,application/pdf,text/csv"
                        onChange={handleFileChange}
                    />

                </label>

                {selectedFile && (
                    <div className="selected-file">

                        <strong>
                            Selected:
                        </strong>

                        <span>
                            {selectedFile.name}
                        </span>

                        <span>
                            {(selectedFile.size / 1024 / 1024).toFixed(2)}
                            {" MB"}
                        </span>

                    </div>
                )}

                <button
                    className="upload-button"
                    onClick={handleUpload}
                    disabled={!selectedFile || uploading}
                >
                    <Upload size={18} />

                    {uploading
                        ? "Uploading..."
                        : "Upload Document"}
                </button>

                {message && (
                    <div className="upload-message success">

                        <CheckCircle size={18} />

                        <span>
                            {message}
                        </span>

                    </div>
                )}

                {error && (
                    <div className="upload-message error">

                        <AlertCircle size={18} />

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