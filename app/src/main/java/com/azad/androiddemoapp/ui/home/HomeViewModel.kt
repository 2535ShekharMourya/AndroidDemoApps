package com.azad.androiddemoapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.azad.androiddemoapp.data.repository.NewsRepository
import com.azad.androiddemoapp.domain.model.Article
import com.azad.androiddemoapp.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(Category.GENERAL)
    val selectedCategory: StateFlow<Category> = _selectedCategory.asStateFlow()

    val favoriteUrls: StateFlow<Set<String>> = repository.getFavoriteArticles()
        .map { articles -> articles.map { it.url }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val newsFlow = _selectedCategory.flatMapLatest { category ->
        repository.getPagedTopHeadlines(category)
    }.cachedIn(viewModelScope)

    fun selectCategory(category: Category) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
        }
    }

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            repository.toggleFavorite(article)
        }
    }
}
