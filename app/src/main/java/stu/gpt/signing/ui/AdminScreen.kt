package stu.gpt.signing.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import stu.gpt.signing.data.Repository

private enum class DataKind { Names, Lists, Locations, RegularList, Questions, Titles }

@Suppress("DEPRECATION")
private fun showLargeToast(context: Context, message: String, textSizeSp: Float = 22f) {
    val tv = android.widget.TextView(context).apply {
        text = message
        setTextColor(android.graphics.Color.WHITE)
        setPadding(32, 24, 32, 24)
        setBackgroundColor("#AA000000".toColorInt())
        textSize = textSizeSp
    }
    Toast(context).apply {
        duration = Toast.LENGTH_SHORT
        this.view = tv
        show()
    }
}

@Composable
fun AdminScreen(
    onSave: () -> Unit,
    onExitApp: () -> Unit,
    onExportHistory: () -> Unit,
) {
    val settings = Repository.settings
    var showPinChange by remember { mutableStateOf(false) }
    var selectedColorHex by remember { mutableStateOf(settings.selectedColorHex) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalActivity.current

    var pendingPick by remember { mutableStateOf<DataKind?>(null) }
    var refreshQueue by remember { mutableStateOf(listOf<DataKind>()) }

    val namesUploaded by remember { derivedStateOf { Repository.names.isNotEmpty() } }
    val stageUploaded by remember { derivedStateOf { Repository.stageMembers.isNotEmpty() } }

    var jobHourInput by remember { mutableStateOf(Repository.settings.dailyJobHour.toString().padStart(2, '0')) }
    var jobMinuteInput by remember { mutableStateOf(Repository.settings.dailyJobMinute.toString().padStart(2, '0')) }
    var hourlyIntervalInput by remember { mutableStateOf(Repository.settings.hourlyRefreshInterval.toString()) }
    var carRegDurationInput by remember { mutableStateOf(Repository.settings.carRegReminderDuration.toString()) }

    var showColorPickerDialog by remember { mutableStateOf(false) }

    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    Repository.updateSettings { it.copy(
                        logFolderUriString = uri.toString(),
                        logFolderDisplayName = Repository.getDisplayName(uri.toString()),
                        logFolderRelativePath = Repository.getRelativePath(uri.toString())
                    ) }
                    Repository.logAdminEvent("Log folder changed to: ${Repository.getDisplayName(uri.toString()) ?: uri.toString()}")
                } catch (e: Exception) {
                    showLargeToast(context, "Error selecting folder: ${e.message}")
                }
            }
        }
    )

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val lines = when (pendingPick) {
                    DataKind.Names -> Repository.readExcelNames(uri.toString())
                    DataKind.Titles, DataKind.Questions -> Repository.readExcelColumn(uri.toString(), 1)
                    else -> Repository.readExcelColumn(uri.toString(), 0)
                }
                if (lines.isNotEmpty()) {
                    when (pendingPick) {
                        DataKind.Names -> {
                            Repository.loadNames(lines)
                            Repository.updateSettings { it.copy(
                                namesUriString = uri.toString(),
                                namesDisplayName = Repository.getDisplayName(uri.toString()),
                                namesRelativePath = Repository.getRelativePath(uri.toString()),
                                namesMediaStoreId = Repository.getMediaStoreId(uri.toString()),
                                namesCreatedMillis = Repository.getUriCreatedMillis(uri.toString()),
                                namesLastModifiedMillis = Repository.getUriLastModifiedMillis(uri.toString()),
                                namesFirst1000Hash = Repository.computeFirstNHash(uri.toString(), 1000)
                            ) }
                        }
                        DataKind.Lists -> {
                            Repository.updateSettings { it.copy(rolesHeader = lines.firstOrNull()?.take(30) ?: "") }
                            Repository.loadRoles(lines.drop(1).filter { it.isNotBlank() })
                            Repository.updateSettings { it.copy(
                                rolesUriString = uri.toString(),
                                rolesDisplayName = Repository.getDisplayName(uri.toString()),
                                rolesRelativePath = Repository.getRelativePath(uri.toString()),
                                rolesMediaStoreId = Repository.getMediaStoreId(uri.toString()),
                                rolesCreatedMillis = Repository.getUriCreatedMillis(uri.toString()),
                                rolesLastModifiedMillis = Repository.getUriLastModifiedMillis(uri.toString()),
                                rolesFirst1000Hash = Repository.computeFirstNHash(uri.toString(), 1000)
                            ) }
                        }
                        DataKind.Locations -> {
                            Repository.updateSettings { it.copy(locationsHeader = lines.firstOrNull()?.take(30) ?: "") }
                            Repository.loadLocations(lines.drop(1).filter { it.isNotBlank() })
                            Repository.updateSettings { it.copy(
                                locationsUriString = uri.toString(),
                                locationsDisplayName = Repository.getDisplayName(uri.toString()),
                                locationsRelativePath = Repository.getRelativePath(uri.toString()),
                                locationsMediaStoreId = Repository.getMediaStoreId(uri.toString()),
                                locationsCreatedMillis = Repository.getUriCreatedMillis(uri.toString()),
                                locationsLastModifiedMillis = Repository.getUriLastModifiedMillis(uri.toString()),
                                locationsFirst1000Hash = Repository.computeFirstNHash(uri.toString(), 1000)
                            ) }
                        }
                        DataKind.RegularList -> {
                            scope.launch { Repository.processRegularListLines(lines) }
                            Repository.updateSettings { it.copy(
                                regularListUriString = uri.toString(),
                                regularListDisplayName = Repository.getDisplayName(uri.toString()),
                                regularListRelativePath = Repository.getRelativePath(uri.toString()),
                                regularListMediaStoreId = Repository.getMediaStoreId(uri.toString()),
                                regularListCreatedMillis = Repository.getUriCreatedMillis(uri.toString()),
                                regularListLastModifiedMillis = Repository.getUriLastModifiedMillis(uri.toString()),
                                regularListFirst1000Hash = Repository.computeFirstNHash(uri.toString(), 1000)
                            ) }
                        }
                        DataKind.Questions -> {
                            Repository.loadQuestionLines(lines)
                            Repository.updateSettings { it.copy(
                                questionUriString = uri.toString(),
                                questionDisplayName = Repository.getDisplayName(uri.toString()),
                                questionRelativePath = Repository.getRelativePath(uri.toString()),
                                questionMediaStoreId = Repository.getMediaStoreId(uri.toString()),
                                questionCreatedMillis = Repository.getUriCreatedMillis(uri.toString()),
                                questionLastModifiedMillis = Repository.getUriLastModifiedMillis(uri.toString()),
                                questionFirst1000Hash = Repository.computeFirstNHash(uri.toString(), 1000)
                            ) }
                        }
                        DataKind.Titles -> {
                            Repository.loadTitles(lines)
                            Repository.updateSettings { it.copy(
                                titlesUriString = uri.toString(),
                                titlesDisplayName = Repository.getDisplayName(uri.toString()),
                                titlesRelativePath = Repository.getRelativePath(uri.toString()),
                                titlesMediaStoreId = Repository.getMediaStoreId(uri.toString()),
                                titlesCreatedMillis = Repository.getUriCreatedMillis(uri.toString()),
                                titlesLastModifiedMillis = Repository.getUriLastModifiedMillis(uri.toString()),
                                titlesFirst1000Hash = Repository.computeFirstNHash(uri.toString(), 1000)
                            ) }
                        }
                        null -> {}
                    }
                    try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Throwable) { }
                    showLargeToast(context, "File uploaded successfully", 18f)
                }
            }
            pendingPick = null
        }
    )

    fun mimeTypes() = arrayOf(
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/octet-stream"
    )

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
                        val sheet = workbook.createSheet("History")
                        val header = sheet.createRow(0)
                        listOf("Name", "Role/Location", "Sign-in", "Sign-out", "Vehicle Reg").forEachIndexed { i, s -> header.createCell(i).setCellValue(s) }
                        val rows = Repository.getHistoryWithin(90L)
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(java.time.ZoneId.systemDefault())
                        rows.forEachIndexed { idx, e ->
                            val row = sheet.createRow(idx + 1)
                            row.createCell(0).setCellValue(e.name)
                            row.createCell(1).setCellValue(e.roleOrLocation)
                            row.createCell(2).setCellValue(formatter.format(java.time.Instant.ofEpochMilli(e.arrivalEpochMillis)))
                            row.createCell(3).setCellValue(formatter.format(java.time.Instant.ofEpochMilli(e.departureEpochMillis)))
                            row.createCell(4).setCellValue(e.vehicleReg ?: "")
                        }
                        workbook.write(out)
                        workbook.close()
                    }
                } catch (_: Throwable) { }
            }
            onExportHistory()
        }
    )

    val createDocxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    val bytes = Repository.buildAppGuideDocxBytes()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(bytes)
                        out.flush()
                    }
                } catch (_: Throwable) { }
            }
        }
    )

    Surface(modifier = Modifier.fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                Repository.updateActivityTime()
            })
        }, color = Repository.primaryBgColor) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(24.dp))
            val effectiveTitle = if (Repository.stageMembers.isNotEmpty()) Repository.playTitle else settings.headerText
            Text("Admin Area – $effectiveTitle", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 10.dp)) {
                // Row 1: Security & Warehouse Mode
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminTile(title = "Security", modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Button(onClick = { showPinChange = true }, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp)) {
                            Text("Change PIN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    AdminTile(title = "Warehouse Mode", modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Enabled", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Switch(
                                checked = Repository.settings.warehouseModeEnabled,
                                onCheckedChange = { enabled -> 
                                    Repository.updateSettings { s -> s.copy(warehouseModeEnabled = enabled) }
                                    if (!enabled) {
                                        // If feature is disabled, force back to default Cast-1
                                        Repository.updateSettings { it.copy(isWarehouseActive = false) }
                                        scope.launch { Repository.refreshAllFromPersistedUris() }
                                    }
                                },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Row 2: Appearance & Reminders
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminTile(title = "Appearance", modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Click to change", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(try { Color(android.graphics.Color.parseColor(selectedColorHex)) } catch(_: Exception) { Color.Gray })
                                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                    .clickable {
                                        showColorPickerDialog = true
                                    }
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Flip (180°)", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                            Switch(modifier = Modifier.scale(0.9f), checked = Repository.settings.isScreenFlipped, onCheckedChange = { 
                                Repository.updateSettings { s -> s.copy(isScreenFlipped = it) }
                                activity?.requestedOrientation = if (it) ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            })
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Screen Saver", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                            Switch(
                                checked = Repository.settings.screenSaverEnabled,
                                onCheckedChange = { 
                                    Repository.updateSettings { s -> s.copy(screenSaverEnabled = it) }
                                },
                                modifier = Modifier.scale(0.9f)
                            )
                        }
                    }

                    AdminTile(title = "Reminders", modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Car Reg Prompt", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                            Switch(modifier = Modifier.scale(0.9f), checked = Repository.settings.vehicleRegPromptEnabled, onCheckedChange = { Repository.updateSettings { s -> s.copy(vehicleRegPromptEnabled = it) } })
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reminder Pop-up", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                            Switch(modifier = Modifier.scale(0.9f), checked = Repository.settings.carRegReminderEnabled, onCheckedChange = { Repository.updateSettings { s -> s.copy(carRegReminderEnabled = it) } })
                        }
                        if (Repository.settings.carRegReminderEnabled) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Duration (1-5s)", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.6f))
                                TextField(
                                    value = carRegDurationInput,
                                    onValueChange = { carRegDurationInput = it.filter { c -> c.isDigit() }.take(1) },
                                    singleLine = true,
                                    modifier = Modifier.width(60.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                AdminTile(title = "Automation & Maintenance") {
                    val lastSyncStr = remember(Repository.settings.lastSyncTime) {
                        if (Repository.settings.lastSyncTime > 0) {
                            java.time.Instant.ofEpochMilli(Repository.settings.lastSyncTime)
                                .atZone(java.time.ZoneId.systemDefault())
                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        } else "Never"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Last Sync Status", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                        Text(lastSyncStr, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Auto Sign-out Everyone Daily", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                        Switch(modifier = Modifier.scale(1.0f), checked = Repository.settings.dailyJobEnabled, onCheckedChange = {
                            Repository.updateSettings { s -> s.copy(dailyJobEnabled = it) }
                            if (it) Repository.rescheduleDailyMaintenance() else Repository.cancelDailyMaintenance()
                        })
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Auto Export Data Daily", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                        Switch(modifier = Modifier.scale(1.0f), checked = Repository.settings.autoExportEnabled, onCheckedChange = {
                            Repository.updateSettings { s -> s.copy(autoExportEnabled = it) }
                        })
                    }
                    if (Repository.settings.dailyJobEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Auto Sign-out Time (HH:MM)", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.5f))
                            Column(modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextField(value = jobHourInput, onValueChange = { jobHourInput = it.filter { c -> c.isDigit() }.take(2) }, singleLine = true, modifier = Modifier.width(60.dp), textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    Text(":", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                    TextField(value = jobMinuteInput, onValueChange = { jobMinuteInput = it.filter { c -> c.isDigit() }.take(2) }, singleLine = true, modifier = Modifier.width(60.dp), textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Refresh Interval (minutes)", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(0.7f))
                        TextField(value = hourlyIntervalInput, onValueChange = { hourlyIntervalInput = it.filter { c -> c.isDigit() }.take(4) }, singleLine = true, modifier = Modifier.width(80.dp), textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Log Save Folder", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            val folderPathDisplay = if (settings.logFolderUriString == null) {
                                "Not Selected"
                            } else {
                                val rawRel = settings.logFolderRelativePath ?: ""
                                val disp = settings.logFolderDisplayName ?: ""
                                val cleanRel = rawRel.removePrefix("/").removePrefix("primary:")
                                buildString {
                                    if (cleanRel.isNotBlank()) {
                                        append(cleanRel)
                                        if (!cleanRel.endsWith("/") && disp.isNotBlank()) append("/")
                                    } else {
                                        append("Documents/Sign-In Data/")
                                    }
                                    if (disp.isNotBlank() && !cleanRel.endsWith(disp)) {
                                        append(disp)
                                    }
                                }.trimEnd('/') + "/"
                            }
                            Text(
                                text = folderPathDisplay,
                                fontSize = 12.sp,
                                color = if (settings.logFolderUriString == null) Color.Red else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { directoryPicker.launch(null) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.logFolderUriString != null) Color(0xFFBEE9AD) else Color(0xFFE57373),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(if (settings.logFolderUriString != null) "CHANGE" else "SELECT", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                AdminTile(
                    title = "Data Management",
                    headerTrailing = {
                        val performanceDate = settings.lastPerformanceDate
                        if (!performanceDate.isNullOrBlank()) {
                            Repository.getFormattedPerformanceDate()?.let { date ->
                                Text(
                                    text = "Ends: $date",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD32F2F),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DataButton("Members", modifier = Modifier.weight(1f).height(40.dp), backgroundColor = if (namesUploaded) Color(0xFFBEE9AD) else Color(0xFFD9D9D9)) { pendingPick = DataKind.Names; filePicker.launch(mimeTypes()) }
                            DataButton("Cast", modifier = Modifier.weight(1f).height(40.dp), backgroundColor = if (stageUploaded) Color(0xFFBEE9AD) else Color(0xFFD9D9D9)) { pendingPick = DataKind.RegularList; filePicker.launch(mimeTypes()) }
                            DataButton("Roles", modifier = Modifier.weight(1f).height(40.dp), backgroundColor = if (Repository.roles.isNotEmpty()) Color(0xFFBEE9AD) else Color(0xFFD9D9D9)) { pendingPick = DataKind.Lists; filePicker.launch(mimeTypes()) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DataButton("Locations", modifier = Modifier.weight(1f).height(40.dp), backgroundColor = if (Repository.locations.isNotEmpty()) Color(0xFFBEE9AD) else Color(0xFFD9D9D9)) { pendingPick = DataKind.Locations; filePicker.launch(mimeTypes()) }
                            DataButton("Titles", modifier = Modifier.weight(1f).height(40.dp), backgroundColor = if (Repository.settings.titlesUriString != null) Color(0xFFBEE9AD) else Color(0xFFD9D9D9)) { pendingPick = DataKind.Titles; filePicker.launch(mimeTypes()) }
                            DataButton("Questions", modifier = Modifier.weight(1f).height(40.dp), backgroundColor = if (Repository.questionLines.isNotEmpty()) Color(0xFFBEE9AD) else Color(0xFFD9D9D9)) { pendingPick = DataKind.Questions; filePicker.launch(mimeTypes()) }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                // Bottom Action Bar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = { 
                        val hour = jobHourInput.toIntOrNull()?.coerceIn(0, 23) ?: settings.dailyJobHour
                        val min = jobMinuteInput.toIntOrNull()?.coerceIn(0, 59) ?: settings.dailyJobMinute
                        val interval = hourlyIntervalInput.toIntOrNull()?.coerceIn(15, 1440) ?: settings.hourlyRefreshInterval
                        val duration = carRegDurationInput.toIntOrNull()?.coerceIn(1, 5) ?: settings.carRegReminderDuration
                        Repository.updateSettings { it.copy(dailyJobHour = hour, dailyJobMinute = min, hourlyRefreshInterval = interval, carRegReminderDuration = duration) }
                        Repository.rescheduleDailyMaintenance()
                        onSave()
                        showLargeToast(context, "Saved")
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) { Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    
                    Button(
                        onClick = { 
                            scope.launch {
                                Repository.logAdminEvent("Manual refresh triggered")
                                Repository.refreshAllFromPersistedUris()
                                showLargeToast(context, "Refreshed") 
                            }
                        }, 
                        modifier = Modifier.weight(1f), 
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), 
                        shape = RoundedCornerShape(8.dp), 
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) { 
                        Text("REFRESH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                    }

                    Button(onClick = { createCsvLauncher.launch("Sign-In_History.xlsx") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) { Text("EXPORT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    
                    Button(onClick = onExitApp, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) { Text("EXIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                }

                Spacer(Modifier.height(8.dp))

                var showAdminHelp by remember { mutableStateOf(false) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { createDocxLauncher.launch("Sign-In_App_Guide.docx") },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("GUIDE DOWNLOAD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                    }

                    Button(
                        onClick = { showAdminHelp = true },
                        modifier = Modifier.weight(0.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("HELP INFO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                    }
                }

                if (showAdminHelp) {
                    AlertDialog(
                        onDismissRequest = { showAdminHelp = false },
                        confirmButton = { Button(onClick = { 
                            Repository.updateActivityTime()
                            showAdminHelp = false 
                        }) { Text("Back", fontSize = 18.sp, fontWeight = FontWeight.Bold) } },
                        title = { Text("Administration & Technical Support", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Box(modifier = Modifier.height(400.dp)) {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()) {
                                    TechnicalBullet("Members: Col A = First Name, Col B = Last Name. Starts Row 2.")
                                    TechnicalBullet("Roles/Locations: Column A, Row 2 down.")
                                    TechnicalBullet("Cast: Col A. Row 1=Title, Row 2=Last Perf. Date (dd/mm/yyyy), Row 3+=Names.")
                                    TechnicalBullet("Titles: Column B. Row 2=Title, Row 3=LH Head, Row 4=RH Head, Row 5=SS Msg.")
                                    TechnicalBullet("Questions: Column B. Row 2-14=Checklist, Row 15=Car L1, Row 16=Car L2.")
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            Text(
                text = "Created by Stu Sorrell, Using Gemini-3 in 2026. v ${Repository.VERSION}",
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }
    }

    if (showPinChange) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinChange = false },
            title = { Text("Set New Admin PIN", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a new 4-digit PIN to access this Admin area via the Name box.", fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = newPin,
                        onValueChange = { input ->
                            newPin = input.filter { it.isDigit() }.take(4)
                        },
                        placeholder = { Text("4-digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            Repository.updateSettings { s -> s.copy(pin = newPin) }
                            showPinChange = false
                            showLargeToast(context, "PIN Changed")
                        }
                    },
                    enabled = newPin.length == 4
                ) { Text("SET", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(onClick = { showPinChange = false }) { Text("CANCEL", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (showColorPickerDialog) {
        val initialColor = try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (_: Exception) {
            Color(0xFF3366CC)
        }

        var red by remember { mutableFloatStateOf(initialColor.red * 255f) }
        var green by remember { mutableFloatStateOf(initialColor.green * 255f) }
        var blue by remember { mutableFloatStateOf(initialColor.blue * 255f) }

        val currentColor = Color(red.toInt().coerceIn(0, 255), green.toInt().coerceIn(0, 255), blue.toInt().coerceIn(0, 255))
        val computedHex = String.format("#%02X%02X%02X", red.toInt().coerceIn(0, 255), green.toInt().coerceIn(0, 255), blue.toInt().coerceIn(0, 255))

        AlertDialog(
            onDismissRequest = { showColorPickerDialog = false },
            title = { Text("Choose Theme Color", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Use the RGB sliders to choose any custom color:", fontSize = 14.sp)
                    
                    // Live Color Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(currentColor)
                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = computedHex,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentColor.red * 0.299f + currentColor.green * 0.587f + currentColor.blue * 0.114f > 0.5f) Color.Black else Color.White
                        )
                    }

                    // RGB Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Red: ${red.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = red,
                            onValueChange = { red = it },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                        )

                        Text("Green: ${green.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = green,
                            onValueChange = { green = it },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                        )

                        Text("Blue: ${blue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = blue,
                            onValueChange = { blue = it },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedColorHex = computedHex
                    Repository.updateSettings { s -> s.copy(selectedColorHex = computedHex) }
                    showColorPickerDialog = false
                }) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = { showColorPickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminTile(
    title: String,
    modifier: Modifier = Modifier,
    headerTrailing: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF9F9F9),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(10.dp).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.DarkGray,
                    textDecoration = TextDecoration.Underline
                )
                headerTrailing()
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DataButton(label: String, modifier: Modifier = Modifier, backgroundColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun TechnicalBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text("•", modifier = Modifier.width(16.dp), fontWeight = FontWeight.Bold)
        Text(text, fontSize = 15.sp)
    }
}
