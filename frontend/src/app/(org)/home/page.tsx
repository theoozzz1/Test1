"use client";

import { useAuth } from "@/store/auth.store";
import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function OrgHomePage() {
  const { user } = useAuth();
  const router = useRouter();

  if (!user || user.role !== "ORG_ADMIN") {
    return (
      <div className="page">
        <div className="card text-center py-16">
          <h1 className="text-4xl font-bold mb-4">Home</h1>
          <p className="text-muted">Please log in as an organisation to view your home page.</p>
        </div>
      </div>
    );
  }
  useEffect(() => {
    router.replace("/");
  }, [router]);

  return null;
}
