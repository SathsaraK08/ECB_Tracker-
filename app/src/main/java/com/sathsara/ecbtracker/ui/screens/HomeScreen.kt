package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.ui.components.LoadingSkeleton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.navigation.Screen
import com.sathsara.ecbtracker.ui.theme.Cyan
import com.sathsara.ecbtracker.ui.theme.CyanDim
import com.sathsara.ecbtracker.ui.theme.DmMonoFamily
import com.sathsara.ecbtracker.ui.theme.Green
import com.sathsara.ecbtracker.ui.theme.GreenDim
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.theme.OutfitFamily
import com.sathsara.ecbtracker.ui.theme.SurfaceDark
import com.sathsara.ecbtracker.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp) // Bottom padding for nav bar
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (uiState.isLoading) "Loading..." else "Hi, ${uiState.username}",
                        fontFamily = OutfitFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Here's your energy summary",
                        fontSize = 14.sp,
                        color = Muted
                    )
                }
                Box(
                    modifier = Modifier.size(42.dp).background(SurfaceDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 20.sp)
                }
            }
        }

        item {
            // Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This Month",
                            fontSize = 14.sp,
                            color = Muted,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .background(GreenDim, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "On Track",
                                color = Green,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    VerticalSpacer(8)

                    Row(verticalAlignment = Alignment.Bottom) {
                        if (uiState.isLoading) {
                            LoadingSkeleton(modifier = Modifier.height(36.dp).width(120.dp))
                        } else {
                            Text(
                                text = String.format("%.1f", uiState.monthlyKwh),
                                fontFamily = DmMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = " kWh",
                                fontSize = 16.sp,
                                color = Muted,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                    }

                    VerticalSpacer(12)

                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Est. Bill", fontSize = 14.sp, color = Muted)
                        if (uiState.isLoading) {
                            LoadingSkeleton(modifier = Modifier.height(20.dp).width(80.dp))
                        } else {
                            Text(
                                text = "LKR ${String.format("%,.0f", uiState.estimatedBill)}",
                                fontFamily = DmMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Cyan
                            )
                        }
                    }
                }
            }
            VerticalSpacer(24)
        }

        item {
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Today",
                    value = String.format("%.1f", uiState.todayKwh),
                    unit = "kWh",
                    modifier = Modifier.weight(1f),
                    isLoading = uiState.isLoading
                )
                StatCard(
                    title = "Avg/Day",
                    value = String.format("%.1f", if (uiState.monthlyKwh > 0) uiState.monthlyKwh / 30 else 0.0), // Simplified avg
                    unit = "kWh",
                    modifier = Modifier.weight(1f),
                    isLoading = uiState.isLoading
                )
                StatCard(
                    title = "Rate",
                    value = String.format("%.0f", uiState.ratePerUnit),
                    unit = "LKR/u",
                    modifier = Modifier.weight(1f),
                    isLoading = uiState.isLoading
                )
            }
            VerticalSpacer(24)
        }

        item {
            // Weekly Usage Bar Chart
            Text(
                text = "Weekly Usage",
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                val chartData = uiState.chartData
                if (uiState.isLoading) {
                    LoadingSkeleton(modifier = Modifier.fillMaxSize())
                } else if (chartData.isEmpty()) {
                    Text(
                        "No data for this week", 
                        color = Muted, 
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    SimpleBarChart(data = chartData, modifier = Modifier.fillMaxSize())
                }
            }
            VerticalSpacer(24)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontFamily = OutfitFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { navController.navigate(Screen.Records.route) }) {
                    Text("View All", color = Cyan, fontSize = 14.sp)
                }
            }
        }

        if (uiState.isLoading) {
            items(3) {
                LoadingSkeleton(modifier = Modifier.fillMaxWidth().height(70.dp).padding(bottom = 12.dp))
            }
        } else if (uiState.recentActivity.isEmpty()) {
            item {
                Text(
                    "No recent activity logged.", 
                    color = Muted, 
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else {
            items(uiState.recentActivity) { entry ->
                ActivityItem(entry)
                VerticalSpacer(12)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = title, fontSize = 13.sp, color = Muted, modifier = Modifier.padding(bottom = 8.dp))
        if (isLoading) {
            LoadingSkeleton(modifier = Modifier.height(20.dp).fillMaxWidth())
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontFamily = DmMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = " $unit",
                    fontSize = 11.sp,
                    color = Muted,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    // simplified drawing mimicking the HTML chart
    val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    val outlineColor = MaterialTheme.colorScheme.outline
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = 14.dp.toPx()
        val spacing = (width - (barWidth * data.size)) / (data.size + 1)
        
        data.forEachIndexed { i, entry ->
            val barHeight = (entry.second / maxVal) * (height - 20.dp.toPx()) // save room for text
            val x = spacing + (i * (barWidth + spacing))
            val y = height - barHeight - 20.dp.toPx()
            
            val color = if (i == data.size - 1) Cyan else outlineColor
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth/2, barWidth/2)
            )
            
            // Draw day label (e.g. "Mon" from date)
            // Simplified: just taking last char of date string for now to match visual
            // val dayLabel = entry.first.takeLast(2)
        }
    }
}

@Composable
fun ActivityItem(entry: Entry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CyanDim, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Meter Reading",
                    fontFamily = OutfitFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${entry.date} at ${entry.time}",
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+${String.format("%.1f", entry.used)}",
                fontFamily = DmMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "kWh",
                fontSize = 12.sp,
                color = Muted
            )
        }
    }
}
