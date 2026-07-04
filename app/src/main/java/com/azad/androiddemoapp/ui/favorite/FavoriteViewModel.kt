package com.azad.androiddemoapp.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azad.androiddemoapp.data.repository.NewsRepository
import com.azad.androiddemoapp.domain.model.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    val favorites: StateFlow<List<Article>> = repository.getFavoriteArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            repository.toggleFavorite(article)
        }
    }
}
