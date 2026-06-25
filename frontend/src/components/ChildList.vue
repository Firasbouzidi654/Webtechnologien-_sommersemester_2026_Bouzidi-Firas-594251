<template>
  <section class="child-list">
    <header>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
    </header>

    <button
      v-for="child in children"
      :key="child.id"
      class="child-row"
      :class="{ active: child.id === selectedChildId }"
      type="button"
      @click="$emit('select-child', child.id)"
    >
      <img v-if="child.photoUrl" :src="child.photoUrl" :alt="child.name" class="avatar avatar-photo" />
      <span v-else class="avatar">{{ initials(child.name) }}</span>
      <span class="child-details">
        <strong>{{ child.name || 'Unnamed child' }}</strong>
        <small class="health-plan"><CareIcon name="health" />{{ medicationCount(child) }} Medication{{ medicationCount(child) === 1 ? '' : 's' }}</small>
        <span class="child-statuses">
          <span class="wellness-chip"><CareIcon name="wellness" />Healthy Day</span>
          <span v-if="groupName(child)" class="group-chip"><CareIcon name="children" />{{ groupName(child) }}</span>
        </span>
        <span class="allergy-list" aria-label="Allergies">
          <template v-if="allergiesFor(child).length">
            <span v-for="allergy in allergiesFor(child)" :key="allergy" class="allergy-chip">{{ allergy }}</span>
          </template>
          <span v-else class="no-allergies">No allergies recorded</span>
        </span>
      </span>
    </button>
  </section>
</template>

<script>
import CareIcon from './CareIcon.vue';

export default {
  name: 'ChildList',
  components: { CareIcon },
  props: {
    children: {
      type: Array,
      required: true
    },
    selectedChildId: {
      type: Number,
      default: null
    },
    title: {
      type: String,
      default: 'Children'
    },
    eyebrow: {
      type: String,
      default: 'Profiles'
    }
  },
  emits: ['select-child'],
  methods: {
    initials(name) {
      return (name || '?')
        .split(' ')
        .map((part) => part[0])
        .join('')
        .slice(0, 2);
    },
    medicationCount(child) {
      return Array.isArray(child?.medications) ? child.medications.length : 0;
    },
    allergiesFor(child) {
      const allergies = Array.isArray(child?.allergies)
        ? child.allergies
        : String(child?.allergies || '').split(',');
      return allergies.map((allergy) => allergy.trim()).filter(Boolean);
    },
    groupName(child) {
      return child?.group || child?.classroom || child?.groupName || '';
    }
  }
};
</script>

<style scoped>
.child-list {
  display: grid;
  gap: 12px;
}

header {
  margin-bottom: 4px;
}

.eyebrow {
  margin: 0 0 4px;
  color: #287b68;
  font-size: 0.78rem;
  font-weight: 800;
  text-transform: uppercase;
}

h2 {
  margin: 0;
  font-size: 1.15rem;
}

.child-row {
  display: flex;
  width: 100%;
  min-height: 76px;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 12px;
  background: linear-gradient(135deg, var(--bg-card), #f8fbff);
  color: var(--text-primary);
  text-align: left;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
}

.child-row:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--color-brand) 38%, var(--color-border));
  box-shadow: var(--shadow-md);
}

.child-row.active {
  border-color: #2d8f7b;
  box-shadow: 0 0 0 3px rgba(45, 143, 123, 0.14);
}

.avatar {
  display: grid;
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #dcedff, #e9dcff);
  color: #295987;
  box-shadow: inset 0 0 0 3px rgba(255,255,255,.72);
  font-weight: 900;
}

.avatar-photo {
  object-fit: cover;
  background: transparent;
}

strong,
small {
  display: block;
}

.health-plan,
.child-statuses,
.wellness-chip,
.group-chip { display: flex; align-items: center; gap: 5px; }

.health-plan { margin-top: 4px; font-size: .74rem; font-weight: 750; }
.health-plan svg { width: 14px; color: #397bb7; }
.child-statuses { flex-wrap: wrap; gap: 6px; margin-top: 7px; }
.wellness-chip,
.group-chip { border-radius: 999px; padding: 4px 8px; font-size: .7rem; font-weight: 800; line-height: 1.2; }
.wellness-chip { background: var(--pastel-green); color: #23724f; }.group-chip { background: var(--pastel-blue); color: #295987; }
.wellness-chip svg,.group-chip svg { width: 13px; }

.child-row:nth-of-type(3n) .avatar { background: linear-gradient(135deg, #daf4e6, #dcedff); color: #23724f; }
.child-row:nth-of-type(3n + 1) .avatar { background: linear-gradient(135deg, #ffe6eb, #fff0cf); color: #9f4a5d; }

.child-details {
  min-width: 0;
}

small {
  margin-top: 3px;
  color: var(--text-secondary);
}

.allergy-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.allergy-chip,
.no-allergies {
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 0.72rem;
  font-weight: 800;
  line-height: 1.2;
}

.allergy-chip {
  border: 1px solid color-mix(in srgb, var(--color-pending-border) 45%, transparent);
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.no-allergies {
  color: var(--text-secondary);
  background: var(--color-bg-tertiary);
}

@media (max-width: 480px) {
  .child-list {
    gap: 8px;
  }

  h2 {
    font-size: 1rem;
  }

  .child-row {
    min-height: 64px;
    gap: 9px;
    border-radius: 12px;
    padding: 10px;
  }

  .avatar {
    flex-basis: 38px;
    width: 38px;
    height: 38px;
  }

  .health-plan,
  .allergy-list {
    margin-top: 5px;
  }

  .child-statuses,
  .allergy-list {
    gap: 5px;
  }

  .wellness-chip,
  .group-chip,
  .allergy-chip,
  .no-allergies {
    padding: 3px 7px;
    font-size: 0.66rem;
  }
}
</style>
