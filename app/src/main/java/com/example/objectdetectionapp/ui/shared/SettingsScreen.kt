package com.example.objectdetectionapp.ui.shared

import androidx.compose.ui.unit.round
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )

    val notificationCooldown by viewModel.notificationCooldown.collectAsState()
    val saveCooldown by viewModel.saveCooldown.collectAsState()
    val soundCooldown by viewModel.soundCooldown.collectAsState()
    val emailCooldown by viewModel.emailCooldown.collectAsState()

    val notificationOptions = listOf(3L, 5L, 10L, 15L, 20L, 30L)
    val saveOptions = listOf(3L, 5L, 10L, 15L, 20L, 30L)
    val emailOptions = listOf(3L, 5L, 10L, 15L, 20L, 30L)
    val soundOptions = listOf(8L, 10L, 20L, 30L, 60L, 120L,240L)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Cursive,
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            CooldownDropdown(
                label = "Notification Cooldown (min)",
                options = notificationOptions,
                selected = notificationCooldown,
                onOptionSelected = { viewModel.updateNotificationCooldown(it) },
                infoText = "Minimum time (in minutes) between two consecutive notifications for the same event."
            )

            CooldownDropdown(
                label = "Save Interval (min)",
                options = saveOptions,
                selected = saveCooldown,
                onOptionSelected = { viewModel.updateSaveCooldown(it) },
                infoText = "Controls how often detection images are saved to storage (in minutes)."
            )

            CooldownDropdown(
                label = "Sound Cooldown (sec)",
                options = soundOptions,
                selected = soundCooldown,
                onOptionSelected = { viewModel.updateSoundCooldown(it) },
                infoText = "Wait time (in seconds) before playing alert sound again after detecting a person."
            )

            CooldownDropdown(
                label = "Email Cooldown (min)",
                options = emailOptions,
                selected = emailCooldown,
                onOptionSelected = { viewModel.updateEmailCooldown(it) },
                infoText = "Minimum interval (in minutes) between sending alert emails when a person is detected."
            )

        }
    }
}

@Composable
fun CooldownDropdown(
    label: String,
    options: List<Long>,
    selected: Long,
    onOptionSelected: (Long) -> Unit,
    infoText: String
) {
    var expanded by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val infoAnchor = remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Info",
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp)
                    .clickable { showInfo = !showInfo } // Toggle the showInfo state
                    .onGloballyPositioned { coordinates ->
                        infoAnchor.value = coordinates
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (showInfo && infoAnchor.value != null) {
            val density = LocalDensity.current
            Popup(
                onDismissRequest = { showInfo = false },
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = with(density) { (infoAnchor.value!!.size.width.toDp() + 4.dp).roundToPx() },
                    y = with(density) { ((-infoAnchor.value!!.size.height).toDp() / 2).roundToPx() }
                )
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = infoText,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("$selected")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.TopCenter) // Align dropdown to the start of the button
                    .padding(horizontal = 8.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "$option",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (option == selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}