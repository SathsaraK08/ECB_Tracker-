package com.sathsara.ecbtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sathsara.ecbtracker.ui.components.PrimaryButton
import com.sathsara.ecbtracker.ui.components.SecondaryOutlineButton
import com.sathsara.ecbtracker.ui.components.VerticalSpacer
import com.sathsara.ecbtracker.ui.theme.Cyan
import com.sathsara.ecbtracker.ui.theme.Muted
import com.sathsara.ecbtracker.ui.theme.OutfitFamily
import com.sathsara.ecbtracker.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // In a real app, use a SnackbarHost. For now, simple error handling.
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        // Logo Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Cyan, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 24.sp) // Simplified icon for now
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "ECB Tracker",
                    fontFamily = OutfitFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Energy Management",
                    fontSize = 11.sp,
                    color = Muted
                )
            }
        }

        Text(
            text = "Welcome back",
            fontFamily = OutfitFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = "Track your energy, reduce your bills",
            fontSize = 14.sp,
            color = Muted,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Built custom outlines to match the exact mockup styling
        val m3Colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = Cyan,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
        )

        // Email
        Text("Email address", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VerticalSpacer(5)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("you@example.com", color = Muted) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(8.dp),
            colors = m3Colors,
            singleLine = true
        )

        VerticalSpacer(14)

        // Password
        Text("Password", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        VerticalSpacer(5)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("••••••••", color = Muted) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(8.dp),
            colors = m3Colors,
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 18.dp),
            contentAlignment = Alignment.CenterRight
        ) {
            Text(
                "Forgot password?", 
                fontSize = 12.sp, 
                color = Cyan,
                modifier = Modifier.clickable { }
            )
        }

        PrimaryButton(
            text = if (uiState.isLoading) "Loading..." else "Sign In",
            onClick = { viewModel.signIn(email, password) },
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
        )
        
        VerticalSpacer(10)
        
        SecondaryOutlineButton(
            text = "Create Account",
            onClick = { viewModel.signUp(email, password) },
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "By continuing you agree to our Terms & Privacy Policy",
            fontSize = 12.sp,
            color = Muted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
