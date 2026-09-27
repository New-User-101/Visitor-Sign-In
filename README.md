# Sign-In v4.0 – User & Technical Guide

This guide explains how to use and maintain the Sign-In application.

---

## Main Screen (Sign-In Interface)

- **[Version & Branding]**
  - **Minimal UI:** The main screen is designed to be as clean as possible. Top corners are intentionally left empty.
  - **Admin Access:** To access settings, type your 4-digit PIN (default **1925**) into the **Name** input box. This will immediately navigate to the Admin area.

- **[Signing In & Safeguards]**
  - **Name Entry & Prediction:** Start typing your name. A dropdown will appear with matching names from the database.
  - **Duplicate Prevention:** If a person attempts to sign in while already checked in, an **"Already Signed In"** pop-up warning appears and the name box resets automatically.
  - **Guest Validation:** If you are a Guest (name not in database), the name must be at least **"AA AAA"** (2 letters for the first part, 3 for the second) to enable sign-in.
  - **Role/Location:** Select either a **Role** or a **Location** (only one is required).
  - **Sign-In Button:** Turns bright green when all required fields are valid.

- **[Vehicle Registration]**
  - **Memory:** The app automatically remembers your last used registration number for your name.
  - **Confirmation:** If enabled, a confirmation dialog appears before sign-in.
  - **Display:** On your sign-in tile, the registration number is stacked neatly below your sign-in time.

- **[Lists & Sorting]**
  - **Guest Sorting:** Guest visitors are always pinned to the **top** of both Role and Location sections.
  - **Live Mode (With Cast):** Visitors are listed on the left (with a separating bar between Role and Youth/Location entries), and Cast is on the right.
  - **Dark Mode (No Cast):** When no cast list is loaded, visitors are dynamically split across Left and Right panels based on the median alphabetical letter of the uploaded Members database (e.g., A-J and K-Z), separated into Role and Location sections with a theme-colored bar.

---

## Admin Screen (Configuration)

- **[Design & Appearance]**
  - **Themes & Modes:** Choose from four modes working left to right:
    1. **Theatre** (`#2BBCF2`)
    2. **Rehearsals** (`#75FDB4`)
    3. **Youth** (`#85EFC8`)
    4. **Dark** (`#71A0F1`)
  - **Color Selection Engine:** Tap any theme swatch to open the built-in RGB color sliders and pick any custom color. Text contrast automatically adapts (white on dark backgrounds, black on light backgrounds).
  - **Screen Saver:** Test the screen saver directly from the Appearance tile.

- **[Data Management & Warehouse Mode]**
  - **Database Files:**
    - **Members:** Column A (First Name) & Column B (Last Name).
    - **Cast:** Row 1 = Title, Row 2 = Last Performance Date (dd/mm/yyyy format), Row 3+ = Names.
    - **Roles & Locations:** Column A.
    - **Titles & Questions:** Column B.
  - **Warehouse Mode:** Toggle Warehouse mode in Admin to switch between Theatre worksheets and Warehouse worksheets (`GPT` / `YT` toggle button).
  - **Automated Reset:** If Row 2 of the Cast file has a date that has passed (evaluated at the scheduled Auto Signout Time, default 04:00), the app automatically resets the Cast list and title.

- **[Automation & Auto-Start]**
  - **Daily Maintenance & Auto Signout:** Scheduled at your choice (default **04:00**) to sign out everyone, refresh databases, and save daily attendance logs into the `Records` subfolder.
  - **Log Save Folder:** Displays the clean path (`Documents/Sign-In Data/Admin/`).
  - **Display Over Other Apps (Auto-Start):** Required for automatic startup over the lock screen. If grayed out on Android 11+, tap the top-right three dots (`⋮`) and select **"Allow restricted settings"** first.
  - **Tablet Auto-Restart & Battery Optimization Note:** On certain tablets (Samsung, Lenovo, etc.), OEM battery management may terminate apps launched automatically via boot receivers. To ensure the app stays running after reboot, go to **Settings > Apps > Sign-In v4.0 > Battery**, set it to **"Unrestricted"**, and enable **Auto-start / Allow background activity** in device care.

---

## Technical Information for Developers

- **Framework:** Jetpack Compose (Modern Android UI).
- **Architecture:** `Repository.kt` as Single Source of Truth.
- **Persistence:** All databases are persisted locally in JSON format to ensure offline availability.
- **Excel Processing:** Powered by **Apache POI**. All data files are expected in Documents.
