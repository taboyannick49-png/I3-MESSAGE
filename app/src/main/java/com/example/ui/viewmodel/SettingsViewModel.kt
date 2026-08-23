package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConversationEntity
import com.example.data.model.GoogleUserProfile
import com.example.data.repository.ChatRepository
import com.example.ui.theme.ExpressiveThemeType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("i3_settings", Context.MODE_PRIVATE)
    private val repository = ChatRepository(application)

    private val _currentTheme = MutableStateFlow(
        try {
            ExpressiveThemeType.valueOf(prefs.getString("theme", ExpressiveThemeType.HIGH_DENSITY.name) ?: ExpressiveThemeType.HIGH_DENSITY.name)
        } catch (e: Exception) {
            ExpressiveThemeType.HIGH_DENSITY
        }
    )
    val currentTheme: StateFlow<ExpressiveThemeType> = _currentTheme.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(prefs.getBoolean("dynamic_color", true))
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    // AI Features Toggles
    private val _isAiSmartReplies = MutableStateFlow(prefs.getBoolean("ai_smart_replies", true))
    val isAiSmartReplies: StateFlow<Boolean> = _isAiSmartReplies.asStateFlow()

    private val _isAiToneRewriter = MutableStateFlow(prefs.getBoolean("ai_tone_rewriter", true))
    val isAiToneRewriter: StateFlow<Boolean> = _isAiToneRewriter.asStateFlow()

    private val _isAiVoiceSummarizer = MutableStateFlow(prefs.getBoolean("ai_voice_summary", true))
    val isAiVoiceSummarizer: StateFlow<Boolean> = _isAiVoiceSummarizer.asStateFlow()

    private val _isAiAssistant = MutableStateFlow(prefs.getBoolean("ai_assistant", true))
    val isAiAssistant: StateFlow<Boolean> = _isAiAssistant.asStateFlow()

    // RCS Global & Per-Conversation settings
    private val _isRcsEnabled = MutableStateFlow(prefs.getBoolean("rcs_enabled", true))
    val isRcsEnabled: StateFlow<Boolean> = _isRcsEnabled.asStateFlow()

    val conversationsForRcs: StateFlow<List<ConversationEntity>> = repository.activeConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Google Account Registration
    private val _googleProfile = MutableStateFlow<GoogleUserProfile?>(
        if (prefs.getBoolean("google_signed_in", false)) {
            GoogleUserProfile(
                email = prefs.getString("google_email", "taboyannick49@gmail.com") ?: "taboyannick49@gmail.com",
                displayName = prefs.getString("google_name", "Yannick Tabo") ?: "Yannick Tabo",
                avatarInitial = "Y",
                isSignedIn = true
            )
        } else {
            null
        }
    )
    val googleProfile: StateFlow<GoogleUserProfile?> = _googleProfile.asStateFlow()

    fun setTheme(theme: ExpressiveThemeType) {
        _currentTheme.value = theme
        prefs.edit().putString("theme", theme.name).apply()
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun toggleDynamicColor(enabled: Boolean) {
        _isDynamicColor.value = enabled
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
    }

    fun toggleAiSmartReplies(enabled: Boolean) {
        _isAiSmartReplies.value = enabled
        prefs.edit().putBoolean("ai_smart_replies", enabled).apply()
    }

    fun toggleAiToneRewriter(enabled: Boolean) {
        _isAiToneRewriter.value = enabled
        prefs.edit().putBoolean("ai_tone_rewriter", enabled).apply()
    }

    fun toggleAiVoiceSummarizer(enabled: Boolean) {
        _isAiVoiceSummarizer.value = enabled
        prefs.edit().putBoolean("ai_voice_summary", enabled).apply()
    }

    fun toggleAiAssistant(enabled: Boolean) {
        _isAiAssistant.value = enabled
        prefs.edit().putBoolean("ai_assistant", enabled).apply()
    }

    fun toggleGlobalRcs(enabled: Boolean) {
        _isRcsEnabled.value = enabled
        prefs.edit().putBoolean("rcs_enabled", enabled).apply()
    }

    fun toggleConversationRcs(conversationId: String, currentRcs: Boolean) {
        viewModelScope.launch {
            repository.toggleRcs(conversationId, currentRcs)
        }
    }

    fun registerGoogleAccount(email: String, name: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) return false
        val profile = GoogleUserProfile(
            email = email,
            displayName = if (name.isNotBlank()) name else email.substringBefore("@"),
            avatarInitial = email.take(1).uppercase(),
            isSignedIn = true
        )
        _googleProfile.value = profile
        prefs.edit()
            .putBoolean("google_signed_in", true)
            .putString("google_email", profile.email)
            .putString("google_name", profile.displayName)
            .apply()
        return true
    }

    fun signOutGoogle() {
        _googleProfile.value = null
        prefs.edit().putBoolean("google_signed_in", false).apply()
    }
}
