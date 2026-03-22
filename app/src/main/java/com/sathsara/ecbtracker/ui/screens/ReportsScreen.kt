package com.sathsara.ecbtracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.theme.Cyan
import com.sathsara.ecbtracker.ui.theme.CyanDim
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.theme.OutfitFamily
import com.sathsara.ecbtracker.ui.theme.SurfaceDark
import com.sathsara.ecbtracker.ui.viewmodel.ReportsViewModel

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isWeeklyEnabled by viewModel.isWeeklySummaryEnabled.collectAsState()
    val isMonthlyEmailEnabled by viewModel.isMonthlyEmailEnabled.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.exportSuccessMessage) {
        uiState.exportSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.exportError) {
        uiState.exportError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
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
            text = "Reports",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Export and analyze your data",
            fontSize = 14.sp,
            color = Muted,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Date Range Planner
        Text(
            text = "Select Date Range",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Very simplified inputs to represent DatePickers
            Column(modifier = Modifier.weight(1f)) {
                Text("From", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(bottom = 4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(uiState.fromDate, color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("To", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(bottom = 4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(uiState.toDate, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        VerticalSpacer(32)

        Text(
            text = "Export Format",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Excel Export Card
        ExportCard(
            title = "Excel Workbook (.xlsx)",
            desc = "Full raw data with sheets for readings and payments.",
            icon = "📊",
            buttonText = "Download Excel",
            isLoading = uiState.isExporting,
            onClick = { viewModel.exportData("excel") }
        )

        VerticalSpacer(16)

        // PDF Export Card
        ExportCard(
            title = "PDF Report (.pdf)",
            desc = "Clean structured report ready for printing or sharing.",
            icon = "📄",
            buttonText = "Generate PDF",
            isLoading = uiState.isExporting,
            onClick = { viewModel.exportData("pdf") }
        )
        
        VerticalSpacer(16)

        // CSV Export Card
        ExportCard(
            title = "CSV File (.csv)",
            desc = "Simple text data for importing into other tools.",
            icon = "📋",
            buttonText = "Download CSV",
            isLoading = uiState.isExporting,
            onClick = { viewModel.exportData("csv") }
        )

        VerticalSpacer(32)

        Text(
            text = "Automated Reports",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weekly Summary Report", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                        Text("Get a push notification every Sunday", fontSize = 12.sp, color = Muted)
                    }
                    Switch(
                        checked = isWeeklyEnabled,
                        onCheckedChange = { viewModel.toggleWeeklySummary(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Cyan, checkedTrackColor = CyanDim)
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha=0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Monthly Email Export", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                        Text("Receive full PDF via email on the 1st", fontSize = 12.sp, color = Muted)
                    }
                    Switch(
                        checked = isMonthlyEmailEnabled,
                        onCheckedChange = { viewModel.toggleMonthlyEmail(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Cyan, checkedTrackColor = CyanDim)
                    )
                }
            }
        }

        VerticalSpacer(80) // Bottom nav padding
    }
}

@Composable
fun ExportCard(
    title: String,
    desc: String,
    icon: String,
    buttonText: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(
                        text = title,
                        fontFamily = OutfitFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = desc,
                        fontSize = 12.sp,
                        color = Muted
                    )
                }
            }
            PrimaryButton(
                text = if (isLoading) "Exporting..." else buttonText,
                onClick = onClick,
                enabled = !isLoading,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
