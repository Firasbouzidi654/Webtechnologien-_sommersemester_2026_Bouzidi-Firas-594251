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
      <span>
        <strong>{{ child.name || 'Unnamed child' }}</strong>
        <small>{{ medicationCount(child) }} medication(s)</small>
      </span>
    </button>
  </section>
</template>

<script>
export default {
  name: 'ChildList',
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
  border-radius: 8px;
  padding: 12px;
  background: var(--bg-card);
  color: var(--text-primary);
  text-align: left;
  cursor: pointer;
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
  background: #ffe8a8;
  color: #5d4b12;
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

small {
  margin-top: 3px;
  color: var(--text-secondary);
}
</style>
