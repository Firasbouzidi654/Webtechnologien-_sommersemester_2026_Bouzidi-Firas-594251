import { afterEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import SignInView from '../views/SignInView.vue';
import SignupView from '../views/SignupView.vue';
import { translations } from '../content/siteContent';

const RouterLinkStub = {
  props: ['to', 'custom'],
  template: '<slot :navigate="navigate"></slot>',
  methods: {
    navigate() {
      this.$emit('navigate', this.to);
    }
  }
};

function authResponse(body) {
  return Promise.resolve({
    ok: true,
    status: 200,
    json: () => Promise.resolve(body)
  });
}

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
  localStorage.clear();
});

describe('authentication flow', () => {
  it('registers a parent account and emits the parent dashboard route', async () => {
    vi.useFakeTimers();
    const fetchMock = vi.fn(() => authResponse({ id: 12, email: 'parent@example.test', role: 'PARENT' }));
    vi.stubGlobal('fetch', fetchMock);

    const wrapper = mount(SignupView, {
      props: { translations, language: 'en' },
      global: { stubs: { RouterLink: RouterLinkStub, FeaturesList: true } }
    });

    await wrapper.get('#signup-full-name').setValue('Sofia Becker');
    await wrapper.get('#signup-email').setValue('parent@example.test');
    await wrapper.get('#signup-password').setValue('SecurePass123!');
    await wrapper.get('#signup-confirm-password').setValue('SecurePass123!');
    await wrapper.get('input[type="checkbox"]').setValue(true);
    await wrapper.get('form').trigger('submit');
    await flushPromises();
    vi.runAllTimers();
    await flushPromises();

    expect(fetchMock).toHaveBeenCalledWith(expect.stringContaining('/api/auth/register'), expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ email: 'parent@example.test', password: 'SecurePass123!', role: 'PARENT' })
    }));
    expect(wrapper.emitted('navigate')?.[0]).toEqual(['/parent']);
  });

  it('signs in a staff user and emits the admin dashboard route', async () => {
    vi.useFakeTimers();
    const fetchMock = vi.fn(() => authResponse({ id: 4, email: 'staff@example.test', role: 'STAFF' }));
    vi.stubGlobal('fetch', fetchMock);

    const wrapper = mount(SignInView, {
      props: { translations, language: 'en' },
      global: { stubs: { RouterLink: RouterLinkStub, FeaturesList: true } }
    });

    await wrapper.get('#signin-email').setValue('staff@example.test');
    await wrapper.get('#signin-password').setValue('SecurePass123');
    await wrapper.get('form').trigger('submit');
    await flushPromises();
    vi.runAllTimers();
    await flushPromises();

    expect(fetchMock).toHaveBeenCalledWith(expect.stringContaining('/api/auth/login'), expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ email: 'staff@example.test', password: 'SecurePass123' })
    }));
    expect(wrapper.emitted('navigate')?.[0]).toEqual(['/admin']);
  });

  it('shows required-field validation before signup calls the backend', async () => {
    const fetchMock = vi.fn();
    vi.stubGlobal('fetch', fetchMock);

    const wrapper = mount(SignupView, {
      props: { translations, language: 'en' },
      global: { stubs: { RouterLink: RouterLinkStub, FeaturesList: true } }
    });

    await wrapper.get('form').trigger('submit');
    await flushPromises();

    expect(fetchMock).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain('Please review the highlighted registration details.');
    expect(wrapper.text()).toContain('Enter your full name.');
    expect(wrapper.text()).toContain('Enter your email address.');
  });
});
