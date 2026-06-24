<template>
  <svg class="care-icon" viewBox="0 0 24 24" aria-hidden="true">
    <path v-if="name === 'children'" d="M8 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm8-1a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM3.5 20a4.5 4.5 0 0 1 9 0m3.5 0a4 4 0 0 1 4.5-3.95M12 16.5a4 4 0 0 1 3.8-2.7" />
    <path v-else-if="name === 'wellness'" d="M12 20.5s-7-4.3-7-10.1A3.9 3.9 0 0 1 12 8a3.9 3.9 0 0 1 7 2.4c0 5.8-7 10.1-7 10.1Z" />
    <path v-else-if="name === 'activity'" d="m12 3 1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9L12 3Zm6.5 12 .8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8.8-2.2Z" />
    <path v-else-if="name === 'learning'" d="M4 5.5c2.7-1.2 5.3-.7 8 1.3 2.7-2 5.3-2.5 8-1.3v12c-2.7-1.2-5.3-.7-8 1.3-2.7-2-5.3-2.5-8-1.3v-12Zm8 1.3v12" />
    <path v-else-if="name === 'calendar'" d="M6 4v3m12-3v3M4.5 9.5h15M6 5h12a1.5 1.5 0 0 1 1.5 1.5v12A1.5 1.5 0 0 1 18 20H6a1.5 1.5 0 0 1-1.5-1.5v-12A1.5 1.5 0 0 1 6 5Zm2.5 8h.01m3-.01h.01m3-.01h.01m-6 3h.01m3-.01h.01" />
    <path v-else d="M9 3h6v4H9V3Zm-2 2H5v16h14V5h-2M8 13h8m-4-4v8" />
  </svg>
</template>

<script>
export default {
  name: 'CareIcon',
  props: { name: { type: String, default: 'health' } }
};
</script>

<style scoped>
.care-icon { width: 20px; height: 20px; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
</style>
