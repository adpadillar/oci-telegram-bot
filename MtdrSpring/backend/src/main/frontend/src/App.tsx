import Dashboard from "./components/Dashboard";

import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "./utils/query/query-client";

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Dashboard />
    </QueryClientProvider>
  );
}

export default App;
