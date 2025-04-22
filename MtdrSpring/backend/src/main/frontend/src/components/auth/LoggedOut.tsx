import { useQuery } from "@tanstack/react-query";
import { api } from "../../utils/api/client";
import { type PropsWithChildren } from "react";
import { LoadingScreen } from "./LoadingScreen";

export const LoggedOut = ({ children }: PropsWithChildren) => {
  const { data: isAuthenticated, isLoading } = useQuery({
    queryKey: ["auth"],
    queryFn: () => api.auth.check(),
  });

  if (isLoading) {
    return <LoadingScreen />;
  }

  if (isAuthenticated) {
    return null;
  }

  return <>{children}</>;
};
