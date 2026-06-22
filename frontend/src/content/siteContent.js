export const translations = {
  en: {
    common: {
      brandLead: 'Making health communication',
      brandAccent: 'feel safe, simple, and human.',
      languageLabel: 'Language',
      lightMode: 'Light mode',
      darkMode: 'Dark mode',
      privacyLink: 'Privacy Policy',
      backToSignUp: 'Back to Sign Up',
      goToSignIn: 'Go to Sign In',
      goToSignUp: 'Create an account'
    },
    hero: {
      eyebrow: 'Child health overview',
      title: 'A clear view of child health and medication',
      text: 'KinderCare Connect helps parents and staff view child profiles, allergies, and medications.',
      image: 'https://images.unsplash.com/photo-1516627145497-ae6968895b74?auto=format&fit=crop&w=1200&q=80',
      highlights: [
        { id: 1, icon: '👶', text: 'Child profiles' },
        { id: 2, icon: '🩺', text: 'Allergies' },
        { id: 3, icon: '💊', text: 'Medication overview' },
        { id: 4, icon: '📋', text: 'Daily task view' }
      ]
    },
    signup: {
      badge: 'Create account',
      title: 'Register for KinderCare Connect',
      subtitle: 'Create an account to access the child and medication dashboards.',
      fullNameLabel: 'Full name',
      fullNamePlaceholder: 'e.g. Sofia Becker',
      emailLabel: 'Email address',
      emailPlaceholder: 'parent@example.com',
      passwordLabel: 'Password',
      passwordPlaceholder: 'At least 8 characters',
      confirmPasswordLabel: 'Confirm password',
      confirmPasswordPlaceholder: 'Repeat your password',
      checkboxText: 'I agree to the',
      submitButton: 'Create account',
      loadingButton: 'Creating secure account',
      successButton: 'Account ready',
      alternatePrompt: 'Already have an account?',
      alternateAction: 'Sign in',
      successMessage: 'Your account details passed validation and are ready for secure registration.',
      validationError: 'Please review the highlighted registration details.',
      fullNameRequired: 'Enter your full name.',
      emailRequired: 'Enter your email address.',
      emailInvalid: 'Enter a valid email address.',
      passwordRequired: 'Create a password.',
      passwordTooWeak: 'Use a stronger password before continuing.',
      confirmPasswordRequired: 'Confirm your password.',
      mismatchError: 'The passwords do not match yet.',
      termsRequired: 'Please accept the privacy policy to continue.',
      showPassword: 'Show password',
      hidePassword: 'Hide password',
      capsLockWarning: 'Caps Lock is on.',
      strengthEmpty: 'Password strength',
      strengthWeak: 'Weak',
      strengthMedium: 'Medium',
      strengthStrong: 'Strong',
      passwordHelperTitle: 'Password requirements',
      passwordRequirementLength: 'At least 8 characters',
      passwordRequirementCase: 'Uppercase and lowercase letters',
      passwordRequirementNumber: 'At least one number',
      passwordRequirementSpecial: 'A symbol or 14+ characters',
      passwordRequirementMatch: 'Passwords match',
      helperTitle: 'Why families use it',
      helperItems: [
        'Create child profiles with recorded allergies.',
        'Add medications with a child and dosage.',
        'Use the staff dashboard for the daily medication overview.'
      ]
    },
    signin: {
      badge: 'Welcome back',
      title: 'Sign in to your account',
      subtitle: 'Access the child and medication dashboards.',
      emailLabel: 'Email address',
      emailPlaceholder: 'parent@example.com',
      passwordLabel: 'Password',
      passwordPlaceholder: 'Enter your password',
      rememberMe: 'Keep me signed in on this device',
      submitButton: 'Sign in',
      loadingButton: 'Checking credentials',
      successButton: 'Opening dashboard',
      alternatePrompt: 'New to KinderCare Connect?',
      alternateAction: 'Create an account',
      successMessage: 'Sign-in verified. Opening your dashboard.',
      invalidCredentials: 'We could not sign you in. Please check your email and password.',
      validationError: 'Please review the highlighted sign-in details.',
      emailRequired: 'Enter the email linked to your account.',
      emailInvalid: 'Enter a valid email address.',
      passwordRequired: 'Enter your password.',
      passwordTooShort: 'Use at least 8 characters.',
      showPassword: 'Show password',
      hidePassword: 'Hide password',
      capsLockWarning: 'Caps Lock is on.',
      roleLabel: 'Continue as',
      parentRole: 'Parent',
      parentRoleDescription: 'Family dashboard',
      staffRole: 'Staff',
      staffRoleDescription: 'Care team workspace',
      securityTitle: 'Security indicators',
      securityBadges: ['Password-protected account', 'PostgreSQL data storage', 'Child and medication overview']
    },
    features: {
      title: 'Key features',
      items: [
        { id: 1, icon: '👶', text: 'Child profiles' },
        { id: 2, icon: '🩺', text: 'Allergy overview' },
        { id: 3, icon: '💊', text: 'Medication overview' },
        { id: 4, icon: '📋', text: 'Daily task view' }
      ]
    },
    privacy: {
      badge: 'Privacy Policy',
      title: 'How KinderCare Connect protects your information',
      intro: 'KinderCare Connect is a course project for managing basic child and medication information.',
      sections: [
        {
          heading: 'Information we collect',
          text: 'The application stores an email address, a password hash, a role, child names, allergies, medication names, and dosages.'
        },
        {
          heading: 'How we use information',
          text: 'The information is displayed in the parent and staff dashboards to support the course-project workflow.'
        },
        {
          heading: 'Security',
          text: 'Passwords are stored as BCrypt hashes. This prototype is not intended for real childcare or production medical data.'
        },
        {
          heading: 'Contact',
          text: 'If you have questions about privacy or data handling, please contact the KinderCare Connect support team through your kindergarten administrator.'
        }
      ]
    }
  },
  de: {
    common: {
      brandLead: 'Gesundheitskommunikation',
      brandAccent: 'sicher, einfach und menschlich gestalten.',
      languageLabel: 'Sprache',
      lightMode: 'Heller Modus',
      darkMode: 'Dunkler Modus',
      privacyLink: 'Datenschutzrichtlinie',
      backToSignUp: 'Zurück zur Registrierung',
      goToSignIn: 'Zum Login',
      goToSignUp: 'Konto erstellen'
    },
    hero: {
      eyebrow: 'Gesundheitsübersicht für Kinder',
      title: 'Kinder, Allergien und Medikamente übersichtlich verwalten',
      text: 'KinderCare Connect unterstützt Eltern und Kindergartenpersonal bei der Übersicht über Kinderprofile, Allergien und Medikamente.',
      image: 'https://images.unsplash.com/photo-1516627145497-ae6968895b74?auto=format&fit=crop&w=1200&q=80',
      highlights: [
        { id: 1, icon: '👶', text: 'Kinderprofile' },
        { id: 2, icon: '🩺', text: 'Allergien' },
        { id: 3, icon: '💊', text: 'Medikationsübersicht' },
        { id: 4, icon: '📋', text: 'Tagesansicht' }
      ]
    },
    signup: {
      badge: 'Konto erstellen',
      title: 'Bei KinderCare Connect registrieren',
      subtitle: 'Erstellen Sie ein Konto für das Kinder- und Medikamenten-Dashboard.',
      fullNameLabel: 'Vollständiger Name',
      fullNamePlaceholder: 'z. B. Sofia Becker',
      emailLabel: 'E-Mail-Adresse',
      emailPlaceholder: 'eltern@example.com',
      passwordLabel: 'Passwort',
      passwordPlaceholder: 'Mindestens 8 Zeichen',
      confirmPasswordLabel: 'Passwort bestätigen',
      confirmPasswordPlaceholder: 'Passwort wiederholen',
      checkboxText: 'Ich stimme der',
      submitButton: 'Konto erstellen',
      loadingButton: 'Sicheres Konto wird erstellt',
      successButton: 'Konto bereit',
      alternatePrompt: 'Sie haben bereits ein Konto?',
      alternateAction: 'Anmelden',
      successMessage: 'Ihre Kontodaten wurden geprueft und sind bereit fuer die sichere Registrierung.',
      validationError: 'Bitte pruefen Sie die markierten Registrierungsdaten.',
      fullNameRequired: 'Geben Sie Ihren vollstaendigen Namen ein.',
      emailRequired: 'Geben Sie Ihre E-Mail-Adresse ein.',
      emailInvalid: 'Geben Sie eine gueltige E-Mail-Adresse ein.',
      passwordRequired: 'Erstellen Sie ein Passwort.',
      passwordTooWeak: 'Verwenden Sie ein staerkeres Passwort, bevor Sie fortfahren.',
      confirmPasswordRequired: 'Bestaetigen Sie Ihr Passwort.',
      termsRequired: 'Bitte akzeptieren Sie die Datenschutzrichtlinie, um fortzufahren.',
      showPassword: 'Passwort anzeigen',
      hidePassword: 'Passwort ausblenden',
      capsLockWarning: 'Die Feststelltaste ist aktiv.',
      strengthEmpty: 'Passwortstaerke',
      strengthWeak: 'Schwach',
      strengthMedium: 'Mittel',
      strengthStrong: 'Stark',
      passwordHelperTitle: 'Passwortanforderungen',
      passwordRequirementLength: 'Mindestens 8 Zeichen',
      passwordRequirementCase: 'Gross- und Kleinbuchstaben',
      passwordRequirementNumber: 'Mindestens eine Zahl',
      passwordRequirementSpecial: 'Ein Symbol oder 14+ Zeichen',
      passwordRequirementMatch: 'Passwoerter stimmen ueberein',
      mismatchError: 'Die Passwörter stimmen noch nicht überein.',
      helperTitle: 'Warum Familien es nutzen',
      helperItems: [
        'Kinderprofile mit Allergien anlegen.',
        'Medikamente mit Kind und Dosierung erfassen.',
        'Die Tagesansicht für Medikamente nutzen.'
      ]
    },
    signin: {
      badge: 'Willkommen zurück',
      title: 'In Ihr Konto einloggen',
      subtitle: 'Greifen Sie auf das Kinder- und Medikamenten-Dashboard zu.',
      emailLabel: 'E-Mail-Adresse',
      emailPlaceholder: 'eltern@example.com',
      passwordLabel: 'Passwort',
      passwordPlaceholder: 'Passwort eingeben',
      rememberMe: 'Auf diesem Gerät angemeldet bleiben',
      submitButton: 'Anmelden',
      loadingButton: 'Zugang wird geprueft',
      successButton: 'Dashboard wird geoeffnet',
      alternatePrompt: 'Neu bei KinderCare Connect?',
      alternateAction: 'Konto erstellen',
      successMessage: 'Login bestaetigt. Ihr Dashboard wird geoeffnet.',
      invalidCredentials: 'Die Anmeldung war nicht erfolgreich. Bitte pruefen Sie E-Mail und Passwort.',
      validationError: 'Bitte pruefen Sie die markierten Login-Daten.',
      emailRequired: 'Geben Sie die E-Mail-Adresse Ihres Kontos ein.',
      emailInvalid: 'Geben Sie eine gueltige E-Mail-Adresse ein.',
      passwordRequired: 'Geben Sie Ihr Passwort ein.',
      passwordTooShort: 'Verwenden Sie mindestens 8 Zeichen.',
      showPassword: 'Passwort anzeigen',
      hidePassword: 'Passwort ausblenden',
      capsLockWarning: 'Die Feststelltaste ist aktiv.',
      roleLabel: 'Fortfahren als',
      parentRole: 'Elternteil',
      parentRoleDescription: 'Familien-Dashboard',
      staffRole: 'Team',
      staffRoleDescription: 'Arbeitsbereich Betreuung',
      securityTitle: 'Sicherheitsmerkmale',
      securityBadges: ['Passwortgeschütztes Konto', 'PostgreSQL-Datenspeicherung', 'Kinder- und Medikationsübersicht']
    },
    features: {
      title: 'Wichtige Funktionen',
      items: [
        { id: 1, icon: '👶', text: 'Kinderprofile' },
        { id: 2, icon: '🩺', text: 'Allergieübersicht' },
        { id: 3, icon: '💊', text: 'Medikationsübersicht' },
        { id: 4, icon: '📋', text: 'Tagesansicht' }
      ]
    },
    privacy: {
      badge: 'Datenschutzrichtlinie',
      title: 'So schützt KinderCare Connect Ihre Informationen',
      intro: 'KinderCare Connect ist ein Kursprojekt zur Verwaltung grundlegender Kinder- und Medikationsdaten.',
      sections: [
        {
          heading: 'Welche Informationen wir erfassen',
          text: 'Die Anwendung speichert E-Mail-Adresse, Passwort-Hash, Rolle, Kindernamen, Allergien sowie Medikamentennamen und Dosierungen.'
        },
        {
          heading: 'Wie wir Informationen verwenden',
          text: 'Die Daten werden im Eltern- und Personal-Dashboard für den Ablauf des Kursprojekts angezeigt.'
        },
        {
          heading: 'Sicherheit',
          text: 'Passwörter werden als BCrypt-Hashes gespeichert. Der Prototyp ist nicht für echte Betreuungs- oder Gesundheitsdaten vorgesehen.'
        },
        {
          heading: 'Kontakt',
          text: 'Bei Fragen zum Datenschutz oder zur Datenverarbeitung wenden Sie sich bitte über Ihre Kindergartenverwaltung an das Support-Team von KinderCare Connect.'
        }
      ]
    }
  }
};
