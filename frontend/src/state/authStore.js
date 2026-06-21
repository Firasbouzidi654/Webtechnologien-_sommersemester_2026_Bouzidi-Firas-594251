import { reactive } from 'vue';

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

// Login is intentionally local for this course project: no password is stored.
export async function login(email, password, selectedRole) {
  if (!email || !password) throw new Error('Email and password are required.');
  const role = selectedRole || (email.toLowerCase().includes('admin') ? 'ADMIN' : 'PARENT');
  const user = { fullName: email.split('@')[0], email, role };
  setAuth(user);
  return user;
}

export async function signup(fullName, email, password, role) {
  if (!fullName || !email || !password) throw new Error('Please complete the required fields.');
  const user = { fullName, email, role: role || 'PARENT' };
  setAuth(user);
  return user;
}

export function logout() { clearAuth(); }
