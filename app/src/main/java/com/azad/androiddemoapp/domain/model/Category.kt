package com.azad.androiddemoapp.domain.model

enum class Category(val key: String, val displayName: String) {
    GENERAL("general", "General"),
    BUSINESS("business", "Business"),
    TECHNOLOGY("technology", "Technology"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    SCIENCE("science", "Science"),
    SPORTS("sports", "Sports"),
    HEALTH("health", "Health")
}
