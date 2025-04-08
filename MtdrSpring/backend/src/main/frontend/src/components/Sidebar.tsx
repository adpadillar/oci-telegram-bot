import React from "react";
import { Link } from "react-router-dom";
import { ChevronRight, X } from 'lucide-react';

interface SidebarProps {
  activeNavItem: string;
  setActiveNavItem: (key: string) => void;
  navItems: { key: string; icon: React.ReactNode; label: string }[];
  isMobileMenuOpen: boolean;
  setIsMobileMenuOpen: (isOpen: boolean) => void;
}

const Sidebar: React.FC<SidebarProps> = ({
  activeNavItem,
  setActiveNavItem,
  navItems,
  isMobileMenuOpen,
  setIsMobileMenuOpen,
}) => {
  return (
    <div 
      className={`
        fixed md:static inset-y-0 left-0 z-40
        w-64 bg-white border-r border-gray-200 shadow-sm
        transform transition-transform duration-300 ease-in-out
        ${isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}
      `}
    >
      <div className="flex items-center justify-between p-4 border-b border-gray-100">
        <h2 className="text-lg font-medium text-gray-800">Dashboard</h2>
        <button
          onClick={() => setIsMobileMenuOpen(false)}
          className="md:hidden p-2 rounded-md hover:bg-gray-100"
          aria-label="Close menu"
        >
          <X size={20} />
        </button>
      </div>
      <nav className="flex-grow p-3 overflow-y-auto h-[calc(100vh-4rem)]">
        {navItems.map((item) => (
          <Link
            key={item.key}
            to={`/${item.key}`}
            onClick={() => {
              setActiveNavItem(item.key);
              setIsMobileMenuOpen(false);
            }}
            className={`
              group relative w-full flex items-center space-x-3 p-3 rounded-lg mb-1
              transition-all duration-200
              ${activeNavItem === item.key
                ? "bg-blue-50 text-blue-600"
                : "text-gray-700 hover:bg-gray-50"
              }
            `}
          >
            <div 
              className={`
                flex items-center justify-center w-8 h-8 rounded-md
                ${activeNavItem === item.key
                  ? "bg-blue-500 text-white"
                  : "bg-gray-100 text-gray-600 group-hover:bg-gray-200"
                }
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
