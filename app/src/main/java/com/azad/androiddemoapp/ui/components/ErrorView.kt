package com.azad.androiddemoapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.azad.androiddemoapp.util.ErrorType

@Composable
fun ErrorView(
    errorType: ErrorType,
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (errorType) {
        ErrorType.NoInternet -> "No Connection"
        ErrorType.Timeout -> "Request Timeout"
        ErrorType.ServerError -> "Server Error"
        ErrorType.EmptyResponse -> "No Data Found"
        is ErrorType.HttpError -> "HTTP Error (${errorType.code})"
        ErrorType.Unknown -> "Something Went Wrong"
    }

    val description = message ?: when (errorType) {
        ErrorType.NoInternet -> "Please check your internet settings and try again."
        ErrorType.Timeout -> "The server is taking too long to respond. Please try again."
        ErrorType.ServerError -> "We're experiencing technical difficulties. Please try again later."
        ErrorType.EmptyResponse -> "There are no products to display at the moment."
        is ErrorType.HttpError -> "An error occurred while contacting the server."
        ErrorType.Unknown -> "An unexpected error occurred. Please try again."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error Icon",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
