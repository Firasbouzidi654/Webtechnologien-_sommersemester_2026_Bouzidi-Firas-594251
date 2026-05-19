<template>
  <article class="contact-card" :class="{ expanded: detailsVisible }">
    <div class="contact-summary">
      <div>
        <span class="priority">Priority {{ contact.priority }}</span>
        <h3>{{ contact.name }}</h3>
        <p>{{ contact.relationship }}</p>
      </div>
      <button
        type="button"
        class="toggle-button"
        v-if="compact"
        @click="toggleDetails"
        :aria-expanded="detailsVisible"
        :aria-controls="detailsId"
      >
        {{ buttonLabel }}
      </button>
    </div>

    <div :id="detailsId" class="contact-details" :class="{ open: detailsVisible }">
      <dl>
        <div>
          <dt>Phone</dt>
          <dd>{{ contact.phone || 'N/A' }}</dd>
        </div>
        <div>
          <dt>Email</dt>
          <dd>{{ contact.email || 'N/A' }}</dd>
        </div>
        <div>
          <dt>Relation</dt>
          <dd>{{ contact.relationship || 'N/A' }}</dd>
        </div>
        <div v-if="contact.childName">
          <dt>Child</dt>
          <dd>{{ contact.childName }}</dd>
        </div>
        <div v-if="contact.notes">
          <dt>Details</dt>
          <dd>{{ contact.notes }}</dd>
        </div>
      </dl>
    </div>
  </article>
</template>

<script>
export default {
  name: 'EmergencyContactCard',
  props: {
    contact: {
      type: Object,
      required: true
    },
    compact: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      detailsOpen: !this.compact
    };
  },
  watch: {
    compact(value) {
      this.detailsOpen = !value;
    }
  },
  computed: {
    detailsVisible() {
      return !this.compact || this.detailsOpen;
    },
    buttonLabel() {
      return this.detailsOpen ? 'Hide details' : 'View details';
    },
    detailsId() {
      const safeId = String(this.contact.id || this.contact.name || 'contact').replace(/\s+/g, '-').toLowerCase();
      return `emergency-contact-details-${safeId}`;
    }
  },
  methods: {
    toggleDetails() {
      this.detailsOpen = !this.detailsOpen;
    }
  }
};
</script>

<style scoped>
.contact-card {
  width: 100%;
  max-width: 560px;
  display: grid;
  gap: 14px;
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 14px;
  padding: 16px;
  background: var(--bg-card);
  color: var(--text-primary);
  overflow: hidden;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

:global([data-theme="dark"]) .contact-card {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background:
    linear-gradient(
      135deg,
      #111827 0%,
      #1f2937 100%
    );
  color: #f8fafc;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

.contact-card.expanded {
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.05);
}

.contact-summary {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
}

.priority {
  display: inline-flex;
  border-radius: 999px;
  padding: 5px 11px;
  background: #ffe8a8;
  color: #5d4b12;
  font-size: 0.78rem;
  font-weight: 800;
}

h3 {
  margin: 10px 0 4px;
  font-size: 1rem;
}

p,
dl,
dd {
  margin: 0;
}

.toggle-button {
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  padding: 10px 16px;
  min-height: 42px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: transform 0.2s ease, background-color 0.2s ease, border-color 0.2s ease;
}

:global([data-theme="dark"]) .contact-card h3,
:global([data-theme="dark"]) .contact-card dd {
  color: #f8fafc;
}

:global([data-theme="dark"]) .contact-card p,
:global([data-theme="dark"]) .contact-card dt {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .contact-card .priority {
  background: rgba(245, 158, 11, 0.22);
  color: #fde68a;
}

:global([data-theme="dark"]) .contact-card .toggle-button {
  border-color: rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

:global([data-theme="dark"]) .contact-card .toggle-button:hover {
  background: rgba(255, 255, 255, 0.12);
}

.toggle-button:hover {
  transform: translateY(-1px);
  border-color: var(--color-brand);
  background: rgba(49, 130, 206, 0.12);
}

.contact-details {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
  transition: max-height 0.32s ease, opacity 0.32s ease, transform 0.32s ease;
  transform: translateY(-6px);
}

.contact-details.open {
  max-height: 420px;
  opacity: 1;
  transform: translateY(0);
}

dl {
  display: grid;
  gap: 10px;
}

dt {
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

dd {
  overflow-wrap: anywhere;
  font-weight: 700;
  color: var(--color-text-primary);
}

@media (max-width: 640px) {
  .contact-summary {
    flex-direction: column;
    align-items: stretch;
  }

  .toggle-button {
    width: 100%;
  }
}
</style>
