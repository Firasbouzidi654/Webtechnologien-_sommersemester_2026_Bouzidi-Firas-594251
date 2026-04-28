<template>
  <article class="contact-card">
    <div>
      <span class="priority">Priority {{ contact.priority }}</span>
      <h3>{{ contact.name }}</h3>
      <p>{{ contact.relationship }}</p>
    </div>
    <dl>
      <div>
        <dt>Phone</dt>
        <dd>{{ contact.phone }}</dd>
      </div>
      <div>
        <dt>Email</dt>
        <dd>{{ contact.email }}</dd>
      </div>
    </dl>
  </article>
</template>

<script>
export default {
  name: 'EmergencyContactCard',
  props: {
    contact: {
      type: Object,
      required: true
    }
  }
};
</script>

<style scoped>
.contact-card {
  display: grid;
  gap: 14px;
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.priority {
  display: inline-flex;
  border-radius: 999px;
  padding: 4px 9px;
  background: #ffe8a8;
  color: #5d4b12;
  font-size: 0.78rem;
  font-weight: 800;
}

h3 {
  margin: 10px 0 2px;
  font-size: 1rem;
}

p,
dl,
dd {
  margin: 0;
}

dl {
  display: grid;
  gap: 8px;
}

dt {
  color: #637486;
  font-size: 0.78rem;
  font-weight: 800;
}

dd {
  overflow-wrap: anywhere;
  font-weight: 700;
}
</style>
