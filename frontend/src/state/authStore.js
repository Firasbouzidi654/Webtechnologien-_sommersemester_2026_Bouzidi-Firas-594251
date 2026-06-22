import { reactive } from 'vue';
import { api } from '../services/api';

const AUTH_KEY = 'kindercare-simple-user';

function storedUser() {
  try { return JSON.parse(localStorage.getItem(AUTH_KEY)) || null; } catch { return null; }
}

export const authStore = reactive({ user: storedUser() });

export function setAuth(user) {
  authStore.user = user;
  localStorage.setItem(AUTH_KEY, JSON.stringify(user));
}

export function clearAuth() {
  authStore.user = null;
  localStorage.removeItem(AUTH_KEY);
}

export function isAuthenticated() { return Boolean(authStore.user); }
export function currentUser() { return authStore.user; }

// Credentials are validated against the backend database (see AuthController).
export async function login(email, password) {
  if (!email || !password) throw new Error('Invalid email or password.');
  const account = await api.loginUser({ email, password });
  const user = { ...account, fullName: account.email.split('@')[0] };
  setAuth(user);
  return user;
}

export async function signup(fullName, email, password, role) {
  if (!fullName || !email || !password) throw new Error('Please complete the required fields.');
  const account = await api.registerUser({ email, password, role: role || 'PARENT' });
  const user = { ...account, fullName };
  setAuth(user);
  return user;
}

export function logout() { clearAuth(); }
