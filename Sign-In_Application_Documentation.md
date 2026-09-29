# Sign-In Application v4.0 - Professional Documentation

## Table of Contents
1. Overview
2. System Requirements
3. Installation and Setup
4. User Interface Overview
5. Core Functionality & Safeguards
6. Administrative Features & Customization
7. Visual Color Coding & High-Contrast Text
8. Data Management & File Layouts (Cast, Roles, Locations, Warehouse)
9. Special Input Codes
10. Log Retention & Maintenance
11. Troubleshooting
12. Technical Specifications

---

## 1. Overview

Sign-In v4.0 is a robust digital attendance system designed for professional environments like theatre companies and offices. It features real-time tracking, full offline persistence, automated data management via synced Excel files, custom theme color engine, and robust sign-in safeguards.

---

## 2. System Requirements
- **Device**: Android tablet (API 29+ / Android 10+).
- **Storage**: Minimum 500MB available.
- **Sync**: A helper app (e.g., DriveSync) is recommended to keep local files synchronized with Google Drive.

---

## 3. Installation and Setup

### Administrative Access
- **Default PIN**: 1925
- **Admin Access Method**: Type your 4-digit PIN (default **1925**) into the **Name** input box on the main screen to open the Admin area.

### Auto-Start & Display Over Other Apps Permission
- To allow the app to automatically launch and display over the lock screen upon device startup, the **"Display over other apps"** permission is required.
- **Restricted Settings Override**: On Android 11+ tablets where the permission switch appears grayed out or disabled:
  1. Tap the **three dots (`⋮`)** at the top right corner of the Android settings screen.
  2. Select **"Allow restricted settings"**.
  3. Return to the settings page and enable the permission toggle for Sign-In v4.0.

### Tablet Auto-Restart & Battery Optimization Note
- On certain tablets (Samsung, Lenovo, etc.), OEM background process managers may terminate apps launched automatically via boot receivers. 
- **Action**: Go to **Settings > Apps > Sign-In v4.0 > Battery**, set it to **"Unrestricted"**, and enable **Auto-start / Allow background activity** in device settings.

---

## 4. User Interface Overview
- **Header**: Minimalist design with top corners left empty. 
- **Dual Panels**: Visitors/Members on the Left, Cast list on the Right.
- **Instruction Block**: Simplified sign-in and sign-out guidance.

---

## 5. Core Functionality & Safeguards

### 5.1 Sign-In, Out & Duplicate Prevention
- **Predictions**: Start typing a name to see matches from your "Members" database.
- **Duplicate Safeguard**: If a person attempts to sign in while already checked in, an **"Already Signed In"** alert dialog pops up and the name box automatically resets.
- **Guest Rules**: New guests must enter a first and last name (AA AAA format).
- **Vehicle Reg**: Remembers your car registration and shows it on your attendance tile.
- **Last Person Out**: Automatically shows a checklist to the final person signing out.

---

## 6. Administrative Features & Customization

### 6.1 Theme Color Customization
- Go to **Admin > Appearance**, tap **Click to change** on the right of the tile, and use the built-in **RGB Color Sliders** engine to choose any custom theme color.
- **Automatic High-Contrast Text**: Text elements (headers, titles, instructions) automatically switch to crisp white on dark backgrounds and black on light backgrounds based on perceptual luminance.

---

## 7. Visual Color Coding
- **#D7EFF9 (Light Blue)**: Guest sign-ins.
- **#BEE9AD (Light Green)**: Roles and Active Cast members.
- **#DAF2D0 (Light Sage Green)**: Locations.
- **#FD958D (Red)**: Inactive/Absent Cast members (pinned to the top).

---

## 8. Data Management & File Layouts

All files must be in Excel (`.xlsx`) format.

### Cast File (RH Side)
- **Row 1**: Play Title.
- **Row 2**: **Last Performance Date** (dd/mm/yyyy format). If today is after this date, the list resets automatically during maintenance (default 04:00 AM).
- **Row 3+**: Names.

### Members File
- **Column A**: First Name.
- **Column B**: Last Name.
*(The app automatically sorts members alphabetically and computes the dynamic midpoint split letter for Dark mode panels).*

### Roles & Locations Files
- **Column A**: Role or Location names (Row 2+).

### Titles & Questions Files
- **Column B**: Configuration rows.

### Warehouse Mode
- When Warehouse Mode is enabled in Admin, the app switches to Warehouse worksheets and enables the `GPT` / `YT` toggle button on the sign-in form.

---

## 9. Special Input Codes
Entered into the **Name** input box:
- **`##`**: Performs a full reset of the Cast list and restores the Default Title.
- **`@@`**: Activates the Screen Saver immediately.
- **`PIN`**: Opens the Administrative area directly when entered into the Name box.

---

## 10. Log Retention & Maintenance
## 10. Log Retention & Maintenance
- **Admin Logs**: Retained for **30 days** (older text log files are automatically pruned).
- **Sign-In History & Records**: Retained for **60–90 days** in local storage and daily backups.
- **Storage Consumption**: Extremely lightweight, consuming **less than 20 MB total**.

### 10.1 FAQ: What happens if the Admin log save folder is not selected?
If the Admin log save folder is not selected, **core sign-in functionality, database loading/saving, and local in-app history continue to operate normally**. The app simply skips writing external text log files and daily CSV/Excel export files to disk until a folder is designated in Admin settings.

---

## 11. Technical Specifications
- **Framework**: Kotlin / Jetpack Compose.
- **Versioning**: v4.0 - September 2026 Release.
- **Storage**: Full JSON local persistence for all databases.
- **Excel Library**: Apache POI.
- **Background Work**: Android WorkManager for hourly refreshes and daily maintenance at 04:00 AM.
