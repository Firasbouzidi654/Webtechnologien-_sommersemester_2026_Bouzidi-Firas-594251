<template>
  <section class="care-highlights" aria-label="Childcare overview">
    <article class="care-highlight wellness-highlight">
      <header><span class="highlight-icon wellness-icon"><CareIcon name="wellness" /></span><div><p class="eyebrow">Wellness</p><h2>Child Wellness Overview</h2></div></header>
      <ul><li><span class="status-dot healthy"></span><span>Healthy Day</span></li><li><span class="status-dot current"></span><span>Records Up To Date</span></li><li><span class="status-dot scheduled"></span><span>{{ medicationCount }} Medication {{ medicationCount === 1 ? 'Schedule' : 'Schedules' }}</span></li></ul>
    </article>
    <article class="care-highlight activity-highlight">
      <header><span class="highlight-icon activity-icon"><CareIcon name="activity" /></span><div><p class="eyebrow">Daily plan</p><h2>Today's Activities</h2></div></header>
      <ul class="activity-list"><li><CareIcon name="activity" /><span>Creative Play</span></li><li><CareIcon name="learning" /><span>Story Time</span></li><li><CareIcon name="children" /><span>Outdoor Activities</span></li></ul>
    </article>
  </section>
</template>

<script>
import CareIcon from './CareIcon.vue';
export default { name: 'CareHighlights', components: { CareIcon }, props: { medicationCount: { type: Number, default: 0 } } };
</script>

<style scoped>
.care-highlights { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:12px; }
.care-highlight { display:grid; gap:14px; min-width:0; border:1px solid var(--color-border); border-radius:16px; padding:16px; box-shadow:var(--shadow-sm); }
.wellness-highlight { background:linear-gradient(135deg,#effaf5 0%,#e6f5ff 100%); }.activity-highlight { background:linear-gradient(135deg,#fff9e7 0%,#fff0f3 100%); }
header { display:flex; gap:10px; align-items:center; }.highlight-icon { display:grid; width:38px; height:38px; place-items:center; border-radius:12px; }.wellness-icon { background:#d7f2e4; color:#177a55; }.activity-icon { background:#ffebd2; color:#b45309; }
.eyebrow { margin:0 0 2px; color:var(--color-text-secondary); font-size:.7rem; font-weight:800; letter-spacing:.07em; text-transform:uppercase; } h2 { margin:0; color:var(--color-text-primary); font-size:1rem; }
ul { display:grid; gap:8px; margin:0; padding:0; list-style:none; } li { display:flex; align-items:center; gap:8px; color:var(--color-text-primary); font-size:.84rem; font-weight:700; }.status-dot { width:8px; height:8px; border-radius:50%; }.healthy { background:#44ad7b; }.current { background:#5799d8; }.scheduled { background:#d79c37; }.activity-list svg { width:17px; color:#9a5b1c; }
:global([data-theme="dark"]) .wellness-highlight, :global([data-theme="dark"]) .activity-highlight { border-color:rgba(255,255,255,.06); background:linear-gradient(135deg,#182536 0%,#1e293b 100%); }:global([data-theme="dark"]) h2, :global([data-theme="dark"]) li { color:#f8fafc; }
@media (max-width:640px) { .care-highlights { grid-template-columns:1fr; } }
</style>
