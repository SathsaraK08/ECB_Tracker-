package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sathsara.ecbtracker.ui.components.LoadingSkeleton
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.theme.*
import com.sathsara.ecbtracker.ui.viewmodel.ForecastViewModel

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        VerticalSpacer(24)

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Forecast",
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF3B0764), RoundedCornerShape(12.dp)) // Deep purple bg
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Powered by Gemini",
                    color = Purple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = "Smart predictions based on your usage",
            fontSize = 14.sp,
            color = Muted,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (uiState.isLoading) {
            // Skeleton loader specifically for Forecast
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(SurfaceDark, RoundedCornerShape(12.dp)).padding(20.dp)) {
                Column {
                    LoadingSkeleton(Modifier.width(120.dp).height(20.dp))
                    VerticalSpacer(24)
                    LoadingSkeleton(Modifier.width(180.dp).height(40.dp))
                    VerticalSpacer(12)
                    LoadingSkeleton(Modifier.width(200.dp).height(16.dp))
                }
            }
            VerticalSpacer(24)
            LoadingSkeleton(Modifier.fillMaxWidth().height(100.dp))
            VerticalSpacer(16)
            LoadingSkeleton(Modifier.fillMaxWidth().height(100.dp))
            
            VerticalSpacer(40)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = Purple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Analyzing usage patterns...", color = Purple)
            }
            
        } else if (uiState.isError) {
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
                    Text(
                        uiState.errorMessage ?: "Failed to generate forecast", 
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    PrimaryButton(text = "Try Again", onClick = { viewModel.loadForecast() })
                }
            }
        } else if (uiState.forecast != null) {
            val forecast = uiState.forecast!!
            
            // Forecast Hero
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
                        Text("Projected Bill", fontSize = 14.sp, color = Muted, fontWeight = FontWeight.Medium)
                        
                        // Efficiency Badge
                        val (badgeBg, badgeText) = when (forecast.efficiencyRating.lowercase()) {
                            "high" -> Pair(GreenDim, Green)
                            "medium" -> Pair(AmberDim, Amber)
                            else -> Pair(Color(0xFF450a0a), Red) // RedDim equivalent
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${forecast.efficiencyRating} Efficiency",
                                color = badgeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    VerticalSpacer(12)
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("LKR ", fontSize = 16.sp, color = Muted, modifier = Modifier.padding(bottom = 6.dp))
                        Text(
                            text = String.format("%,.0f", forecast.projectedBill),
                            fontFamily = DMMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Cyan
                        )
                    }
                    
                    if (!forecast.peakHours.isNullOrEmpty()) {
                        VerticalSpacer(12)
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏳", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                            Text("High usage observed during: ${forecast.peakHours}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }

            VerticalSpacer(32)

            Text(
                text = "AI Recommendations",
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            forecast.tips.forEach { tip ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = tip.title,
                                fontFamily = OutfitFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f).padding(end = 12.dp)
                            )
                            Text(
                                text = "-LKR ${String.format("%,.0f", tip.savingLkr)}",
                                fontFamily = DMMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Green
                            )
                        }
                        VerticalSpacer(8)
                        Text(
                            text = tip.description,
                            fontSize = 13.sp,
                            color = Muted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        VerticalSpacer(80) // Navigation bar padding
    }
}
