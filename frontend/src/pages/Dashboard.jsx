import { useEffect, useState } from "react";

import {
    DollarSign,
    TrendingDown,
    FileText,
    AlertTriangle,
    RefreshCw,
    Server,
    Activity,
} from "lucide-react";

import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    PieChart,
    Pie,
    Cell,
    Legend,
    LineChart,
    Line,
} from "recharts";

import api from "../services/api";

import "../styles/dashboard.css";


function Dashboard() {

    // =====================================================
    // STATE
    // =====================================================

    const [backendStatus, setBackendStatus] =
        useState("Checking...");

    const [backendError, setBackendError] =
        useState(false);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState(null);


    const [summary, setSummary] =
        useState({
            serviceCount: 0,
            highestCostService: null,
            highestServiceCost: 0,
            totalCost: 0,
        });


    const [serviceCosts, setServiceCosts] =
        useState([]);


    const [monthlyCosts, setMonthlyCosts] =
        useState([]);


    // =====================================================
    // LOAD DASHBOARD DATA
    // =====================================================

    const loadDashboardData = async () => {

        try {

            setLoading(true);
            setError(null);


            // =============================================
            // HEALTH
            // =============================================

            try {

                const healthResponse =
                    await api.get("/health");

                console.log(
                    "Health API:",
                    healthResponse.data
                );

                setBackendStatus(
                    healthResponse.data.status
                );

                setBackendError(false);

            } catch (err) {

                console.error(
                    "HEALTH API FAILED:",
                    err
                );

                setBackendStatus("DOWN");
                setBackendError(true);
            }


            // =============================================
            // SUMMARY
            // =============================================

            try {

                const summaryResponse =
                    await api.get("/costs/summary");

                console.log(
                    "Summary API:",
                    summaryResponse.data
                );

                setSummary(
                    summaryResponse.data
                );

            } catch (err) {

                console.error(
                    "SUMMARY API FAILED:",
                    err
                );

            }


            // =============================================
            // COST BY SERVICE
            // =============================================

            try {

                const serviceResponse =
                    await api.get(
                        "/costs/by-service"
                    );

                console.log(
                    "Service API:",
                    serviceResponse.data
                );

                setServiceCosts(
                    serviceResponse.data
                );

            } catch (err) {

                console.error(
                    "SERVICE API FAILED:",
                    err
                );

            }


            // =============================================
            // COST BY MONTH
            // =============================================

            try {

                const monthResponse =
                    await api.get(
                        "/costs/by-month"
                    );

                console.log(
                    "Month API:",
                    monthResponse.data
                );

                setMonthlyCosts(
                    monthResponse.data
                );

            } catch (err) {

                console.error(
                    "MONTH API FAILED:",
                    err
                );

            }

        } catch (err) {

            console.error(
                "Dashboard loading failed:",
                err
            );

            setError(
                "Unable to load dashboard data."
            );

        } finally {

            setLoading(false);

        }
    };


    // =====================================================
    // LOAD DATA ON PAGE LOAD
    // =====================================================

    useEffect(() => {

        loadDashboardData();

    }, []);


    // =====================================================
    // FORMAT CURRENCY
    // =====================================================

    const formatCurrency = (value) => {

        return `₹${Number(
            value || 0
        ).toLocaleString("en-IN")}`;

    };


    // =====================================================
    // CHART DATA
    // =====================================================

    const serviceChartData =
        serviceCosts.map((item) => ({
            service: item.service,
            cost: Number(item.cost || 0),
        }));


    const monthlyChartData =
        monthlyCosts.map((item) => ({
            month: item.month,
            cost: Number(item.cost || 0),
        }));


    // =====================================================
    // PIE CHART COLORS
    // =====================================================

    const pieColors = [
        "#4f46e5",
        "#7c3aed",
        "#06b6d4",
        "#22c55e",
        "#f59e0b",
        "#ef4444",
        "#ec4899",
    ];


    // =====================================================
    // STATS
    // =====================================================

    const stats = [

        {
            title: "Monthly Cloud Cost",

            value:
                formatCurrency(
                    summary.totalCost
                ),

            subtitle:
                "Total recorded spending",

            icon: DollarSign,

            iconClass: "blue",
        },

        {
            title: "Potential Savings",

            value: "₹0",

            subtitle:
                "AI optimization coming soon",

            icon: TrendingDown,

            iconClass: "green",
        },

        {
            title: "Documents",

            value: "0",

            subtitle:
                "Uploaded documents",

            icon: FileText,

            iconClass: "purple",
        },

        {
            title: "Anomalies",

            value: "0",

            subtitle:
                "Detected cost anomalies",

            icon: AlertTriangle,

            iconClass: "red",
        },

    ];


    // =====================================================
    // RENDER
    // =====================================================

    return (

        <div className="dashboard">

            {/* =================================================
                HEADER
            ================================================= */}

            <div className="dashboard-header">

                <div>

                    <h1>
                        Cloud Cost Dashboard
                    </h1>

                    <p>
                        Monitor your cloud spending,
                        analyze infrastructure data,
                        and optimize costs with AI.
                    </p>

                </div>


                <button
                    className="refresh-button"
                    onClick={loadDashboardData}
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


            {/* =================================================
                ERROR
            ================================================= */}

            {error && (

                <div className="dashboard-error">

                    {error}

                </div>

            )}


            {/* =================================================
                STAT CARDS
            ================================================= */}

            <div className="stats-grid">

                {stats.map((stat) => {

                    const Icon =
                        stat.icon;

                    return (

                        <div
                            className="stat-card"
                            key={stat.title}
                        >

                            <div
                                className={`stat-icon ${stat.iconClass}`}
                            >

                                <Icon
                                    size={22}
                                />

                            </div>


                            <div className="stat-info">

                                <span className="stat-title">

                                    {stat.title}

                                </span>


                                <strong className="stat-value">

                                    {stat.value}

                                </strong>


                                <span className="stat-subtitle">

                                    {stat.subtitle}

                                </span>

                            </div>

                        </div>

                    );

                })}

            </div>


            {/* =================================================
                CHART ROW
            ================================================= */}

            <div className="charts-grid">


                {/* =============================================
                    COST BY SERVICE
                ============================================= */}

                <div className="chart-card">

                    <div className="chart-header">

                        <div>

                            <h2>
                                Cost by Service
                            </h2>

                            <p>
                                Cloud spending across
                                your infrastructure.
                            </p>

                        </div>

                        <Server
                            size={21}
                        />

                    </div>


                    {serviceChartData.length > 0 ? (

                        <div className="chart-container">

                            <ResponsiveContainer
                                width="100%"
                                height="100%"
                            >

                                <BarChart
                                    data={
                                        serviceChartData
                                    }
                                    margin={{
                                        top: 20,
                                        right: 10,
                                        left: 5,
                                        bottom: 5,
                                    }}
                                >

                                    <CartesianGrid
                                        strokeDasharray="3 3"
                                        vertical={false}
                                    />

                                    <XAxis
                                        dataKey="service"
                                        tick={{
                                            fontSize: 12,
                                        }}
                                    />

                                    <YAxis
                                        tick={{
                                            fontSize: 12,
                                        }}
                                        tickFormatter={
                                            (value) =>
                                                `₹${(
                                                    value /
                                                    1000
                                                ).toFixed(0)}k`
                                        }
                                    />

                                    <Tooltip
                                        formatter={(
                                            value
                                        ) => [
                                            formatCurrency(
                                                value
                                            ),
                                            "Cost",
                                        ]}
                                    />

                                    <Bar
                                        dataKey="cost"
                                        fill="#4f46e5"
                                        radius={[
                                            6,
                                            6,
                                            0,
                                            0,
                                        ]}
                                    />

                                </BarChart>

                            </ResponsiveContainer>

                        </div>

                    ) : (

                        <div className="empty-chart">

                            No cost data available.

                        </div>

                    )}

                </div>


                {/* =============================================
                    COST DISTRIBUTION
                ============================================= */}

                <div className="chart-card">

                    <div className="chart-header">

                        <div>

                            <h2>
                                Cost Distribution
                            </h2>

                            <p>
                                Spending percentage
                                by service.
                            </p>

                        </div>

                        <DollarSign
                            size={21}
                        />

                    </div>


                    {serviceChartData.length > 0 ? (

                        <div className="chart-container">

                            <ResponsiveContainer
                                width="100%"
                                height="100%"
                            >

                                <PieChart>

                                    <Pie
                                        data={
                                            serviceChartData
                                        }
                                        dataKey="cost"
                                        nameKey="service"
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={65}
                                        outerRadius={105}
                                        paddingAngle={2}
                                        labelLine={false}
                                        label={({
                                            percent,
                                        }) =>
                                            `${(
                                                percent *
                                                100
                                            ).toFixed(
                                                1
                                            )}%`
                                        }
                                    >

                                        {serviceChartData.map(
                                            (
                                                entry,
                                                index
                                            ) => (

                                                <Cell
                                                    key={
                                                        entry.service
                                                    }
                                                    fill={
                                                        pieColors[
                                                            index %
                                                            pieColors.length
                                                        ]
                                                    }
                                                />

                                            )
                                        )}

                                    </Pie>


                                    <Tooltip
                                        formatter={(
                                            value
                                        ) => [
                                            formatCurrency(
                                                value
                                            ),
                                            "Cost",
                                        ]}
                                    />


                                    <Legend
                                        verticalAlign="middle"
                                        align="right"
                                        layout="vertical"
                                    />

                                </PieChart>

                            </ResponsiveContainer>

                        </div>

                    ) : (

                        <div className="empty-chart">

                            No cost data available.

                        </div>

                    )}

                </div>

            </div>


            {/* =================================================
                MONTHLY COST TREND
            ================================================= */}

            <div className="chart-card monthly-chart-card">

                <div className="chart-header">

                    <div>

                        <h2>
                            Monthly Cost Trend
                        </h2>

                        <p>
                            Track cloud spending
                            over time.
                        </p>

                    </div>

                    <Activity
                        size={21}
                    />

                </div>


                {monthlyChartData.length > 0 ? (

                    <div className="monthly-chart-container">

                        <ResponsiveContainer
                            width="100%"
                            height="100%"
                        >

                            <LineChart
                                data={
                                    monthlyChartData
                                }
                                margin={{
                                    top: 20,
                                    right: 20,
                                    left: 10,
                                    bottom: 5,
                                }}
                            >

                                <CartesianGrid
                                    strokeDasharray="3 3"
                                    vertical={false}
                                />

                                <XAxis
                                    dataKey="month"
                                    tick={{
                                        fontSize: 12,
                                    }}
                                />

                                <YAxis
                                    tick={{
                                        fontSize: 12,
                                    }}
                                    tickFormatter={
                                        (value) =>
                                            `₹${(
                                                value /
                                                1000
                                            ).toFixed(0)}k`
                                    }
                                />

                                <Tooltip
                                    formatter={(
                                        value
                                    ) => [
                                        formatCurrency(
                                            value
                                        ),
                                        "Cost",
                                    ]}
                                />

                                <Line
                                    type="monotone"
                                    dataKey="cost"
                                    stroke="#4f46e5"
                                    strokeWidth={3}
                                    dot={{
                                        r: 5,
                                    }}
                                    activeDot={{
                                        r: 7,
                                    }}
                                />

                            </LineChart>

                        </ResponsiveContainer>

                    </div>

                ) : (

                    <div className="empty-chart">

                        No monthly cost data available.

                    </div>

                )}

            </div>


            {/* =================================================
                BOTTOM SUMMARY ROW
            ================================================= */}

            <div className="dashboard-bottom-grid">


                {/* =============================================
                    HIGHEST COST SERVICE
                ============================================= */}

                <div className="bottom-summary-card">

                    <div className="bottom-card-icon">

                        <TrendingDown
                            size={24}
                        />

                    </div>


                    <div className="bottom-card-content">

                        <span className="bottom-card-label">

                            HIGHEST COST SERVICE

                        </span>


                        <h2>

                            {summary?.highestCostService ||
                                "N/A"}

                        </h2>


                        <p>

                            {summary?.highestCostService

                                ? `${summary.highestCostService} is currently your highest cloud expense at ${formatCurrency(
                                      summary.highestServiceCost
                                  )}.`

                                : "No cost data available."
                            }

                        </p>

                    </div>

                </div>


                {/* =============================================
                    SYSTEM STATUS
                ============================================= */}

                <div className="bottom-summary-card system-card">


                    <div className="system-card-header">

                        <div>

                            <span className="bottom-card-label">

                                SYSTEM

                            </span>


                            <h2>

                                System Status

                            </h2>

                        </div>


                        <Activity
                            size={25}
                            className="system-icon"
                        />

                    </div>


                    <div className="system-status-row">

                        <div className="system-status-left">

                            <span
                                className={
                                    backendError
                                        ? "status-dot down"
                                        : "status-dot"
                                }
                            />


                            <span>

                                Spring Boot Backend

                            </span>

                        </div>


                        <strong
                            className={
                                backendError
                                    ? "status-down"
                                    : "status-up"
                            }
                        >

                            {backendStatus}

                        </strong>

                    </div>

                </div>

            </div>

        </div>

    );
}


export default Dashboard;