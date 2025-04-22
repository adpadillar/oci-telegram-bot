import { Layers } from "lucide-react";

export const LoadingScreen = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="flex flex-col items-center gap-4">
        <div className="animate-bounce">
          <div className="bg-blue-500 text-white p-4 rounded-xl">
            <Layers className="h-8 w-8" />
          </div>
        </div>
        <h2 className="text-xl font-semibold text-gray-700">Loading...</h2>
      </div>
    </div>
  );
};
