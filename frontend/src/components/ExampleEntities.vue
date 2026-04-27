<template>
  <section class="example-entities" aria-labelledby="example-entities-title">
    <div class="example-entities-header">
      <p class="eyebrow">Sample entities</p>
      <h2 id="example-entities-title">Example Entities</h2>
    </div>

    <ul class="entity-list">
      <li v-for="child in children" :key="child.id" class="entity-card">
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

  data() {
    return {
      openChildId: null,

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

.example-entities-header {
  margin-bottom: 18px;
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
  border: 1px solid rgba(34, 62, 79, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 14px 30px rgba(32, 48, 63, 0.08);
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
  color: #20303f;
  font-size: 1.08rem;
}

.entity-card dl {
  display: grid;
  gap: 12px;
  margin: 0;
}

.entity-card dt {
  color: rgba(91, 107, 123, 1);
  font-size: 0.82rem;
  font-weight: 800;
}

.entity-card dd {
  margin: 3px 0 0;
  color: #20303f;
  font-weight: 600;
}

@media (max-width: 820px) {
  .entity-list {
    grid-template-columns: 1fr;
  }
}
</style>
