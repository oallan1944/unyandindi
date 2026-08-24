/**
 * Session-scoped storage for the ADMIN JWT specifically — kept separate
 * from tokenStorage.ts (customer/seller JWT) since they're already
 * distinct tokens signed with isolated keys on the backend
 * (AdminController / admin auth uses its own JWT key, separate from
 * customer/seller auth). Mixing them into one generic key-based utility
 * would make it easy to accidentally read the wrong token across that
 * boundary — two small, explicit functions are safer here than one
 * clever parameterized one.
 */

const ADMIN_JWT_KEY = "admin_jwt";

export function getAdminToken(): string | null {
  return sessionStorage.getItem(ADMIN_JWT_KEY);
}

export function setAdminToken(token: string): void {
  sessionStorage.setItem(ADMIN_JWT_KEY, token);
}

export function clearAdminToken(): void {
  sessionStorage.removeItem(ADMIN_JWT_KEY);
}