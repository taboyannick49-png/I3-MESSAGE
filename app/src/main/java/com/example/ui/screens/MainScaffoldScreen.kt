package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.CarrierSmsManager
import com.example.ui.components.ExpressiveSplitDivider
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ContactsViewModel
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

enum class MainDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
) {
    CHATS("Discussions", Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    CONTACTS("Contacts", Icons.Default.People, Icons.Outlined.People),
    ARCHIVES("Archives", Icons.Default.Archive, Icons.Outlined.Archive),
    SETTINGS("Paramètres", Icons.Default.Settings, Icons.Outlined.Settings)
}

sealed class ScreenState {
    data object MainTabs : ScreenState()
    data class ChatDetail(val conversationId: String) : ScreenState()
    data object ArchivedChats : ScreenState()
}

/**
 * M3 Expressive Adaptive Scaffold:
 * - 10-Inch Tablet / Large Screen Landscape: Responsive Dual-Pane Split Screen with Draggable Handle Divider (like Google Play Store & Foldables).
 * - Phone / Compact Portrait: Fluid Single-Screen navigation with bottom bar and spring transitions.
 */
@Composable
fun MainScaffoldScreen(
    chatViewModel: ChatViewModel,
    contactsViewModel: ContactsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            TabletLandscapeDualPaneScreen(
                chatViewModel = chatViewModel,
                contactsViewModel = contactsViewModel,
                settingsViewModel = settingsViewModel,
                totalAvailableWidthDp = maxWidth
            )
        } else {
            PhoneSinglePaneScreen(
                chatViewModel = chatViewModel,
                contactsViewModel = contactsViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

/**
 * Dual-Pane Layout specifically crafted for 10-inch Tablets & Landscape Mode
 * Features:
 * - NavigationRail on the left edge
 * - Left Pane: Dynamic (Chats list / Contacts / Archives / Settings)
 * - Central Interactive Draggable Split Handle (Resize conversation space freely between 22% and 78%)
 * - Right Pane: Active Chat Conversation or Expressive Tablet Empty State
 */
@Composable
private fun TabletLandscapeDualPaneScreen(
    chatViewModel: ChatViewModel,
    contactsViewModel: ContactsViewModel,
    settingsViewModel: SettingsViewModel,
    totalAvailableWidthDp: androidx.compose.ui.unit.Dp
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentTab by rememberSaveable { mutableStateOf(MainDestination.CHATS) }
    var selectedConversationId by rememberSaveable { mutableStateOf<String?>(null) }
    var splitRatio by rememberSaveable { mutableStateOf(0.38f) } // Default 38% list, 62% chat

    val activeConversations by chatViewModel.activeConversations.collectAsStateWithLifecycle()
    val totalUnread = remember(activeConversations) {
        activeConversations.sumOf { it.unreadCount }
    }
    val contacts by contactsViewModel.contacts.collectAsStateWithLifecycle()
    val hasContactPermission by contactsViewModel.hasContactPermission.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        contactsViewModel.onPermissionResult(isGranted)
        if (isGranted) {
            Toast.makeText(context, "Contacts du téléphone synchronisés !", Toast.LENGTH_SHORT).show()
        }
    }

    val density = LocalDensity.current
    val railWidthDp = 80.dp
    val dividerWidthDp = 24.dp
    val availableContentWidthDp = (totalAvailableWidthDp - railWidthDp - dividerWidthDp).coerceAtLeast(200.dp)
    val totalContentWidthPx = with(density) { availableContentWidthDp.toPx() }

    val leftPaneWidthDp = availableContentWidthDp * splitRatio
    val rightPaneWidthDp = availableContentWidthDp * (1f - splitRatio)

    Row(modifier = Modifier.fillMaxSize()) {
        // 1. M3 Expressive Navigation Rail
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            header = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "I3",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .width(railWidthDp)
                .fillMaxHeight()
                .testTag("tablet_navigation_rail")
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Main Destinations
            MainDestination.values().forEach { destination ->
                val isSelected = currentTab == destination
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { currentTab = destination },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (destination == MainDestination.CHATS && totalUnread > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("$totalUnread")
                                    }
                                } else if (destination == MainDestination.CONTACTS && contacts.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text("${contacts.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        }
                    },
                    label = {
                        Text(
                            text = destination.title,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick phone contact sync trigger in rail footer
            IconButton(
                onClick = {
                    if (!hasContactPermission) {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    } else {
                        currentTab = MainDestination.CONTACTS
                    }
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (hasContactPermission) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    imageVector = if (hasContactPermission) Icons.Outlined.Sync else Icons.Outlined.PhoneAndroid,
                    contentDescription = "Contacts",
                    tint = if (hasContactPermission) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Left Pane (List of chats, Contacts carnet, Archives, Settings)
        Box(
            modifier = Modifier
                .width(leftPaneWidthDp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (currentTab) {
                MainDestination.CHATS -> {
                    ChatListScreen(
                        chatViewModel = chatViewModel,
                        selectedConversationId = selectedConversationId,
                        onNavigateToChat = { convId ->
                            selectedConversationId = convId
                        },
                        onNavigateToArchived = {
                            currentTab = MainDestination.ARCHIVES
                        },
                        onNavigateToContacts = {
                            currentTab = MainDestination.CONTACTS
                        }
                    )
                }
                MainDestination.CONTACTS -> {
                    ContactsScreen(
                        contactsViewModel = contactsViewModel,
                        onStartChat = { convId ->
                            selectedConversationId = convId
                        }
                    )
                }
                MainDestination.ARCHIVES -> {
                    ArchivedChatsScreen(
                        chatViewModel = chatViewModel,
                        onNavigateToChat = { convId ->
                            selectedConversationId = convId
                        },
                        onBack = {
                            currentTab = MainDestination.CHATS
                        }
                    )
                }
                MainDestination.SETTINGS -> {
                    SettingsScreen(
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }

        // 3. Central Interactive Resizable Split Handle (Play Store / Android Split Screen style)
        ExpressiveSplitDivider(
            splitRatio = splitRatio,
            onSplitRatioChange = { splitRatio = it },
            totalWidthPx = totalContentWidthPx,
            onResetToDefault = { splitRatio = 0.38f }
        )

        // 4. Right Pane (Active Chat Conversation or Expressive Tablet Empty State)
        Box(
            modifier = Modifier
                .width(rightPaneWidthDp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            if (selectedConversationId != null) {
                ChatDetailScreen(
                    conversationId = selectedConversationId!!,
                    chatViewModel = chatViewModel,
                    onBack = { selectedConversationId = null },
                    showBackNavigation = false,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                TabletEmptyChatState(
                    hasContactPermission = hasContactPermission,
                    onRequestContactPermission = {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    onOpenContacts = { currentTab = MainDestination.CONTACTS },
                    onStartAiChat = {
                        selectedConversationId = "conv_ai"
                    },
                    onSetSplitRatio = { splitRatio = it }
                )
            }
        }
    }
}

/**
 * Expressive Empty State specifically designed for Tablet Landscape Mode
 */
@Composable
private fun TabletEmptyChatState(
    hasContactPermission: Boolean,
    onRequestContactPermission: () -> Unit,
    onOpenContacts: () -> Unit,
    onStartAiChat: () -> Unit,
    onSetSplitRatio: (Float) -> Unit
) {
    val context = LocalContext.current
    val carrierStatus = remember { CarrierSmsManager.getCarrierNetworkStatus(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            // Hero Icon
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "I3 Messages",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sélectionnez une discussion à gauche pour afficher la conversation ou échangez directement avec vos contacts téléphoniques.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!hasContactPermission) {
                    Button(
                        onClick = onRequestContactPermission,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Outlined.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Autoriser l'accès à mes contacts téléphoniques")
                    }
                } else {
                    Button(
                        onClick = onOpenContacts,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Parcourir mes contacts du téléphone")
                    }
                }

                OutlinedButton(
                    onClick = onStartAiChat,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Outlined.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Discuter avec l'Assistant IA I3")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Split Presets Quick Controls (Play Store / Tablet convenience)
            Text(
                text = "Ajustement rapide de l'écran divisé :",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = { onSetSplitRatio(0.30f) },
                    label = { Text("30% / 70%") }
                )
                SuggestionChip(
                    onClick = { onSetSplitRatio(0.38f) },
                    label = { Text("38% / 62% (Défaut)") }
                )
                SuggestionChip(
                    onClick = { onSetSplitRatio(0.50f) },
                    label = { Text("50% / 50%") }
                )
                SuggestionChip(
                    onClick = { onSetSplitRatio(0.65f) },
                    label = { Text("65% / 35%") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RCS & SMS Opérateur : ${carrierStatus.operatorName} • Chiffré",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Compact / Phone Single-Pane layout
 */
@Composable
private fun PhoneSinglePaneScreen(
    chatViewModel: ChatViewModel,
    contactsViewModel: ContactsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by rememberSaveable { mutableStateOf(MainDestination.CHATS) }
    var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.MainTabs) }

    val activeConversations by chatViewModel.activeConversations.collectAsStateWithLifecycle()
    val totalUnread = remember(activeConversations) {
        activeConversations.sumOf { it.unreadCount }
    }

    // Handle system back button
    BackHandler(enabled = currentScreen !is ScreenState.MainTabs) {
        currentScreen = ScreenState.MainTabs
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState is ScreenState.ChatDetail || targetState is ScreenState.ArchivedChats) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                ) + fadeIn() togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 }
                ) + fadeOut()
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                ) + fadeIn() togetherWith slideOutHorizontally(
                    targetOffsetX = { it }
                ) + fadeOut()
            }
        },
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            is ScreenState.ChatDetail -> {
                ChatDetailScreen(
                    conversationId = screen.conversationId,
                    chatViewModel = chatViewModel,
                    onBack = { currentScreen = ScreenState.MainTabs }
                )
            }
            is ScreenState.ArchivedChats -> {
                ArchivedChatsScreen(
                    chatViewModel = chatViewModel,
                    onNavigateToChat = { convId ->
                        currentScreen = ScreenState.ChatDetail(convId)
                    },
                    onBack = { currentScreen = ScreenState.MainTabs }
                )
            }
            is ScreenState.MainTabs -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .testTag("main_navigation_bar")
                        ) {
                            listOf(MainDestination.CHATS, MainDestination.CONTACTS, MainDestination.SETTINGS).forEach { destination ->
                                val isSelected = currentTab == destination
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = destination },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (destination == MainDestination.CHATS && totalUnread > 0) {
                                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                        Text("$totalUnread")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                                contentDescription = destination.title
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = destination.title,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    },
                    modifier = modifier.fillMaxSize()
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (currentTab) {
                            MainDestination.CHATS -> {
                                ChatListScreen(
                                    chatViewModel = chatViewModel,
                                    onNavigateToChat = { convId ->
                                        currentScreen = ScreenState.ChatDetail(convId)
                                    },
                                    onNavigateToArchived = {
                                        currentScreen = ScreenState.ArchivedChats
                                    },
                                    onNavigateToContacts = {
                                        currentTab = MainDestination.CONTACTS
                                    }
                                )
                            }
                            MainDestination.CONTACTS -> {
                                ContactsScreen(
                                    contactsViewModel = contactsViewModel,
                                    onStartChat = { convId ->
                                        currentScreen = ScreenState.ChatDetail(convId)
                                    }
                                )
                            }
                            MainDestination.SETTINGS -> {
                                SettingsScreen(
                                    settingsViewModel = settingsViewModel
                                )
                            }
                            MainDestination.ARCHIVES -> {
                                ArchivedChatsScreen(
                                    chatViewModel = chatViewModel,
                                    onNavigateToChat = { convId ->
                                        currentScreen = ScreenState.ChatDetail(convId)
                                    },
                                    onBack = { currentTab = MainDestination.CHATS }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
