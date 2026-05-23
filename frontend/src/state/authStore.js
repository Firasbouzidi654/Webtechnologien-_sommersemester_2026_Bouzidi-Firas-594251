import { reactive } from 'vue';
import { apiBaseUrl } from '../services/api';

const AUTH_KEY = 'kindercare-auth-user';

function loadStoredUser() {
  try {
    const raw = localStorage.getItem(AUTH_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export const authStore = reactive({
  user: loadStoredUser()
});

export function setAuth(user) {
  authStore.user = user;
  try {
    localStorage.setItem(AUTH_KEY, JSON.stringify(user));
  } catch {
    // localStorage unavailable in some privacy contexts
  }
}

export function clearAuth() {
  authStore.user = null;
  try {
    localStorage.removeItem(AUTH_KEY);
  } catch {
    // ignore
  }
}

export function isAuthenticated() {
  return Boolean(authStore.user);
}

export function currentUser() {
  return authStore.user;
}

export async function signup(fullName, email, password, role, phone) {
  const response = await fetch(`${apiBaseUrl()}/api/auth/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ fullName, email, password, role: role || 'PARENT', phone: phone || '' })
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.message || 'Account creation failed. Please try again.');
  }

  setAuth(data);
  return data;
}

export async function login(email, password) {
  const response = await fetch(`${apiBaseUrl()}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.message || 'Invalid email or password.');
  }

  setAuth(data);
  return data;
}

export function logout() {
  clearAuth();
}
