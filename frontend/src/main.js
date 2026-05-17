import { createApp } from 'vue'
import './style.css'
import './styles/dashboard.css'
import 'leaflet/dist/leaflet.css'
import App from './App.vue'

// Simple dev-only global error overlay to surface runtime exceptions
function showDevError(message) {
	try {
		let el = document.getElementById('dev-error-overlay');
		if (!el) {
			el = document.createElement('div');
			el.id = 'dev-error-overlay';
			el.style.position = 'fixed';
			el.style.left = '12px';
			el.style.right = '12px';
			el.style.bottom = '12px';
			el.style.zIndex = 99999;
			el.style.background = 'rgba(255,230,230,0.95)';
			el.style.border = '1px solid #ffb3b3';
			el.style.padding = '12px 16px';
			el.style.borderRadius = '8px';
			el.style.color = '#660000';
			el.style.fontFamily = 'Segoe UI, Arial, sans-serif';
			el.style.boxShadow = '0 6px 18px rgba(0,0,0,0.08)';
			document.body.appendChild(el);
		}

		el.textContent = `Runtime error: ${message}`;
	} catch (e) {
		// ignore
	}
}

window.addEventListener('error', (ev) => {
	// ev.error may be undefined for some runtime errors
	const msg = ev.error?.message || ev.message || String(ev);
	// eslint-disable-next-line no-console
	console.error('Global error captured:', ev.error || ev.message || ev);
	showDevError(msg);
});

window.addEventListener('unhandledrejection', (ev) => {
	const msg = ev.reason?.message || String(ev.reason);
	// eslint-disable-next-line no-console
	console.error('Unhandled rejection:', ev.reason);
	showDevError(msg);
});

createApp(App).mount('#app')
