package com.azad.androiddemoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.azad.androiddemoapp.ui.components.ErrorView
import com.azad.androiddemoapp.ui.components.ProductItem
import com.azad.androiddemoapp.ui.components.ShimmerProductItem
import com.azad.androiddemoapp.ui.viewmodel.HomeViewModel
import com.azad.androiddemoapp.util.ErrorType
import com.azad.androiddemoapp.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val categoriesState by viewModel.categoriesState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val lazyPagingItems = viewModel.productsFlow.collectAsLazyPagingItems()

    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount > 0

    val configuration = LocalConfiguration.current
    val columnsCount = when {
        configuration.screenWidthDp >= 840 -> 4
        configuration.screenWidthDp >= 600 -> 3
        else -> 2
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Search Bar Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .clickable { onNavigateToSearch() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search products, brands...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // Category Tabs
        when (val state = categoriesState) {
            is UiState.Loading -> {
                // Show a loading placeholder for categories
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(width = 80.dp, height = 36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                    }
                }
            }
            is UiState.Success -> {
                ScrollableTabRow(
                    selectedTabIndex = state.data.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = {},
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    state.data.forEach { category ->
                        val isSelected = category == selectedCategory
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                // If categories fail, show retry button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Failed to load categories.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.loadCategories() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Retry", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            UiState.Idle -> {}
        }

        // Pull to Refresh container
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            val loadState = lazyPagingItems.loadState

            if (loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount == 0) {
                // Initial loading skeleton
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(8) {
                        ShimmerProductItem()
                    }
                }
            } else if (loadState.refresh is LoadState.Error && lazyPagingItems.itemCount == 0) {
                // Initial load error
                val error = (loadState.refresh as LoadState.Error).error
                val errorType = when (error) {
                    is java.io.IOException -> ErrorType.NoInternet
                    is retrofit2.HttpException -> ErrorType.HttpError(error.code())
                    else -> ErrorType.Unknown
                }
                ErrorView(
                    errorType = errorType,
                    message = error.localizedMessage,
                    onRetry = { lazyPagingItems.retry() }
                )
            } else if (lazyPagingItems.itemCount == 0) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No products found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Product Grid List
                val gridState = rememberLazyGridState()
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(columnsCount),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(lazyPagingItems.itemCount) { index ->
                        val product = lazyPagingItems[index]
                        if (product != null) {
                            val isFavorite by viewModel.isProductFavorite(product.id).collectAsState(initial = false)
                            ProductItem(
                                product = product,
                                isFavorite = isFavorite,
                                onFavoriteClick = { viewModel.toggleFavorite(product) },
                                modifier = Modifier.clickable { onNavigateToProductDetail(product.id) }
                            )
                        }
                    }

                    // Append Loader/Error
                    when (val appendState = loadState.append) {
                        is LoadState.Loading -> {
                            item(span = { GridItemSpan(columnsCount) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item(span = { GridItemSpan(columnsCount) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Failed to load more products",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { lazyPagingItems.retry() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        is LoadState.NotLoading -> {}
                    }
                }
            }
        }
    }
}
