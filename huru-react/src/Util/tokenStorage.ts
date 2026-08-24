/**
 * Centralized JWT storage.
 *
 * Uses sessionStorage, not localStorage: sessionStorage clears
 * automatically when the browser window/tab closes, which is the
 * intended behavior — closing the app logs the user out, rather than
 * silently staying logged in indefinitely the way localStorage does.
 *
 * Every part of the app that reads or writes the JWT should go through
 * these three functions rather than calling sessionStorage/localStorage
 * directly. Centralizing it here means a future change (e.g. switching
 * storage mechanisms again, adding token expiry checks) only has to
 * happen in one place — the alternative is what happened with currency
 * formatting: some components got updated, others didn't, and they
 * silently drifted out of sync with each other.
 */

const JWT_KEY = "jwt";

export function getToken(): string | null {
  return sessionStorage.getItem(JWT_KEY);
}

export function setToken(token: string): void {
  sessionStorage.setItem(JWT_KEY, token);
}

export function clearToken(): void {
  sessionStorage.removeItem(JWT_KEY);
}