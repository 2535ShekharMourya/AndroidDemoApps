package com.azad.androiddemoapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.azad.androiddemoapp.domain.model.Article
import androidx.compose.foundation.layout.padding
import com.azad.androiddemoapp.ui.favorite.FavoriteScreen
import com.azad.androiddemoapp.ui.home.HomeScreen
import com.azad.androiddemoapp.ui.profile.ProfileScreen
import com.azad.androiddemoapp.ui.search.SearchScreen
import com.azad.androiddemoapp.ui.detail.NewsDetailScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onArticleClick = onArticleClick,
                contentPadding = contentPadding
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onArticleClick = onArticleClick,
                contentPadding = contentPadding
            )
        }
        composable(Screen.Favorites.route) {
            FavoriteScreen(
                onArticleClick = onArticleClick,
                contentPadding = contentPadding
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                modifier = Modifier.padding(contentPadding)
            )
        }
        composable(Screen.NewsDetail.route) {
            NewsDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
