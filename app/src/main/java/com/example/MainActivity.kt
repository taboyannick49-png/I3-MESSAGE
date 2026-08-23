package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainScaffoldScreen
import com.example.ui.theme.I3Theme
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ContactsViewModel
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()
    private val contactsViewModel: ContactsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
            val isDynamicColor by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()

            I3Theme(
                selectedTheme = selectedTheme,
                darkTheme = isDarkMode,
                dynamicColor = isDynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScaffoldScreen(
                        chatViewModel = chatViewModel,
                        contactsViewModel = contactsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

