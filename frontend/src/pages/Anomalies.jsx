import { useEffect, useState } from "react";

import {
    AlertTriangle,
    RefreshCw,
    TrendingUp,
    AlertCircle,
    Activity,
    ShieldAlert,
    Server,
} from "lucide-react";

import api from "../services/api";

import "../styles/anomalies.css";


function Anomalies() {

    // =====================================================
    // STATE
    // =====================================================

    const [anomalies, setAnomalies] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState(null);


    // =====================================================
    // LOAD ANOMALIES
    // =====================================================

    const loadAnomalies = async () => {

        try {

            setLoading(true);
            setError(null);

            const response =
                await api.get("/anomalies");

            console.log(
                "Anomalies API:",
                response.data
            );

            setAnomalies(
                response.data.anomalies || []
            );

        } catch (err) {

            console.error(
                "Failed to load anomalies:",
                err
            );

            setError(
                err.response?.data?.error ||
                "Unable to load anomaly data."
            );

        } finally {

            setLoading(false);

        }
    };


    // =====================================================
    // LOAD ON PAGE OPEN
    // =====================================================

    useEffect(() => {

        loadAnomalies();

    }, []);


    // =====================================================
    // FORMAT CURRENCY
    // =====================================================

    const formatCurrency = (value) => {

        return `₹${Number(
            value || 0
        ).toLocaleString("en-IN", {
            maximumFractionDigits: 2,
        })}`;

    };


    // =====================================================
    // ANOMALY STATISTICS
    // =====================================================

    const criticalCount =
        anomalies.filter(
            (anomaly) =>
                anomaly.severity === "CRITICAL"
        ).length;


    const highCount =
        anomalies.filter(
            (anomaly) =>
                anomaly.severity === "HIGH"
        ).length;


    const mediumCount =
        anomalies.filter(
            (anomaly) =>
                anomaly.severity === "MEDIUM"
        ).length;


    const totalIncrease =
        anomalies.reduce(
            (total, anomaly) => {

                const difference =
                    Number(
                        anomaly.actualCost || 0
                    ) -
                    Number(
                        anomaly.averageCost || 0
                    );

                return total +
                    Math.max(
                        difference,
                        0
                    );

            },
            0
        );


    // =====================================================
    // GET SEVERITY CLASS
    // =====================================================

    const getSeverityClass =
        (severity) => {

            if (severity === "CRITICAL") {
                return "critical";
            }

            if (severity === "HIGH") {
                return "high";
            }

            return "medium";

        };


    // =====================================================
    // GET SEVERITY ICON
    // =====================================================

    const getSeverityIcon =
        (severity) => {

            if (
                severity === "CRITICAL"
            ) {

                return (
                    <ShieldAlert size={20} />
                );

            }

            if (
                severity === "HIGH"
            ) {

                return (
                    <AlertTriangle size={20} />
                );

            }

            return (
                <AlertCircle size={20} />
            );

        };


    // =====================================================
    // RENDER
    // =====================================================

    return (

        <div className="anomalies-page">


            {/* =============================================
                HEADER
            ============================================= */}

            <div className="anomalies-header">

                <div className="anomalies-header-left">

                    <div className="anomalies-header-icon">

                        <Activity size={28} />

                    </div>


                    <div>

                        <h1>
                            Anomaly Detection
                        </h1>

                        <p>
                            Monitor unusual cloud spending
                            and identify unexpected
                            cost increases.
                        </p>

                    </div>

                </div>


                <button
                    className="anomaly-refresh-button"
                    onClick={loadAnomalies}
                    disabled={loading}
                >

                    <RefreshCw
                        size={17}
                        className={
                            loading
                                ? "spin"
                                : ""
                        }
                    />

                    Refresh

                </button>

            </div>


            {/* =============================================
                ERROR
            ============================================= */}

            {error && (

                <div className="anomaly-error">

                    <AlertTriangle size={20} />

                    {error}

                </div>

            )}


            {/* =============================================
                STATISTICS
            ============================================= */}

            <div className="anomaly-stats-grid">


                <div className="anomaly-stat-card">

                    <div className="anomaly-stat-icon total">

                        <Activity size={22} />

                    </div>


                    <div>

                        <span>
                            Total Anomalies
                        </span>

                        <strong>
                            {anomalies.length}
                        </strong>

                        <small>
                            Detected unusual increases
                        </small>

                    </div>

                </div>


                <div className="anomaly-stat-card">

                    <div className="anomaly-stat-icon critical">

                        <ShieldAlert size={22} />

                    </div>


                    <div>

                        <span>
                            Critical
                        </span>

                        <strong>
                            {criticalCount}
                        </strong>

                        <small>
                            Requires immediate attention
                        </small>

                    </div>

                </div>


                <div className="anomaly-stat-card">

                    <div className="anomaly-stat-icon high">

                        <AlertTriangle size={22} />

                    </div>


                    <div>

                        <span>
                            High Risk
                        </span>

                        <strong>
                            {highCount}
                        </strong>

                        <small>
                            Significant cost increase
                        </small>

                    </div>

                </div>


                <div className="anomaly-stat-card">

                    <div className="anomaly-stat-icon medium">

                        <TrendingUp size={22} />

                    </div>


                    <div>

                        <span>
                            Potential Extra Spend
                        </span>

                        <strong>
                            {formatCurrency(
                                totalIncrease
                            )}
                        </strong>

                        <small>
                            Compared with previous month
                        </small>

                    </div>

                </div>

            </div>


            {/* =============================================
                ANOMALY LIST
            ============================================= */}

            <div className="anomalies-card">


                <div className="anomalies-card-header">

                    <div>

                        <h2>
                            Detected Cost Anomalies
                        </h2>

                        <p>
                            Services with unusual
                            increases in cloud spending.
                        </p>

                    </div>


                    <div className="anomaly-count-badge">

                        {anomalies.length} detected

                    </div>

                </div>


                {/* =========================================
                    LOADING
                ========================================= */}

                {loading ? (

                    <div className="anomalies-loading">

                        <RefreshCw
                            size={25}
                            className="spin"
                        />

                        <span>
                            Analyzing cloud cost data...
                        </span>

                    </div>


                ) : anomalies.length === 0 ? (

                    /* =====================================
                        EMPTY STATE
                    ===================================== */

                    <div className="anomalies-empty">

                        <div className="empty-icon">

                            <Server size={42} />

                        </div>


                        <h3>
                            No anomalies detected
                        </h3>


                        <p>
                            Your cloud spending currently
                            appears to be within the
                            expected range.
                        </p>

                    </div>


                ) : (

                    /* =====================================
                        ANOMALY TABLE
                    ===================================== */

                    <div className="anomaly-table-wrapper">

                        <table className="anomaly-table">

                            <thead>

                                <tr>

                                    <th>
                                        Service
                                    </th>

                                    <th>
                                        Month
                                    </th>

                                    <th>
                                        Previous Cost
                                    </th>

                                    <th>
                                        Current Cost
                                    </th>

                                    <th>
                                        Increase
                                    </th>

                                    <th>
                                        Severity
                                    </th>

                                    <th>
                                        Details
                                    </th>

                                </tr>

                            </thead>


                            <tbody>

                                {anomalies.map(
                                    (
                                        anomaly,
                                        index
                                    ) => (

                                        <tr
                                            key={
                                                `${anomaly.service}-${anomaly.month}-${index}`
                                            }
                                        >

                                            <td>

                                                <div className="service-name">

                                                    <Server
                                                        size={17}
                                                    />

                                                    <span>

                                                        {anomaly.service}

                                                    </span>

                                                </div>

                                            </td>


                                            <td>

                                                {anomaly.month}

                                            </td>


                                            <td>

                                                {formatCurrency(
                                                    anomaly.averageCost
                                                )}

                                            </td>


                                            <td>

                                                <strong>

                                                    {formatCurrency(
                                                        anomaly.actualCost
                                                    )}

                                                </strong>

                                            </td>


                                            <td>

                                                <span className="increase-value">

                                                    <TrendingUp
                                                        size={15}
                                                    />

                                                    {Number(
                                                        anomaly.percentageDeviation
                                                    ).toFixed(
                                                        1
                                                    )}%

                                                </span>

                                            </td>


                                            <td>

                                                <span
                                                    className={`severity-badge ${getSeverityClass(
                                                        anomaly.severity
                                                    )}`}
                                                >

                                                    {getSeverityIcon(
                                                        anomaly.severity
                                                    )}

                                                    {anomaly.severity}

                                                </span>

                                            </td>


                                            <td className="anomaly-message">

                                                {anomaly.message}

                                            </td>

                                        </tr>

                                    )
                                )}

                            </tbody>

                        </table>

                    </div>

                )}

            </div>

        </div>

    );

}


export default Anomalies;

