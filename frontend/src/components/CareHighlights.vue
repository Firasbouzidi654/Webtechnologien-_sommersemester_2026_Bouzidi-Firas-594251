<template>
  <section class="care-highlights" aria-label="Childcare overview">
    <article class="care-highlight wellness-highlight">
      <header><span class="highlight-icon wellness-icon"><CareIcon name="wellness" /></span><div><p class="eyebrow">Wellness</p><h2>Child Wellness Overview</h2></div></header>
      <ul><li><span class="status-dot healthy"></span><span>Healthy Day</span></li><li><span class="status-dot current"></span><span>Records Up To Date</span></li><li><span class="status-dot scheduled"></span><span>{{ medicationCount }} Medication {{ medicationCount === 1 ? 'Schedule' : 'Schedules' }}</span></li></ul>
    </article>
    <article class="care-highlight activity-highlight">
      <header><span class="highlight-icon activity-icon"><CareIcon name="activity" /></span><div><p class="eyebrow">Daily learning</p><h2>Learning &amp; Play</h2></div></header>
      <ul class="activity-list"><li><CareIcon name="activity" /><span>Arts &amp; Crafts</span></li><li><CareIcon name="learning" /><span>Story Time</span></li><li><CareIcon name="learning" /><span>Alphabet Activities</span></li><li><CareIcon name="children" /><span>Outdoor Play</span></li></ul>
    </article>
    <article class="care-highlight classroom-highlight">
      <header><span class="highlight-icon classroom-icon"><CareIcon name="children" /></span><div><p class="eyebrow">Classroom theme</p><h2>{{ classroom.theme }}</h2></div></header>
      <div class="theme-focus"><span>Today's Focus</span><strong>{{ classroom.focus }}</strong></div>
    </article>
  </section>
</template>

<script>
import CareIcon from './CareIcon.vue';
import { randomClassroomThemeFocus } from '../config/classroomThemes';

export default {
  name: 'CareHighlights',
  components: { CareIcon },
  props: { medicationCount: { type: Number, default: 0 } },
  data() {
    return {
      classroom: randomClassroomThemeFocus()
    };
  }
};
</script>

<style scoped>
.care-highlights {
  display:grid;
  grid-template-columns:repeat(3,minmax(0,1fr));
  gap:12px;
}

.care-highlight {
  --highlight-title: #172033;
  --highlight-text: #243244;
  --highlight-muted: #5f6f82;
  --highlight-focus-bg: rgba(255,255,255,.52);
  --highlight-focus-border: rgba(85,89,167,.16);

  display:grid;
  gap:14px;
  min-width:0;
  border:1px solid var(--color-border);
  border-radius:16px;
  padding:16px;
  box-shadow:var(--shadow-sm);
}

.wellness-highlight { background:linear-gradient(135deg,#effaf5 0%,#e6f5ff 100%); }
.activity-highlight { background:linear-gradient(135deg,#fff9e7 0%,#fff0f3 100%); }
.classroom-highlight { background:linear-gradient(135deg,#edf2ff 0%,#f7efff 100%); }

header {
  display:flex;
  gap:10px;
  align-items:center;
}

.highlight-icon {
  display:grid;
  width:38px;
  height:38px;
  place-items:center;
  border-radius:12px;
}

.wellness-icon { background:#d7f2e4; color:#177a55; }
.activity-icon { background:#ffebd2; color:#b45309; }
.classroom-icon { background:#e4e8ff; color:#5559a7; }

.eyebrow {
  margin:0 0 2px;
  color:var(--highlight-muted) !important;
  font-size:.7rem;
  font-weight:800;
  letter-spacing:.07em;
  text-transform:uppercase;
}

h2 {
  margin:0;
  color:var(--highlight-title) !important;
  font-size:1rem;
}

ul {
  display:grid;
  gap:8px;
  margin:0;
  padding:0;
  list-style:none;
}

li {
  display:flex;
  align-items:center;
  gap:8px;
  color:var(--highlight-text) !important;
  font-size:.84rem;
  font-weight:700;
}

.status-dot {
  width:8px;
  height:8px;
  border-radius:50%;
}

.healthy { background:#16824f; }
.current { background:#256fb3; }
.scheduled { background:#aa7419; }

.activity-list svg {
  width:17px;
  color:#8a4b0b;
}

.theme-focus {
  display:grid;
  gap:3px;
  border:1px solid var(--highlight-focus-border);
  border-radius:12px;
  padding:11px;
  background:var(--highlight-focus-bg);
}

.theme-focus span {
  color:var(--highlight-muted) !important;
  font-size:.75rem;
  font-weight:750;
}

.theme-focus strong {
  color:var(--highlight-title) !important;
  font-size:.92rem;
}

:global([data-theme="dark"]) .care-highlight {
  --highlight-title: #0f172a;
  --highlight-text: #1e293b;
  --highlight-muted: #334155;
  --highlight-focus-bg: rgba(255,255,255,.62);
  --highlight-focus-border: rgba(71,85,105,.22);

  border-color:rgba(255,255,255,.1);
}

:global([data-theme="dark"]) .care-highlight .eyebrow,
:global([data-theme="dark"]) .care-highlight li,
:global([data-theme="dark"]) .theme-focus span {
  color: var(--highlight-muted) !important;
  -webkit-text-fill-color: var(--highlight-muted) !important;
}

:global([data-theme="dark"]) .care-highlight h2,
:global([data-theme="dark"]) .theme-focus strong {
  color: var(--highlight-title) !important;
  -webkit-text-fill-color: var(--highlight-title) !important;
}

:global([data-theme="dark"]) .care-highlight li span {
  color: var(--highlight-text) !important;
  -webkit-text-fill-color: var(--highlight-text) !important;
}

:global([data-theme="dark"]) .wellness-highlight {
  background:linear-gradient(135deg,#e8f8ef 0%,#dceeff 100%);
}

:global([data-theme="dark"]) .activity-highlight {
  background:linear-gradient(135deg,#fff4cf 0%,#ffe2e8 100%);
}

:global([data-theme="dark"]) .classroom-highlight {
  background:linear-gradient(135deg,#e3eaff 0%,#f2e3ff 100%);
}
@media (max-width:900px) { .care-highlights { grid-template-columns:1fr; } }
@media (max-width:480px) {
  .care-highlights { gap:8px; }
  .care-highlight { gap:10px; padding:12px; border-radius:13px; }
  header { gap:8px; }
  .highlight-icon { width:32px; height:32px; border-radius:10px; }
  h2 { font-size:.92rem; }
  ul { gap:6px; }
  li { font-size:.78rem; }
  .theme-focus { padding:9px; }
}
</style>
