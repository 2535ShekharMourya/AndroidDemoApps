package com.azad.androiddemoapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.azad.androiddemoapp.domain.model.Article
import com.azad.androiddemoapp.domain.model.Category
import com.azad.androiddemoapp.ui.components.ArticleItem
import com.azad.androiddemoapp.ui.components.ErrorScreen
import com.azad.androiddemoapp.ui.components.ShimmerArticleCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val favoriteUrls by viewModel.favoriteUrls.collectAsState()
    val lazyPagingItems = viewModel.newsFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        // Scrolling TabRow for Categories
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Category.values().forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = {
                        viewModel.selectCategory(category)
                        // Reset list scroll position when category changes
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    },
                    text = {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        val refreshState = lazyPagingItems.loadState.refresh
        val isRefreshing = refreshState is LoadState.Loading && lazyPagingItems.itemCount > 0

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (lazyPagingItems.itemCount == 0 && refreshState is LoadState.Loading) {
                // Skeleton loading state
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding
                ) {
                    items(10) {
                        ShimmerArticleCard()
                    }
                }
            } else if (refreshState is LoadState.Error) {
                // Main error state
                ErrorScreen(
                    error = (refreshState as LoadState.Error).error,
                    onRetry = { lazyPagingItems.retry() }
                )
            } else {
                // Main paginated articles list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding
                ) {
                    items(
                        count = lazyPagingItems.itemCount
                    ) { index ->
                        val article = lazyPagingItems[index]
                        if (article != null) {
                            val isFav = favoriteUrls.contains(article.url)
                            ArticleItem(
                                article = article,
                                isFavorite = isFav,
                                onArticleClick = onArticleClick,
                                onFavoriteClick = { viewModel.toggleFavorite(it) }
                            )
                        }
                    }

                    // Append/Infinite scroll loading state indicator
                    val appendState = lazyPagingItems.loadState.append
                    if (appendState is LoadState.Loading) {
                        item {
                            ShimmerArticleCard()
                        }
                    } else if (appendState is LoadState.Error) {
                        item {
                            ErrorScreen(
                                error = (appendState as LoadState.Error).error,
                                onRetry = { lazyPagingItems.retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}
