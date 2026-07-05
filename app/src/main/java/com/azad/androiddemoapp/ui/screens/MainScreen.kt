package com.azad.androiddemoapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.azad.androiddemoapp.ui.viewmodel.CalendarViewModel
import com.azad.androiddemoapp.ui.viewmodel.ContactsViewModel
import com.azad.androiddemoapp.ui.viewmodel.GalleryViewModel
import com.azad.androiddemoapp.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.launch

sealed class ScreenTab(val route: String, val title: String, val icon: ImageVector) {
    data object Contacts : ScreenTab("contacts", "Contacts", Icons.Default.Person)
    data object Gallery : ScreenTab("gallery", "Gallery", Icons.Default.PlayArrow)
    data object Calendar : ScreenTab("calendar", "Calendar", Icons.Default.DateRange)
    data object Sms : ScreenTab("sms", "SMS", Icons.Default.Email)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            ScreenTab.Contacts,
            ScreenTab.Gallery,
            ScreenTab.Calendar,
            ScreenTab.Sms
        )
    }
    
    var currentTabIdx by remember { mutableIntStateOf(0) }
    val currentTab = tabs[currentTabIdx]

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // ViewModels scoped to Hilt
    val contactsViewModel: ContactsViewModel = hiltViewModel()
    val galleryViewModel: GalleryViewModel = hiltViewModel()
    val calendarViewModel: CalendarViewModel = hiltViewModel()
    val smsViewModel: SmsViewModel = hiltViewModel()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentTab.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = onThemeToggle) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                if (!isLargeScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = currentTabIdx == index,
                                onClick = { currentTabIdx = index },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title) }
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLargeScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationRailItem(
                                selected = currentTabIdx == index,
                                onClick = { currentTabIdx = index },
                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title) }
                            )
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (currentTab) {
                        ScreenTab.Contacts -> ContactsTab(viewModel = contactsViewModel)
                        ScreenTab.Gallery -> GalleryTab(viewModel = galleryViewModel)
                        ScreenTab.Calendar -> CalendarTab(
                            viewModel = calendarViewModel,
                            showSnackbar = showSnackbar
                        )
                        ScreenTab.Sms -> SmsTab(viewModel = smsViewModel)
                    }
                }
            }
        }
    }
}
