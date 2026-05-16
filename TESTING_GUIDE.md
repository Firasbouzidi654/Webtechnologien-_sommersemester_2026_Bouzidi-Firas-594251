# Frontend Testing Guide

## Launch the project

1. Open a terminal in the repository root.
2. Start the frontend:

```bash
cd frontend
npm install
npm run dev
```

3. Open the browser at the local Vite URL shown in the terminal (usually `http://localhost:5173`).

## Role selection

- Open `/` in the browser.
- Choose either **Parent** or **Staff** from the homepage role cards.
- Expected: selecting a role navigates to the corresponding dashboard without a separate `/login` page.

## Parent dashboard

- On the parent dashboard, verify the profile avatar upload works.
- Add a child photo during the child creation flow and confirm the photo appears in the child card.
- Confirm the medication history panel shows previously logged medication entries.
- Expected: the page stays responsive and the form controls are accessible.

## Admin dashboard

- Open the staff/admin dashboard and confirm the top section shows medication task stats.
- Click a medication task confirmation control to open the verification modal.
- Enter a valid medication ID such as `MED-001`, or type the displayed QR payload text string.
- Expected: verification succeeds, the task status updates, and a verification log entry is recorded.

## QR/ID verification

- In the admin dashboard, use the verification modal to test both:
  - direct medication ID input
  - paste or type a QR payload string
- Expected: matching values show success, mismatched values show a clear error message.

## Verification history

- In the admin sidebar, open the **Verification history** panel.
- Expected: each verification entry shows medication ID, method (`ID` / `QR`), staff name, and formatted timestamp.
- Confirm the newest verifications appear at the top.

## Emergency mode and route buttons

- Open the admin dashboard and click **Emergency mode**.
- In the modal, select a child and verify the child summary and map load.
- Confirm the nearby emergency POIs list displays cards for hospitals, pharmacies, and police stations.
- Click a **Show route** button on a POI card.
- Expected: a new tab opens with OpenStreetMap driving directions from the child's location to the selected POI.

## Dark mode and responsive mode

- Toggle browser dark mode or use a dark theme if available in the app.
- Expected: the app retains readable contrast and the dashboard still renders correctly.
- Resize the browser to mobile widths (320px to 720px).
- Expected: modals, cards, and emergency panels stack cleanly and all buttons remain visible.

## Notes for presentation testing

- Focus on the role selection landing page, parent avatar/photo support, admin verification modal, emergency POI route cards, and verification log panel.
- The app uses the existing Vue.js frontend stack only; there is no backend migration required for these UI flows.
