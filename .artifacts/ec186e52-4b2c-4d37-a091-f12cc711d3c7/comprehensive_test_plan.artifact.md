# Comprehensive Test Plan: Sign-In v4.0

This document outlines the testing strategy for the professional Sign-In application, focusing on core functionality, UI transitions, data integrity, and administrative controls.

---

## 1. Functional Testing: Main Sign-In Screen

### 1.1 Core Sign-In Workflow
- [ ] **Standard Member Sign-In:** Enter a name from the uploaded database, select a Role or Location, and tap "Sign-In". Verify the entry appears in the correct list (A-L or M-Z).
- [ ] **Guest Sign-In Validation:** Attempt to sign in a name not in the database.
    - [ ] Verify it requires the "AA AAA" format (2+ letters first part, 3+ letters second part).
    - [ ] Verify it appends "(Guest)" to the name in the list.
- [ ] **Predictive Search:** Start typing a name. Verify the dropdown appears with matching entries from the Members database.
- [ ] **Role/Location Logic:**
    - [ ] Verify selecting a Role clears the Location field.
    - [ ] Verify selecting a Location clears the Role field.

### 1.2 Sign-Out Workflow
- [ ] **Manual Sign-Out:** Tap a name in the list. Confirm the "Sign-Out?" dialog appears. confirm and verify the name is removed from the screen and added to the History.
- [ ] **Maintenance Questions:** Verify that if only one person is signed in, the "Checklist" questions from the Questions database appear in the sign-out dialog.

### 1.3 Car Registration Reminder
- [ ] **Pop-up Trigger:** Sign in a user. Verify the "CAR REG?" pop-up appears for the duration set in Admin settings (e.g., 2 seconds).
- [ ] **Formatting:** Verify the text styling on the pop-up matches the "UK Plate" yellow background requirement.

---

## 2. Administrative & Security Testing

### 2.1 Access Control
- [ ] **PIN Entry:** Type the current PIN (default "1925") into the Name field. Verify the Admin screen opens immediately.
- [ ] **PIN Change:** Change the PIN in Admin settings. Verify the old PIN no longer works and the new one does.

### 2.2 Appearance & UI Configuration
- [ ] **Theme Selection:** Select the 3 professional colors (Slate Blue, Mint Aqua, Lavender). Verify the background color updates instantly across the app.
- [ ] **Screen Flip:** Toggle the "Flip (180°)" switch. Verify the tablet rotation changes to Reverse Portrait.
- [ ] **Screen Saver:**
    - [ ] Type "@@" in the Name field to trigger manually.
    - [ ] Wait 5 minutes with no interaction (and everyone signed out) to verify automatic trigger.
    - [ ] Verify tapping the screen saver dismisses it.

### 2.3 Data Management (Excel Uploads)
- [ ] **File Loading:** Upload new Excel files for:
    - [ ] **Members:** (Col A/B format).
    - [ ] **Cast:** (Row 1 Title, optional Row 2 Date).
    - [ ] **Roles/Locations:** (Col A format).
- [ ] **Status Indicators:** Verify the buttons in the "Data Management" tile turn Green when data is successfully loaded.

---

## 3. Layout & UX Transitions

### 3.1 "No Cast" vs. "Live" Mode
- [ ] **Dark Mode (No Cast):** Clear the cast list (type "##" or upload an empty file).
    - [ ] Verify the Left and Right panels sit flush side-by-side.
    - [ ] Verify the fine vertical divider line is visible.
    - [ ] Verify the A-L and M-Z watermarks are correctly positioned.
- [ ] **Live Mode (With Cast):** Load a cast list.
    - [ ] Verify the panels spread out with the 16.dp gap.
    - [ ] Verify the Right panel switches from "M-Z" to the "Cast" member list.
- [ ] **Independent Scrolling:** Populate both lists with many names. Verify that scrolling one list does not affect the other.

---

## 4. Automation & Data Persistence

### 4.1 Daily Maintenance
- [ ] **Auto Sign-Out:** Set the auto-maintenance time in Admin to 2 minutes from the current time. Verify all visitors are signed out automatically.
- [ ] **Auto-Reset (Performance Date):** Load a Cast list with a "Row 2" date set to yesterday.
    - [ ] Restart the app. Verify the list clears automatically on launch (Passive Reset).
    - [ ] Wait for the 10-second periodic check. Verify it clears without a restart.

### 4.2 Data Export
- [ ] **Excel Export:** Tap "EXPORT" in the Admin bar. Select a destination. Verify the resulting `.xlsx` file contains Name, In/Out times, and Vehicle Reg.

---

## 5. Edge Case & Robustness Testing

- [ ] **App Restart:** Sign in multiple people and change the theme color. Kill the app and restart. Verify all people remain signed in and settings are preserved.
- [ ] **Empty Databases:** Clear all databases. Verify the app displays "Data Missing" or "Visitor Sign-In" gracefully without crashing.
- [ ] **Permissions:** Deny "Display over other apps" permission. Verify the setup dialog appears explaining how to enable it.
- [ ] **Xmas Check (Regression):** Type "Xmas" in the name field. Verify **nothing happens** (confirming full removal of novelty code).
- [ ] **Large Datasets:** Load a Members list with 500+ names. Verify the predictive search remains fast and doesn't lag.
