package com.azad.androiddemoapp.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.azad.androiddemoapp.data.local.entity.CartEntity
import com.azad.androiddemoapp.data.local.entity.FavoriteEntity
import com.azad.androiddemoapp.data.local.entity.ProductEntity
import com.azad.androiddemoapp.data.remote.model.ProductDto
import com.azad.androiddemoapp.data.repository.ShoppingRepository
import com.azad.androiddemoapp.util.ErrorType
import com.azad.androiddemoapp.util.Resource
import com.azad.androiddemoapp.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<UiState<List<String>>>(UiState.Idle)
    val categoriesState: StateFlow<UiState<List<String>>> = _categoriesState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _snackbarMessage = Channel<String>(Channel.BUFFERED)
    val snackbarMessage = _snackbarMessage.receiveAsFlow()

    val productsFlow: Flow<PagingData<ProductEntity>> = _selectedCategory
        .flatMapLatest { category ->
            repository.getProducts(category)
        }
        .cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            when (val resource = repository.getCategories()) {
                is Resource.Success -> {
                    if (resource.data.isEmpty()) {
                        _categoriesState.value = UiState.Error(ErrorType.EmptyResponse, "No categories found.")
                    } else {
                        _categoriesState.value = UiState.Success(resource.data)
                    }
                }
                is Resource.Error -> {
                    val errorType = when (resource.message) {
                        "No internet connection." -> ErrorType.NoInternet
                        "Request timed out. Please try again." -> ErrorType.Timeout
                        else -> ErrorType.ServerError
                    }
                    _categoriesState.value = UiState.Error(errorType, resource.message)
                }
                Resource.Loading -> {
                    _categoriesState.value = UiState.Loading
                }
            }
        }
    }

    fun toggleFavorite(product: ProductEntity) {
        viewModelScope.launch {
            val isFav = repository.isFavorite(product.id).first()
            if (isFav) {
                repository.removeFavorite(product.id)
                _snackbarMessage.send("${product.title} removed from favorites")
            } else {
                repository.addFavorite(product)
                _snackbarMessage.send("${product.title} added to favorites")
            }
        }
    }

    fun isProductFavorite(productId: Int): Flow<Boolean> {
        return repository.isFavorite(productId)
    }
}

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _snackbarMessage = Channel<String>(Channel.BUFFERED)
    val snackbarMessage = _snackbarMessage.receiveAsFlow()

    val searchResultsFlow: Flow<PagingData<ProductDto>> = _searchQuery
        .debounce(400)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.trim().length < 2) {
                flowOf(PagingData.empty())
            } else {
                repository.searchProducts(query.trim())
            }
        }
        .cachedIn(viewModelScope)

    val recentSearches: StateFlow<List<String>> = repository.getRecentSearches()
        .map { list -> list.map { it.query } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onSearchAction(query: String) {
        if (query.trim().isNotBlank()) {
            _searchQuery.value = query
            viewModelScope.launch {
                repository.addSearchQuery(query.trim())
            }
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
            _snackbarMessage.send("Search history cleared")
        }
    }

    fun toggleFavoriteFromDto(productDto: ProductDto) {
        viewModelScope.launch {
            val productEntity = ProductEntity(
                id = productDto.id,
                title = productDto.title,
                description = productDto.description,
                category = productDto.category,
                price = productDto.price,
                discountPercentage = productDto.discountPercentage,
                rating = productDto.rating,
                stock = productDto.stock,
                brand = productDto.brand,
                thumbnail = productDto.thumbnail
            )
            val isFav = repository.isFavorite(productDto.id).first()
            if (isFav) {
                repository.removeFavorite(productDto.id)
                _snackbarMessage.send("${productDto.title} removed from favorites")
            } else {
                repository.addFavorite(productEntity)
                _snackbarMessage.send("${productDto.title} added to favorites")
            }
        }
    }

    fun isProductFavorite(productId: Int): Flow<Boolean> {
        return repository.isFavorite(productId)
    }
}

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    val favoritesState: StateFlow<List<FavoriteEntity>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(productId: Int, title: String) {
        viewModelScope.launch {
            repository.removeFavorite(productId)
        }
    }
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _productState = MutableStateFlow<UiState<ProductEntity>>(UiState.Idle)
    val productState: StateFlow<UiState<ProductEntity>> = _productState.asStateFlow()

    private val _snackbarMessage = Channel<String>(Channel.BUFFERED)
    val snackbarMessage = _snackbarMessage.receiveAsFlow()

    fun getProductDetail(productId: Int) {
        viewModelScope.launch {
            _productState.value = UiState.Loading
            when (val resource = repository.getProductById(productId)) {
                is Resource.Success -> {
                    _productState.value = UiState.Success(resource.data)
                }
                is Resource.Error -> {
                    val errorType = when (resource.message) {
                        "No internet connection." -> ErrorType.NoInternet
                        "Request timed out. Please try again." -> ErrorType.Timeout
                        else -> ErrorType.ServerError
                    }
                    _productState.value = UiState.Error(errorType, resource.message)
                }
                Resource.Loading -> {
                    _productState.value = UiState.Loading
                }
            }
        }
    }

    fun isProductFavorite(productId: Int): Flow<Boolean> {
        return repository.isFavorite(productId)
    }

    fun toggleFavorite(product: ProductEntity) {
        viewModelScope.launch {
            val isFav = repository.isFavorite(product.id).first()
            if (isFav) {
                repository.removeFavorite(product.id)
                _snackbarMessage.send("${product.title} removed from favorites")
            } else {
                repository.addFavorite(product)
                _snackbarMessage.send("${product.title} added to favorites")
            }
        }
    }

    fun addToCart(product: ProductEntity) {
        viewModelScope.launch {
            repository.addToCart(product)
            _snackbarMessage.send("${product.title} added to cart")
        }
    }
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    val cartItems: StateFlow<List<CartEntity>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _snackbarMessage = Channel<String>(Channel.BUFFERED)
    val snackbarMessage = _snackbarMessage.receiveAsFlow()

    fun updateQuantity(productId: Int, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, newQuantity)
        }
    }

    fun removeFromCart(productId: Int, title: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
            _snackbarMessage.send("$title removed from cart")
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
            _snackbarMessage.send("Cart cleared")
        }
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    application: Application
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("shopping_prefs", Context.MODE_PRIVATE)

    private val _themeState = MutableStateFlow(sharedPrefs.getString("theme", "System") ?: "System")
    val themeState: StateFlow<String> = _themeState.asStateFlow()

    private val _snackbarMessage = Channel<String>(Channel.BUFFERED)
    val snackbarMessage = _snackbarMessage.receiveAsFlow()

    val favoritesState: StateFlow<List<FavoriteEntity>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartState: StateFlow<List<CartEntity>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateTheme(theme: String) {
        _themeState.value = theme
        sharedPrefs.edit().putString("theme", theme).apply()
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                repository.clearCachedData()
                _snackbarMessage.send("Cache cleared successfully")
            } catch (e: Exception) {
                _snackbarMessage.send("Failed to clear cache: ${e.localizedMessage}")
            }
        }
    }
}
