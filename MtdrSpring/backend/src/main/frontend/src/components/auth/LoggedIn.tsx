import { useQuery } from "@tanstack/react-query";
import { api } from "../../utils/api/client";
import { type PropsWithChildren } from "react";

export const LoggedIn = ({ children }: PropsWithChildren) => {
  const { data: isAuthenticated } = useQuery({
    queryKey: ["auth"],
    queryFn: () => api.auth.check(),
  });

  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
};
