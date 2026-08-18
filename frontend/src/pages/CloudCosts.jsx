import { useEffect, useState } from "react";
import {
    DollarSign,
    Server,
    Calendar,
    RefreshCw,
    TrendingUp,
    Database,
} from "lucide-react";

import {
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
} from "recharts";

import api from "../services/api";
import "../pages/CloudCosts.css"

function CloudCosts() {

    const [costs, setCosts] = useState([]);
    const [serviceCosts, setServiceCosts] = useState([]);
    const [monthlyCosts, setMonthlyCosts] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // ==========================================
    // LOAD DATA
    // ==========================================

    const loadCosts = async () => {

        try {

            setLoading(true);
            setError("");

            const [
                costsResponse,
                serviceResponse,
                monthResponse
            ] = await Promise.all([
                api.get("/costs"),
                api.get("/costs/by-service"),
                api.get("/costs/by-month")
            ]);

            console.log("Cloud Costs:", costsResponse.data);
            console.log("Service Costs:", serviceResponse.data);
            console.log("Monthly Costs:", monthResponse.data);

            setCosts(
                Array.isArray(costsResponse.data)
                    ? costsResponse.data
                    : []
            );

            setServiceCosts(
                Array.isArray(serviceResponse.data)
                    ? serviceResponse.data
                    : []
            );

            setMonthlyCosts(
                Array.isArray(monthResponse.data)
                    ? monthResponse.data
                    : []
            );

        } catch (err) {

            console.error(
                "Cloud Costs loading failed:",
                err
            );

            console.error(
                "Response:",
                err.response?.data
            );

            setError(
                err.response?.data?.error ||
                "Unable to load cloud cost data."
            );

        } finally {

            setLoading(false);

        }
    };


    // ==========================================
    // LOAD WHEN PAGE OPENS
    // ==========================================

    useEffect(() => {

        loadCosts();

    }, []);


    // ==========================================
    // FORMAT CURRENCY
    // ==========================================

    const formatCurrency = (value) => {

        return `₹${Number(value || 0).toLocaleString("en-IN")}`;

    };


    // ==========================================
    // TOTAL COST
    // ==========================================

    const totalCost = costs.reduce(
        (total, item) =>
            total + Number(item.cost || 0),
        0
    );


    // ==========================================
    // SERVICE CHART DATA
    // ==========================================

    const serviceChartData =
        serviceCosts.map((item) => ({
            service: item.service,
            cost: Number(item.cost || 0)
        }));


    // ==========================================
    // MONTHLY CHART DATA
    // ==========================================

    const monthlyChartData =
        monthlyCosts.map((item) => ({
            month: item.month,
            cost: Number(item.cost || 0)
        }));


    // ==========================================
    // LOADING
    // ==========================================

    if (loading) {

        return (

            <div className="cloud-costs-page">

                <div className="cloud-costs-loading">

                    <RefreshCw
                        size={28}
                        className="cloud-costs-spin"
                    />

                    <h2>
                        Loading cloud costs...
                    </h2>

                    <p>
                        Fetching your cloud spending data.
                    </p>

                </div>

            </div>

        );

    }


    // ==========================================
    // ERROR
    // ==========================================

    if (error) {

        return (

            <div className="cloud-costs-page">

                <div className="cloud-costs-header">

                    <div>

                        <h1>
                            Cloud Costs
                        </h1>

                        <p>
                            Analyze your cloud spending
                            across services and months.
                        </p>

                    </div>

                    <button
                        className="cloud-refresh-button"
                        onClick={loadCosts}
                    >

                        <RefreshCw size={17} />

                        Retry

                    </button>

                </div>


                <div className="cloud-costs-error">

                    <div className="error-icon">

                        <Database size={26} />

                    </div>

                    <div>

                        <h2>
                            Unable to load cloud costs
                        </h2>

                        <p>
                            {error}
                        </p>

                    </div>

                </div>

            </div>

        );

    }


    return (

        <div className="cloud-costs-page">

            {/* ==========================================
                HEADER
            ========================================== */}

            <div className="cloud-costs-header">

                <div>

                    <h1>
                        Cloud Costs
                    </h1>

                    <p>
                        Analyze your cloud spending
                        across services and months.
                    </p>

                </div>


                <button
                    className="cloud-refresh-button"
                    onClick={loadCosts}
                >

                    <RefreshCw size={17} />

                    Refresh

                </button>

            </div>


            {/* ==========================================
                SUMMARY CARDS
            ========================================== */}

            <div className="cloud-summary-grid">

                <div className="cloud-summary-card">

                    <div className="cloud-summary-icon blue">

                        <DollarSign size={23} />

                    </div>

                    <div>

                        <span>
                            TOTAL CLOUD COST
                        </span>

                        <strong>
                            {formatCurrency(totalCost)}
                        </strong>

                        <p>
                            All recorded spending
                        </p>

                    </div>

                </div>


                <div className="cloud-summary-card">

                    <div className="cloud-summary-icon purple">

                        <Server size={23} />

                    </div>

                    <div>

                        <span>
                            SERVICES
                        </span>

                        <strong>
                            {serviceChartData.length}
                        </strong>

                        <p>
                            Cloud services tracked
                        </p>

                    </div>

                </div>


                <div className="cloud-summary-card">

                    <div className="cloud-summary-icon green">

                        <Calendar size={23} />

                    </div>

                    <div>

                        <span>
                            MONTHS
                        </span>

                        <strong>
                            {monthlyChartData.length}
                        </strong>

                        <p>
                            Months of cost data
                        </p>

                    </div>

                </div>


                <div className="cloud-summary-card">

                    <div className="cloud-summary-icon orange">

                        <TrendingUp size={23} />

                    </div>

                    <div>

                        <span>
                            RECORDS
                        </span>

                        <strong>
                            {costs.length}
                        </strong>

                        <p>
                            Cost records imported
                        </p>

                    </div>

                </div>

            </div>


            {/* ==========================================
                CHARTS
            ========================================== */}

            <div className="cloud-charts-grid">

                {/* ======================================
                    COST BY SERVICE
                ====================================== */}

                <div className="cloud-chart-card">

                    <div className="cloud-chart-header">

                        <div>

                            <h2>
                                Cost by Service
                            </h2>

                            <p>
                                Spending across cloud services.
                            </p>

                        </div>

                        <Server size={21} />

                    </div>


                    {serviceChartData.length > 0 ? (

                        <div className="cloud-chart-container">

                            <ResponsiveContainer
                                width="100%"
                                height="100%"
                            >

                                <BarChart
                                    data={serviceChartData}
                                    margin={{
                                        top: 15,
                                        right: 20,
                                        left: 5,
                                        bottom: 5
                                    }}
                                >

                                    <CartesianGrid
                                        strokeDasharray="3 3"
                                        vertical={false}
                                    />

                                    <XAxis
                                        dataKey="service"
                                        tick={{
                                            fontSize: 12
                                        }}
                                    />

                                    <YAxis
                                        tick={{
                                            fontSize: 12
                                        }}
                                        tickFormatter={(value) =>
                                            `₹${(
                                                Number(value) / 1000
                                            ).toFixed(0)}k`
                                        }
                                    />

                                    <Tooltip
                                        formatter={(value) => [
                                            formatCurrency(value),
                                            "Cost"
                                        ]}
                                    />

                                    <Bar
                                        dataKey="cost"
                                        fill="#4f46e5"
                                        radius={[
                                            7,
                                            7,
                                            0,
                                            0
                                        ]}
                                    />

                                </BarChart>

                            </ResponsiveContainer>

                        </div>

                    ) : (

                        <div className="cloud-empty">

                            <Server size={32} />

                            <p>
                                No service cost data available.
                            </p>

                        </div>

                    )}

                </div>


                {/* ======================================
                    MONTHLY COST
                ====================================== */}

                <div className="cloud-chart-card">

                    <div className="cloud-chart-header">

                        <div>

                            <h2>
                                Monthly Spending
                            </h2>

                            <p>
                                Cloud spending by month.
                            </p>

                        </div>

                        <Calendar size={21} />

                    </div>


                    {monthlyChartData.length > 0 ? (

                        <div className="cloud-chart-container">

                            <ResponsiveContainer
                                width="100%"
                                height="100%"
                            >

                                <BarChart
                                    data={monthlyChartData}
                                    margin={{
                                        top: 15,
                                        right: 20,
                                        left: 5,
                                        bottom: 5
                                    }}
                                >

                                    <CartesianGrid
                                        strokeDasharray="3 3"
                                        vertical={false}
                                    />

                                    <XAxis
                                        dataKey="month"
                                        tick={{
                                            fontSize: 12
                                        }}
                                    />

                                    <YAxis
                                        tick={{
                                            fontSize: 12
                                        }}
                                        tickFormatter={(value) =>
                                            `₹${(
                                                Number(value) / 1000
                                            ).toFixed(0)}k`
                                        }
                                    />

                                    <Tooltip
                                        formatter={(value) => [
                                            formatCurrency(value),
                                            "Cost"
                                        ]}
                                    />

                                    <Bar
                                        dataKey="cost"
                                        fill="#7c3aed"
                                        radius={[
                                            7,
                                            7,
                                            0,
                                            0
                                        ]}
                                    />

                                </BarChart>

                            </ResponsiveContainer>

                        </div>

                    ) : (

                        <div className="cloud-empty">

                            <Calendar size={32} />

                            <p>
                                No monthly cost data available.
                            </p>

                        </div>

                    )}

                </div>

            </div>


            {/* ==========================================
                COST TABLE
            ========================================== */}

            <div className="cloud-table-card">

                <div className="cloud-table-header">

                    <div>

                        <h2>
                            Cloud Cost Records
                        </h2>

                        <p>
                            Detailed breakdown of imported
                            cloud cost data.
                        </p>

                    </div>

                    <Database size={21} />

                </div>


                {costs.length > 0 ? (

                    <div className="cloud-table-wrapper">

                        <table>

                            <thead>

                                <tr>

                                    <th>
                                        Service
                                    </th>

                                    <th>
                                        Month
                                    </th>

                                    <th>
                                        Cost
                                    </th>

                                </tr>

                            </thead>


                            <tbody>

                                {costs.map((item, index) => (

                                    <tr key={item.id || index}>

                                        <td>

                                            <div className="service-cell">

                                                <div className="service-mini-icon">

                                                    <Server size={15} />

                                                </div>

                                                {item.service}

                                            </div>

                                        </td>


                                        <td>

                                            <span className="month-badge">

                                                {item.month}

                                            </span>

                                        </td>


                                        <td className="cost-cell">

                                            {formatCurrency(
                                                item.cost
                                            )}

                                        </td>

                                    </tr>

                                ))}

                            </tbody>

                        </table>

                    </div>

                ) : (

                    <div className="cloud-empty table-empty">

                        <Database size={34} />

                        <h3>
                            No cloud cost records
                        </h3>

                        <p>
                            Upload a CSV from the Documents
                            page to populate this section.
                        </p>

                    </div>

                )}

            </div>

        </div>

    );
}

export default CloudCosts;