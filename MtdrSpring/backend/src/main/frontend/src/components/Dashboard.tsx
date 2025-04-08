import { useEffect, useState } from "react"
import { BrowserRouter as Router, Route, Routes, useLocation } from "react-router-dom"
import { Layers, Bike, ListTodo, LineChart, UsersIcon, Bell, Search, Menu, X } from 'lucide-react'
import Sidebar from "./Sidebar"
import Developers from "./Developers"
import Tasks from "./Tasks"
import Sprints from "./Sprints"
import KPIs from "./KPIs"

// Create a wrapper component to access useLocation
const DashboardContent = ({ navItems }: { navItems: typeof DEFAULT_NAV_ITEMS }) => {
  const location = useLocation()
  const [activeNavItem, setActiveNavItem] = useState(() => {
    const path = location.pathname.slice(1)
    return path || "developers"
  })
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false)
  const [searchOpen, setSearchOpen] = useState(false)

  useEffect(() => {
    const path = location.pathname.slice(1)
    if (path && navItems.some((item) => item.key === path)) {
      setActiveNavItem(path)
    }
  }, [location, navItems])

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 shadow-sm h-16 fixed w-full top-0 z-50">
        <div className="flex items-center justify-between h-full px-4 md:px-6">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="p-2 rounded-md text-gray-500 hover:bg-gray-100 md:hidden"
            >
              {isMobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
            <div className="flex items-center gap-2">
              <div className="bg-blue-500 text-white p-2 rounded-md">
                <Layers className="h-5 w-5" />
              </div>
              <span className="text-xl font-semibold text-gray-800 hidden sm:inline-block">TaskFlow</span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {searchOpen ? (
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search..."
                  className="w-full md:w-64 h-9 pl-9 pr-4 rounded-md border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  autoFocus
                  onBlur={() => setSearchOpen(false)}
                />
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
              </div>
            ) : (
              <button
                onClick={() => setSearchOpen(true)}
                className="p-2 rounded-full text-gray-500 hover:bg-gray-100"
              >
                <Search size={18} />
              </button>
            )}
            <button className="p-2 rounded-full text-gray-500 hover:bg-gray-100 relative">
              <Bell size={18} />
              <span className="absolute top-0 right-0 h-2 w-2 rounded-full bg-red-500"></span>
            </button>
            <div className="h-9 w-9 rounded-full bg-blue-100 flex items-center justify-center overflow-hidden border-2 border-white shadow-sm">
              <img
                src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"
                alt="Profile"
                className="w-full h-full object-cover"
              />
            </div>
          </div>
        </div>
      </header>

      {/* Mobile Sidebar */}
      <div
        className={`fixed inset-0 bg-black/50 z-40 md:hidden transition-opacity duration-200 ${
          isMobileMenuOpen ? "opacity-100" : "opacity-0 pointer-events-none"
        }`}
        onClick={() => setIsMobileMenuOpen(false)}
      ></div>

      {/* Main Content */}
      <div className="flex pt-16 flex-grow">
        <div
          className={`fixed md:static inset-y-16 left-0 z-40 md:z-0 w-64 bg-white border-r border-gray-200 shadow-sm transition-transform duration-300 md:translate-x-0 ${
            isMobileMenuOpen ? "translate-x-0" : "-translate-x-full"
          }`}
        >
          <Sidebar
            activeNavItem={activeNavItem}
            setActiveNavItem={(key) => {
              setActiveNavItem(key)
              setIsMobileMenuOpen(false)
            }}
            navItems={navItems}
          />
        </div>
        <div className="flex-grow p-4 md:p-6 w-full md:w-auto">
          <div className="container mx-auto">
            <Routes>
              <Route path="/developers" element={<Developers />} />
              <Route path="/tasks" element={<Tasks />} />
              <Route path="/sprints" element={<Sprints />} />
              <Route path="/kpis" element={<KPIs />} />
            </Routes>
          </div>
        </div>
      </div>
    </div>
  )
}

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
]

const Dashboard = () => {
  return (
    <Router>
      <DashboardContent navItems={DEFAULT_NAV_ITEMS} />
    </Router>
  )
}

export default Dashboard
