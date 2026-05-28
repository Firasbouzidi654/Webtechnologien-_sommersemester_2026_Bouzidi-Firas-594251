<template>
  <section class="live-kindergarten-overview example-entities" :class="{ 'dark-mode': isDark }" aria-labelledby="example-entities-title">
    <div class="example-entities-header">
      <p class="eyebrow">Live Kindergarten Overview</p>
      <h2 id="example-entities-title">Live Kindergarten Overview</h2>
    </div>

    <!-- Create child form -->
    <form class="create-child-form" @submit.prevent="createChild">
      <label>
        <span>Name</span>
        <input v-model.trim="newChild.name" type="text" required placeholder="e.g. Emma Müller" />
      </label>
      <label>
        <span>Date of birth</span>
        <input v-model="newChild.dateOfBirth" type="date" required />
      </label>
      <label>
        <span>Allergies</span>
        <input v-model.trim="newChild.allergies" type="text" placeholder="e.g. Peanuts, Gluten" />
      </label>
      <button class="create-child-button" type="submit" :disabled="isSavingChild">
        {{ isSavingChild ? 'Saving...' : 'Save child to database' }}
      </button>
    </form>

    <p v-if="createChildMessage" class="create-child-status success" role="status">
      {{ createChildMessage }}
    </p>
    <p v-if="createChildError" class="create-child-status error" role="alert">
      {{ createChildError }}
    </p>

    <!-- Loading state -->
    <p v-if="isLoadingChildren" class="loading-msg" role="status">Loading children from database…</p>
    <p v-if="loadError" class="create-child-status error" role="alert">{{ loadError }}</p>

    <!-- Children list from PostgreSQL -->
    <ul v-if="!isLoadingChildren" class="entity-list">
      <li
        v-for="child in children"
        :key="child.id"
        class="overview-card entity-card"
        :class="{ 'dark-mode': isDark }"
      >
        <div class="entity-card-header">
          <img v-if="child.photoUrl" :src="child.photoUrl" :alt="child.name" class="entity-avatar-photo" />
          <span v-else class="entity-id">#{{ child.id }}</span>
          <h3>{{ child.name }}</h3>
          <button class="info-btn" type="button" @click="toggleInfo(child.id)">ℹ️</button>
        </div>

        <dl>
          <div>
            <dt>Allergies</dt>
            <dd>{{ child.allergies && child.allergies.length ? child.allergies.join(', ') : 'None known' }}</dd>
          </div>
          <div>
            <dt>Group</dt>
            <dd>{{ child.groupName || '—' }}</dd>
          </div>
        </dl>

        <div v-if="openChildId === child.id" class="extra-info">
          <p><strong>Date of birth:</strong> {{ child.dateOfBirth || '—' }}</p>
          <p><strong>Parent:</strong> {{ child.parentName || '—' }}</p>
          <p><strong>Parent email:</strong> {{ child.parentEmail || '—' }}</p>
          <p v-if="child.chronicDiseases && child.chronicDiseases.length">
            <strong>Chronic diseases:</strong> {{ child.chronicDiseases.join(', ') }}
          </p>
          <p v-if="child.healthNotes"><strong>Health notes:</strong> {{ child.healthNotes }}</p>
          <p v-if="child.emergencyContacts && child.emergencyContacts.length">
            <strong>Emergency contact:</strong>
            {{ child.emergencyContacts[0].name }} — {{ child.emergencyContacts[0].phone }}
          </p>
        </div>
      </li>

      <li v-if="children.length === 0 && !isLoadingChildren && !loadError" class="entity-card empty-state">
        No children in the database yet. Use the form above to add the first one.
      </li>
    </ul>
  </section>
</template>

<script>
import { apiBaseUrl } from '../services/api.js';

export default {
  name: 'ExampleEntities',
  props: {
    isDark: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      openChildId: null,
      isSavingChild: false,
      isLoadingChildren: false,
      createChildMessage: '',
      createChildError: '',
      loadError: '',
      newChild: {
        name: '',
        dateOfBirth: '',
        allergies: ''
      },
      children: []
    };
  },

  mounted() {
    this.fetchChildren();
  },

  methods: {
    async fetchChildren() {
      this.isLoadingChildren = true;
      this.loadError = '';
      try {
        const response = await fetch(`${apiBaseUrl()}/api/children`);
        if (!response.ok) throw new Error(`Failed to load children (${response.status})`);
        this.children = await response.json();
      } catch (error) {
        this.loadError = error.message || 'Could not load children from the database.';
      } finally {
        this.isLoadingChildren = false;
      }
    },

    async createChild() {
      this.isSavingChild = true;
      this.createChildMessage = '';
      this.createChildError = '';

      try {
        const response = await fetch(`${apiBaseUrl()}/api/children`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            name: this.newChild.name,
            dateOfBirth: this.newChild.dateOfBirth || null,
            allergies: this.newChild.allergies || null
          })
        });

        if (!response.ok) {
          const data = await response.json().catch(() => ({}));
          throw new Error(data?.message || `Request failed (${response.status})`);
        }

        const saved = await response.json();
        this.createChildMessage = `"${saved.name}" was saved to the database.`;
        this.newChild = { name: '', dateOfBirth: '', allergies: '' };
        await this.fetchChildren();
      } catch (error) {
        this.createChildError = error.message || 'Could not save child.';
      } finally {
        this.isSavingChild = false;
      }
    },

    toggleInfo(id) {
      this.openChildId = this.openChildId === id ? null : id;
    }
  }
};
</script>

<style scoped>
.example-entities {
  max-width: 1120px;
  margin: 0 auto;
  padding: 32px 24px 48px;
}

:global(.dark-mode) .live-kindergarten-overview,
:global([data-theme="dark"]) .live-kindergarten-overview,
.live-kindergarten-overview.dark-mode {
  max-width: none;
  background:
    linear-gradient(
      180deg,
      #0f172a 0%,
      #111827 100%
    ) !important;
}

:global([data-theme="dark"]) .example-entities {
  max-width: none;
  background:
    linear-gradient(
      180deg,
      #0f172a 0%,
      #111827 100%
    );
}

.example-entities-header {
  margin-bottom: 18px;
}

.create-child-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
  margin: 0 0 14px;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-card);
}

.create-child-form label {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.create-child-form span {
  color: var(--text-secondary);
  font-size: 0.82rem;
  font-weight: 800;
}

.create-child-form input {
  width: 100%;
  min-height: 42px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 8px 10px;
  background: var(--bg-card);
  color: var(--text-primary);
}

.create-child-button {
  min-height: 42px;
  border: 0;
  border-radius: 6px;
  padding: 8px 12px;
  color: #ffffff;
  background: #2d8f7b;
  font-weight: 800;
  cursor: pointer;
}

.create-child-button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.create-child-status {
  margin: 0 0 14px;
  font-weight: 700;
}

.create-child-status.success {
  color: #176044;
}

.create-child-status.error {
  color: #9f2f2f;
}

.loading-msg {
  margin: 0 0 14px;
  color: var(--text-secondary);
  font-style: italic;
}

.eyebrow {
  margin: 0 0 6px;
  color: #2d8f7b;
  font-size: 0.82rem;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.example-entities h2 {
  margin: 0;
  color: #20303f;
  font-size: 1.6rem;
}

.entity-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.entity-card {
  min-width: 0;
  padding: 18px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-card);
  color: var(--text-primary);
  box-shadow: 0 14px 30px rgba(32, 48, 63, 0.08);
}

.empty-state {
  grid-column: 1 / -1;
  color: var(--text-secondary);
  font-style: italic;
}

:global(.dark-mode) .live-kindergarten-overview .overview-card,
:global([data-theme="dark"]) .live-kindergarten-overview .overview-card,
.live-kindergarten-overview .overview-card.dark-mode {
  background:
    linear-gradient(
      135deg,
      #111827 0%,
      #1e293b 100%
    ) !important;
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"]) .example-entities h2,
:global([data-theme="dark"]) .entity-card h3,
:global([data-theme="dark"]) .entity-card dd,
:global([data-theme="dark"]) .extra-info strong {
  color: #f8fafc;
}

:global([data-theme="dark"]) .example-entities .eyebrow,
:global([data-theme="dark"]) .entity-card dt,
:global([data-theme="dark"]) .extra-info p {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .entity-card {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background:
    linear-gradient(
      135deg,
      #111827 0%,
      #1e293b 100%
    );
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"]) .extra-info {
  border-color: rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
}

.entity-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.entity-id {
  flex: 0 0 auto;
  border-radius: 999px;
  padding: 4px 9px;
  color: #fff;
  background: #e45b5b;
  font-size: 0.8rem;
  font-weight: 800;
}

.entity-avatar-photo {
  flex: 0 0 36px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-color);
}

.entity-card h3 {
  min-width: 0;
  margin: 0;
  color: var(--text-primary);
  font-size: 1.08rem;
}

.entity-card dl {
  display: grid;
  gap: 12px;
  margin: 0;
}

.entity-card dt {
  color: var(--text-secondary);
  font-size: 0.82rem;
  font-weight: 800;
}

.entity-card dd {
  margin: 3px 0 0;
  color: var(--text-primary);
  font-weight: 600;
}

.extra-info {
  margin-top: 14px;
  padding: 10px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.03);
  font-size: 0.9rem;
}

.extra-info p {
  margin: 4px 0;
}

.info-btn {
  margin-left: auto;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 1rem;
  padding: 2px 4px;
}

@media (max-width: 820px) {
  .create-child-form {
    grid-template-columns: 1fr;
  }

  .entity-list {
    grid-template-columns: 1fr;
  }
}
</style>
