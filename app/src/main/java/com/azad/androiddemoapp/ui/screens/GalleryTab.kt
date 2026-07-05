package com.azad.androiddemoapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.azad.androiddemoapp.model.MediaItem
import com.azad.androiddemoapp.model.ResourceState
import com.azad.androiddemoapp.ui.components.CachedImage
import com.azad.androiddemoapp.ui.components.EmptyView
import com.azad.androiddemoapp.ui.components.ErrorView
import com.azad.androiddemoapp.ui.components.shimmer
import com.azad.androiddemoapp.ui.viewmodel.GalleryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTab(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var hasPermissionsState by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissionsState = permissions.values.all { it }
        if (hasPermissionsState) {
            viewModel.loadMedia()
        }
    }

    if (!hasPermissionsState) {
        PermissionRationaleView(
            permissionName = "Gallery",
            description = "This app requires access to your media files to display your local images and videos.",
            onGrantClick = {
                launcher.launch(requiredPermissions)
            },
            modifier = modifier
        )
    } else {
        GalleryContent(
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryContent(
    viewModel: GalleryViewModel,
    modifier: Modifier = Modifier
) {
    val mediaState by viewModel.mediaState.collectAsState()
    val isVideoOnly by viewModel.isVideoOnly.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(mediaState) {
        if (mediaState !is ResourceState.Loading) {
            isRefreshing = false
        }
    }

    val tabs = listOf("All", "Images", "Videos")
    selectedTab = when (isVideoOnly) {
        null -> 0
        false -> 1
        true -> 2
    }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        val filter = when (index) {
                            1 -> false
                            2 -> true
                            else -> null
                        }
                        viewModel.loadMedia(filter)
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadMedia(isVideoOnly)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = mediaState) {
                is ResourceState.Loading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(12) {
                            MediaShimmerItem()
                        }
                    }
                }
                is ResourceState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyView(message = "No media found in your gallery.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.data, key = { it.id }) { mediaItem ->
                                GalleryGridItem(mediaItem = mediaItem)
                            }
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorView(
                        error = state as ResourceState.Error,
                        onRetry = { viewModel.loadMedia(isVideoOnly) }
                    )
                }
            }
        }
    }
}

@Composable
fun GalleryGridItem(mediaItem: MediaItem) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CachedImage(
                url = mediaItem.uriString,
                contentDescription = mediaItem.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            if (mediaItem.isVideo) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video marker",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
                
                mediaItem.duration?.let { durationMs ->
                    val seconds = (durationMs / 1000) % 60
                    val minutes = (durationMs / (1000 * 60)) % 60
                    val durationText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaShimmerItem() {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .shimmer()
    )
}
