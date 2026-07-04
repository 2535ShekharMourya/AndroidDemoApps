package com.azad.androiddemoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.azad.androiddemoapp.ui.screens.MainScreen
import com.azad.androiddemoapp.ui.theme.AndroidDemoAppTheme
import com.azad.androiddemoapp.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by profileViewModel.themeState.collectAsState()
            val darkTheme = when (themeState) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            AndroidDemoAppTheme(darkTheme = darkTheme) {
                MainScreen(
                    modifier = Modifier.fillMaxSize(),
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}