import { useEffect, useState } from "react";

import {
    DollarSign,
    TrendingDown,
    FileText,
    AlertTriangle,
} from "lucide-react";

import api from "../services/api";

function Dashboard() {

    const [backendStatus, setBackendStatus] =
        useState("Checking...");

    const [backendError, setBackendError] =
        useState(false);

    useEffect(() => {

        api.get("/health")

            .then((response) => {

                setBackendStatus(
                    response.data.status
                );

            })

            .catch((error) => {

                console.error(
                    "Backend connection failed:",
                    error
                );

                setBackendStatus("DOWN");

                setBackendError(true);
            });

    }, []);

    const stats = [
        {
            title: "Monthly Cloud Cost",
            value: "₹0",
            icon: DollarSign,
        },
        {
            title: "Potential Savings",
            value: "₹0",
            icon: TrendingDown,
        },
        {
            title: "Documents",
            value: "0",
            icon: FileText,
        },
        {
            title: "Anomalies",
            value: "0",
            icon: AlertTriangle,
        },
    ];

    return (

        <div className="dashboard">

            <div className="dashboard-header">

                <h1>
                    Cloud Cost Dashboard
                </h1>

                <p>
                    Monitor your cloud spending,
                    analyze infrastructure data,
                    and optimize costs with AI.
                </p>

            </div>


            <div className="stats-grid">

                {stats.map((stat) => {

                    const Icon = stat.icon;

                    return (

                        <div
                            className="stat-card"
                            key={stat.title}
                        >

                            <div className="stat-card-header">

                                <span className="stat-card-title">
                                    {stat.title}
                                </span>

                                <Icon size={20} />

                            </div>

                            <div className="stat-card-value">
                                {stat.value}
                            </div>

                        </div>

                    );

                })}

            </div>


            <div className="status-card">

                <h2>
                    System Status
                </h2>

                <div className="backend-status">

                    <span
                        className={
                            backendError
                                ? "status-dot down"
                                : "status-dot"
                        }
                    />

                    <span>
                        Spring Boot Backend:
                    </span>

                    <strong>
                        {backendStatus}
                    </strong>

                </div>

            </div>

        </div>

    );
}

export default Dashboard;