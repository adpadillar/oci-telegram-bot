import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { User, Plus, ListTodo, ChevronRight } from "lucide-react";
// import useApi from "../../utils/useAPI";

const Developers = () => {
  const navigate = useNavigate();
  // const api = useApi("http://localhost:8000");

  const [developers] = useState([
    {
      id: 1,
      name: "John Smith",
      role: "Frontend Developer",
      profilePic: "https://api.dicebear.com/7.x/avataaars/svg?seed=John",
      pendingTasks: 5,
      email: "john.smith@company.com",
      skills: ["React", "TypeScript", "Tailwind"],
      yearsOfExperience: 3,
    },
    {
      id: 2,
      name: "Sarah Johnson",
      role: "Backend Developer",
      profilePic: "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah",
      pendingTasks: 3,
      email: "sarah.j@company.com",
      skills: ["Node.js", "Python", "MongoDB"],
      yearsOfExperience: 5,
    },
    {
      id: 3,
      name: "Mike Chen",
      role: "Full Stack Developer",
      profilePic: "https://api.dicebear.com/7.x/avataaars/svg?seed=Mike",
      pendingTasks: 7,
      email: "mike.chen@company.com",
      skills: ["React", "Node.js", "PostgreSQL"],
      yearsOfExperience: 4,
    },
  ]);

  const [selectedDeveloper, setSelectedDeveloper] = useState<
    (typeof developers)[0] | null
  >(null);
  const [showAddModal, setShowAddModal] = useState(false);

  const AddDeveloperModal = ({ onClose }: { onClose: () => void }) => {
    const [formData, setFormData] = useState({
      name: "",
      email: "",
      role: "",
    });

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      // API call would go here
      console.log("New developer data:", formData);
      onClose();
    };

    return (
      <div className="fixed inset-0 bg-black/50 bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">
              Add New Developer
            </h2>
            <button
              onClick={onClose}
              className="text-gray-600 hover:text-gray-900"
            >
              ✕
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Form fields */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Name
              </label>
              <input
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Email
              </label>
              <input
                type="email"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.email}
                onChange={(e) =>
                  setFormData({ ...formData, email: e.target.value })
                }
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">
                Role
              </label>
              <input
                type="text"
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={formData.role}
                onChange={(e) =>
                  setFormData({ ...formData, role: e.target.value })
                }
                required
              />
            </div>
            <div className="flex space-x-3 pt-4">
              <button
                type="submit"
                className="flex-1 bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
              >
                Add Developer
              </button>
              <button
                type="button"
                onClick={onClose}
                className="flex-1 bg-gray-200 text-gray-800 py-2 rounded-md hover:bg-gray-300"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  const ViewDeveloperDetails = ({
    developer,
    onClose,
  }: {
    developer: (typeof developers)[0];
    onClose: () => void;
  }) => {
    return (
      <div className="fixed inset-0 bg-black/50 bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">
              Developer Details
            </h2>
            <button
              onClick={onClose}
              className="text-gray-600 hover:text-gray-900"
            >
              ✕
            </button>
          </div>

          <div className="space-y-4">
            <div className="flex items-center space-x-4">
              <img
                src={developer.profilePic}
                alt={developer.name}
                className="w-16 h-16 rounded-full"
              />
              <div>
                <h3 className="text-xl font-semibold">{developer.name}</h3>
                <p className="text-gray-600">{developer.role}</p>
              </div>
            </div>

            <div className="space-y-2">
              <div>
                <p className="text-sm text-gray-500">Email</p>
                <p className="font-medium">{developer.email}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Skills</p>
                <div className="flex flex-wrap gap-2 mt-1">
                  {developer.skills.map((skill) => (
                    <span
                      key={skill}
                      className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-sm"
                    >
                      {skill}
                    </span>
                  ))}
                </div>
              </div>
              <div>
                <p className="text-sm text-gray-500">Experience</p>
                <p className="font-medium">
                  {developer.yearsOfExperience} years
                </p>
              </div>
            </div>

            <div className="flex space-x-2">
              <button
                onClick={() => navigate(`/tasks?developer=${developer.id}`)}
                className="flex-1 bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 flex items-center justify-center space-x-2"
              >
                <ListTodo size={18} />
                <span>View Tasks</span>
                <span className="bg-white text-blue-500 rounded-full px-2 ml-2">
                  {developer.pendingTasks}
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="p-6 bg-gray-50">
      {selectedDeveloper && (
        <ViewDeveloperDetails
          developer={selectedDeveloper}
          onClose={() => setSelectedDeveloper(null)}
        />
      )}
      {showAddModal && (
        <AddDeveloperModal onClose={() => setShowAddModal(false)} />
      )}

      <div className="bg-white shadow-md rounded-lg">
        <div className="flex justify-between items-center p-6 border-b">
          <h1 className="text-2xl font-semibold text-gray-800">Developers</h1>
          <button
            onClick={() => setShowAddModal(true)}
            className="bg-blue-500 text-white px-4 py-2 rounded-md flex items-center space-x-2 hover:bg-blue-600"
          >
            <Plus size={20} />
            <span>Add Developer</span>
          </button>
        </div>

        <div className="p-6">
          <div className="space-y-4">
            {developers.map((developer) => (
              <div
                key={developer.id}
                className="flex justify-between items-center bg-gray-50 p-4 rounded-lg shadow-sm hover:bg-gray-100 transition"
              >
                <div className="flex items-center space-x-4">
                  <img
                    src={developer.profilePic}
                    alt={developer.name}
                    className="w-12 h-12 rounded-full"
                  />
                  <div>
                    <h3 className="text-lg font-semibold text-gray-800">
                      {developer.name}
                    </h3>
                    <div className="flex items-center space-x-2">
                      <p className="text-gray-600">{developer.role}</p>
                      <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                        {developer.pendingTasks} pending tasks
                      </span>
                    </div>
                  </div>
                </div>
                <div className="flex items-center space-x-4">
                  <button
                    onClick={() => setSelectedDeveloper(developer)}
                    className="text-gray-600 hover:text-gray-800 flex items-center space-x-1"
                  >
                    <User size={18} />
                    <span>Details</span>
                  </button>
                  <button
                    onClick={() => navigate(`/tasks?developer=${developer.id}`)}
                    className="text-blue-500 hover:text-blue-700 flex items-center space-x-1"
                  >
                    <ListTodo size={18} />
                    <span>Tasks</span>
                    <ChevronRight size={18} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Developers;
