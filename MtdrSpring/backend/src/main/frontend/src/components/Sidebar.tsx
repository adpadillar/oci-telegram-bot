import React from "react";
import { Link } from "react-router-dom";

interface SidebarProps {
  activeNavItem: string;
  setActiveNavItem: (key: string) => void;
  navItems: { key: string; icon: React.ReactNode; label: string }[];
}

const Sidebar: React.FC<SidebarProps> = ({
  activeNavItem,
  setActiveNavItem,
  navItems,
}) => {
  return (
    <div className="w-64 bg-white border-r shadow-md flex flex-col">
      <nav className="flex-grow p-4">
        {navItems.map((item) => (
          <Link
            key={item.key}
            to={`/${item.key}`}
            onClick={() => setActiveNavItem(item.key)}
            className={`
              w-full flex items-center space-x-3 p-3 rounded-md mb-2
              ${
                activeNavItem === item.key
                  ? "bg-blue-500 text-white"
                  : "text-gray-600 hover:bg-gray-100"
              }
            `}
          >
            {item.icon}
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>
    </div>
  );
};

export default Sidebar;
