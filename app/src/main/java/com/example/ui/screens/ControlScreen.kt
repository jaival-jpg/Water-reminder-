package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.HydrateViewModel
import com.example.ui.theme.*
import com.example.utils.NotificationHelper

@Composable
fun ControlScreen(viewModel: HydrateViewModel) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()
    val cupSize by viewModel.cupSize.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val displayOverlayScreen by viewModel.displayOverlayScreen.collectAsStateWithLifecycle()
    val reminderInterval by viewModel.reminderInterval.collectAsStateWithLifecycle()
    
    val country by viewModel.country.collectAsStateWithLifecycle()
    val useCountrySleep by viewModel.useCountrySleep.collectAsStateWithLifecycle()
    val useManualSleep by viewModel.useManualSleep.collectAsStateWithLifecycle()
    val manualStartHour by viewModel.manualSleepStartHour.collectAsStateWithLifecycle()
    val manualStartMinute by viewModel.manualSleepStartMinute.collectAsStateWithLifecycle()
    val manualEndHour by viewModel.manualSleepEndHour.collectAsStateWithLifecycle()
    val manualEndMinute by viewModel.manualSleepEndMinute.collectAsStateWithLifecycle()

    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleReminders(true)
        }
    }

    var showEditName by remember { mutableStateOf(false) }
    var showEditGoal by remember { mutableStateOf(false) }
    var showEditCup by remember { mutableStateOf(false) }
    var showIntervalSaveDialog by remember { mutableStateOf(false) }
    var selectedPendingInterval by remember { mutableStateOf<Pair<String, Int>?>(null) }

    if (showEditName) {
        EditDialog(
            title = "Edit Name",
            initialValue = name,
            onDismiss = { showEditName = false },
            onSave = { 
                viewModel.updateName(it)
                showEditName = false
            }
        )
    }

    if (showEditGoal) {
        OptionSelectorDialog(
            title = "Select Daily Goal",
            options = listOf(1500, 2000, 2500, 3000, 3500, 4000),
            currentValue = dailyGoal,
            unit = "ml",
            onDismiss = { showEditGoal = false },
            onSelect = { 
                viewModel.updateDailyGoal(it)
                showEditGoal = false
            }
        )
    }

    if (showEditCup) {
        OptionSelectorDialog(
            title = "Select Cup Size",
            options = listOf(100, 150, 200, 250, 300, 400, 500),
            currentValue = cupSize,
            unit = "ml",
            onDismiss = { showEditCup = false },
            onSelect = { 
                viewModel.updateCupSize(it)
                showEditCup = false
            }
        )
    }

    if (showIntervalSaveDialog && selectedPendingInterval != null) {
        IntervalConfirmSaveDialog(
            intervalLabel = selectedPendingInterval!!.first,
            intervalMinutes = selectedPendingInterval!!.second,
            onDismiss = { showIntervalSaveDialog = false },
            onConfirm = {
                viewModel.updateReminderInterval(selectedPendingInterval!!.second)
                showIntervalSaveDialog = false
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                "Controls",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DeepOceanBlue,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // User Info
            ControlSection("Profile", Icons.Filled.Person) {
                ControlRow("Name", name, onClick = { showEditName = true })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Water Settings
            ControlSection("Hydration Settings", Icons.Filled.WaterDrop) {
                ControlRow("Daily Goal", "${dailyGoal}ml", onClick = { showEditGoal = true })
                Spacer(modifier = Modifier.height(16.dp))
                ControlRow("Cup Size", "${cupSize}ml", onClick = { showEditCup = true })
                Spacer(modifier = Modifier.height(20.dp))
                
                // Reminder Duration Selector Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reminder Interval",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatIntervalLabel(reminderInterval),
                            color = RoyalBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val intervalOptions = listOf(
                        Pair("5 Min", 5),
                        Pair("15 Min", 15),
                        Pair("30 Min", 30),
                        Pair("1 Hour", 60),
                        Pair("2 Hours", 120),
                        Pair("3 Hours", 180)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        intervalOptions.chunked(3).forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowOptions.forEach { (label, minutesValue) ->
                                    val isSelected = reminderInterval == minutesValue
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) RoyalBlue 
                                                else RoyalBlue.copy(alpha = 0.08f)
                                            )
                                            .clickable {
                                                selectedPendingInterval = Pair(label, minutesValue)
                                                showIntervalSaveDialog = true
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else RoyalBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notifications
            ControlSection("Notifications", Icons.Filled.Notifications) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reminders", color = DeepOceanBlue, fontSize = 16.sp)
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { enable ->
                            if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.toggleReminders(enable)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SoftWhite,
                            checkedTrackColor = Color(0xFF0E258C),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFFF1F5F9)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Display Overlay Screen", color = DeepOceanBlue, fontSize = 16.sp)
                    Switch(
                        checked = displayOverlayScreen,
                        onCheckedChange = { enable ->
                            viewModel.updateDisplayOverlayScreen(enable)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SoftWhite,
                            checkedTrackColor = Color(0xFF0E258C),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFFF1F5F9)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ControlRow("Preview Notification", "", onClick = {
                    NotificationHelper.showReminderNotification(context)
                    if (displayOverlayScreen && Settings.canDrawOverlays(context)) {
                        val overlayIntent = Intent(context, com.example.services.OverlayReminderService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(overlayIntent)
                        } else {
                            context.startService(overlayIntent)
                        }
                    } else {
                        NotificationHelper.playNotificationSound(context)
                    }
                })
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sleep Protection Section
            ControlSection("Sleep Protection", Icons.Filled.Notifications) {
                // Display user's current country for sleep time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Country", color = DeepOceanBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(country, color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                    var showCountryDialog by remember { mutableStateOf(false) }
                    if (showCountryDialog) {
                        val countries = listOf(
                            "India", "United States", "United Kingdom", "Canada", "Australia",
                            "Japan", "Brazil", "Germany", "France", "South Africa"
                        )
                        OptionSelectorDialogString(
                            title = "Select Country",
                            options = countries,
                            currentValue = country,
                            onDismiss = { showCountryDialog = false },
                            onSelect = {
                                viewModel.updateCountry(it)
                                showCountryDialog = false
                            }
                        )
                    }
                    Button(
                        onClick = { showCountryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepOceanBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Change", color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                // Toggle 1: "not disturb sleeping time"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DND Sleeping Time", color = DeepOceanBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        val sleepRangeText = when (country) {
                            "Japan" -> "11:00 PM - 07:00 AM"
                            "France", "Germany", "United Kingdom" -> "11:00 PM - 07:00 AM"
                            else -> "10:00 PM - 06:00 AM"
                        }
                        Text("Use country standard night ($sleepRangeText)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Switch(
                        checked = useCountrySleep,
                        onCheckedChange = { enable ->
                            viewModel.updateUseCountrySleep(enable)
                            if (enable) {
                                viewModel.updateUseManualSleep(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SoftWhite,
                            checkedTrackColor = Color(0xFF0E258C),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFFF1F5F9)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                // Toggle 2: "manual set sleeping time"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manual Sleep Schedule", color = DeepOceanBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Set custom hours for DND", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Switch(
                        checked = useManualSleep,
                        onCheckedChange = { enable ->
                            viewModel.updateUseManualSleep(enable)
                            if (enable) {
                                viewModel.updateUseCountrySleep(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SoftWhite,
                            checkedTrackColor = Color(0xFF0E258C),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFFF1F5F9)
                        )
                    )
                }

                if (useManualSleep) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var startH by remember { mutableStateOf(manualStartHour) }
                    var startM by remember { mutableStateOf(manualStartMinute) }
                    var endH by remember { mutableStateOf(manualEndHour) }
                    var endM by remember { mutableStateOf(manualEndMinute) }

                    LaunchedEffect(manualStartHour, manualStartMinute, manualEndHour, manualEndMinute) {
                        startH = manualStartHour
                        startM = manualStartMinute
                        endH = manualEndHour
                        endM = manualEndMinute
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Start Time:", color = DeepOceanBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NumberSelector(value = startH, range = 0..23, onValueChange = { startH = it })
                                Text(" : ", color = DeepOceanBlue, fontWeight = FontWeight.Bold)
                                NumberSelector(value = startM, range = 0..59, onValueChange = { startM = it })
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("End Time:", color = DeepOceanBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NumberSelector(value = endH, range = 0..23, onValueChange = { endH = it })
                                Text(" : ", color = DeepOceanBlue, fontWeight = FontWeight.Bold)
                                NumberSelector(value = endM, range = 0..59, onValueChange = { endM = it })
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateManualSleepTimes(startH, startM, endH, endM)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DeepOceanBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Sleep Times", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = RoyalBlue, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun ControlRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = DeepOceanBlue, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, color = Color(0xFF94A3B8), fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (onClick != null) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
fun EditDialog(title: String, initialValue: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = DeepOceanBlue, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = RoyalBlue,
                    unfocusedBorderColor = RoyalBlue.copy(alpha = 0.5f),
                    focusedLabelColor = RoyalBlue,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = RoyalBlue
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onSave(text) }) {
                Text("Save", color = RoyalBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun OptionSelectorDialog(
    title: String,
    options: List<Int>,
    currentValue: Int,
    unit: String,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = DeepOceanBlue, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!showCustomInput) {
                    options.forEach { option ->
                        val isSelected = option == currentValue
                        Button(
                            onClick = { onSelect(option) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) RoyalBlue else RoyalBlue.copy(alpha = 0.12f),
                                contentColor = if (isSelected) Color.White else RoyalBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("$option $unit", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                    // Custom option
                    Button(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue.copy(alpha = 0.08f),
                            contentColor = RoyalBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Custom...", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                } else {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text("Enter custom value ($unit)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = RoyalBlue.copy(alpha = 0.5f),
                            focusedLabelColor = RoyalBlue,
                            unfocusedLabelColor = Color.Gray,
                            cursorColor = RoyalBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (showCustomInput) {
                TextButton(onClick = {
                    val num = customText.toIntOrNull()
                    if (num != null && num > 0) {
                        onSelect(num)
                    }
                }) {
                    Text("Save", color = RoyalBlue, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun OptionSelectorDialogString(
    title: String,
    options: List<String>,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = DeepOceanBlue, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == currentValue
                    Button(
                        onClick = { onSelect(option) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) RoyalBlue else RoyalBlue.copy(alpha = 0.12f),
                            contentColor = if (isSelected) Color.White else RoyalBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(option, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun NumberSelector(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded },
        contentAlignment = Alignment.Center
    ) {
        val formatValue = String.format("%02d", value)
        Text(text = formatValue, color = DeepOceanBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .height(200.dp)
                .background(Color.White)
        ) {
            range.forEach { num ->
                DropdownMenuItem(
                    text = { Text(String.format("%02d", num), color = DeepOceanBlue) },
                    onClick = {
                        onValueChange(num)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun formatIntervalLabel(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes Min"
        minutes % 60 == 0 -> {
            val h = minutes / 60
            "$h ${if (h == 1) "Hour" else "Hours"}"
        }
        else -> {
            val h = minutes / 60
            val m = minutes % 60
            "$h hr $m min"
        }
    }
}

@Composable
fun IntervalConfirmSaveDialog(
    intervalLabel: String,
    intervalMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = RoyalBlue
                )
                Text("Save Reminder Interval", color = DeepOceanBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "Do you want to save the reminder interval as $intervalLabel?",
                    color = DeepOceanBlue.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "This will immediately configure and schedule your reminders to ring precisely every $intervalLabel.",
                            color = Color(0xFF475569),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = RoyalBlue)
            }
        },
        containerColor = Color.White
    )
}
