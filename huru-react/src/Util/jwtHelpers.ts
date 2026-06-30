import { jwtDecode } from "jwt-decode";


interface JwtPayload {
  role?: string;
  sub?: string;
  // add other properties your token might have
}

export const getRoleFromToken = (token: string): string | null => {
  try {
    const decoded = jwtDecode<JwtPayload>(token);
    return decoded.role || (decoded.sub?.startsWith("seller_") ? "SELLER" : "USER");
  } catch (err) {
    console.error("Invalid token", err);
    return null;
  }
};
