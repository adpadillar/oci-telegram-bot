import { LoggedOut } from "./LoggedOut";
import { Layers } from "lucide-react";
import { useState } from "react";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { api } from "../../utils/api/client";

export const LoginPage = () => {
  const [step, setStep] = useState<"request" | "validate">("request");
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [isMasterCode, setIsMasterCode] = useState(false);
  const queryClient = useQueryClient();

  const requestCodeMutation = useMutation({
    mutationFn: () => api.auth.requestCode(),
    onSuccess: () => {
      setStep("validate");
      setError("");
    },
    onError: () => {
      setError("Failed to request code. Please try again.");
    },
  });

  const validateCodeMutation = useMutation({
    mutationFn: (code: string) => api.auth.validateCode(code),
    onSuccess: () => {
      // Invalidate the auth query to trigger a re-fetch
      queryClient.invalidateQueries({ queryKey: ["auth"] });
      setError("");
    },
    onError: () => {
      setError("Invalid code. Please try again.");
    },
  });

  const handleRequestCode = (e: React.FormEvent) => {
    e.preventDefault();
    requestCodeMutation.mutate();
  };

  const handleValidateCode = (e: React.FormEvent) => {
    e.preventDefault();
    if (code.trim()) {
      validateCodeMutation.mutate(code);
    }
  };

  const handleBack = () => {
    setStep("request");
    setCode("");
    setError("");
    setIsMasterCode(false);
  };

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
              {step === "request"
                ? "Please ask your manager for an access code!"
                : isMasterCode
                  ? "Enter the master code"
                  : "Enter the code sent to your project manager"}
            </p>
          </div>

          {error && (
            <div
              className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative"
              role="alert"
            >
              <span className="block sm:inline">{error}</span>
            </div>
          )}

          {step === "request" ? (
            <form onSubmit={handleRequestCode} className="mt-8 space-y-6">
              <button
                type="submit"
                disabled={requestCodeMutation.isPending}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-blue-300"
              >
                {requestCodeMutation.isPending
                  ? "Requesting..."
                  : "Request Code"}
              </button>
              <div className="text-center">
                <button
                  type="button"
                  onClick={() => {
                    setStep("validate");
                    setIsMasterCode(true);
                  }}
                  className="text-sm text-blue-600 hover:text-blue-500"
                >
                  Have a master code?
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleValidateCode} className="mt-8 space-y-6">
              <div>
                <label htmlFor="code" className="sr-only">
                  {isMasterCode ? "Master Code" : "Code"}
                </label>
                <input
                  id="code"
                  name="code"
                  type="text"
                  required
                  className="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                  placeholder={
                    isMasterCode
                      ? "Enter master code"
                      : "Enter your 6-digit code"
                  }
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  maxLength={isMasterCode ? undefined : 6}
                  pattern={isMasterCode ? undefined : "[0-9]{6}"}
                />
              </div>

              <div className="flex gap-4">
                <button
                  type="button"
                  onClick={handleBack}
                  className="flex-1 py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  Back
                </button>
                <button
                  type="submit"
                  disabled={validateCodeMutation.isPending || !code.trim()}
                  className="flex-1 py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-blue-300"
                >
                  {validateCodeMutation.isPending
                    ? "Validating..."
                    : "Validate Code"}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </LoggedOut>
  );
};
