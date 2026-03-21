package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sathsara.ecbtracker.ui.components.SecondaryOutlineButton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.theme.*
import com.sathsara.ecbtracker.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val billReminders by viewModel.billReminders.collectAsState()
    val usageAlerts by viewModel.usageAlerts.collectAsState()

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onLogout()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        VerticalSpacer(24)

        Text(
            text = "Settings",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Profile Section
        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(CyanDim, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.profile?.username?.take(1)?.uppercase() ?: "U",
                    fontSize = 24.sp,
                    color = Cyan,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = uiState.profile?.username ?: "User",
                    fontFamily = OutfitFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = uiState.email.takeIf { it.isNotBlank() } ?: "Manage your account",
                    fontSize = 13.sp,
                    color = Muted
                )
            }
        }

        VerticalSpacer(32)

        Text(
            text = "Preferences",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SettingsCard {
            SettingsToggleRow(
                title = "Dark Mode",
                subtitle = "Use dark theme",
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.3f), modifier = Modifier.padding(vertical = 12.dp))
            
            // Electricity Rate Input (Simplified for Compose view)
            var rateText by remember(uiState.settings?.lkrPerUnit) { mutableStateOf(uiState.settings?.lkrPerUnit?.toString() ?: "32.0") }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text("Electricity Rate", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                    Text("LKR per kWh unit", fontSize = 12.sp, color = Muted)
                }
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { 
                        rateText = it
                        it.toDoubleOrNull()?.let { rate -> viewModel.updateRate(rate) }
                    },
                    modifier = Modifier.width(80.dp).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }

        VerticalSpacer(24)

        Text(
            text = "Notifications",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SettingsCard {
            SettingsToggleRow(
                title = "Bill Reminders",
                subtitle = "Get notified before due date",
                checked = billReminders,
                onCheckedChange = { viewModel.toggleBillReminders(it) }
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.3f), modifier = Modifier.padding(vertical = 12.dp))
            SettingsToggleRow(
                title = "High Usage Alerts",
                subtitle = "Notify when usage spikes",
                checked = usageAlerts,
                onCheckedChange = { viewModel.toggleUsageAlerts(it) }
            )
        }

        VerticalSpacer(32)

        Text(
            text = "Account",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SettingsCard {
            Row(modifier = Modifier.fillMaxWidth().clickable { /* TODO */ }.padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Edit Profile", color = MaterialTheme.colorScheme.onBackground)
                Text(">", color = Muted)
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha=0.3f), modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth().clickable { /* TODO */ }.padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CEB Account Number", color = MaterialTheme.colorScheme.onBackground)
                Text(uiState.settings?.accountNumber ?: "Add", color = Cyan)
            }
        }

        VerticalSpacer(24)

        SecondaryOutlineButton(
            text = "Sign Out",
            onClick = { viewModel.signOut() }
        )

        VerticalSpacer(80) // Bottom nav padding
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = Muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Cyan, checkedTrackColor = CyanDim)
        )
    }
}
