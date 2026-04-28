<template>
  <section v-if="child" class="health-profile">
    <header>
      <div>
        <p class="eyebrow">Health profile</p>
        <h2>{{ child.name }}</h2>
        <p>{{ child.groupName }} - born {{ child.dateOfBirth }}</p>
      </div>
      <span class="badge">Parent view</span>
    </header>

    <form class="health-form" @submit.prevent="save">
      <label>
        <span>Allergies</span>
        <input v-model="form.allergies" type="text" placeholder="Peanuts, milk, bee stings" />
      </label>
      <label>
        <span>Chronic diseases</span>
        <input v-model="form.chronicDiseases" type="text" placeholder="Asthma, diabetes" />
      </label>
      <label class="wide">
        <span>Health notes</span>
        <textarea v-model="form.healthNotes" rows="3" placeholder="Important daily care notes"></textarea>
      </label>
      <button type="submit">Save health info</button>
    </form>

    <p v-if="feedback" class="feedback">{{ feedback }}</p>

    <div class="placeholder-grid">
      <button type="button">Add allergy</button>
      <button type="button">Add chronic disease</button>
      <button type="button">Add medication</button>
      <button type="button">Upload prescription</button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'ChildHealthProfile',
  props: {
    child: {
      type: Object,
      default: null
    }
  },
  emits: ['update-child'],
  data() {
    return {
      form: {
        allergies: '',
        chronicDiseases: '',
        healthNotes: ''
      },
      feedback: ''
    };
  },
  watch: {
    child: {
      immediate: true,
      handler(child) {
        if (!child) {
          return;
        }

        this.form = {
          allergies: child.allergies.join(', '),
          chronicDiseases: child.chronicDiseases.join(', '),
          healthNotes: child.healthNotes
        };
        this.feedback = '';
      }
    }
  },
  methods: {
    save() {
      this.$emit('update-child', {
        ...this.child,
        allergies: this.splitField(this.form.allergies),
        chronicDiseases: this.splitField(this.form.chronicDiseases),
        healthNotes: this.form.healthNotes
      });
      this.feedback = 'Saved in the local prototype data.';
    },
    splitField(value) {
      return value
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean);
    }
  }
};
</script>

<style scoped>
.health-profile {
  display: grid;
  gap: 18px;
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 8px;
  padding: 18px;
  background: #fff;
}

header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
}

.eyebrow,
h2,
p {
  margin: 0;
}

.eyebrow {
  color: #287b68;
  font-size: 0.78rem;
  font-weight: 900;
  text-transform: uppercase;
}

h2 {
  margin-top: 4px;
  font-size: 1.35rem;
}

header p {
  color: #637486;
}

.badge {
  border-radius: 999px;
  padding: 5px 10px;
  background: #e9f6f2;
  color: #287b68;
  font-size: 0.78rem;
  font-weight: 900;
  white-space: nowrap;
}

.health-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

label {
  display: grid;
  gap: 7px;
  color: #20303f;
  font-weight: 800;
}

.wide {
  grid-column: 1 / -1;
}

input,
textarea {
  width: 100%;
  border: 1px solid rgba(32, 48, 63, 0.14);
  border-radius: 8px;
  padding: 11px 12px;
  color: #20303f;
  resize: vertical;
}

button {
  min-height: 42px;
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
  background: #2d8f7b;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

.feedback {
  border-radius: 8px;
  padding: 10px 12px;
  background: #e9f6f2;
  color: #287b68;
  font-weight: 800;
}

.placeholder-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.placeholder-grid button {
  background: #edf2f7;
  color: #405265;
}

@media (max-width: 720px) {
  .health-form,
  .placeholder-grid {
    grid-template-columns: 1fr;
  }
}
</style>
