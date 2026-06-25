import { createRouter, createWebHistory } from 'vue-router';
import AdminDashboard from '../views/AdminDashboard.vue';
import ParentDashboard from '../views/ParentDashboard.vue';
import PrivacyView from '../views/PrivacyView.vue';
import SignInView from '../views/SignInView.vue';
import SignupView from '../views/SignupView.vue';
import { currentUser, isAuthenticated } from '../state/authStore';

if (typeof window !== 'undefined' && window.location.hash.startsWith('#/')) {
  window.history.replaceState({}, '', window.location.hash.slice(1));
}

export const routes = [
  { path: '/', redirect: '/signin' },
  { path: '/login', redirect: '/signin' },
  { path: '/signin', name: 'signin', component: SignInView },
  { path: '/signup', name: 'signup', component: SignupView },
  { path: '/privacy', name: 'privacy', component: PrivacyView },
  { path: '/parent', name: 'parent', component: ParentDashboard, meta: { requiresAuth: true, roles: ['PARENT'] } },
  { path: '/admin', name: 'admin', component: AdminDashboard, meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] } },
  { path: '/:pathMatch(.*)*', redirect: '/signup' }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (!to.meta.requiresAuth) return true;

  if (!isAuthenticated()) {
    return { name: 'signin' };
  }

  const role = (currentUser()?.role || '').toUpperCase();
  if (to.meta.roles && !to.meta.roles.includes(role)) {
    return { name: 'signin' };
  }

  return true;
});

export default router;
