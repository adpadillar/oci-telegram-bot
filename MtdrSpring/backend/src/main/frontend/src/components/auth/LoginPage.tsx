import { LoggedOut } from "./LoggedOut";
import { Layers } from "lucide-react";

export const LoginPage = () => {
  return (
    <LoggedOut>
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
        <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-xl shadow-lg">
          <div className="text-center">
            <div className="mx-auto h-12 w-12 bg-blue-500 text-white rounded-lg flex items-center justify-center">
              <Layers className="h-8 w-8" />
            </div>
            <h2 className="mt-6 text-3xl font-bold text-gray-900">TaskFlow</h2>
            <p className="mt-2 text-sm text-gray-600">
              Project Manager Sign In
            </p>
          </div>
          <form className="mt-8 space-y-6">
            <div className="rounded-md shadow-sm -space-y-px">
              <div>
                <label htmlFor="project-identifier" className="sr-only">
                  Project Identifier
                </label>
                <input
                  id="project-identifier"
                  name="project-identifier"
                  type="text"
                  autoComplete="off"
                  required
                  className="appearance-none  relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                  placeholder="Project Identifier"
                />
              </div>
            </div>

            <div>
              <button
                type="submit"
                className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Sign in
              </button>
            </div>
          </form>
        </div>
      </div>
    </LoggedOut>
  );
};
