import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { nextTick } from 'vue'
import ExampleEntities from '../components/ExampleEntities.vue'

describe('ExampleEntities', () => {
  let mockFetch

  beforeEach(() => {
    // Replace the real fetch with a mock before every test
    mockFetch = vi.fn()
    vi.stubGlobal('fetch', mockFetch)
  })

  afterEach(() => {
    // Restore the real fetch after every test
    vi.unstubAllGlobals()
  })

  it('shows a loading message while waiting for the API', async () => {
    // fetch returns a promise that never resolves → loading stays visible
    mockFetch.mockReturnValue(new Promise(() => {}))

    const wrapper = mount(ExampleEntities, { props: { isDark: false } })
    // Vue batches DOM updates as microtasks; wait one tick for the
    // isLoadingChildren = true assignment to reach the DOM
    await nextTick()

    expect(wrapper.text()).toContain('Loading children from database')
  })

  it('renders a child card after the API responds', async () => {
    // Simulate a successful GET /api/children response
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => [
        { id: 1, name: 'Emma Müller', allergies: ['Peanuts'], groupName: 'Sunflowers' }
      ]
    })

    const wrapper = mount(ExampleEntities, { props: { isDark: false } })
    // Wait for all promises (fetch + Vue re-render) to finish
    await flushPromises()

    expect(wrapper.text()).toContain('Emma Müller')
  })

  it('shows an error message when the server returns an error', async () => {
    // Simulate a 500 response
    mockFetch.mockResolvedValue({ ok: false, status: 500 })

    const wrapper = mount(ExampleEntities, { props: { isDark: false } })
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load children')
  })
})
