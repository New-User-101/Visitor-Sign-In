package stu.gpt.signing.data

import androidx.compose.ui.graphics.Color
import java.time.Instant

// App configurable settings
data class AppSettings(
    val pin: String = "1925",
    val headerText: String = "Data Missing",
    val selectedColorHex: String = "#BBD0E2",
    val screenSaverHeader: String = "",
    val screenSaverMessage: String = "",
    val carRegReminderText: String = "CAR REG?",
    val carRegReminderTextLine2: String = "",
    val lastSignOutTime: Long = 0,
    val isScreenFlipped: Boolean = false,
    val dailyJobEnabled: Boolean = true,
    val autoExportEnabled: Boolean = false,
    // Daily auto refresh time (24h clock). Previously fixed at 03:00.
    val dailyJobHour: Int = 4,
    val dailyJobMinute: Int = 0,
    // UI Headings (blank on first install)
    val leftHeading: String = "",
    val rightHeading: String = "",
    // Persisted last-picked document URIs (as strings) for auto refresh
    val namesUriString: String? = null,
    val rolesUriString: String? = null,
    val locationsUriString: String? = null,
    val regularListUriString: String? = null,
    val questionUriString: String? = null,
    val titlesUriString: String? = null,
    // Persisted file display names and relative paths for name-based resolution
    val namesDisplayName: String? = null,
    val namesRelativePath: String? = null,
    val rolesDisplayName: String? = null,
    val rolesRelativePath: String? = null,
    val locationsDisplayName: String? = null,
    val locationsRelativePath: String? = null,
    val regularListDisplayName: String? = null,
    val regularListRelativePath: String? = null,
    val questionDisplayName: String? = null,
    val questionRelativePath: String? = null,
    val titlesDisplayName: String? = null,
    val titlesRelativePath: String? = null,
    // MediaStore IDs to detect file replacement even when content is identical
    val namesMediaStoreId: Long? = null,
    val rolesMediaStoreId: Long? = null,
    val locationsMediaStoreId: Long? = null,
    val regularListMediaStoreId: Long? = null,
    val questionMediaStoreId: Long? = null,
    val titlesMediaStoreId: Long? = null,
    // Flags to surface when provider access is denied and user must re-pick files in Admin
    val namesNeedsRegrant: Boolean = false,
    val rolesNeedsRegrant: Boolean = false,
    val locationsNeedsRegrant: Boolean = false,
    val regularListNeedsRegrant: Boolean = false,
    val questionNeedsRegrant: Boolean = false,
    val titlesNeedsRegrant: Boolean = false,
    // Last known modified time (epoch millis) for Regular List URI, to detect external changes
    val regularListLastModifiedMillis: Long? = null,
    // Cached performance date from the Cast file to support automated reset
    val lastPerformanceDate: String? = null,
    // Timestamps for other lists to support conditional reloads
    val namesCreatedMillis: Long? = null,
    val namesLastModifiedMillis: Long? = null,
    val rolesCreatedMillis: Long? = null,
    val rolesLastModifiedMillis: Long? = null,
    val locationsCreatedMillis: Long? = null,
    val locationsLastModifiedMillis: Long? = null,
    val regularListCreatedMillis: Long? = null,
    val questionCreatedMillis: Long? = null,
    val questionLastModifiedMillis: Long? = null,
    val titlesCreatedMillis: Long? = null,
    val titlesLastModifiedMillis: Long? = null,
    // File sizes to improve change detection for Google Sheets/Excel
    val namesSize: Long? = null,
    val rolesSize: Long? = null,
    val locationsSize: Long? = null,
    val regularListSize: Long? = null,
    val questionSize: Long? = null,
    val titlesSize: Long? = null,
    // Last uploaded Regular List header (row 1, col 1) to detect changes
    val lastRegularListHeader: String? = null,
    val rolesHeader: String? = null,
    val locationsHeader: String? = null,
    // One-time flag: after special reset (##), next auto refresh conditionally reloads Cast
    val conditionalRegularListReloadPending: Boolean = false,
    // First-1000-rows content hashes used to detect real data changes
    val namesFirst1000Hash: String? = null,
    val rolesFirst1000Hash: String? = null,
    val locationsFirst1000Hash: String? = null,
    val regularListFirst1000Hash: String? = null,
    val questionFirst1000Hash: String? = null,
    val titlesFirst1000Hash: String? = null,
    // Timestamp of the last successful database sync/refresh
    val lastSyncTime: Long = 0,
    // Feature flag: when true, show Sign-In confirmation with optional Vehicle Reg input
    val vehicleRegPromptEnabled: Boolean = false,
    // When true, show a UK-style car reg reminder pop-up for 2 seconds after sign-in
    val carRegReminderEnabled: Boolean = true,
    // Duration in seconds for the car reg reminder pop-up (1-5)
    val carRegReminderDuration: Int = 2,
    // Refresh interval in minutes (15-1440)
    val hourlyRefreshInterval: Int = 60,
    // Master toggle for the screen saver
    val screenSaverEnabled: Boolean = true,
    // Track version change to force a full sync once after upgrade
    val lastUsedVersion: String = "",
    val logFolderUriString: String? = null,
    val logFolderDisplayName: String? = null,
    val logFolderRelativePath: String? = null,
    // Warehouse Mode settings
    val warehouseModeEnabled: Boolean = false,
    val isWarehouseActive: Boolean = false,
    // Dynamic split character for Dark mode panels (computed from members list upload)
    val darkPanelSplitChar: Char = 'L',
) {
    val primaryBgColor: Color get() = try {
        Color(android.graphics.Color.parseColor(selectedColorHex))
    } catch (_: Exception) {
        Color(0xFF3366CC)
    }
    val primaryTextColor: Color get() {
        val color = primaryBgColor
        val red = color.red
        val green = color.green
        val blue = color.blue
        val luminance = 0.2126f * red + 0.7152f * green + 0.0722f * blue
        return if (luminance < 0.5f) Color.White else Color.Black
    }
}

// Domain models

data class VisitorEntry(
    val name: String,
    val roleOrLocation: String,
    val arrivalEpochMillis: Long = Instant.now().toEpochMilli(),
    val vehicleReg: String? = null,
)

data class HistoryEntry(
    val name: String,
    val roleOrLocation: String,
    val arrivalEpochMillis: Long,
    val departureEpochMillis: Long = Instant.now().toEpochMilli(),
    val vehicleReg: String? = null,
)
