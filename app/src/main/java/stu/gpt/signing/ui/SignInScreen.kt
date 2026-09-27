package stu.gpt.signing.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import stu.gpt.signing.R
import stu.gpt.signing.data.Repository
import stu.gpt.signing.data.VisitorEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SignInScreen(
    onAdminDirectSuccess: () -> Unit,
    onSignInSuccess: () -> Unit,
) {
    val settings = Repository.settings
    val visitors = Repository.visitors
    val stageMembers = Repository.stageMembers
    val stageTappedAt = Repository.stageTappedAt
    val stageActive = Repository.stageActive

    val repositoryScope = rememberCoroutineScope()
    var nameInput by remember { mutableStateOf("") }
    var alreadySignedInWarningName by remember { mutableStateOf<String?>(null) }
    var roleInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var showRoleDropdown by remember { mutableStateOf(false) }
    var showLocationDropdown by remember { mutableStateOf(false) }
    var showNameDropdown by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val outerFocusManager = LocalFocusManager.current
    val outerKeyboard = LocalSoftwareKeyboardController.current

    var signOutTarget by remember { mutableStateOf<VisitorEntry?>(null) }
    var memberSignOutTarget by remember { mutableStateOf<String?>(null) }
    var memberSignInTarget by remember { mutableStateOf<String?>(null) }
    var visitorSignInTargetName by remember { mutableStateOf<String?>(null) }
    var visitorSignInTargetRole by remember { mutableStateOf("") }
    var memberVehicleReg by remember { mutableStateOf("") }

    var showCarRegReminder by remember { mutableStateOf(false) }

    val performSignIn = remember {
        { name: String, roleOrLoc: String ->
            val trimmed = name.trim()
            if (Repository.isAlreadySignedIn(trimmed)) {
                alreadySignedInWarningName = trimmed
                nameInput = ""
                roleInput = ""
                locationInput = ""
                outerFocusManager.clearFocus()
                outerKeyboard?.hide()
            } else {
                if (Repository.settings.vehicleRegPromptEnabled) {
                    visitorSignInTargetName = trimmed
                    visitorSignInTargetRole = roleOrLoc
                    memberVehicleReg = Repository.lastVehicleRegForName(trimmed) ?: ""
                    
                    nameInput = ""
                    roleInput = ""
                    locationInput = ""
                    outerFocusManager.clearFocus()
                    outerKeyboard?.hide()
                } else {
                    Repository.signIn(trimmed, roleOrLoc)
                    showCarRegReminder = true
                    nameInput = ""
                    roleInput = ""
                    locationInput = ""
                    outerFocusManager.clearFocus()
                    outerKeyboard?.hide()
                    onSignInSuccess()
                }
            }
        }
    }

    LaunchedEffect(showCarRegReminder) {
        if (showCarRegReminder) {
            delay(Repository.settings.carRegReminderDuration * 1000L)
            showCarRegReminder = false
        }
    }

    val stageMembersOrdered by remember {
        derivedStateOf {
            stageMembers.sortedWith(
                compareBy<String> { stageActive.containsKey(it) } // Absent (false) first, Signed-In (true) second
                    .thenBy { it.lowercase() } // Alphabetical within both groups
            )
        }
    }

    val filteredNames = remember(nameInput) {
        if (nameInput.isBlank()) emptyList()
        else Repository.names.filter { it.startsWith(nameInput, ignoreCase = true) }
    }

    val isSignInEnabled = remember(nameInput, roleInput, locationInput) {
        val trimmed = nameInput.trim()
        val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val isKnownMember = Repository.names.any { it.equals(trimmed, ignoreCase = true) }
        
        val nameValid = if (isKnownMember) {
            trimmed.isNotBlank()
        } else {
            // Guest validation: 2 letters first part, 3 letters second part (AA AAA)
            parts.size >= 2 && parts[0].length >= 2 && parts[1].length >= 3
        }
        nameValid && (roleInput.isNotBlank() || locationInput.isNotBlank())
    }
    val signInButtonColor = if (isSignInEnabled) Color(0xff00ff00) else Color(0xFFEAF8E4)

    val isDarkMode = stageMembers.isEmpty()
    val validMidChar = settings.darkPanelSplitChar
    val nextChar = if (validMidChar < 'Z') (validMidChar + 1) else 'Z'

    val visitorsLeft = visitors.filter { e ->
        val ch = e.name.trim().firstOrNull()?.uppercaseChar() ?: 'A'
        ch <= validMidChar
    }
    val visitorsRight = visitors.filter { e ->
        val ch = e.name.trim().firstOrNull()?.uppercaseChar() ?: 'A'
        ch > validMidChar
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    Repository.isManualScreenSaver = false
                    Repository.updateActivityTime()
                    outerFocusManager.clearFocus()
                    outerKeyboard?.hide()
                })
            },
        color = settings.primaryBgColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Discreet Sync Timestamp (Top-Right)
            if (settings.lastSyncTime > 0) {
                val syncTime = remember(settings.lastSyncTime) {
                    java.time.Instant.ofEpochMilli(settings.lastSyncTime)
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                }
                Text(
                    text = "Sync: $syncTime",
                    fontSize = 10.sp,
                    color = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .zIndex(10f)
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
            Spacer(Modifier.height(15.dp)) // Lowered main heading (5dp + 10dp)
            val effectiveTitle = if (Repository.stageMembers.isNotEmpty()) Repository.playTitle else settings.headerText
            
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = effectiveTitle,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = settings.primaryTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(11.dp))

            // SIGN-IN FORM AREA (Removed Surface box as requested)
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                // Row 1: Name and Sign-In Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp), // Decreased from 8dp
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { 
                                Repository.updateActivityTime()
                                when {
                                    it == Repository.settings.pin -> {
                                        onAdminDirectSuccess()
                                        nameInput = ""
                                    }
                                    it == "##" -> {
                                        repositoryScope.launch {
                                            Repository.resetRegularListAndUseDefaultTitle()
                                        }
                                        nameInput = ""
                                    }
                                    it == "@@" -> {
                                        Repository.isManualScreenSaver = true
                                        nameInput = ""
                                    }
                                    else -> {
                                        nameInput = it.take(30)
                                        showNameDropdown = it.isNotBlank()
                                    }
                                }
                            },
                            placeholder = { Text("Name", fontSize = 22.sp, color = Color.Gray.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp), // Radiused corners
                            textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            visualTransformation = if (nameInput.all { it.isDigit() } && nameInput.isNotEmpty()) PasswordVisualTransformation() else VisualTransformation.None,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9F9F9),
                                unfocusedContainerColor = Color(0xFFF9F9F9)
                            )
                        )
                        
                        DropdownMenu(
                            expanded = showNameDropdown && filteredNames.isNotEmpty(),
                            onDismissRequest = { showNameDropdown = false },
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                            modifier = Modifier.fillMaxWidth(0.5f)
                        ) {
                            filteredNames.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        nameInput = name
                                        showNameDropdown = false
                                        outerFocusManager.clearFocus()
                                        outerKeyboard?.hide()
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isSignInEnabled) {
                                performSignIn(nameInput, roleInput.ifBlank { locationInput })
                            }
                        },
                        enabled = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp) // Adjusted height slightly to be more compact
                            .shadow(if (isSignInEnabled) 4.dp else 0.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = signInButtonColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Sign-In",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(3.dp)) // Decreased from 8dp

                // Row 2: Role, Warehouse Toggle (if enabled), and Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp), // Decreased from 8dp
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = roleInput,
                            onValueChange = { 
                                Repository.updateActivityTime()
                                roleInput = it.take(20)
                                if (it.isNotBlank()) locationInput = ""
                            },
                            placeholder = { Text("Role", fontSize = 22.sp, color = Color.Gray.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (it.isFocused) showRoleDropdown = true },
                            singleLine = true,
                            readOnly = false,
                            shape = RoundedCornerShape(8.dp), // Radiused corners
                            textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                if (isSignInEnabled) {
                                    Repository.signIn(nameInput, roleInput.ifBlank { locationInput })
                                    showCarRegReminder = true
                                    nameInput = ""
                                    roleInput = ""
                                    locationInput = ""
                                    outerFocusManager.clearFocus()
                                    outerKeyboard?.hide()
                                    onSignInSuccess()
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9F9F9),
                                unfocusedContainerColor = Color(0xFFF9F9F9)
                            )
                        )
                        DropdownMenu(
                            expanded = showRoleDropdown,
                            onDismissRequest = { showRoleDropdown = false },
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                        ) {
                            Repository.roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        roleInput = role
                                        locationInput = ""
                                        showRoleDropdown = false
                                        outerFocusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }

                    // Warehouse Mode Push Button
                    if (settings.warehouseModeEnabled) {
                        Button(
                            onClick = {
                                Repository.updateActivityTime()
                                val newState = !settings.isWarehouseActive
                                Repository.updateSettings { it.copy(isWarehouseActive = newState) }
                                // Trigger refresh to load the correct worksheet
                                repositoryScope.launch {
                                    Repository.refreshAllFromPersistedUris()
                                }
                            },
                            modifier = Modifier
                                .width(80.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Gray),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (settings.isWarehouseActive) Color(0xFFFED101) else Color(0xFFBEE9AD),
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (settings.isWarehouseActive) "YT" else "GPT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { 
                                Repository.updateActivityTime()
                                locationInput = it.take(20)
                                if (it.isNotBlank()) roleInput = ""
                            },
                            placeholder = { Text("Location", fontSize = 22.sp, color = Color.Gray.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (it.isFocused) showLocationDropdown = true },
                            singleLine = true,
                            readOnly = false,
                            shape = RoundedCornerShape(8.dp), // Radiused corners
                            textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                if (isSignInEnabled) {
                                    performSignIn(nameInput, roleInput.ifBlank { locationInput })
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9F9F9),
                                unfocusedContainerColor = Color(0xFFF9F9F9)
                            )
                        )
                        DropdownMenu(
                            expanded = showLocationDropdown,
                            onDismissRequest = { showLocationDropdown = false },
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                        ) {
                            Repository.locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        locationInput = loc
                                        roleInput = ""
                                        showLocationDropdown = false
                                        outerFocusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Row 3: Instruction Block
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp, top = 3.dp, bottom = 3.dp)) {
                val currentMaxWidth = maxWidth
                var dynamicFontSize by remember(currentMaxWidth) { mutableStateOf(22.sp) }

                Text(
                    text = buildAnnotatedString {
                        append("To Sign-In - Enter Name, select Role/Location, then tap Sign-In\n")
                        append("To Sign-Out - Tap Name and follow instructions.")
                    },
                    fontSize = dynamicFontSize,
                    fontWeight = FontWeight.Medium,
                    color = settings.primaryTextColor,
                    lineHeight = (dynamicFontSize.value * 1.1f).sp,
                    textAlign = TextAlign.Left,
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.lineCount > 2 && dynamicFontSize > 12.sp) {
                            dynamicFontSize = (dynamicFontSize.value - 0.5f).sp
                        }
                    }
                )
            }

            Spacer(Modifier.height(5.dp))

            // Row 4: Lists
            // Always use a single horizontal container row for both Left & Right Panels
            // But when in Dark Mode (No Cast), we eliminate the 16.dp panel spacing gap (0.dp) to blend them together visually
            Row(
                modifier = Modifier.weight(1f), 
                horizontalArrangement = if (isDarkMode) Arrangement.Start else Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel
                Column(modifier = Modifier.weight(1f)) {
                    val leftText = if (isDarkMode) {
                        (settings.leftHeading.ifBlank { "Members / Visitors" }) + " (A-$validMidChar)"
                    } else {
                        settings.leftHeading.ifBlank { "Members / Visitors" }
                    }
                    Text(
                        text = leftText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = settings.primaryTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF9F9F9),
                        // When in dark mode, strip the right rounding so it fits flush against the divider line
                        shape = if (isDarkMode) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp) else RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val targetVisitors = if (isDarkMode) visitorsLeft else visitors
                            val roleVis = targetVisitors.filter { e -> Repository.roles.any { it.equals(e.roleOrLocation, ignoreCase = true) } }
                            val locVis = targetVisitors.filter { e -> !Repository.roles.any { it.equals(e.roleOrLocation, ignoreCase = true) } }
                            
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val totalHeight = maxHeight
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        if (roleVis.isNotEmpty()) {
                                            val maxRolesHeight = if (locVis.isNotEmpty()) totalHeight * 0.75f else totalHeight
                                            Box(modifier = Modifier.heightIn(max = maxRolesHeight)) {
                                                LazyColumn(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentPadding = PaddingValues(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    items(roleVis) { entry ->
                                                        VisitorRow(entry = entry, onTap = { signOutTarget = entry })
                                                    }
                                                }
                                            }
                                        }

                                        if (roleVis.isNotEmpty() && locVis.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().height(5.dp).background(settings.primaryBgColor)
                                            )
                                        }

                                        if (locVis.isNotEmpty()) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                LazyColumn(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentPadding = PaddingValues(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    items(locVis) { entry ->
                                                        VisitorRow(entry = entry, onTap = { signOutTarget = entry })
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (isDarkMode) {
                                        // Watermark A-ValidMidChar
                                        Text(
                                            text = "A-$validMidChar",
                                            fontSize = 80.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black.copy(alpha = 0.05f),
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Vertical light divider line built explicitly between the flush panels in Dark Mode
                if (isDarkMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            // Align background height with the surfaces below headers by matching spacers/text height roughly
                            .padding(top = 38.dp) 
                            .background(Color.Black.copy(alpha = 0.05f))
                    )
                }

                // Right Panel
                Column(modifier = Modifier.weight(1f)) {
                    val rightText = if (isDarkMode) {
                        (settings.leftHeading.ifBlank { "Members / Visitors" }) + " ($nextChar-Z)"
                    } else {
                        settings.rightHeading.ifBlank { "Cast" }
                    }
                    Text(
                        text = rightText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Repository.primaryTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF9F9F9),
                        // When in dark mode, strip the left rounding so it sits flush against the divider line
                        shape = if (isDarkMode) RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp) else RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(if (isDarkMode) 0.dp else 8.dp)) {
                            if (isDarkMode) {
                                val roleVisRight = visitorsRight.filter { e -> Repository.roles.any { it.equals(e.roleOrLocation, ignoreCase = true) } }
                                val locVisRight = visitorsRight.filter { e -> !Repository.roles.any { it.equals(e.roleOrLocation, ignoreCase = true) } }
                                
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val totalHeight = maxHeight
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            if (roleVisRight.isNotEmpty()) {
                                                val maxRolesHeight = if (locVisRight.isNotEmpty()) totalHeight * 0.75f else totalHeight
                                                Box(modifier = Modifier.heightIn(max = maxRolesHeight)) {
                                                    LazyColumn(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        contentPadding = PaddingValues(8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        items(roleVisRight) { entry ->
                                                            VisitorRow(entry = entry, onTap = { signOutTarget = entry })
                                                        }
                                                    }
                                                }
                                            }

                                            if (roleVisRight.isNotEmpty() && locVisRight.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().height(5.dp).background(settings.primaryBgColor)
                                                )
                                            }

                                            if (locVisRight.isNotEmpty()) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    LazyColumn(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentPadding = PaddingValues(8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        items(locVisRight) { entry ->
                                                            VisitorRow(entry = entry, onTap = { signOutTarget = entry })
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        // Watermark NextChar-Z
                                        Text(
                                            text = "$nextChar-Z",
                                            fontSize = 80.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black.copy(alpha = 0.05f),
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                                        )
                                    }
                                }
                            } else {
                                MembersList(
                                    members = stageMembersOrdered,
                                    tappedAt = stageTappedAt,
                                    onTap = { name, isActive ->
                                        if (isActive) {
                                            memberSignOutTarget = name
                                        } else {
                                            if (Repository.settings.vehicleRegPromptEnabled) {
                                                memberSignInTarget = name
                                                memberVehicleReg = Repository.stageVehicleReg[name]
                                                    ?: Repository.lastVehicleRegForName(name)
                                                    ?: ""
                                            } else {
                                                Repository.regularListToggle(name)
                                                showCarRegReminder = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
      }
    }

    if (showCarRegReminder && settings.carRegReminderEnabled) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            CarRegReminderPopup()
        }
    }

    if (signOutTarget != null) {
        val showQuestion = Repository.questionLines.isNotEmpty() && Repository.totalSignedInCount == 1
        AlertDialog(
            onDismissRequest = { signOutTarget = null },
            title = { Text("Sign-Out?", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sign-out ${signOutTarget!!.name}?", fontSize = 20.sp)
                    if (showQuestion) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please check the following:",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Repository.questionLines.forEach { line ->
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("•", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                Spacer(Modifier.width(8.dp))
                                Text(line, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Repository.updateActivityTime()
                        Repository.signOut(signOutTarget!!)
                        signOutTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff00b050))
                ) { Text("Sign-Out", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        Repository.updateActivityTime()
                        signOutTarget = null 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffff0000))
                ) { Text("Cancel", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (memberSignOutTarget != null) {
        val showQuestion = Repository.questionLines.isNotEmpty() && Repository.totalSignedInCount == 1
        AlertDialog(
            onDismissRequest = { memberSignOutTarget = null },
            title = { Text("Sign-Out?", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(memberSignOutTarget!!, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Do you want to sign-out?", fontSize = 20.sp)
                    if (showQuestion) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Please check the following:",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Repository.questionLines.forEach { line ->
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                                Text("•", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                Spacer(Modifier.width(12.dp))
                                Text(line, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Repository.updateActivityTime()
                        Repository.regularListToggle(memberSignOutTarget!!)
                        memberSignOutTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff00b050))
                ) { Text("Sign-Out", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        Repository.updateActivityTime()
                        memberSignOutTarget = null 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffff0000))
                ) { Text("Cancel", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (memberSignInTarget != null) {
        AlertDialog(
            onDismissRequest = { memberSignInTarget = null },
            title = { Text("Sign-In?", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sign-in ${memberSignInTarget!!}?", fontSize = 20.sp)
                    TextField(
                        value = memberVehicleReg,
                        onValueChange = { input ->
                            memberVehicleReg = input.uppercase().take(20)
                        },
                        placeholder = { Text("Vehicle Registration (optional)") },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Repository.updateActivityTime()
                        val raw = memberVehicleReg.uppercase()
                        val alnum = raw.filter { it.isLetterOrDigit() }
                        val formatted = when {
                            alnum.length >= 3 && alnum.take(3).all { it.isLetter() } ->
                                (alnum.take(3) + " " + alnum.drop(3))
                            alnum.length >= 4 && alnum[0].isLetter() && alnum[1].isLetter() && alnum[2].isDigit() && alnum[3].isDigit() ->
                                (alnum.take(4) + " " + alnum.drop(4))
                            alnum.length >= 4 && alnum[0].isLetter() && alnum[1].isDigit() && alnum[2].isDigit() && alnum[3].isDigit() ->
                                (alnum.take(4) + " " + alnum.drop(4))
                            else -> raw
                        }.take(20)
                        Repository.regularListToggle(memberSignInTarget!!, formatted.ifBlank { null })
                        showCarRegReminder = true
                        memberSignInTarget = null
                        memberVehicleReg = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff00b050))
                ) { Text("Sign-In", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        Repository.updateActivityTime()
                        memberSignInTarget = null 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffff0000))
                ) { Text("Cancel", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (visitorSignInTargetName != null) {
        AlertDialog(
            onDismissRequest = { visitorSignInTargetName = null },
            title = { Text("Sign-In?", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sign-in ${visitorSignInTargetName!!}?", fontSize = 20.sp)
                    TextField(
                        value = memberVehicleReg,
                        onValueChange = { input ->
                            memberVehicleReg = input.uppercase().take(20)
                        },
                        placeholder = { Text("Vehicle Registration (optional)") },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Repository.updateActivityTime()
                        val raw = memberVehicleReg.uppercase()
                        val alnum = raw.filter { it.isLetterOrDigit() }
                        val formatted = when {
                            alnum.length >= 3 && alnum.take(3).all { it.isLetter() } ->
                                (alnum.take(3) + " " + alnum.drop(3))
                            alnum.length >= 4 && alnum[0].isLetter() && alnum[1].isLetter() && alnum[2].isDigit() && alnum[3].isDigit() ->
                                (alnum.take(4) + " " + alnum.drop(4))
                            alnum.length >= 4 && alnum[0].isLetter() && alnum[1].isDigit() && alnum[2].isDigit() && alnum[3].isDigit() ->
                                (alnum.take(4) + " " + alnum.drop(4))
                            else -> raw
                        }.take(20)
                        Repository.signIn(visitorSignInTargetName!!, visitorSignInTargetRole, formatted.ifBlank { null })
                        showCarRegReminder = true
                        visitorSignInTargetName = null
                        memberVehicleReg = ""
                        onSignInSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff00b050))
                ) { Text("Sign-In", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        Repository.updateActivityTime()
                        visitorSignInTargetName = null 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffff0000))
                ) { Text("Cancel", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (alreadySignedInWarningName != null) {
        AlertDialog(
            onDismissRequest = { alreadySignedInWarningName = null },
            title = { Text("Already Signed In", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = { Text("$alreadySignedInWarningName is already signed in!", fontSize = 18.sp) },
            confirmButton = {
                Button(onClick = { alreadySignedInWarningName = null }) {
                    Text("OK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun VisitorRow(entry: VisitorEntry, onTap: () -> Unit) {
    val time = Instant.ofEpochMilli(entry.arrivalEpochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))

    val isRole = Repository.roles.any { it.equals(entry.roleOrLocation, ignoreCase = true) }
    val isGuest = entry.name.contains("(Guest)", ignoreCase = true)

    val backgroundColor = when {
        isGuest -> Color(0xFFD7EFF9)
        isRole -> Color(0xFFBEE9AD)
        else -> Color(0xFFDAF2D0) // Location
    }
    val borderColor = when {
        isGuest -> Color(0xFF81C784).copy(alpha = 0.5f)
        isRole -> Color(0xFF388E3C).copy(alpha = 0.5f)
        else -> Color(0xFF4CAF50).copy(alpha = 0.5f)
    }
    val textColor = Color.Black

        Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                Repository.updateActivityTime()
                onTap() 
            },
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(text = entry.roleOrLocation, fontSize = 16.sp, color = textColor.copy(alpha = 0.7f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = time, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                if (!entry.vehicleReg.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFFED101),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, Color.Black)
                    ) {
                        Text(
                            text = entry.vehicleReg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MembersList(
    members: List<String>,
    tappedAt: Map<String, Long>,
    onTap: (String, Boolean) -> Unit
) {
    val stageActive = Repository.stageActive
    val stageVehicleReg = Repository.stageVehicleReg
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(members) { name ->
            val isActive = stageActive.containsKey(name)
            val backgroundColor = if (isActive) Color(0xFFBEE9AD) else Color(0xFFFD958D)
            val borderColor = (if (isActive) Color(0xFF388E3C) else Color(0xFFE57373)).copy(alpha = 0.3f)
            val textColor = Color.Black

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        Repository.updateActivityTime()
                        onTap(name, isActive) 
                    },
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (isActive) {
                        Column(horizontalAlignment = Alignment.End) {
                            val time = Instant.ofEpochMilli(stageActive[name]!!)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                            Text(text = time, fontSize = 16.sp, color = textColor)
                            
                            val reg = stageVehicleReg[name]
                            if (!reg.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    color = Color(0xFFFED101),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color.Black)
                                ) {
                                    Text(
                                        text = reg,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CarRegReminderPopup() {
    val settings = Repository.settings
    val line1 = settings.carRegReminderText
    val line2 = settings.carRegReminderTextLine2
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFFFED101),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .wrapContentWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp)),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = line1,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 52.sp,
                    letterSpacing = 1.sp
                )
                if (line2.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = line2,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 52.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
