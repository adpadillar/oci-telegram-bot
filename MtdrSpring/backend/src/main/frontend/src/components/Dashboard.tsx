import { useEffect, useState } from "react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import {
  Users as UsersIcon,
  ListTodo,
  Bell,
  Layers,
  LineChart,
  Bike,
} from "lucide-react";
import Sidebar from "./Sidebar";
import Developers from "./Developers";
import Tasks from "./Tasks";
import Sprints from "./Sprints";

// Create a wrapper component to access useLocation
const DashboardContent = ({
  navItems,
}: {
  navItems: typeof DEFAULT_NAV_ITEMS;
}) => {
  const location = useLocation();
  const [activeNavItem, setActiveNavItem] = useState(() => {
    // Get the current path without the leading slash
    const path = location.pathname.slice(1);
    // If path is empty, return default, otherwise return current path
    return path || "developers";
  });

  // Keep activeNavItem in sync with route changes
  useEffect(() => {
    const path = location.pathname.slice(1);
    if (path && navItems.some((item) => item.key === path)) {
      setActiveNavItem(path);
    }
  }, [location, navItems]);

  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <header className="bg-white border-b shadow-sm h-16 fixed w-full top-0 z-50">
        <div className="flex items-center justify-between h-full px-6">
          <div className="flex items-center">
            {/* Replace with your actual logo */}
            <div className="flex items-center space-x-2">
              <Layers className="h-8 w-8 text-blue-500" />
              <span className="text-xl font-semibold">TaskFlow</span>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            {/* Notifications */}
            <button className="p-2 hover:bg-gray-100 rounded-full relative">
              <Bell size={20} />
              <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>
            {/* Profile Picture */}
            <button className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center overflow-hidden">
              <img
                src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"
                alt="Profile"
                className="w-full h-full object-cover"
              />
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex pt-16 flex-grow">
        <Sidebar
          activeNavItem={activeNavItem}
          setActiveNavItem={setActiveNavItem}
          navItems={navItems}
        />
        <div className="flex-grow p-6">
          <div className="container mx-auto">
            <div className="bg-white shadow-md rounded-lg">
              <Routes>
                <Route path="/developers" element={<Developers />} />
                <Route path="/tasks" element={<Tasks />} />
                <Route path="/sprints" element={<Sprints />} />
                {/* <Route path="/kpis" element={<KPIs />} /> */}
              </Routes>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Define nav items outside the component to avoid recreating on each render
const DEFAULT_NAV_ITEMS = [
  {
    key: "developers",
    label: "Developers",
    icon: <UsersIcon size={20} />,
  },
  {
    key: "tasks",
    label: "Tasks",
    icon: <ListTodo size={20} />,
  },
  {
    key: "sprints",
    label: "Sprints",
    icon: <Bike size={20} />,
  },
  {
    key: "kpis",
    label: "KPIs",
    icon: <LineChart size={20} />,
  },
];

const Dashboard = () => {
  return (
    <Router>
      <DashboardContent navItems={DEFAULT_NAV_ITEMS} />
    </Router>
  );
};

export default Dashboard;
