package stu.gpt.signing.data

import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.DocumentsContract
import android.content.ContentValues
import android.graphics.Color
import java.io.File
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.security.MessageDigest
import android.util.Log
// Apache POI imports for Excel file reading
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DateUtil
import java.text.SimpleDateFormat
import java.util.Locale
import org.apache.poi.xwpf.usermodel.XWPFDocument
import kotlinx.coroutines.*
import java.time.LocalDateTime

object Repository {
    const val VERSION = "v4.0"
    private lateinit var appContext: Context
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private const val PREFS_NAME = "signing_prefs"
    private const val KEY_PIN = "pin"
    private const val KEY_HEADER = "headerText"
    private const val KEY_SELECTED_COLOR = "selectedColorHex"
    private const val KEY_SS_HEADER = "screenSaverHeader"
    private const val KEY_SS_MESSAGE = "screenSaverMessage"
    private const val KEY_CAR_REG_REMINDER_TEXT = "carRegReminderText"
    private const val KEY_CAR_REG_REMINDER_TEXT_L2 = "carRegReminderTextL2"
    private const val KEY_SS_ENABLED = "screenSaverEnabled"
    private const val KEY_LAST_SIGNOUT = "lastSignOutTime"
    private const val KEY_FLIPPED = "isScreenFlipped"
    private const val KEY_DAILY_ENABLED = "dailyJobEnabled"
    private const val KEY_DAILY_HOUR = "dailyJobHour"
    private const val KEY_DAILY_MINUTE = "dailyJobMinute"
    private const val KEY_HOURLY_REFRESH_INTERVAL = "hourlyRefreshInterval"
    private const val KEY_AUTO_EXPORT = "autoExportEnabled"
    private const val KEY_LEFT_HEADING = "leftHeading"
    private const val KEY_RIGHT_HEADING = "rightHeading"
    private const val KEY_URI_NAMES = "namesUriString"
    private const val KEY_URI_ROLES = "rolesUriString"
    private const val KEY_URI_LOCATIONS = "locationsUriString"
    private const val KEY_URI_REGULAR_LIST = "regularListUriString"
    private const val KEY_URI_QUESTION = "questionUriString"
    private const val KEY_URI_TITLES = "titlesUriString"
    private const val KEY_DARK_SPLIT_CHAR = "darkPanelSplitChar"
    private const val KEY_NAMES_DISPLAY_NAME = "namesDisplayName"
    private const val KEY_NAMES_RELATIVE_PATH = "namesRelativePath"
    private const val KEY_ROLES_DISPLAY_NAME = "rolesDisplayName"
    private const val KEY_ROLES_RELATIVE_PATH = "rolesRelativePath"
    private const val KEY_LOCATIONS_DISPLAY_NAME = "locationsDisplayName"
    private const val KEY_LOCATIONS_RELATIVE_PATH = "locationsRelativePath"
    private const val KEY_REGULAR_LIST_DISPLAY_NAME = "regularListDisplayName"
    private const val KEY_REGULAR_LIST_RELATIVE_PATH = "regularListRelativePath"
    private const val KEY_QUESTION_DISPLAY_NAME = "questionDisplayName"
    private const val KEY_QUESTION_RELATIVE_PATH = "questionRelativePath"
    private const val KEY_TITLES_DISPLAY_NAME = "titlesDisplayName"
    private const val KEY_TITLES_RELATIVE_PATH = "titlesRelativePath"
    private const val KEY_REGULAR_LIST_LAST_MODIFIED = "regularListLastModifiedMillis"
    private const val KEY_NAMES_CREATED = "namesCreatedMillis"
    private const val KEY_NAMES_LAST_MODIFIED = "namesLastModifiedMillis"
    private const val KEY_ROLES_CREATED = "rolesCreatedMillis"
    private const val KEY_ROLES_LAST_MODIFIED = "rolesLastModifiedMillis"
    private const val KEY_LOCATIONS_CREATED = "locationsCreatedMillis"
    private const val KEY_LOCATIONS_LAST_MODIFIED = "locationsLastModifiedMillis"
    private const val KEY_REGULAR_LIST_CREATED = "regularListCreatedMillis"
    private const val KEY_QUESTION_CREATED = "questionCreatedMillis"
    private const val KEY_QUESTION_LAST_MODIFIED = "questionLastModifiedMillis"
    private const val KEY_TITLES_CREATED = "titlesCreatedMillis"
    private const val KEY_TITLES_LAST_MODIFIED = "titlesLastModifiedMillis"
    private const val KEY_LAST_REGULAR_LIST_HEADER = "lastRegularListHeader"
    private const val KEY_ROLES_HEADER = "rolesHeader"
    private const val KEY_LOCATIONS_HEADER = "locationsHeader"
    private const val KEY_CONDITIONAL_REGULAR_LIST_RELOAD_PENDING = "conditionalRegularListReloadPending"
    private const val KEY_LAST_PERF_DATE = "lastPerformanceDate"
    private const val KEY_HASH_NAMES_1000 = "namesFirst1000Hash"
    private const val KEY_HASH_ROLES_1000 = "rolesFirst1000Hash"
    private const val KEY_HASH_LOCATIONS_1000 = "locationsFirst1000Hash"
    private const val KEY_HASH_REGULAR_LIST_1000 = "regularListFirst1000Hash"
    private const val KEY_HASH_QUESTION_1000 = "questionFirst1000Hash"
    private const val KEY_HASH_TITLES_1000 = "titlesFirst1000Hash"
    private const val KEY_LAST_SYNC_TIME = "lastSyncTime"
    private const val KEY_ID_NAMES = "namesMediaStoreId"
    private const val KEY_ID_ROLES = "rolesMediaStoreId"
    private const val KEY_ID_LOCATIONS = "locationsMediaStoreId"
    private const val KEY_ID_REGULAR_LIST = "regularListMediaStoreId"
    private const val KEY_ID_QUESTION = "questionMediaStoreId"
    private const val KEY_ID_TITLES = "titlesMediaStoreId"
    private const val KEY_SIZE_NAMES = "namesSize"
    private const val KEY_SIZE_ROLES = "rolesSize"
    private const val KEY_SIZE_LOCATIONS = "locationsSize"
    private const val KEY_SIZE_REGULAR = "regularListSize"
    private const val KEY_SIZE_QUESTION = "questionSize"
    private const val KEY_SIZE_TITLES = "titlesSize"
    private const val KEY_NEEDS_REGRANT_NAMES = "namesNeedsRegrant"
    private const val KEY_NEEDS_REGRANT_ROLES = "rolesNeedsRegrant"
    private const val KEY_NEEDS_REGRANT_LOCATIONS = "locationsNeedsRegrant"
    private const val KEY_NEEDS_REGRANT_REGULAR = "regularListNeedsRegrant"
    private const val KEY_NEEDS_REGRANT_QUESTION = "questionNeedsRegrant"
    private const val KEY_NEEDS_REGRANT_TITLES = "titlesNeedsRegrant"
    private const val KEY_VEHICLE_REG_PROMPT_ENABLED = "vehicleRegPromptEnabled"
    private const val KEY_CAR_REG_REMINDER_ENABLED = "carRegReminderEnabled"
    private const val KEY_CAR_REG_REMINDER_DURATION = "carRegReminderDuration"
    private const val KEY_LAST_VERSION = "lastUsedVersion"
    private const val KEY_LOG_FOLDER_URI = "logFolderUriString"
    private const val KEY_LOG_FOLDER_DISPLAY_NAME = "logFolderDisplayName"
    private const val KEY_LOG_FOLDER_RELATIVE_PATH = "logFolderRelativePath"
    private const val KEY_WAREHOUSE_MODE_ENABLED = "warehouseModeEnabled"
    private const val KEY_IS_WAREHOUSE_ACTIVE = "isWarehouseActive"

    // Settings (in-memory for now; can be persisted with DataStore later)
    var settings by mutableStateOf(AppSettings())
        private set

    // Screen Saver manual override
    var isManualScreenSaver by mutableStateOf(false)

    // Runtime play title obtained from Regular List (first row, column 1). Not persisted.
    var playTitle by mutableStateOf("")

    // Data sources (in-memory for now)
    val names = mutableStateListOf<String>()
    val roles = mutableStateListOf<String>()
    val locations = mutableStateListOf<String>()
    val questionLines = mutableStateListOf<String>()
    // RegularList members (formerly Stage)
    val stageMembers = mutableStateListOf<String>()
    // Map of member name -> tapped time (epoch millis) for highlighting and sorting
    val stageTappedAt = androidx.compose.runtime.mutableStateMapOf<String, Long>()
    // Map of member name -> active sign-in (arrival epoch millis)
    val stageActive = androidx.compose.runtime.mutableStateMapOf<String, Long>()
    // Map of member name -> vehicle reg captured at sign-in (optional)
    val stageVehicleReg = androidx.compose.runtime.mutableStateMapOf<String, String>()

    // RegularList aliases for consistency (map to Stage-backed data)
    val regularListMembers: List<String> get() = stageMembers
    val regularListTappedAt: Map<String, Long> get() = stageTappedAt
    val regularListActive: Map<String, Long> get() = stageActive

    val totalSignedInCount: Int get() = visitors.size + stageActive.size

    val primaryBgColor: androidx.compose.ui.graphics.Color get() {
        return try {
            androidx.compose.ui.graphics.Color(Color.parseColor(settings.selectedColorHex))
        } catch (_: Exception) {
            androidx.compose.ui.graphics.Color(0xFF3366CC)
        }
    }

    val primaryTextColor: androidx.compose.ui.graphics.Color get() {
        val color = primaryBgColor
        val red = color.red
        val green = color.green
        val blue = color.blue
        val luminance = 0.2126f * red + 0.7152f * green + 0.0722f * blue
        return if (luminance < 0.5f) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
    }

    val visitors = mutableStateListOf<VisitorEntry>()
    val history = mutableStateListOf<HistoryEntry>()

    /**
     * Must be called once from application (e.g., MainActivity.onCreate).
     * Loads persisted history, prunes entries older than 90 days, and saves back.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        
        repositoryScope.launch {
            withContext(Dispatchers.IO) {
                // Ensure default data folder exists under public Downloads
                ensureDefaultDataFolder()
                // Load settings from SharedPreferences
                loadSettings()
                
                logAdminEvent("App initialized")

                // Force a full refresh once if version has changed (app update)
                if (settings.lastUsedVersion != VERSION) {
                    Log.d("SignIn", "Version update detected (Old: ${settings.lastUsedVersion}, New: $VERSION). Forcing full refresh.")
                    refreshAllFromPersistedUris()
                    updateSettings { it.copy(lastUsedVersion = VERSION) }
                }

                // Backfill display name and relative path for stored URIs when missing (first run after upgrade)
                backfillDocumentMetadata()

                // 1. Load persisted Databases FIRST (Names, Roles, Locations, RegularList, Questions)
                // This ensures the UI is populated immediately from local history.
                runCatching { Storage.loadNames(appContext) }.getOrNull()?.let { items ->
                    if (items.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            names.clear()
                            names.addAll(items)
                        }
                    }
                }
                runCatching { Storage.loadRoles(appContext) }.getOrNull()?.let { items ->
                    if (items.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            roles.clear()
                            roles.addAll(items)
                        }
                    }
                }
                runCatching { Storage.loadLocations(appContext) }.getOrNull()?.let { items ->
                    if (items.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            locations.clear()
                            locations.addAll(items)
                        }
                    }
                }
                runCatching { Storage.loadRegularListMembers(appContext) }.getOrNull()?.let { members ->
                    if (members.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            stageMembers.clear()
                            stageMembers.addAll(members)
                        }
                    }
                }
                runCatching { Storage.loadQuestionLines(appContext) }.getOrNull()?.let { lines ->
                    withContext(Dispatchers.Main) {
                        questionLines.clear()
                        questionLines.addAll(lines)
                    }
                }
                
                // Load regular list active/tapped state and vehicle regs if available
                runCatching { Storage.loadRegularListState(appContext) }.getOrNull()?.let { st ->
                    withContext(Dispatchers.Main) {
                        stageActive.clear(); stageActive.putAll(st.active)
                        stageTappedAt.clear(); stageTappedAt.putAll(st.tappedAt)
                        stageVehicleReg.clear(); stageVehicleReg.putAll(st.vehicleReg)
                    }
                }

                // Load any prior history from disk
                val loaded = Storage.loadHistory(appContext)
                withContext(Dispatchers.Main) {
                    history.clear()
                    history.addAll(loaded)
                    // Enforce retention at startup (90 days)
                    pruneHistoryOlderThan(days = 90L)
                }
                saveHistory()

                // Passive reset check: clear expired data immediately if the date has passed
                // WE DO THIS AFTER LOADING so we check the correct in-memory state
                checkAndApplyPassiveReset()

                // Load previously active visitors
                runCatching { Storage.loadVisitors(appContext) }.getOrNull()?.let { prevVisitors ->
                    withContext(Dispatchers.Main) {
                        visitors.clear()
                        visitors.addAll(prevVisitors)
                        resortVisitors()
                    }
                }

                // 2. Now perform background refresh from persisted URIs (Google Drive/Excel)
                // Use conditional refresh so that if the list was cleared by '##', it stays cleared unless the file has changed.
                refreshAllFromPersistedUrisIfChanged(conditionalCast = true)
                
                // If the title wasn't loaded from the Excel file (e.g. no change), restore from settings or default.
                if (playTitle.isBlank()) {
                    withContext(Dispatchers.Main) {
                        playTitle = if (settings.conditionalRegularListReloadPending) settings.headerText else (settings.lastRegularListHeader ?: settings.headerText)
                    }
                }
                
                // Ensure daily maintenance and hourly refresh are scheduled
                scheduleDailyMaintenance(appContext)
                scheduleHourlyRefresh(appContext)

                // Cleanup legacy Stage files if migration to RegularList has taken place
                Storage.removeLegacyStageFilesIfMigrated(appContext)
            }
        }
    }


    /**
     * Get the next scheduled sign-out time as a HH:mm string.
     */
    fun getNextSignOutTimeString(): String {
        val now = java.time.ZonedDateTime.now()
        var nextRun = now.withHour(settings.dailyJobHour.coerceIn(0, 23))
            .withMinute(settings.dailyJobMinute.coerceIn(0, 59))
            .withSecond(0)
            .withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return nextRun.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.US))
    }

    /**
     * Get the next scheduled sign-out time as a readable string for Admin.
     */
    fun getNextSignOutStatus(): String {
        if (!settings.dailyJobEnabled) return "Disabled"
        return getNextSignOutTimeString()
    }

    private fun ensureDefaultDataFolder() {
        // No-op: Sign-In Data folder and _.keep file are no longer required or created.
    }

    fun updateSettings(update: (AppSettings) -> AppSettings) {
        val oldPin = settings.pin
        val oldSettings = settings
        settings = update(settings)
        saveSettings()
        if (oldPin != settings.pin) {
            logAdminEvent("Pin changed")
        } else if (oldSettings != settings) {
            val importantChange = oldSettings.pin != settings.pin ||
                    oldSettings.headerText != settings.headerText ||
                    oldSettings.selectedColorHex != settings.selectedColorHex ||
                    oldSettings.screenSaverHeader != settings.screenSaverHeader ||
                    oldSettings.screenSaverMessage != settings.screenSaverMessage ||
                    oldSettings.carRegReminderText != settings.carRegReminderText ||
                    oldSettings.carRegReminderTextLine2 != settings.carRegReminderTextLine2 ||
                    oldSettings.isScreenFlipped != settings.isScreenFlipped ||
                    oldSettings.dailyJobEnabled != settings.dailyJobEnabled ||
                    oldSettings.autoExportEnabled != settings.autoExportEnabled ||
                    oldSettings.dailyJobHour != settings.dailyJobHour ||
                    oldSettings.dailyJobMinute != settings.dailyJobMinute ||
                    oldSettings.leftHeading != settings.leftHeading ||
                    oldSettings.rightHeading != settings.rightHeading ||
                    oldSettings.namesUriString != settings.namesUriString ||
                    oldSettings.rolesUriString != settings.rolesUriString ||
                    oldSettings.locationsUriString != settings.locationsUriString ||
                    oldSettings.regularListUriString != settings.regularListUriString ||
                    oldSettings.questionUriString != settings.questionUriString ||
                    oldSettings.titlesUriString != settings.titlesUriString ||
                    oldSettings.logFolderUriString != settings.logFolderUriString ||
                    oldSettings.vehicleRegPromptEnabled != settings.vehicleRegPromptEnabled ||
                    oldSettings.carRegReminderEnabled != settings.carRegReminderEnabled ||
                    oldSettings.carRegReminderDuration != settings.carRegReminderDuration ||
                    oldSettings.screenSaverEnabled != settings.screenSaverEnabled ||
                    oldSettings.warehouseModeEnabled != settings.warehouseModeEnabled ||
                    oldSettings.isWarehouseActive != settings.isWarehouseActive

            if (importantChange && oldPin == settings.pin) {
                logAdminEvent("Admin settings saved")
            }
        }
    }

    private val logLock = Any()

    fun logAdminEvent(message: String) {
        if (!::appContext.isInitialized) return
        val uriStr = settings.logFolderUriString ?: return
        if (uriStr.isBlank()) return

        val now = java.time.LocalDateTime.now()
        val fileDateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val logTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        val targetFileName = "Admin_logs_${now.format(fileDateFormatter)}.txt"
        val logLine = "[${now.format(logTimeFormatter)}] $message"

        repositoryScope.launch(Dispatchers.IO) {
            synchronized(logLock) {
                try {
                    val treeUri = android.net.Uri.parse(uriStr)
                    val treeId = DocumentsContract.getTreeDocumentId(treeUri)
                    val rootUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeId)
                    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeId)
                    
                    var fileUri: android.net.Uri? = null
                    appContext.contentResolver.query(childrenUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                        val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                        if (idCol >= 0 && nameCol >= 0) {
                            while (cursor.moveToNext()) {
                                if (cursor.getString(nameCol) == targetFileName) {
                                    val docId = cursor.getString(idCol)
                                    fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                                    break
                                }
                            }
                        }
                    }

                    if (fileUri == null) {
                        fileUri = DocumentsContract.createDocument(appContext.contentResolver, rootUri, "text/plain", targetFileName)
                    }

                    if (fileUri != null) {
                        val existingText = try {
                            appContext.contentResolver.openInputStream(fileUri!!)?.use { it.bufferedReader().readText() } ?: ""
                        } catch (_: Exception) { "" }

                        val delimiter = if (existingText.isNotEmpty() && !existingText.endsWith("\n")) "\n" else ""
                        val updatedText = existingText + delimiter + logLine + "\n"

                        appContext.contentResolver.openOutputStream(fileUri!!, "wt")?.use { out ->
                            out.bufferedWriter().use { it.write(updatedText) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Repository", "Failed to write admin log: ${e.message}", e)
                }
            }
        }
    }

    fun pruneAdminLogsOlderThan30Days() {
        if (!::appContext.isInitialized) return
        val uriStr = settings.logFolderUriString ?: return
        if (uriStr.isBlank()) return

        val now = java.time.LocalDate.now()
        val thirtyDaysAgo = now.minusDays(30)

        try {
            val treeUri = android.net.Uri.parse(uriStr)
            val treeId = DocumentsContract.getTreeDocumentId(treeUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeId)

            appContext.contentResolver.query(childrenUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (idCol >= 0 && nameCol >= 0) {
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameCol) ?: continue
                        if (name.startsWith("Admin_logs_") && name.endsWith(".txt")) {
                            val dateStr = name.substringAfter("Admin_logs_").substringBefore(".txt")
                            runCatching {
                                val fileDate = java.time.LocalDate.parse(dateStr)
                                if (fileDate.isBefore(thirtyDaysAgo)) {
                                    val docId = cursor.getString(idCol)
                                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                                    DocumentsContract.deleteDocument(appContext.contentResolver, fileUri)
                                    Log.d("Repository", "Deleted old admin log file: $name")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to prune admin logs: ${e.message}", e)
        }
    }

    /**
     * Saves the current sign-in history to a daily record file in a "Records" subfolder.
     */
    fun saveDailyRecordFile() {
        if (!::appContext.isInitialized) return
        val uriStr = settings.logFolderUriString ?: return
        if (uriStr.isBlank()) return

        repositoryScope.launch(Dispatchers.IO) {
            try {
                val parentUri = android.net.Uri.parse(uriStr)
                val parentId = DocumentsContract.getTreeDocumentId(parentUri)
                val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, parentId)
                
                // 1. Ensure "Records" subfolder exists
                var recordsFolderUri: android.net.Uri? = null
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, parentId)
                appContext.contentResolver.query(childrenUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE), null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameCol) == "Records" && cursor.getString(mimeCol) == DocumentsContract.Document.MIME_TYPE_DIR) {
                            recordsFolderUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, cursor.getString(idCol))
                            break
                        }
                    }
                }
                
                if (recordsFolderUri == null) {
                    recordsFolderUri = DocumentsContract.createDocument(appContext.contentResolver, parentDocUri, DocumentsContract.Document.MIME_TYPE_DIR, "Records")
                }

                if (recordsFolderUri == null) return@launch

                // 2. Create the daily record file
                val now = java.time.LocalDateTime.now()
                val fileName = "Records_${now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))}.txt"
                val recordFileUri = DocumentsContract.createDocument(appContext.contentResolver, recordsFolderUri!!, "text/plain", fileName) ?: return@launch

                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(java.time.ZoneId.systemDefault())
                val content = buildString {
                    append("Sign-In Records for ${now.toLocalDate()}\n")
                    append("Generated at: ${now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))}\n")
                    append("-".repeat(50) + "\n")
                    append(String.format("%-25s %-20s %-15s %-15s %-15s\n", "Name", "Role/Loc", "In", "Out", "Reg"))
                    history.forEach { e ->
                        append(String.format("%-25s %-20s %-15s %-15s %-15s\n",
                            e.name.take(24),
                            e.roleOrLocation.take(19),
                            formatter.format(java.time.Instant.ofEpochMilli(e.arrivalEpochMillis)).substringAfter(" "),
                            formatter.format(java.time.Instant.ofEpochMilli(e.departureEpochMillis)).substringAfter(" "),
                            e.vehicleReg ?: ""
                        ))
                    }
                }

                appContext.contentResolver.openOutputStream(recordFileUri, "wt")?.use { out ->
                    out.bufferedWriter().use { it.write(content) }
                }
                Log.d("Repository", "Daily record saved: $fileName")
            } catch (e: Exception) {
                Log.e("Repository", "Failed to save daily record: ${e.message}", e)
            }
        }
    }

    /**
     * Prunes record files in the "Records" subfolder older than 60 days.
     */
    fun pruneDailyRecordsOlderThan60Days() {
        if (!::appContext.isInitialized) return
        val uriStr = settings.logFolderUriString ?: return
        
        val now = java.time.LocalDate.now()
        val sixtyDaysAgo = now.minusDays(60)

        try {
            val parentUri = android.net.Uri.parse(uriStr)
            val parentId = DocumentsContract.getTreeDocumentId(parentUri)
            
            // Find "Records" folder ID
            var recordsFolderId: String? = null
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, parentId)
            appContext.contentResolver.query(childrenUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE), null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameCol) == "Records" && cursor.getString(mimeCol) == DocumentsContract.Document.MIME_TYPE_DIR) {
                        recordsFolderId = cursor.getString(idCol)
                        break
                    }
                }
            }

            val folderId = recordsFolderId ?: return
            val recordsChildrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, folderId)

            appContext.contentResolver.query(recordsChildrenUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameCol) ?: continue
                    if (name.startsWith("Records_") && name.endsWith(".txt")) {
                        val dateStr = name.substringAfter("Records_").substringBefore(".txt")
                        runCatching {
                            val fileDate = java.time.LocalDate.parse(dateStr)
                            if (fileDate.isBefore(sixtyDaysAgo)) {
                                val docId = cursor.getString(idCol)
                                val fileUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)
                                DocumentsContract.deleteDocument(appContext.contentResolver, fileUri)
                                Log.d("Repository", "Deleted old record file: $name")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to prune daily records: ${e.message}", e)
        }
    }

    /**
     * Resets the inactivity timer by updating lastSignOutTime to current time.
     * This is called on any user interaction to prevent the screensaver from triggering.
     */
    fun updateActivityTime() {
        updateSettings { it.copy(lastSignOutTime = System.currentTimeMillis()) }
    }

    /**
     * Check if a person is already signed in (either as a visitor or stage member).
     */
    fun isAlreadySignedIn(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val inVisitors = visitors.any { 
            it.name.equals(trimmed, ignoreCase = true) || 
            it.name.equals("$trimmed (Guest)", ignoreCase = true) ||
            it.name.startsWith("$trimmed (Guest)", ignoreCase = true)
        }
        val inStage = stageActive.keys.any { it.equals(trimmed, ignoreCase = true) }
        return inVisitors || inStage
    }

    fun signIn(name: String, roleOrLocation: String, vehicleReg: String? = null) {
        val trimmedName = name.trim().ifEmpty { return }
        val col = roleOrLocation.trim()
        val existsInDb = names.any { it.equals(trimmedName, ignoreCase = true) }
        val finalName = if (existsInDb) trimmedName else "$trimmedName (Guest)"
        visitors.add(VisitorEntry(finalName, col, Instant.now().toEpochMilli(), vehicleReg?.take(20)))
        resortVisitors()
        if (::appContext.isInitialized) Storage.saveVisitors(appContext, visitors)
    }

    private fun resortVisitors() {
        // Role entries first, then Guest visitors first within each category, then alphabetical by name (case-insensitive).
        val sorted = visitors.sortedWith(compareBy(
            { e ->
                val isRole = roles.any { it.equals(e.roleOrLocation, ignoreCase = true) }
                if (isRole) 0 else 1
            },
            { e ->
                val isGuest = e.name.contains("(Guest)", ignoreCase = true)
                if (isGuest) 0 else 1
            },
            { e -> e.name.lowercase() },
            { e -> e.roleOrLocation.lowercase() }
        ))
        visitors.clear(); visitors.addAll(sorted)
    }

    fun signOut(entry: VisitorEntry) {
        visitors.remove(entry)
        history.add(
            HistoryEntry(
                name = entry.name,
                roleOrLocation = entry.roleOrLocation,
                arrivalEpochMillis = entry.arrivalEpochMillis,
                departureEpochMillis = Instant.now().toEpochMilli(),
                vehicleReg = entry.vehicleReg
            )
        )
        // Enforce retention and persist (90 days)
        pruneHistoryOlderThan(days = 90L)
        saveHistory()
        if (visitors.isEmpty() && stageActive.isEmpty()) {
            updateSettings { it.copy(lastSignOutTime = Instant.now().toEpochMilli()) }
        }
        if (::appContext.isInitialized) Storage.saveVisitors(appContext, visitors)
    }

    /**
     * Sign-out all currently signed-in Visitors immediately and move them to history.
     */
    fun signOutAllNow() {
        if (visitors.isEmpty()) return
        logAdminEvent("Sign-out All visitors triggered")
        val now = Instant.now().toEpochMilli()
        val toArchive = visitors.map { v ->
            HistoryEntry(
                name = v.name,
                roleOrLocation = v.roleOrLocation,
                arrivalEpochMillis = v.arrivalEpochMillis,
                departureEpochMillis = now
            )
        }
        visitors.clear()
        history.addAll(toArchive)
        pruneHistoryOlderThan(days = 90L)
        saveHistory()
        updateSettings { it.copy(lastSignOutTime = Instant.now().toEpochMilli()) }
        if (::appContext.isInitialized) Storage.saveVisitors(appContext, visitors)
    }

    fun loadNames(items: List<String>) {
        val cleaned = items.filter { it.isNotBlank() }.distinct().sortedBy { it.trim().lowercase() }
        names.clear()
        names.addAll(cleaned)

        val midIndex = if (cleaned.isNotEmpty()) cleaned.size / 2 else 0
        val midChar = cleaned.getOrNull(midIndex)?.trim()?.firstOrNull()?.uppercaseChar() ?: 'L'
        val validMidChar = if (midChar in 'A'..'Z') midChar else 'L'

        updateSettings { it.copy(darkPanelSplitChar = validMidChar) }

        if (::appContext.isInitialized) Storage.saveNames(appContext, names)
    }
    fun loadRoles(items: List<String>) { 
        roles.clear(); roles.addAll(items.filter { it.isNotBlank() }.distinct()) 
        if (::appContext.isInitialized) Storage.saveRoles(appContext, roles)
    }
    fun loadLocations(items: List<String>) { 
        locations.clear(); locations.addAll(items.filter { it.isNotBlank() }.distinct()) 
        if (::appContext.isInitialized) Storage.saveLocations(appContext, locations)
    }
    fun loadQuestionLines(items: List<String>) {
        // items is Column B of Questions file
        // Row 1 (index 0) is Header
        // Rows 2-14 (indices 1-13) are Question checklist items
        // Row 15 (index 14) is Car Reg Line 1
        // Row 16 (index 15) is Car Reg Line 2
        
        // Checklist items (max 13 items)
        val qLines = if (items.size > 1) {
            items.subList(1, minOf(items.size, 14)).filter { it.isNotBlank() }
        } else emptyList()
        
        val carRegL1 = items.getOrNull(14)?.ifBlank { "CAR REG?" } ?: "CAR REG?"
        val carRegL2 = items.getOrNull(15) ?: ""
        
        Log.d("SignIn", "loadQuestionLines: Loaded ${qLines.size} checklist items. CarRegL1: '$carRegL1', CarRegL2: '$carRegL2'")

        questionLines.clear()
        questionLines.addAll(qLines)
        
        updateSettings { it.copy(
            carRegReminderText = carRegL1,
            carRegReminderTextLine2 = carRegL2
        ) }

        if (::appContext.isInitialized) {
            Storage.saveQuestionLines(appContext, questionLines)
        }
    }
    fun loadTitles(items: List<String>) {
        // items is Column B
        if (items.size >= 1) {
            val h = items.getOrNull(1)?.take(35)?.ifBlank { "Data Missing" } ?: "Data Missing"
            val l = items.getOrNull(2)?.take(30) ?: ""
            val r = items.getOrNull(3)?.take(15) ?: ""
            val ssMessage = items.getOrNull(4)?.take(100) ?: ""

            updateSettings { it.copy(
                headerText = h, 
                leftHeading = l, 
                rightHeading = r, 
                screenSaverHeader = h, // B2
                screenSaverMessage = ssMessage // B5
            ) }
        }
    }
    fun loadRegularListMembers(items: List<String>) {
        val clean = items.filter { it.isNotBlank() }.distinct()
        stageMembers.clear(); stageMembers.addAll(clean)
        // Reset tapped state on new upload so all appear as untapped (red-highlighted)
        stageTappedAt.clear()
        // Clear any active RegularList sign-ins when a fresh list is uploaded
        stageActive.clear()
        if (::appContext.isInitialized) {
            Storage.saveRegularListState(appContext, stageActive, stageTappedAt, stageVehicleReg)
            Storage.saveRegularListMembers(appContext, stageMembers)
        }
    }

    /**
     * Replace Stage members list while preserving current active/tapped state for members that remain.
     * Any active/tapped entries for names no longer present are removed. No history entries are created here.
     */
    fun replaceRegularListMembersPreserveState(newItems: List<String>) {
        val clean = newItems.filter { it.isNotBlank() }.distinct()
        // Remove state for names that no longer exist
        val toRemoveActive = stageActive.keys - clean.toSet()
        toRemoveActive.forEach { stageActive.remove(it) }
        val toRemoveTapped = stageTappedAt.keys - clean.toSet()
        toRemoveTapped.forEach { stageTappedAt.remove(it) }
        // Update the list
        stageMembers.clear(); stageMembers.addAll(clean)
        if (::appContext.isInitialized) {
            Storage.saveRegularListState(appContext, stageActive, stageTappedAt, stageVehicleReg)
            Storage.saveRegularListMembers(appContext, stageMembers)
        }
    }

    /**
     * Toggle a Stage member's sign-in state.
     * - If currently not signed in: record Sign-In time and highlight green.
     * - If currently signed in: record Sign-Out time, add to history for export, and revert to red.
     */
    fun regularListToggle(name: String, vehicleReg: String? = null) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val now = Instant.now().toEpochMilli()
        val current = stageActive[trimmed]
        if (current == null) {
            // Sign-In
            stageActive[trimmed] = now
            stageTappedAt[trimmed] = now
            vehicleReg?.let { reg -> stageVehicleReg[trimmed] = reg.take(20) }
        } else {
            // Sign-Out -> move to history and clear highlight
            stageActive.remove(trimmed)
            stageTappedAt.remove(trimmed)
            val reg = stageVehicleReg.remove(trimmed)
            history.add(
                HistoryEntry(
                    name = trimmed,
                    roleOrLocation = settings.rightHeading.ifBlank { "RegularList" },
                    arrivalEpochMillis = current,
                    departureEpochMillis = now,
                    vehicleReg = reg
                )
            )
            // Enforce retention and persist (90 days)
            pruneHistoryOlderThan(days = 90L)
            saveHistory()
            if (visitors.isEmpty() && stageActive.isEmpty()) {
                updateSettings { it.copy(lastSignOutTime = Instant.now().toEpochMilli()) }
            }
        }
        if (::appContext.isInitialized) Storage.saveRegularListState(appContext, stageActive, stageTappedAt, stageVehicleReg)
    }

    /**
     * Force sign-out all active Stage members, sending them to history and clearing highlights.
     */
    fun regularListSignOutAllNow() {
        if (stageActive.isEmpty()) return
        logAdminEvent("Sign-out All Cast triggered")
        val now = Instant.now().toEpochMilli()
        val toArchive = stageActive.map { (name, start) ->
            HistoryEntry(
                name = name,
                roleOrLocation = settings.rightHeading.ifBlank { "RegularList" },
                arrivalEpochMillis = start,
                departureEpochMillis = now
            )
        }
        stageActive.clear()
        stageTappedAt.clear()
        history.addAll(toArchive)
        pruneHistoryOlderThan(days = 90L)
        saveHistory()
        updateSettings { it.copy(lastSignOutTime = Instant.now().toEpochMilli()) }
        if (::appContext.isInitialized) Storage.saveRegularListState(appContext, stageActive, stageTappedAt, stageVehicleReg)
    }

    /**
     * Clear the entire Cast (Stage members) list and any active/tapped state,
     * then reset the title to "Visitor Sign-In". All changes are persisted so
     * they survive accidental app closure.
     */
    fun clearRegularListAndResetTitle() {
        stageMembers.clear()
        stageActive.clear()
        stageTappedAt.clear()
        if (::appContext.isInitialized) {
            Storage.saveRegularListMembers(appContext, stageMembers)
            Storage.saveRegularListState(appContext, stageActive, stageTappedAt, stageVehicleReg)
        }
        updateSettings { it.copy(headerText = "Visitor Sign-In") }
    }

    fun exportHistoryCsv(): Boolean {
        // Export is handled via SAF in the UI where a Uri is provided.
        // Keeping stub to avoid breaking callers; returns false to indicate no-op.
        return false
    }

    fun clearVisitorsOlderThan(days: Long) {
        val cutoff = Instant.now().minusSeconds(days * 24 * 3600).toEpochMilli()
        visitors.removeAll { it.arrivalEpochMillis < cutoff }
    }

    fun clearVisitorsOnExport() {
        visitors.clear()
    }

    private fun pruneHistoryOlderThan(days: Long) {
        val cutoff = Instant.now().minusSeconds(days * 24 * 3600).toEpochMilli()
        history.removeAll { it.departureEpochMillis < cutoff }
    }

    private fun saveHistory() {
        if (::appContext.isInitialized) {
            Storage.saveHistory(appContext, history)
        }
    }

    fun lastVehicleRegForName(name: String): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        // Check currently signed-in visitors first
        visitors.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }?.vehicleReg?.let { if (it.isNotBlank()) return it }
        // Then scan history from newest to oldest
        for (e in history.asReversed()) {
            if (e.name.equals(trimmed, ignoreCase = true)) {
                val reg = e.vehicleReg
                if (!reg.isNullOrBlank()) return reg
            }
        }
        return null
    }

    fun exportHistoryToDefaultFolder(): Boolean {
        if (!::appContext.isInitialized) return false
        return runCatching {
            val resolver = appContext.contentResolver
            val contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val relativePath = Environment.DIRECTORY_DOCUMENTS + "/Sign-In Data/"
            val displayName = "Sign-In_Data_Export.xlsx"

            // Check if file already exists to replace it
            val selection = "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME}=?"
            val selectionArgs = arrayOf(relativePath, displayName)
            val existingUri = resolver.query(contentUri, arrayOf(MediaStore.MediaColumns._ID), selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    Uri.withAppendedPath(contentUri, id.toString())
                } else null
            }

            val uri = if (existingUri != null) {
                existingUri
            } else {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                resolver.insert(contentUri, values) ?: return false
            }

            resolver.openOutputStream(uri, "wt")?.use { out ->
                val workbook = XSSFWorkbook()
                try {
                    val sheet = workbook.createSheet("SignIn")

                    val header = sheet.createRow(0)
                    header.createCell(0).setCellValue("Name")
                    header.createCell(1).setCellValue("Role/Location")
                    header.createCell(2).setCellValue("Sign-in")
                    header.createCell(3).setCellValue("Sign-out")
                    header.createCell(4).setCellValue("Vehicle Reg")

                    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .withZone(java.time.ZoneId.systemDefault())

                    history.forEachIndexed { idx, e ->
                        val row = sheet.createRow(idx + 1)
                        row.createCell(0).setCellValue(e.name)
                        row.createCell(1).setCellValue(e.roleOrLocation)
                        row.createCell(2).setCellValue(formatter.format(java.time.Instant.ofEpochMilli(e.arrivalEpochMillis)))
                        row.createCell(3).setCellValue(formatter.format(java.time.Instant.ofEpochMilli(e.departureEpochMillis)))
                        row.createCell(4).setCellValue(e.vehicleReg ?: "")
                    }

                    workbook.write(out)
                    out.flush()
                } finally {
                    runCatching { workbook.close() }
                }
            } ?: return false
            true
        }.getOrElse { 
            Log.e("Repository", "Export failed", it)
            false 
        }
    }

    fun buildAppGuideDocxBytes(): ByteArray {
        val doc = XWPFDocument()
        return runCatching {
            fun addHeading(text: String, size: Int = 18) {
                val p = doc.createParagraph()
                val r = p.createRun()
                r.isBold = true
                r.fontSize = size
                r.setText(text)
            }

            fun addLine(text: String, bold: Boolean = false) {
                val p = doc.createParagraph()
                val r = p.createRun()
                r.isBold = bold
                r.fontSize = 11
                r.setText(text)
            }

            addHeading("Sign-In Application v${VERSION} - Technical & User Guide")
            addLine("Generated: " + java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            addLine("Created by Stu Sorrell using Gemini-3 in 2026. - Version: v${VERSION}")

            addHeading("1. Overview", 14)
            addLine("Sign-In v${VERSION} is a professional digital attendance system featuring real-time tracking, dual-panel lists, and full local persistence.")

            addHeading("2. Main Screen & Admin Access", 14)
            addLine("- Minimal Design: Top corners are left empty for a clean look.", true)
            addLine("- Admin Access: Type 'Admin' into the Name box to open settings.", true)
            addLine("- Predictive Search: Start typing a name to see matching database entries.", true)

            addHeading("3. Visual Color Coding", 14)
            addLine("- Light Blue (#D7EFF9): Guest sign-ins.")
            addLine("- Light Green (#BEE9AD): Roles and Active Cast members.")
            addLine("- Light Sage Green (#DAF2D0): Locations.")
            addLine("- Red (#FD958D): Inactive/Absent Cast members (pinned to top).")

            addHeading("4. Vehicle Registration", 14)
            addLine("- Memory: The app remembers registration numbers and auto-fills them.", true)
            addLine("- Plate Style: Number plates feature a professional yellow background.", true)

            addHeading("5. Database File Requirements", 14)
            addLine("Files must be in 'Documents/Sign-In Data/' in Excel format.", true)
            addLine("- Members: Col A = First Name, Col B = Last Name (Row 2+).")
            addLine("- Roles & Locations: Column A (Row 2+).")
            addLine("- Cast (RH Side): Row 1 = Title, Row 2 = Last Perf. Date (Optional), Rows 3+ = Names.")
            addLine("- Titles: COLUMN B (Rows 2-5).")
            addLine("- Questions: COLUMN B (Rows 2-16).")

            addHeading("6. Automation & Triggers", 14)
            addLine("- Daily Maintenance: Auto sign-out and refresh at your scheduled time.", true)
            addLine("- '##': Reset Cast list and Title.")
            addLine("- '@@': Manually trigger Screen Saver.")

            addHeading("7. Technical Specifications", 14)
            addLine("- Minimum Android 10 (API 29).")
            addLine("- Full Local Persistence: Databases remain available offline and after restarts.")
            addLine("- Minimum Android 10 (API 29).")
            addLine("- Framework: Kotlin/Jetpack Compose.")
            addLine("- Core Libraries: Apache POI (Excel), WorkManager (Background Tasks).")

            val baos = java.io.ByteArrayOutputStream()
            doc.use { d -> d.write(baos) }
            baos.toByteArray()
        }.getOrElse {
            runCatching { doc.close() }
            ByteArray(0)
        }
    }

    private fun loadSettings() {
        if (!::appContext.isInitialized) return
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Legacy migration: if previous 'stageUriString' exists, use it as regularListUriString when new key is absent
        val legacyStageUri = prefs.getString("stageUriString", null)
        val mergedRegularListUri = prefs.getString(KEY_URI_REGULAR_LIST, null) ?: legacyStageUri
        val loaded = AppSettings(
            pin = prefs.getString(KEY_PIN, "1925") ?: "1925",
            headerText = prefs.getString(KEY_HEADER, "") ?: "",
            selectedColorHex = prefs.getString(KEY_SELECTED_COLOR, "#BBD0E2") ?: "#BBD0E2",
            screenSaverHeader = prefs.getString(KEY_SS_HEADER, "") ?: "",
            screenSaverMessage = prefs.getString(KEY_SS_MESSAGE, "") ?: "",
            carRegReminderText = prefs.getString(KEY_CAR_REG_REMINDER_TEXT, "CAR REG?") ?: "CAR REG?",
            carRegReminderTextLine2 = prefs.getString(KEY_CAR_REG_REMINDER_TEXT_L2, "") ?: "",
            lastSignOutTime = prefs.getLong(KEY_LAST_SIGNOUT, 0L),
            isScreenFlipped = prefs.getBoolean(KEY_FLIPPED, false),
            dailyJobEnabled = prefs.getBoolean(KEY_DAILY_ENABLED, true),
            autoExportEnabled = prefs.getBoolean(KEY_AUTO_EXPORT, false),
            dailyJobHour = prefs.getInt(KEY_DAILY_HOUR, 3),
            dailyJobMinute = prefs.getInt(KEY_DAILY_MINUTE, 0),
            leftHeading = (prefs.getString(KEY_LEFT_HEADING, null) ?: AppSettings().leftHeading).take(30),
            rightHeading = (prefs.getString(KEY_RIGHT_HEADING, null) ?: AppSettings().rightHeading).take(15),
            namesUriString = prefs.getString(KEY_URI_NAMES, null),
            rolesUriString = prefs.getString(KEY_URI_ROLES, null),
            locationsUriString = prefs.getString(KEY_URI_LOCATIONS, null),
            regularListUriString = mergedRegularListUri,
            questionUriString = prefs.getString(KEY_URI_QUESTION, null),
            titlesUriString = prefs.getString(KEY_URI_TITLES, null),
            namesDisplayName = prefs.getString(KEY_NAMES_DISPLAY_NAME, null),
            namesRelativePath = prefs.getString(KEY_NAMES_RELATIVE_PATH, null),
            rolesDisplayName = prefs.getString(KEY_ROLES_DISPLAY_NAME, null),
            rolesRelativePath = prefs.getString(KEY_ROLES_RELATIVE_PATH, null),
            locationsDisplayName = prefs.getString(KEY_LOCATIONS_DISPLAY_NAME, null),
            locationsRelativePath = prefs.getString(KEY_LOCATIONS_RELATIVE_PATH, null),
            regularListDisplayName = prefs.getString(KEY_REGULAR_LIST_DISPLAY_NAME, null),
            regularListRelativePath = prefs.getString(KEY_REGULAR_LIST_RELATIVE_PATH, null),
            questionDisplayName = prefs.getString(KEY_QUESTION_DISPLAY_NAME, null),
            questionRelativePath = prefs.getString(KEY_QUESTION_RELATIVE_PATH, null),
            titlesDisplayName = prefs.getString(KEY_TITLES_DISPLAY_NAME, null),
            titlesRelativePath = prefs.getString(KEY_TITLES_RELATIVE_PATH, null),
            namesMediaStoreId = prefs.getLong(KEY_ID_NAMES, -1L).let { if (it >= 0) it else null },
            rolesMediaStoreId = prefs.getLong(KEY_ID_ROLES, -1L).let { if (it >= 0) it else null },
            locationsMediaStoreId = prefs.getLong(KEY_ID_LOCATIONS, -1L).let { if (it >= 0) it else null },
            regularListMediaStoreId = prefs.getLong(KEY_ID_REGULAR_LIST, -1L).let { if (it >= 0) it else null },
            questionMediaStoreId = prefs.getLong(KEY_ID_QUESTION, -1L).let { if (it >= 0) it else null },
            titlesMediaStoreId = prefs.getLong(KEY_ID_TITLES, -1L).let { if (it >= 0) it else null },
            namesNeedsRegrant = prefs.getBoolean(KEY_NEEDS_REGRANT_NAMES, false),
            rolesNeedsRegrant = prefs.getBoolean(KEY_NEEDS_REGRANT_ROLES, false),
            locationsNeedsRegrant = prefs.getBoolean(KEY_NEEDS_REGRANT_LOCATIONS, false),
            regularListNeedsRegrant = prefs.getBoolean(KEY_NEEDS_REGRANT_REGULAR, false),
            questionNeedsRegrant = prefs.getBoolean(KEY_NEEDS_REGRANT_QUESTION, false),
            titlesNeedsRegrant = prefs.getBoolean(KEY_NEEDS_REGRANT_TITLES, false),
            regularListLastModifiedMillis = prefs.getLong(KEY_REGULAR_LIST_LAST_MODIFIED, -1L).let { if (it >= 0) it else null },
            namesCreatedMillis = prefs.getLong(KEY_NAMES_CREATED, -1L).let { if (it >= 0) it else null },
            namesLastModifiedMillis = prefs.getLong(KEY_NAMES_LAST_MODIFIED, -1L).let { if (it >= 0) it else null },
            rolesCreatedMillis = prefs.getLong(KEY_ROLES_CREATED, -1L).let { if (it >= 0) it else null },
            rolesLastModifiedMillis = prefs.getLong(KEY_ROLES_LAST_MODIFIED, -1L).let { if (it >= 0) it else null },
            locationsCreatedMillis = prefs.getLong(KEY_LOCATIONS_CREATED, -1L).let { if (it >= 0) it else null },
            locationsLastModifiedMillis = prefs.getLong(KEY_LOCATIONS_LAST_MODIFIED, -1L).let { if (it >= 0) it else null },
            regularListCreatedMillis = prefs.getLong(KEY_REGULAR_LIST_CREATED, -1L).let { if (it >= 0) it else null },
            questionCreatedMillis = prefs.getLong(KEY_QUESTION_CREATED, -1L).let { if (it >= 0) it else null },
            questionLastModifiedMillis = prefs.getLong(KEY_QUESTION_LAST_MODIFIED, -1L).let { if (it >= 0) it else null },
            titlesCreatedMillis = prefs.getLong(KEY_TITLES_CREATED, -1L).let { if (it >= 0) it else null },
            titlesLastModifiedMillis = prefs.getLong(KEY_TITLES_LAST_MODIFIED, -1L).let { if (it >= 0) it else null },
            namesSize = prefs.getLong(KEY_SIZE_NAMES, -1L).let { if (it >= 0) it else null },
            rolesSize = prefs.getLong(KEY_SIZE_ROLES, -1L).let { if (it >= 0) it else null },
            locationsSize = prefs.getLong(KEY_SIZE_LOCATIONS, -1L).let { if (it >= 0) it else null },
            regularListSize = prefs.getLong(KEY_SIZE_REGULAR, -1L).let { if (it >= 0) it else null },
            questionSize = prefs.getLong(KEY_SIZE_QUESTION, -1L).let { if (it >= 0) it else null },
            titlesSize = prefs.getLong(KEY_SIZE_TITLES, -1L).let { if (it >= 0) it else null },
            lastRegularListHeader = prefs.getString(KEY_LAST_REGULAR_LIST_HEADER, null),
            rolesHeader = prefs.getString(KEY_ROLES_HEADER, null),
            locationsHeader = prefs.getString(KEY_LOCATIONS_HEADER, null),
            conditionalRegularListReloadPending = prefs.getBoolean(KEY_CONDITIONAL_REGULAR_LIST_RELOAD_PENDING, false),
            lastPerformanceDate = prefs.getString(KEY_LAST_PERF_DATE, null),
            namesFirst1000Hash = prefs.getString(KEY_HASH_NAMES_1000, null),
            rolesFirst1000Hash = prefs.getString(KEY_HASH_ROLES_1000, null),
            locationsFirst1000Hash = prefs.getString(KEY_HASH_LOCATIONS_1000, null),
            regularListFirst1000Hash = prefs.getString(KEY_HASH_REGULAR_LIST_1000, null),
            questionFirst1000Hash = prefs.getString(KEY_HASH_QUESTION_1000, null),
            titlesFirst1000Hash = prefs.getString(KEY_HASH_TITLES_1000, null),
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0L),
            vehicleRegPromptEnabled = prefs.getBoolean(KEY_VEHICLE_REG_PROMPT_ENABLED, false),
            carRegReminderEnabled = prefs.getBoolean(KEY_CAR_REG_REMINDER_ENABLED, true),
            carRegReminderDuration = prefs.getInt(KEY_CAR_REG_REMINDER_DURATION, 2),
            hourlyRefreshInterval = prefs.getInt(KEY_HOURLY_REFRESH_INTERVAL, 60),
            screenSaverEnabled = prefs.getBoolean(KEY_SS_ENABLED, true),
            lastUsedVersion = prefs.getString(KEY_LAST_VERSION, "") ?: "",
            logFolderUriString = prefs.getString(KEY_LOG_FOLDER_URI, null),
            logFolderDisplayName = prefs.getString(KEY_LOG_FOLDER_DISPLAY_NAME, null),
            logFolderRelativePath = prefs.getString(KEY_LOG_FOLDER_RELATIVE_PATH, null),
            warehouseModeEnabled = prefs.getBoolean(KEY_WAREHOUSE_MODE_ENABLED, false),
            isWarehouseActive = prefs.getBoolean(KEY_IS_WAREHOUSE_ACTIVE, false)
        )
        settings = loaded
        // Persist migrated value if we used the legacy key
        if (legacyStageUri != null && prefs.getString(KEY_URI_REGULAR_LIST, null) == null) {
            prefs.edit().putString(KEY_URI_REGULAR_LIST, legacyStageUri).apply()
        }
    }

    private fun saveSettings() {
        if (!::appContext.isInitialized) return
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PIN, settings.pin)
            .putString(KEY_HEADER, settings.headerText)
            .putString(KEY_SELECTED_COLOR, settings.selectedColorHex)
            .putString(KEY_SS_HEADER, settings.screenSaverHeader)
            .putString(KEY_SS_MESSAGE, settings.screenSaverMessage)
            .putString(KEY_CAR_REG_REMINDER_TEXT, settings.carRegReminderText)
            .putString(KEY_CAR_REG_REMINDER_TEXT_L2, settings.carRegReminderTextLine2)
            .putLong(KEY_LAST_SIGNOUT, settings.lastSignOutTime)
            .putBoolean(KEY_FLIPPED, settings.isScreenFlipped)
            .putBoolean(KEY_DAILY_ENABLED, settings.dailyJobEnabled)
            .putBoolean(KEY_AUTO_EXPORT, settings.autoExportEnabled)
            .putInt(KEY_DAILY_HOUR, settings.dailyJobHour)
            .putInt(KEY_DAILY_MINUTE, settings.dailyJobMinute)
            .putInt(KEY_HOURLY_REFRESH_INTERVAL, settings.hourlyRefreshInterval)
            .putBoolean(KEY_SS_ENABLED, settings.screenSaverEnabled)
            .putString(KEY_LEFT_HEADING, settings.leftHeading.take(30))
            .putString(KEY_RIGHT_HEADING, settings.rightHeading.take(15))
            .putString(KEY_URI_NAMES, settings.namesUriString)
            .putString(KEY_URI_ROLES, settings.rolesUriString)
            .putString(KEY_URI_LOCATIONS, settings.locationsUriString)
            .putString(KEY_URI_REGULAR_LIST, settings.regularListUriString)
            .putString(KEY_URI_QUESTION, settings.questionUriString)
            .putString(KEY_URI_TITLES, settings.titlesUriString)
            .putString(KEY_NAMES_DISPLAY_NAME, settings.namesDisplayName)
            .putString(KEY_NAMES_RELATIVE_PATH, settings.namesRelativePath)
            .putString(KEY_ROLES_DISPLAY_NAME, settings.rolesDisplayName)
            .putString(KEY_ROLES_RELATIVE_PATH, settings.rolesRelativePath)
            .putString(KEY_LOCATIONS_DISPLAY_NAME, settings.locationsDisplayName)
            .putString(KEY_LOCATIONS_RELATIVE_PATH, settings.locationsRelativePath)
            .putString(KEY_REGULAR_LIST_DISPLAY_NAME, settings.regularListDisplayName)
            .putString(KEY_REGULAR_LIST_RELATIVE_PATH, settings.regularListRelativePath)
            .putString(KEY_QUESTION_DISPLAY_NAME, settings.questionDisplayName)
            .putString(KEY_QUESTION_RELATIVE_PATH, settings.questionRelativePath)
            .putString(KEY_TITLES_DISPLAY_NAME, settings.titlesDisplayName)
            .putString(KEY_TITLES_RELATIVE_PATH, settings.titlesRelativePath)
            .putString(KEY_LOG_FOLDER_URI, settings.logFolderUriString)
            .putString(KEY_LOG_FOLDER_DISPLAY_NAME, settings.logFolderDisplayName)
            .putString(KEY_LOG_FOLDER_RELATIVE_PATH, settings.logFolderRelativePath)
            .putBoolean(KEY_WAREHOUSE_MODE_ENABLED, settings.warehouseModeEnabled)
            .putBoolean(KEY_IS_WAREHOUSE_ACTIVE, settings.isWarehouseActive)
            .putString(KEY_DARK_SPLIT_CHAR, settings.darkPanelSplitChar.toString())
            .also { editor ->
                val lastMod = settings.regularListLastModifiedMillis
                if (lastMod != null) {
                    editor.putLong(KEY_REGULAR_LIST_LAST_MODIFIED, lastMod)
                } else {
                    editor.remove(KEY_REGULAR_LIST_LAST_MODIFIED)
                }
                settings.namesCreatedMillis?.let { editor.putLong(KEY_NAMES_CREATED, it) } ?: editor.remove(KEY_NAMES_CREATED)
                settings.namesLastModifiedMillis?.let { editor.putLong(KEY_NAMES_LAST_MODIFIED, it) } ?: editor.remove(KEY_NAMES_LAST_MODIFIED)
                settings.rolesCreatedMillis?.let { editor.putLong(KEY_ROLES_CREATED, it) } ?: editor.remove(KEY_ROLES_CREATED)
                settings.rolesLastModifiedMillis?.let { editor.putLong(KEY_ROLES_LAST_MODIFIED, it) } ?: editor.remove(KEY_ROLES_LAST_MODIFIED)
                settings.locationsCreatedMillis?.let { editor.putLong(KEY_LOCATIONS_CREATED, it) } ?: editor.remove(KEY_LOCATIONS_CREATED)
                settings.locationsLastModifiedMillis?.let { editor.putLong(KEY_LOCATIONS_LAST_MODIFIED, it) } ?: editor.remove(KEY_LOCATIONS_LAST_MODIFIED)
                settings.regularListCreatedMillis?.let { editor.putLong(KEY_REGULAR_LIST_CREATED, it) } ?: editor.remove(KEY_REGULAR_LIST_CREATED)
                settings.questionCreatedMillis?.let { editor.putLong(KEY_QUESTION_CREATED, it) } ?: editor.remove(KEY_QUESTION_CREATED)
                settings.questionLastModifiedMillis?.let { editor.putLong(KEY_QUESTION_LAST_MODIFIED, it) } ?: editor.remove(KEY_QUESTION_LAST_MODIFIED)
                settings.titlesCreatedMillis?.let { editor.putLong(KEY_TITLES_CREATED, it) } ?: editor.remove(KEY_TITLES_CREATED)
                settings.titlesLastModifiedMillis?.let { editor.putLong(KEY_TITLES_LAST_MODIFIED, it) } ?: editor.remove(KEY_TITLES_LAST_MODIFIED)
                settings.namesSize?.let { editor.putLong(KEY_SIZE_NAMES, it) } ?: editor.remove(KEY_SIZE_NAMES)
                settings.rolesSize?.let { editor.putLong(KEY_SIZE_ROLES, it) } ?: editor.remove(KEY_SIZE_ROLES)
                settings.locationsSize?.let { editor.putLong(KEY_SIZE_LOCATIONS, it) } ?: editor.remove(KEY_SIZE_LOCATIONS)
                settings.regularListSize?.let { editor.putLong(KEY_SIZE_REGULAR, it) } ?: editor.remove(KEY_SIZE_REGULAR)
                settings.questionSize?.let { editor.putLong(KEY_SIZE_QUESTION, it) } ?: editor.remove(KEY_SIZE_QUESTION)
                settings.titlesSize?.let { editor.putLong(KEY_SIZE_TITLES, it) } ?: editor.remove(KEY_SIZE_TITLES)
            }
            .putString(KEY_LAST_REGULAR_LIST_HEADER, settings.lastRegularListHeader)
            .putString(KEY_ROLES_HEADER, settings.rolesHeader)
            .putString(KEY_LOCATIONS_HEADER, settings.locationsHeader)
            .putBoolean(KEY_CONDITIONAL_REGULAR_LIST_RELOAD_PENDING, settings.conditionalRegularListReloadPending)
            .putString(KEY_LAST_PERF_DATE, settings.lastPerformanceDate)
            .putString(KEY_HASH_NAMES_1000, settings.namesFirst1000Hash)
            .putString(KEY_HASH_ROLES_1000, settings.rolesFirst1000Hash)
            .putString(KEY_HASH_LOCATIONS_1000, settings.locationsFirst1000Hash)
            .putString(KEY_HASH_REGULAR_LIST_1000, settings.regularListFirst1000Hash)
            .putString(KEY_HASH_QUESTION_1000, settings.questionFirst1000Hash)
            .putString(KEY_HASH_TITLES_1000, settings.titlesFirst1000Hash)
            .putLong(KEY_LAST_SYNC_TIME, settings.lastSyncTime)
            .putBoolean(KEY_VEHICLE_REG_PROMPT_ENABLED, settings.vehicleRegPromptEnabled)
            .putBoolean(KEY_CAR_REG_REMINDER_ENABLED, settings.carRegReminderEnabled)
            .putInt(KEY_CAR_REG_REMINDER_DURATION, settings.carRegReminderDuration)
            .also { editor ->
                settings.namesMediaStoreId?.let { editor.putLong(KEY_ID_NAMES, it) } ?: editor.remove(KEY_ID_NAMES)
                settings.rolesMediaStoreId?.let { editor.putLong(KEY_ID_ROLES, it) } ?: editor.remove(KEY_ID_ROLES)
                settings.locationsMediaStoreId?.let { editor.putLong(KEY_ID_LOCATIONS, it) } ?: editor.remove(KEY_ID_LOCATIONS)
                settings.regularListMediaStoreId?.let { editor.putLong(KEY_ID_REGULAR_LIST, it) } ?: editor.remove(KEY_ID_REGULAR_LIST)
                settings.questionMediaStoreId?.let { editor.putLong(KEY_ID_QUESTION, it) } ?: editor.remove(KEY_ID_QUESTION)
                settings.titlesMediaStoreId?.let { editor.putLong(KEY_ID_TITLES, it) } ?: editor.remove(KEY_ID_TITLES)
                editor.putBoolean(KEY_NEEDS_REGRANT_NAMES, settings.namesNeedsRegrant)
                editor.putBoolean(KEY_NEEDS_REGRANT_ROLES, settings.rolesNeedsRegrant)
                editor.putBoolean(KEY_NEEDS_REGRANT_LOCATIONS, settings.locationsNeedsRegrant)
                editor.putBoolean(KEY_NEEDS_REGRANT_REGULAR, settings.regularListNeedsRegrant)
                editor.putBoolean(KEY_NEEDS_REGRANT_QUESTION, settings.questionNeedsRegrant)
                editor.putBoolean(KEY_NEEDS_REGRANT_TITLES, settings.titlesNeedsRegrant)
                editor.putString(KEY_LAST_VERSION, settings.lastUsedVersion)
            }
            .apply()
    }

    private fun canOpenUri(uriString: String?): Boolean {
        if (!::appContext.isInitialized) return false
        if (uriString.isNullOrBlank()) return false
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            appContext.contentResolver.openInputStream(uri)?.use { } != null
        }.getOrElse { false }
    }

    /** Read MediaStore _ID for a given content Uri string, if available. */
    fun getMediaStoreId(uriString: String?): Long? {
        if (!::appContext.isInitialized) return null
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(MediaStore.MediaColumns._ID)
                    if (idx >= 0) c.getLong(idx) else null
                } else null
            }
        }.getOrNull()
    }

    /** Best-effort read of DISPLAY_NAME for a Uri string. */
    fun getDisplayName(uriString: String?): String? {
        if (!::appContext.isInitialized) return null
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (idx >= 0) c.getString(idx) else null
                } else null
            }
        }.getOrNull()
    }

    /** Best-effort read of RELATIVE_PATH for a Uri string (Android Q+). */
    fun getRelativePath(uriString: String?): String? {
        if (!::appContext.isInitialized) return null
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            val projection = arrayOf(MediaStore.MediaColumns.RELATIVE_PATH)
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                    if (idx >= 0) c.getString(idx) else null
                } else null
            }
        }.getOrNull()
    }

    /** Derive a RELATIVE_PATH-like value from a DocumentsProvider URI if MediaStore query returns null. */
    private fun deriveRelativePathFromDocumentsUri(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            val last = uri.lastPathSegment ?: return@runCatching null
            // Expect something like "document/primary:Documents/Sign-In Data/Cast.xlsx"
            val docId = last.substringAfter("document/", last)
            val afterColon = docId.substringAfter(":", docId)
            val dir = afterColon.substringBeforeLast('/', "")
            dir.substringBeforeLast('/', "")
        }.getOrNull()
    }

    /**
     * Resolve a Uri by display name and optional relative path via MediaStore primary volume.
     * Returns null if not found.
     */
    fun resolveUriByName(displayName: String?, relativePath: String?): Uri? {
        if (!::appContext.isInitialized) return null
        if (displayName.isNullOrBlank()) return null
        return runCatching {
            val contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val cols = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.RELATIVE_PATH, MediaStore.MediaColumns.DATE_ADDED)
            // Try exact match on path
            val normPath = relativePath?.let { if (it.endsWith('/')) it else "$it/" }
            if (!normPath.isNullOrBlank()) {
                val selEq = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
                val argsEq = arrayOf(displayName, normPath)
                appContext.contentResolver.query(contentUri, cols, selEq, argsEq, null)?.use { c ->
                    if (c.moveToFirst()) {
                        val idIdx = c.getColumnIndex(MediaStore.MediaColumns._ID)
                        val id = if (idIdx >= 0) c.getLong(idIdx) else null
                        if (id != null) return Uri.withAppendedPath(contentUri, id.toString())
                    }
                }
                // Try LIKE with wildcard suffix
                val selLike = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                val argsLike = arrayOf(displayName, "$normPath%")
                appContext.contentResolver.query(contentUri, cols, selLike, argsLike, "${MediaStore.MediaColumns.DATE_ADDED} DESC")?.use { c ->
                    if (c.moveToFirst()) {
                        val idIdx = c.getColumnIndex(MediaStore.MediaColumns._ID)
                        val id = if (idIdx >= 0) c.getLong(idIdx) else null
                        if (id != null) return Uri.withAppendedPath(contentUri, id.toString())
                    }
                }
            }
            // Fallback: search by display name only, pick most recent
            val selNameOnly = "${MediaStore.MediaColumns.DISPLAY_NAME}=?"
            val argsNameOnly = arrayOf(displayName)
            appContext.contentResolver.query(contentUri, cols, selNameOnly, argsNameOnly, "${MediaStore.MediaColumns.DATE_ADDED} DESC")?.use { c ->
                if (c.moveToFirst()) {
                    val idIdx = c.getColumnIndex(MediaStore.MediaColumns._ID)
                    val id = if (idIdx >= 0) c.getLong(idIdx) else null
                    if (id != null) return Uri.withAppendedPath(contentUri, id.toString())
                }
            }
            null
        }.getOrNull()
    }

    /** Fill in missing displayName/relativePath from existing URIs. */
    private fun backfillDocumentMetadata() {
        if (!::appContext.isInitialized) return
        var changed = false
        var s = settings
        fun fill(uriStr: String?, currentName: String?, currentPath: String?, set: (String?, String?) -> Unit) {
            if (!uriStr.isNullOrBlank() && (currentName.isNullOrBlank() || currentPath.isNullOrBlank())) {
                val dn = getDisplayName(uriStr)
                val rp = getRelativePath(uriStr) ?: deriveRelativePathFromDocumentsUri(uriStr)
                set(dn, rp)
            }
        }
        fill(s.namesUriString, s.namesDisplayName, s.namesRelativePath) { dn, rp -> s = s.copy(namesDisplayName = dn ?: s.namesDisplayName, namesRelativePath = rp ?: s.namesRelativePath); changed = true }
        fill(s.rolesUriString, s.rolesDisplayName, s.rolesRelativePath) { dn, rp -> s = s.copy(rolesDisplayName = dn ?: s.rolesDisplayName, rolesRelativePath = rp ?: s.rolesRelativePath); changed = true }
        fill(s.locationsUriString, s.locationsDisplayName, s.locationsRelativePath) { dn, rp -> s = s.copy(locationsDisplayName = dn ?: s.locationsDisplayName, locationsRelativePath = rp ?: s.locationsRelativePath); changed = true }
        fill(s.regularListUriString, s.regularListDisplayName, s.regularListRelativePath) { dn, rp -> s = s.copy(regularListDisplayName = dn ?: s.regularListDisplayName, regularListRelativePath = rp ?: s.regularListRelativePath); changed = true }
        fill(s.questionUriString, s.questionDisplayName, s.questionRelativePath) { dn, rp -> s = s.copy(questionDisplayName = dn ?: s.questionDisplayName, questionRelativePath = rp ?: s.questionRelativePath); changed = true }
        fill(s.titlesUriString, s.titlesDisplayName, s.titlesRelativePath) { dn, rp -> s = s.copy(titlesDisplayName = dn ?: s.titlesDisplayName, titlesRelativePath = rp ?: s.titlesRelativePath); changed = true }
        if (changed) { settings = s; saveSettings() }
    }

    /** Prefer resolving by name/path; use it only if we can actually open it. Otherwise fall back. */
    private fun preferredUriString(originalUriString: String?, displayName: String?, relativePath: String?): String? {
        Log.d("SignIn", "preferredUriString: Resolving '$displayName' in '$relativePath' (Original: $originalUriString)")
        
        // First attempt with provided metadata (Search in MediaStore by Name/Path)
        fun tryResolve(name: String?, path: String?): String? {
            val resolved = resolveUriByName(name, path)
            if (resolved != null) {
                val ok = runCatching {
                    appContext.contentResolver.openInputStream(resolved)?.use { /* success */ }
                    true
                }.getOrElse { false }
                if (ok) {
                    Log.d("SignIn", "preferredUriString: Successfully resolved by name '$name' to $resolved")
                    return resolved.toString()
                }
            }
            return null
        }

        tryResolve(displayName, relativePath)?.let { return it }

        // If that failed, derive from the original URI without querying providers
        val derivedPath = deriveRelativePathFromDocumentsUri(originalUriString)
        val derivedName = runCatching {
            val uri = originalUriString?.let { android.net.Uri.parse(it) }
            val last = uri?.lastPathSegment
            // last might look like "document/primary:Documents/Sign-In Data/Cast.xlsx"
            last?.substringAfter(":")?.substringAfterLast('/')
        }.getOrNull()

        tryResolve(derivedName ?: displayName, derivedPath ?: relativePath)?.let { return it }

        // FINAL FALLBACK: If name resolution fails completely (e.g. sync app moved it), 
        // try the original URI directly if it's still openable.
        if (originalUriString != null) {
            val originalOk = runCatching {
                appContext.contentResolver.openInputStream(android.net.Uri.parse(originalUriString))?.use { }
                true
            }.getOrElse { false }
            if (originalOk) {
                Log.d("SignIn", "preferredUriString: Falling back to original URI: $originalUriString")
                return originalUriString
            }
        }

        Log.w("SignIn", "preferredUriString: FAILED to resolve '$displayName'")
        return null
    }

    /** Compute SHA-256 over the first [kbytes] KB of a content Uri. Binary-safe for Excel files. */
    fun computeFirstNHash(uriString: String?, kbytes: Int = 100): String? {
        if (!::appContext.isInitialized) return null
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(kbytes * 1024)
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) return@runCatching null
                
                val md = MessageDigest.getInstance("SHA-256")
                md.update(buffer, 0, bytesRead)
                val digest = md.digest()
                digest.joinToString("") { b -> "%02x".format(b) }
            }
        }.getOrNull()
    }

    // Apply current retention setting immediately and persist (90 days)
    fun applyRetentionNow() {
        pruneHistoryOlderThan(days = 90L)
        saveHistory()
    }

    fun getHistoryWithin(days: Long): List<HistoryEntry> {
        val cutoff = Instant.now().minusSeconds(days * 24 * 3600).toEpochMilli()
        return history.filter { it.departureEpochMillis >= cutoff }
    }

    /**
     * Reload all data lists from persisted last-picked URIs in settings. No-op if URIs are absent.
     * Updates metadata so the 'IfChanged' check knows we are current.
     */
    suspend fun refreshAllFromPersistedUris() {
        if (!::appContext.isInitialized) return

        withContext(Dispatchers.IO) {
            // PASSIVE RESET CHECK: Always check date first
            checkAndApplyPassiveReset()

            val namesSheets = if (!settings.warehouseModeEnabled) listOf("Theatre") else (if (settings.isWarehouseActive) listOf("Youth") else listOf("Rehearsals"))
            val rolesSheets = if (!settings.warehouseModeEnabled) listOf("Role") else listOf("Warehouse Roles")
            val locsSheets = if (!settings.warehouseModeEnabled) listOf("Locations") else listOf("Warehouse")

            // Names
            val namesUriPref = preferredUriString(settings.namesUriString, settings.namesDisplayName, settings.namesRelativePath)
            val namesLines = readExcelNames(namesUriPref, namesSheets)
            if (namesLines.isNotEmpty()) {
                loadNames(namesLines)
                updateSettings { it.copy(
                    namesMediaStoreId = getMediaStoreId(namesUriPref),
                    namesLastModifiedMillis = getUriLastModifiedMillis(namesUriPref),
                    namesSize = getUriSize(namesUriPref),
                    namesFirst1000Hash = computeFirstNHash(namesUriPref, 1000) ?: it.namesFirst1000Hash
                ) }
            }
            
            // Roles
            val rolesUriPref = preferredUriString(settings.rolesUriString, settings.rolesDisplayName, settings.rolesRelativePath)
            val rolesLines = readExcelColumn(rolesUriPref, 0, rolesSheets)
            if (rolesLines.isNotEmpty()) {
                val header = rolesLines.firstOrNull()?.take(30) ?: ""
                if (header.isNotEmpty()) updateSettings { it.copy(rolesHeader = header) }
                val rolesItems = rolesLines.drop(1).filter { it.isNotBlank() }
                if (rolesItems.isNotEmpty()) loadRoles(rolesItems)
                updateSettings { it.copy(
                    rolesMediaStoreId = getMediaStoreId(rolesUriPref),
                    rolesLastModifiedMillis = getUriLastModifiedMillis(rolesUriPref),
                    rolesSize = getUriSize(rolesUriPref),
                    rolesFirst1000Hash = computeFirstNHash(rolesUriPref, 1000) ?: it.rolesFirst1000Hash
                ) }
            }

            // Locations
            val locationsUriPref = preferredUriString(settings.locationsUriString, settings.locationsDisplayName, settings.locationsRelativePath)
            val locationsLines = readExcelColumn(locationsUriPref, 0, locsSheets)
            if (locationsLines.isNotEmpty()) {
                val header = locationsLines.firstOrNull()?.take(30) ?: ""
                if (header.isNotEmpty()) updateSettings { it.copy(locationsHeader = header) }
                val locsItems = locationsLines.drop(1).filter { it.isNotBlank() }
                if (locsItems.isNotEmpty()) loadLocations(locsItems)
                updateSettings { it.copy(
                    locationsMediaStoreId = getMediaStoreId(locationsUriPref),
                    locationsLastModifiedMillis = getUriLastModifiedMillis(locationsUriPref),
                    locationsSize = getUriSize(locationsUriPref),
                    locationsFirst1000Hash = computeFirstNHash(locationsUriPref, 1000) ?: it.locationsFirst1000Hash
                ) }
            }
            // Regular List
            val regularUriPref = preferredUriString(settings.regularListUriString, settings.regularListDisplayName, settings.regularListRelativePath)
            val regularLines = readExcelColumn(regularUriPref, 0, namesSheets)
            if (regularLines.isNotEmpty()) {
                processRegularListLines(regularLines)
                updateSettings { it.copy(
                    regularListMediaStoreId = getMediaStoreId(regularUriPref),
                    regularListLastModifiedMillis = getUriLastModifiedMillis(regularUriPref),
                    regularListSize = getUriSize(regularUriPref),
                    regularListFirst1000Hash = computeFirstNHash(regularUriPref, 1000) ?: it.regularListFirst1000Hash,
                    conditionalRegularListReloadPending = false
                ) }
            }
            // Questions ...
            val questionUriPref = preferredUriString(settings.questionUriString, settings.questionDisplayName, settings.questionRelativePath)
            val questionLinesRaw = readExcelColumn(questionUriPref, 1)
            if (questionLinesRaw.isNotEmpty()) {
                loadQuestionLines(questionLinesRaw)
                updateSettings { it.copy(
                    questionMediaStoreId = getMediaStoreId(questionUriPref),
                    questionLastModifiedMillis = getUriLastModifiedMillis(questionUriPref),
                    questionSize = getUriSize(questionUriPref),
                    questionFirst1000Hash = computeFirstNHash(questionUriPref, 1000) ?: it.questionFirst1000Hash
                ) }
            }
            // Titles ...
            val titlesUriPref = preferredUriString(settings.titlesUriString, settings.titlesDisplayName, settings.titlesRelativePath)
            val titlesLines = readExcelColumn(titlesUriPref, 1)
            if (titlesLines.isNotEmpty()) {
                loadTitles(titlesLines)
                updateSettings { it.copy(
                    titlesMediaStoreId = getMediaStoreId(titlesUriPref),
                    titlesLastModifiedMillis = getUriLastModifiedMillis(titlesUriPref),
                    titlesSize = getUriSize(titlesUriPref),
                    titlesFirst1000Hash = computeFirstNHash(titlesUriPref, 1000) ?: it.titlesFirst1000Hash
                ) }
            }
            updateSettings { it.copy(lastSyncTime = Instant.now().toEpochMilli()) }
        }
    }

    /**
     * Refresh only lists whose underlying file modified time changed. If conditionalCast is true and
     * a one-time conditional reload is pending, apply header/timestamp check for Regular List and clear flag.
     */
    suspend fun refreshAllFromPersistedUrisIfChanged(conditionalCast: Boolean) {
        if (!::appContext.isInitialized) return
        Log.d("SignIn", "Repository.refreshAllFromPersistedUrisIfChanged start (conditionalCast=$conditionalCast)")

        withContext(Dispatchers.IO) {
            // PASSIVE RESET CHECK: Always check date first, independent of file metadata.
            // This is the most critical check for automatic clearing of the Cast list.
            checkAndApplyPassiveReset()
            
            // Names: resolve, check openability, then compare MediaStore ID, last-modified, and first 1000 lines hash
            val namesSheets = if (!settings.warehouseModeEnabled) listOf("Theatre") else (if (settings.isWarehouseActive) listOf("Youth") else listOf("Rehearsals"))
            val rolesSheets = if (!settings.warehouseModeEnabled) listOf("Role") else listOf("Warehouse Roles")
            val locsSheets = if (!settings.warehouseModeEnabled) listOf("Locations") else listOf("Warehouse")

            // Names
            preferredUriString(settings.namesUriString, settings.namesDisplayName, settings.namesRelativePath)?.let { uriStr ->
                if (!canOpenUri(uriStr)) {
                    updateSettings { it.copy(namesNeedsRegrant = true) }
                } else {
                    val currentId = getMediaStoreId(uriStr)
                    val idChanged = currentId != null && currentId != settings.namesMediaStoreId
                    val currentLastMod = getUriLastModifiedMillis(uriStr)
                    val storedLastMod = settings.namesLastModifiedMillis
                    val lastModChanged = when {
                        currentLastMod == null -> false
                        storedLastMod == null -> true
                        currentLastMod > storedLastMod -> true
                        else -> false
                    }
                    val currentHash = computeFirstNHash(uriStr, 1000)
                    val hashChanged = currentHash != null && currentHash != settings.namesFirst1000Hash
                    val currentSize = getUriSize(uriStr)
                    val sizeChanged = currentSize != null && currentSize != settings.namesSize
                    val shouldReload = idChanged || lastModChanged || hashChanged || sizeChanged
                    if (shouldReload) {
                        Log.d("SignIn", "Names Database Change Detected. Reloading.")
                        val lines = readExcelNames(uriStr, namesSheets)
                        if (lines.isNotEmpty()) {
                            loadNames(lines)
                            updateSettings { it.copy(
                                namesMediaStoreId = currentId,
                                namesLastModifiedMillis = currentLastMod,
                                namesSize = currentSize,
                                namesFirst1000Hash = currentHash ?: it.namesFirst1000Hash
                            ) }
                        }
                    }
                }
            }
            // Roles
            preferredUriString(settings.rolesUriString, settings.rolesDisplayName, settings.rolesRelativePath)?.let { uriStr ->
                if (!canOpenUri(uriStr)) {
                    updateSettings { it.copy(rolesNeedsRegrant = true) }
                } else {
                    val currentId = getMediaStoreId(uriStr)
                    val idChanged = currentId != null && currentId != settings.rolesMediaStoreId
                    val currentLastMod = getUriLastModifiedMillis(uriStr)
                    val storedLastMod = settings.rolesLastModifiedMillis
                    val lastModChanged = when {
                        currentLastMod == null -> false
                        storedLastMod == null -> true
                        currentLastMod > storedLastMod -> true
                        else -> false
                    }
                    val currentHash = computeFirstNHash(uriStr, 1000)
                    val hashChanged = currentHash != null && currentHash != settings.rolesFirst1000Hash
                    val currentSize = getUriSize(uriStr)
                    val sizeChanged = currentSize != null && currentSize != settings.rolesSize
                    val shouldReload = idChanged || lastModChanged || hashChanged || sizeChanged
                    if (shouldReload) {
                        Log.d("SignIn", "Roles Database Change Detected. Reloading.")
                        val lines = readExcelColumn(uriStr, 0, rolesSheets)
                        if (lines.isNotEmpty()) {
                            val header = lines.firstOrNull()?.take(30) ?: ""
                            if (header.isNotEmpty()) updateSettings { it.copy(rolesHeader = header) }
                            val roles = lines.drop(1).filter { it.isNotBlank() }
                            if (roles.isNotEmpty()) loadRoles(roles)
                            updateSettings { it.copy(
                                rolesMediaStoreId = currentId,
                                rolesLastModifiedMillis = currentLastMod,
                                rolesSize = currentSize,
                                rolesFirst1000Hash = currentHash ?: it.rolesFirst1000Hash
                            ) }
                        }
                    }
                }
            }
            // Locations
            preferredUriString(settings.locationsUriString, settings.locationsDisplayName, settings.locationsRelativePath)?.let { uriStr ->
                if (!canOpenUri(uriStr)) {
                    updateSettings { it.copy(locationsNeedsRegrant = true) }
                } else {
                    val currentId = getMediaStoreId(uriStr)
                    val idChanged = currentId != null && currentId != settings.locationsMediaStoreId
                    val currentLastMod = getUriLastModifiedMillis(uriStr)
                    val storedLastMod = settings.locationsLastModifiedMillis
                    val lastModChanged = when {
                        currentLastMod == null -> false
                        storedLastMod == null -> true
                        currentLastMod > storedLastMod -> true
                        else -> false
                    }
                    val currentHash = computeFirstNHash(uriStr, 1000)
                    val hashChanged = currentHash != null && currentHash != settings.locationsFirst1000Hash
                    val currentSize = getUriSize(uriStr)
                    val sizeChanged = currentSize != null && currentSize != settings.locationsSize
                    val shouldReload = idChanged || lastModChanged || hashChanged || sizeChanged
                    if (shouldReload) {
                        Log.d("SignIn", "Locations Database Change Detected. Reloading.")
                        val lines = readExcelColumn(uriStr, 0, locsSheets)
                        if (lines.isNotEmpty()) {
                            val header = lines.firstOrNull()?.take(30) ?: ""
                            if (header.isNotEmpty()) updateSettings { it.copy(locationsHeader = header) }
                            val locs = lines.drop(1).filter { it.isNotBlank() }
                            if (locs.isNotEmpty()) loadLocations(locs)
                            updateSettings { it.copy(
                                locationsMediaStoreId = currentId,
                                locationsLastModifiedMillis = currentLastMod,
                                locationsSize = currentSize,
                                locationsFirst1000Hash = currentHash ?: it.locationsFirst1000Hash
                            ) }
                        }
                    }
                }
            }
            // Regular List
            preferredUriString(settings.regularListUriString, settings.regularListDisplayName, settings.regularListRelativePath)?.let { uriStr ->
                if (!canOpenUri(uriStr)) {
                    updateSettings { it.copy(regularListNeedsRegrant = true) }
                } else {
                    val lines = readExcelColumn(uriStr, 0, namesSheets)
                    if (lines.isNotEmpty()) {
                        val potentialDate = lines.getOrNull(1)
                        if (isDateInPast(potentialDate)) {
                            resetRegularListAndUseDefaultTitle()
                        } else {
                            val currentId = getMediaStoreId(uriStr)
                            val idChanged = currentId != null && currentId != settings.regularListMediaStoreId
                            val currentLastMod = getUriLastModifiedMillis(uriStr)
                            val storedLastMod = settings.regularListLastModifiedMillis
                            val lastModChanged = currentLastMod != null && (storedLastMod == null || currentLastMod > storedLastMod)
                            val currentHash = computeFirstNHash(uriStr, 1000)
                            val hashChanged = currentHash != null && currentHash != settings.regularListFirst1000Hash
                            val currentSize = getUriSize(uriStr)
                            val sizeChanged = currentSize != null && currentSize != settings.regularListSize
                            
                            if (idChanged || lastModChanged || hashChanged || sizeChanged) {
                                processRegularListLines(lines)
                                updateSettings { it.copy(
                                    regularListMediaStoreId = currentId,
                                    regularListLastModifiedMillis = currentLastMod,
                                    regularListSize = currentSize,
                                    regularListFirst1000Hash = currentHash ?: it.regularListFirst1000Hash
                                ) }
                            }
                        }
                    }
                }
            }
            // Clear pending flag regardless when conditional path requested
            if (conditionalCast && settings.conditionalRegularListReloadPending) {
                updateSettings { it.copy(conditionalRegularListReloadPending = false) }
            }
        // Questions: reload if ID changed OR last-modified changed OR hash changed OR size changed
        preferredUriString(settings.questionUriString, settings.questionDisplayName, settings.questionRelativePath)?.let { uriStr ->
            if (!canOpenUri(uriStr)) {
                updateSettings { it.copy(questionNeedsRegrant = true) }
            } else {
                val currentId = getMediaStoreId(uriStr)
                val idChanged = currentId != null && currentId != settings.questionMediaStoreId
                val currentLastMod = getUriLastModifiedMillis(uriStr)
                val storedLastMod = settings.questionLastModifiedMillis
                val lastModChanged = currentLastMod != null && (storedLastMod == null || currentLastMod > storedLastMod)
                val currentHash = computeFirstNHash(uriStr, 1000)
                val hashChanged = currentHash != null && currentHash != settings.questionFirst1000Hash
                val currentSize = getUriSize(uriStr)
                val sizeChanged = currentSize != null && currentSize != settings.questionSize
                
                if (idChanged || lastModChanged || hashChanged || sizeChanged) {
                    Log.d("SignIn", "Questions Database Change Detected (ID=$idChanged, Mod=$lastModChanged, Hash=$hashChanged, Size=$sizeChanged). Reloading.")
                    logAdminEvent("Questions database change detected and enacted. File: ${settings.questionDisplayName ?: uriStr}")
                    val lines = readExcelColumn(uriStr, 1)
                    if (lines.isNotEmpty()) {
                        loadQuestionLines(lines)
                        updateSettings { it.copy(
                            questionMediaStoreId = currentId,
                            questionLastModifiedMillis = currentLastMod,
                            questionSize = currentSize,
                            questionFirst1000Hash = currentHash ?: it.questionFirst1000Hash
                        ) }
                    }
                }
            }
        }
        
        // Titles: reload if ID changed OR last-modified changed OR hash changed OR size changed
        preferredUriString(settings.titlesUriString, settings.titlesDisplayName, settings.titlesRelativePath)?.let { uriStr ->
            if (!canOpenUri(uriStr)) {
                updateSettings { it.copy(titlesNeedsRegrant = true) }
            } else {
                val currentId = getMediaStoreId(uriStr)
                val idChanged = currentId != null && currentId != settings.titlesMediaStoreId
                val currentLastMod = getUriLastModifiedMillis(uriStr)
                val storedLastMod = settings.titlesLastModifiedMillis
                val lastModChanged = currentLastMod != null && (storedLastMod == null || currentLastMod > storedLastMod)
                val currentHash = computeFirstNHash(uriStr, 1000)
                val hashChanged = currentHash != null && currentHash != settings.titlesFirst1000Hash
                val currentSize = getUriSize(uriStr)
                val sizeChanged = currentSize != null && currentSize != settings.titlesSize
                
                if (idChanged || lastModChanged || hashChanged || sizeChanged) {
                    Log.d("SignIn", "Titles Database Change Detected (ID=$idChanged, Mod=$lastModChanged, Hash=$hashChanged, Size=$sizeChanged). Reloading.")
                    logAdminEvent("Titles database change detected and enacted. File: ${settings.titlesDisplayName ?: uriStr}")
                    val lines = readExcelColumn(uriStr, 1)
                    if (lines.isNotEmpty()) {
                        loadTitles(lines)
                        updateSettings { it.copy(
                            titlesMediaStoreId = currentId,
                            titlesLastModifiedMillis = currentLastMod,
                            titlesSize = currentSize,
                            titlesFirst1000Hash = currentHash ?: it.titlesFirst1000Hash
                        ) }
                    }
                }
            }
            updateSettings { it.copy(lastSyncTime = Instant.now().toEpochMilli()) }
        }
        }
        Log.d("SignIn", "Repository.refreshAllFromPersistedUrisIfChanged end")
    }

    /**
     * Centralized processing for the "Regular List" (Cast).
     * Row 1: Play Title
     * Row 2: Optional Last Performance Date (dd/mm/yyyy)
     * Row 3+: Names
     */
    suspend fun processRegularListLines(lines: List<String>) {
        if (lines.isEmpty()) return
        Log.d("SignIn", "processRegularListLines: Processing ${lines.size} rows")
        
        // 1. Play Title (Row 1)
        val newHeader = lines.first().take(30)
        
        // 2. Performance Date Check (Row 2 / Cell A2)
        val potentialDate = lines.getOrNull(1)
        Log.d("SignIn", "processRegularListLines: Checking Row 2 for date: '$potentialDate'")
        val parsedDate = parseDate(potentialDate)
        
        if (parsedDate != null) {
            // It's a valid date. Cache it and check if it's in the past.
            val dateStr = parsedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            Log.d("SignIn", "processRegularListLines: Found valid date: $dateStr")
            
            if (isDateInPast(dateStr)) {
                Log.d("SignIn", "processRegularListLines: Performance date $dateStr is in the past. Resetting Cast List.")
                resetRegularListAndUseDefaultTitle()
            } else {
                Log.d("SignIn", "processRegularListLines: Performance date $dateStr is today or future. Loading members.")
                // Date is today or in the future, load names from Row 3 onwards
                updateSettings { it.copy(lastPerformanceDate = dateStr, lastRegularListHeader = newHeader) }
                val members = lines.drop(2).filter { it.isNotBlank() }
                withContext(Dispatchers.Main) {
                    playTitle = newHeader
                    replaceRegularListMembersPreserveState(members)
                }
            }
        } else {
            Log.d("SignIn", "processRegularListLines: No valid date found in Row 2. Treating as legacy format (names from Row 2).")
            // No valid date found in Row 2. Treat Row 2 as the first member (old format).
            updateSettings { it.copy(lastPerformanceDate = null, lastRegularListHeader = newHeader) }
            val members = lines.drop(1).filter { it.isNotBlank() }
            withContext(Dispatchers.Main) {
                playTitle = newHeader
                replaceRegularListMembersPreserveState(members)
            }
        }
    }

    /**
     * Parse a date string into a LocalDate. Supports common formats and Excel numeric dates.
     */
    private fun parseDate(dateStr: String?): java.time.LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        
        // 1. Pre-cleaning: remove common labels and standardize separators
        var clean = dateStr.trim().lowercase()
            .replace("last performance:", "")
            .replace("performance date:", "")
            .replace("ends:", "")
            .replace("date:", "")
            .trim()
            .replace("-", "/")
            .replace(".", "/")
            .replace("\\s+".toRegex(), " ")

        // 2. Try to isolate a date-like pattern (handles d/m/y or y/m/d)
        val datePattern = Regex("""\d{1,4}[/]\d{1,2}[/]\d{1,4}""")
        val match = datePattern.find(clean)
        val candidate = if (match != null) match.value else clean

        // 3. Try common numeric formats
        val formats = listOf(
            "dd/MM/yyyy", "d/M/yyyy", "yyyy/MM/dd", "yyyy/M/d",
            "MM/dd/yyyy", "M/d/yyyy", "dd/MM/yy", "d/M/yy"
        )
        for (format in formats) {
            try {
                return java.time.LocalDate.parse(candidate, java.time.format.DateTimeFormatter.ofPattern(format, java.util.Locale.US))
            } catch (_: Exception) {}
        }

        // 4. Try parsing as a word-based date (ignoring day of week)
        // Strip day names if present (e.g., "Monday 18 Sep 2026")
        val stripped = clean.replace(Regex("""^(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed|thu|fri|sat|sun)[,.\s]*"""), "").trim()
        val textFormats = listOf(
            "d MMM yyyy", "dd MMM yyyy", "d MMMM yyyy", "dd MMMM yyyy",
            "MMM d yyyy", "MMMM d yyyy", "MMM dd yyyy", "MMMM dd yyyy"
        )
        for (format in textFormats) {
            try {
                return java.time.LocalDate.parse(stripped, java.time.format.DateTimeFormatter.ofPattern(format, java.util.Locale.US))
            } catch (_: Exception) {}
        }

        // 5. Handle Excel numeric date strings (e.g., "46238")
        if (clean.all { it.isDigit() } && clean.length in 4..6) {
            try {
                val excelDay = clean.toLong()
                return java.time.LocalDate.of(1899, 12, 30).plusDays(excelDay)
            } catch (_: Exception) {}
        }
        
        Log.w("SignIn", "parseDate: Failed to parse '$dateStr' (cleaned to '$clean', candidate '$candidate')")
        return null
    }

    /**
     * Get the cached performance date formatted for UI display (e.g. 04 Aug '26).
     */
    fun getFormattedPerformanceDate(): String? {
        val date = parseDate(settings.lastPerformanceDate) ?: return null
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM ''yy", java.util.Locale.US))
    }

    /**
     * Get the number of days remaining until the cached performance date (e.g. 5d).
     */
    fun getDaysRemaining(): String? {
        val date = parseDate(settings.lastPerformanceDate) ?: return null
        val today = java.time.LocalDate.now()
        val days = java.time.temporal.ChronoUnit.DAYS.between(today, date)
        // If it's today or in the future, return the number.
        return if (days >= 0) "$days" else null
    }

    /**
     * Check if a date string represents a date that is strictly in the past,
     * taking into account the configured Auto Signout Time (dailyJobHour/Minute)
     * so that performances running past midnight don't trigger premature resets.
     */
    private fun isDateInPast(dateStr: String?): Boolean {
        if (dateStr.isNullOrBlank()) return false
        val date = parseDate(dateStr)
        if (date == null) {
            Log.w("SignIn", "isDateInPast: Could not parse '$dateStr'. Assuming not in past to avoid accidental reset.")
            return false
        }
        val now = LocalDateTime.now()
        val cutoffTime = now.toLocalDate().atTime(settings.dailyJobHour.coerceIn(0, 23), settings.dailyJobMinute.coerceIn(0, 59))
        val effectiveToday = if (now.isBefore(cutoffTime)) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        val inPast = date.isBefore(effectiveToday)
        Log.d("SignIn", "isDateInPast Check: Performance Date [$date] vs Effective Today [$effectiveToday] (Cutoff: $cutoffTime) -> inPast=$inPast (Source: '$dateStr')")
        return inPast
    }

    /**
     * Best-effort read of the file size for a given content Uri string.
     */
    fun getUriSize(uriString: String?): Long? {
        if (!::appContext.isInitialized) return null
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            appContext.contentResolver.openFileDescriptor(uri, "r")?.use { 
                it.statSize 
            }
        }.getOrNull()
    }

    /**
     * Best-effort read of the last-modified timestamp for a given content Uri string.
     * Tries DocumentsContract's COLUMN_LAST_MODIFIED, then falls back to MediaStore DATE_MODIFIED/DATE_ADDED.
     */
    fun getUriLastModifiedMillis(uriString: String?): Long? {
        if (!::appContext.isInitialized) return null
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            // Try DocumentsContract API (works for many SAF providers)
            val docLastMod: Long? = runCatching {
                val projection = arrayOf(android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                appContext.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                    if (c.moveToFirst()) {
                        val idx = c.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                        if (idx >= 0) {
                            val v = c.getLong(idx)
                            if (v > 0L) return@runCatching v
                        }
                    }
                }
                null
            }.getOrNull()
            if (docLastMod != null && docLastMod > 0L) return docLastMod

            // Fallback: query MediaStore
            val projection = arrayOf(
                android.provider.MediaStore.MediaColumns.DATE_MODIFIED,
                android.provider.MediaStore.MediaColumns.DATE_ADDED
            )
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idxMod = c.getColumnIndex(android.provider.MediaStore.MediaColumns.DATE_MODIFIED)
                    val idxAdd = c.getColumnIndex(android.provider.MediaStore.MediaColumns.DATE_ADDED)
                    val sec = when {
                        idxMod >= 0 -> c.getLong(idxMod)
                        idxAdd >= 0 -> c.getLong(idxAdd)
                        else -> 0L
                    }
                    // MediaStore dates are in seconds since epoch; convert to millis
                    return if (sec > 0) sec * 1000 else null
                }
            }
            null
        }.getOrNull()
    }

    /**
     * Schedule a WorkManager periodic job to run daily around 3:00 AM local time.
     */
    private fun scheduleDailyMaintenance(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val uniqueName = "DailyMaintenanceWork"

        if (!settings.dailyJobEnabled) {
            workManager.cancelUniqueWork(uniqueName)
            return
        }

        // Compute initial delay until next scheduled time
        val now = java.time.ZonedDateTime.now()
        var nextRun = now.withHour(settings.dailyJobHour.coerceIn(0,23))
            .withMinute(settings.dailyJobMinute.coerceIn(0,59))
            .withSecond(0)
            .withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        val initialDelayMinutes = java.time.Duration.between(now, nextRun).toMinutes().coerceAtLeast(1)

        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun rescheduleDailyMaintenance() {
        if (!::appContext.isInitialized) return
        scheduleDailyMaintenance(appContext)
        scheduleHourlyRefresh(appContext)
    }

    fun rescheduleHourlyRefresh() {
        if (!::appContext.isInitialized) return
        scheduleHourlyRefresh(appContext)
    }

    fun cancelDailyMaintenance() {
        if (!::appContext.isInitialized) return
        val workManager = WorkManager.getInstance(appContext)
        workManager.cancelUniqueWork("DailyMaintenanceWork")
        workManager.cancelUniqueWork("HourlyRefreshWork")
    }

    /**
     * Schedule hourly refresh worker that runs every hour
     */
    private fun scheduleHourlyRefresh(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val uniqueName = "HourlyRefreshWork"

        if (!settings.dailyJobEnabled) {
            workManager.cancelUniqueWork(uniqueName)
            return
        }

        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Schedule to run every configurable interval in minutes
        val effectiveIntervalMinutes = settings.hourlyRefreshInterval.coerceAtLeast(15).toLong()
        val request = PeriodicWorkRequestBuilder<HourlyRefreshWorker>(effectiveIntervalMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES) // Start quickly after app launch
            .build()

        workManager.enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun nextDailyRunTimeString(): String {
        val now = java.time.ZonedDateTime.now()
        var nextRun = now.withHour(settings.dailyJobHour.coerceIn(0,23))
            .withMinute(settings.dailyJobMinute.coerceIn(0,59))
            .withSecond(0)
            .withNano(0)
        if (!nextRun.isAfter(now)) nextRun = nextRun.plusDays(1)
        val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(now.zone)
        return fmt.format(nextRun)
    }

    /**
     * Reset Regular List ("Cast") to empty and set runtime Play Title to the current Default Title.
     * Does not modify the Default Title itself. Also clears the persisted Regular List URI so the
     * Admin upload button appears in its default state next time.
     */
       suspend fun resetRegularListAndUseDefaultTitle() {
        logAdminEvent("Cast reset triggered")
        withContext(Dispatchers.Main) {
            stageMembers.clear()
            stageActive.clear()
            stageTappedAt.clear()
            if (::appContext.isInitialized) {
                Storage.saveRegularListMembers(appContext, stageMembers)
                Storage.saveRegularListState(appContext, stageActive, stageTappedAt, stageVehicleReg)
            }
            playTitle = settings.headerText
            // Keep the Regular List URI so the next auto refresh can conditionally reload from it
            updateSettings { it.copy(conditionalRegularListReloadPending = true, lastPerformanceDate = null, lastRegularListHeader = null) }
        }
    }

    /**
     * Resets the warehouse active button to OFF. Called during daily refresh.
     */
    fun resetWarehouseState() {
        updateSettings { it.copy(isWarehouseActive = false) }
    }

    /**
     * Check if the cached performance date is in the past and trigger a reset if so.
     */
    suspend fun checkAndApplyPassiveReset() {
        if (settings.lastPerformanceDate != null && isDateInPast(settings.lastPerformanceDate)) {
            Log.d("SignIn", "Passive Reset Check: Performance date ${settings.lastPerformanceDate} is in the past. Triggering reset.")
            resetRegularListAndUseDefaultTitle()
        }
    }

    /**
     * Best-effort read of the created timestamp for a given content Uri string.
     * Tries MediaStore DATE_ADDED first; DocumentsContract does not expose created reliably.
     */
    fun getUriCreatedMillis(uriString: String?): Long? {
        if (!::appContext.isInitialized) return null
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(uriString)
            // Prefer MediaStore DATE_ADDED when available
            val projection = arrayOf(
                android.provider.MediaStore.MediaColumns.DATE_ADDED
            )
            appContext.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idxAdd = c.getColumnIndex(android.provider.MediaStore.MediaColumns.DATE_ADDED)
                    val sec = if (idxAdd >= 0) c.getLong(idxAdd) else 0L
                    return if (sec > 0) sec * 1000 else null
                }
            }
            null
        }.getOrNull()
    }

    /**
     * Helper to extract cell value as a trimmed string.
     * Correctly handles numeric dates by formatting them as yyyy-MM-dd.
     */
    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    val date = cell.dateCellValue
                    if (date != null) {
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
                    } else ""
                } else {
                    // Standard number
                    cell.numericCellValue.toLong().toString().trim()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                // Try to get evaluated value if possible, else string
                try { cell.stringCellValue.trim() } catch (_: Exception) {
                    try { cell.numericCellValue.toLong().toString().trim() } catch (_: Exception) { "" }
                }
            }
            else -> ""
        }
    }

    /**
     * Excel file reader: reads first names from column A and second names from column B for Names database
     * Starts from row 2 to skip headers, combines names with space, sorts alphabetically by first name
     */
    fun readExcelNames(uriStr: String?, preferredSheets: List<String>? = null): List<String> {
        if (uriStr.isNullOrBlank()) return emptyList()
        return try {
            val uri = Uri.parse(uriStr)
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val workbook: Workbook = WorkbookFactory.create(input)

                workbook.use { wb ->
                    var sheet: Sheet? = null
                    preferredSheets?.forEach { name ->
                        if (sheet == null) sheet = wb.getSheet(name)
                    }
                    val finalSheet = sheet ?: wb.getSheetAt(0)
                    val result = mutableListOf<String>()
                    val lastRow = finalSheet.lastRowNum
                    
                    for (rowIndex in 0..lastRow) {
                        // Skip header row (row 0 - index 0)
                        if (rowIndex == 0) continue
                        
                        val row = finalSheet.getRow(rowIndex)
                        if (row == null) continue
                        
                        // Get first name from column A (index 0)
                        val firstName = getCellValueAsString(row.getCell(0))
                        
                        // Get second name from column B (index 1)
                        val secondName = getCellValueAsString(row.getCell(1))
                        
                        // Combine first and second names with space, but only if at least first name exists
                        val fullName = when {
                            firstName.isNotBlank() && secondName.isNotBlank() -> "$firstName $secondName"
                            firstName.isNotBlank() -> firstName
                            else -> ""
                        }
                        
                        if (fullName.isNotBlank()) {
                            result.add(fullName)
                        }
                    }
                    
                    // Sort alphabetically by first name (which is the primary part of the full name)
                    result.sortedBy { it.split(" ").first().lowercase() }
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("Repository", "Error reading Excel names file: ${e.message}")
            emptyList()
        }
    }

    /**
     * Excel file reader: reads a specific column from .xls/.xlsx files
     */
    fun readExcelColumn(uriStr: String?, colIndex: Int = 0, preferredSheets: List<String>? = null): List<String> {
        if (uriStr.isNullOrBlank()) return emptyList()
        return try {
            val uri = Uri.parse(uriStr)
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                val workbook: Workbook = WorkbookFactory.create(input)

                workbook.use { wb ->
                    var sheet: Sheet? = null
                    preferredSheets?.forEach { name ->
                        if (sheet == null) sheet = wb.getSheet(name)
                    }
                    val finalSheet = sheet ?: wb.getSheetAt(0)
                    val result = mutableListOf<String>()
                    val lastRow = finalSheet.lastRowNum
                    for (i in 0..lastRow) {
                        val row = finalSheet.getRow(i)
                        result.add(getCellValueAsString(row?.getCell(colIndex)))
                    }
                    result
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("Repository", "Error reading Excel file: ${e.message}")
            emptyList()
        }
    }
}
