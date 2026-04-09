<template>
  <div class="signup-container">
    <!-- Left Panel with Background Image and Overlay -->
    <div class="left-panel">
      <div class="overlay"></div>
    </div>

    <!-- Right Panel with Signup Form -->
    <div class="right-panel">
      <div class="form-container">
        <!-- Branding -->
        <h1 class="app-title">KinderCare Connect</h1>
        <p class="app-subtitle">Safe health communication between parents and kindergarten staff</p>

        <!-- Signup Form -->
        <form @submit.prevent="submitForm" class="signup-form">
          <!-- Full Name -->
          <div class="input-group">
            <label for="fullName">Full Name</label>
            <input
              id="fullName"
              type="text"
              v-model="fullName"
              placeholder="Enter your full name"
              required
            />
          </div>

          <!-- Email -->
          <div class="input-group">
            <label for="email">Email</label>
            <input
              id="email"
              type="email"
              v-model="email"
              placeholder="Enter your email"
              required
            />
          </div>

          <!-- Password -->
          <div class="input-group">
            <label for="password">Password</label>
            <input
              id="password"
              type="password"
              v-model="password"
              placeholder="Create a password"
              required
            />
          </div>

          <!-- Confirm Password -->
          <div class="input-group">
            <label for="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              v-model="confirmPassword"
              placeholder="Confirm your password"
              required
            />
          </div>

          <!-- Phone Number -->
          <div class="input-group">
            <label for="phoneNumber">Phone Number</label>
            <input
              id="phoneNumber"
              type="tel"
              v-model="phoneNumber"
              placeholder="Enter your phone number"
              required
            />
          </div>

          <!-- Terms and Privacy Checkbox -->
          <div class="checkbox-group">
            <input
              id="agreeToTerms"
              type="checkbox"
              v-model="agreeToTerms"
              required
            />
            <label for="agreeToTerms">I agree to the Terms and Privacy Policy</label>
          </div>

          <!-- Submit Button -->
          <button type="submit" class="signup-btn">Sign Up</button>

          <!-- Sign In Link -->
          <p class="signin-link">
            Already have an account? <a href="#" @click.prevent>Sign in</a>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SignupView',
  data() {
    return {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
      phoneNumber: '',
      agreeToTerms: false
    };
  },
  methods: {
    submitForm() {
      // Log form data to console as required
      console.log({
        fullName: this.fullName,
        email: this.email,
        password: this.password,
        confirmPassword: this.confirmPassword,
        phoneNumber: this.phoneNumber,
        agreeToTerms: this.agreeToTerms
      });
    }
  }
};
</script>

<style scoped>
/* Root Variables for Color Palette */
:root {
  --light-blue: #87CEEB;
  --soft-green: #98FB98;
  --pastel-purple: #DDA0DD;
  --white: #FFFFFF;
  --shadow-color: rgba(0, 0, 0, 0.1);
  --text-color: #333333;
  --border-color: #CCCCCC;
}

/* Main Container */
.signup-container {
  display: flex;
  height: 100vh;
  font-family: 'Arial', sans-serif;
}

/* Left Panel - Background Image with Overlay */
.left-panel {
  flex: 1;
  background-image: url('https://images.unsplash.com/photo-1544717297-fa95b6ee9643?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(135, 206, 235, 0.5), rgba(221, 160, 221, 0.5));
}

/* Right Panel */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--white);
  padding: 20px;
}

/* Form Container */
.form-container {
  max-width: 400px;
  width: 100%;
  padding: 40px;
  background: var(--white);
  border-radius: 10px;
  box-shadow: 0 4px 20px var(--shadow-color);
}

/* Branding */
.app-title {
  font-size: 2.5rem;
  color: var(--pastel-purple);
  margin-bottom: 10px;
  text-align: center;
  font-weight: bold;
}

.app-subtitle {
  font-size: 1rem;
  color: var(--text-color);
  margin-bottom: 30px;
  text-align: center;
  line-height: 1.5;
}

/* Form Styles */
.signup-form {
  display: flex;
  flex-direction: column;
}

/* Input Groups */
.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  color: var(--text-color);
  font-weight: 500;
}

.input-group input {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-bottom: 2px solid var(--border-color);
  background: transparent;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.input-group input:focus {
  outline: none;
  border-bottom-color: var(--light-blue);
}

/* Checkbox Group */
.checkbox-group {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.checkbox-group input[type="checkbox"] {
  margin-right: 10px;
  width: 16px;
  height: 16px;
}

.checkbox-group label {
  color: var(--text-color);
  font-size: 0.9rem;
  cursor: pointer;
}

/* Submit Button */
.signup-btn {
  background: linear-gradient(135deg, var(--light-blue), var(--soft-green));
  color: var(--white);
  border: none;
  padding: 12px 20px;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-bottom: 20px;
}

.signup-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px var(--shadow-color);
}

/* Sign In Link */
.signin-link {
  text-align: center;
  color: var(--text-color);
  font-size: 0.9rem;
}

.signin-link a {
  color: var(--pastel-purple);
  text-decoration: none;
  font-weight: bold;
  transition: color 0.3s ease;
}

.signin-link a:hover {
  color: var(--light-blue);
}

/* Responsive Design */
@media (max-width: 768px) {
  .signup-container {
    flex-direction: column;
  }

  .left-panel {
    flex: none;
    height: 200px;
  }

  .right-panel {
    flex: 1;
  }

  .form-container {
    padding: 20px;
    margin: 20px;
  }

  .app-title {
    font-size: 2rem;
  }

  .app-subtitle {
    font-size: 0.9rem;
  }
}
</style>
