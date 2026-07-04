package com.azad.androiddemoapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azad.androiddemoapp.data.repository.NewsRepository
import com.azad.androiddemoapp.domain.model.Article
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val repository: NewsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val article: Article = savedStateHandle.get<String>("articleJson")?.let {
        Gson().fromJson(it, Article::class.java)
    } ?: throw IllegalArgumentException("Article content is missing")

    val isFavorite: Flow<Boolean> = repository.isFavoriteFlow(article.url)

    fun toggleFavorite() {
        viewModelScope.launch {
            repository.toggleFavorite(article)
        }
    }
}
