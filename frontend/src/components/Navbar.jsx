import { Cloud, UserCircle } from "lucide-react";

function Navbar() {
    return (
        <header className="navbar">
            <div className="navbar-brand">
                <Cloud size={26} />
                <span>AI Cloud Cost Optimizer</span>
            </div>

            <div className="navbar-user">
                <UserCircle size={22} />
                <span>Developer</span>
            </div>
        </header>
    );
}

export default Navbar;