<template>
  <section class="live-kindergarten-overview example-entities" :class="{ 'dark-mode': isDark }" aria-labelledby="example-entities-title">
    <div class="example-entities-header">
      <p class="eyebrow">Live Kindergarten Overview</p>
      <h2 id="example-entities-title">Live Kindergarten Overview</h2>
    </div>

    <!-- Section;Style CSS appliqué via la classe .entity-card -->
    <form class="create-child-form" @submit.prevent="createChild">
      <label>
        <span>Name</span>
        <input v-model.trim="newChild.name" type="text" required />
      </label>
      <label>
        <span>Date of birth</span>
        <input v-model="newChild.dateOfBirth" type="date" required />
      </label>
      <label>
        <span>Allergies</span>
        <input v-model.trim="newChild.allergies" type="text" />
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

    <ul class="entity-list">
      <li v-for="child in children" :key="child.id" class="overview-card entity-card" :class="{ 'dark-mode': isDark }">
        <div class="entity-card-header">
          <span class="entity-id">#{{ child.id }}</span>
          <h3>{{ child.name }}</h3>
          <button class="info-btn" @click="toggleInfo(child.id)">ℹ️</button>
        </div>

        <dl>
          <div>
            <dt>Allergies</dt>
            <dd>{{ child.allergies }}</dd>
          </div>
          <div>
            <dt>Emergency contact</dt>
            <dd>{{ child.emergencyContact }}</dd>
          </div>
        </dl>
          <div v-if="openChildId === child.id" class="extra-info">
            <p><strong>Age:</strong> {{ child.age }}</p>
            <p><strong>Parent Email:</strong> {{ child.parentEmail }}</p>
            <p><strong>Siblings:</strong> {{ child.siblings }}</p>
            <p><strong>Hobbies:</strong> {{ child.hobbies }}</p>
            <p><strong>Notes:</strong> {{ child.notes }}</p>
        </div>
      </li>
    </ul>
  </section>
</template>

<script>
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
      createChildMessage: '',
      createChildError: '',
      newChild: {
        name: 'Emma',
        dateOfBirth: '2020-05-10',
        allergies: 'Peanuts'
      },

      children: [
        {
          id: 1,
          name: 'Mila Schneider',
          allergies: 'Peanuts',
          emergencyContact: 'Sara Schneider, +49 151 123456',
          age: 4,
          parentEmail: 'sara@email.de',
          siblings: '1 brother',
          hobbies: 'Painting',
          notes: 'Carry allergy pen'
        },
        {
          id: 2,
          name: 'Noah Becker',
          allergies: 'None known',
          emergencyContact: 'Jonas Becker, +49 152 987654',
          age: 5,
          parentEmail: 'jonas@email.de',
          siblings: 'No siblings',
          hobbies: 'Football',
          notes: 'Needs inhaler'
        },
        {
          id: 3,
          name: 'Lina Wagner',
          allergies: 'Bee stings',
          emergencyContact: 'Amira Wagner, +49 176 456789',
          age: 4,
          parentEmail: 'amira@email.de',
          siblings: '1 sister',
          hobbies: 'Music',
          notes: 'Bee allergy spray'
        },
        {
          id: 4,
          name: 'Liam Müller',
          allergies: 'Shellfish',
          emergencyContact: 'Anna Müller, +49 153 345678',
          age: 5,
          parentEmail: 'anna@email.de',
          siblings: '2 sisters',
          hobbies: 'Cars',
          notes: 'Avoid seafood'
        },
        {
          id: 5,
          name: 'Emma Fischer',
          allergies: 'Gluten',
          emergencyContact: 'Markus Fischer, +49 154 234567',
          age: 4,
          parentEmail: 'markus@email.de',
          siblings: '1 brother',
          hobbies: 'Reading',
          notes: 'Needs gluten-free snacks'
        },
        {
          id: 6,
          name: 'Ben Hoffmann',
          allergies: 'None known',
          emergencyContact: 'Laura Hoffmann, +49 155 678901',
          age: 5,
          parentEmail: 'laura@email.de',
          siblings: '1 sister',
          hobbies: 'Gaming',
          notes: 'Prefers quiet activities'
        }
      ]
    };
  },

  methods: {
    async createChild() {
      this.isSavingChild = true;
      this.createChildMessage = '';
      this.createChildError = '';

      try {
        const response = await fetch('https://kindercare-backend.onrender.com/api/children', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            name: this.newChild.name,
            dateOfBirth: this.newChild.dateOfBirth,
            allergies: this.newChild.allergies
          })
        });

        if (!response.ok) {
          throw new Error(`Request failed with status ${response.status}`);
        }

        const savedChild = await response.json();
        this.createChildMessage = `Saved ${savedChild.name || this.newChild.name} to the database.`;
      } catch (error) {
        this.createChildError = error.message || 'Could not save child.';
      } finally {
        this.isSavingChild = false;
      }
    },

    toggleInfo(id) {
      if (this.openChildId === id) {
        this.openChildId = null;
      } else {
        this.openChildId = id;
      }
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

@media (max-width: 820px) {
  .create-child-form {
    grid-template-columns: 1fr;
  }

  .entity-list {
    grid-template-columns: 1fr;
  }
}
</style>
