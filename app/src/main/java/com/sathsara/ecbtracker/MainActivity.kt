package com.sathsara.ecbtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.repository.AuthRepository
import com.sathsara.ecbtracker.ui.navigation.EcbNavHost
import com.sathsara.ecbtracker.ui.navigation.Screen
import com.sathsara.ecbtracker.ui.theme.EcbTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Determine start destination based on Supabase session synchronously for initial render
        var startDestination = Screen.Login.route
        runBlocking {
            if (authRepository.isUserLoggedIn.first()) {
                startDestination = Screen.Home.route
            }
        }

        setContent {
            val isDarkMode by dataStoreManager.isDarkMode.collectAsState(initial = true)

            EcbTrackerTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EcbNavHost(startDestination = startDestination)
                }
            }
        }
    }
}
