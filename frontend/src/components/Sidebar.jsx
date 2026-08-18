import { NavLink } from "react-router-dom";

import {
    LayoutDashboard,
    FileText,
    MessageSquare,
    DollarSign,
    AlertTriangle,
} from "lucide-react";


const navigationItems = [

    {
        name: "Dashboard",
        path: "/",
        icon: LayoutDashboard,
    },

    {
        name: "Documents",
        path: "/documents",
        icon: FileText,
    },

    {
        name: "RAG Chat",
        path: "/chat",
        icon: MessageSquare,
    },

    {
        name: "Cloud Costs",
        path: "/cloud-costs",
        icon: DollarSign,
    },

    {
        name: "Anomalies",
        path: "/anomalies",
        icon: AlertTriangle,
    },

];


function Sidebar() {

    return (

        <aside className="sidebar">

            {/* =========================================
                SIDEBAR HEADER
            ========================================= */}

            <div className="sidebar-header">

                <span>
                    Cloud Intelligence
                </span>

            </div>


            {/* =========================================
                NAVIGATION
            ========================================= */}

            <nav className="sidebar-navigation">

                {navigationItems.map((item) => {

                    const Icon = item.icon;

                    return (

                        <NavLink
                            key={item.path}
                            to={item.path}

                            end={item.path === "/"}

                            className={({ isActive }) =>
                                `sidebar-link ${
                                    isActive
                                        ? "active"
                                        : ""
                                }`
                            }
                        >

                            <Icon size={19} />

                            <span>
                                {item.name}
                            </span>

                        </NavLink>

                    );

                })}

            </nav>

        </aside>

    );

}


export default Sidebar;