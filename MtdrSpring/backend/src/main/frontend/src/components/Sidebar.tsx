import React from "react";
import { Link } from "react-router-dom";
import { ChevronRight } from 'lucide-react';

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
    <div className="w-64 bg-white border-r border-gray-200 shadow-sm flex flex-col h-full">
      <div className="p-4 border-b border-gray-100">
        <h2 className="text-lg font-medium text-gray-800">Dashboard</h2>
      </div>
      <nav className="flex-grow p-3">
        {navItems.map((item) => (
          <Link
            key={item.key}
            to={`/${item.key}`}
            onClick={() => setActiveNavItem(item.key)}
            className={`
              group relative w-full flex items-center space-x-3 p-3 rounded-lg mb-1 transition-all duration-200
              ${
                activeNavItem === item.key
                  ? "bg-blue-50 text-blue-600"
                  : "text-gray-700 hover:bg-gray-50"
              }
            `}
          >
            <div 
              className={`
                flex items-center justify-center w-8 h-8 rounded-md
                ${activeNavItem === item.key ? "bg-blue-500 text-white" : "bg-gray-100 text-gray-600 group-hover:bg-gray-200"}
              `}
            >
              {item.icon}
            </div>
            <span className="font-medium">{item.label}</span>
            {activeNavItem === item.key && (
              <ChevronRight className="w-4 h-4 ml-auto text-blue-500" />
            )}
            {activeNavItem === item.key && (
              <div className="absolute left-0 top-0 bottom-0 w-1 bg-blue-500 rounded-r-md"></div>
            )}
          </Link>
        ))}
      </nav>
    </div>
  );
};

export default Sidebar;
