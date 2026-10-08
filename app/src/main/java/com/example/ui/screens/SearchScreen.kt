package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyContentState
import com.example.ui.components.MovieCard
import com.example.ui.components.SeriesCard
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val filterCategory by viewModel.searchFilterCategory.collectAsState()
    val searchMovies by viewModel.searchMovies.collectAsState()
    val searchSeries by viewModel.searchSeries.collectAsState()

    val showMovies = filterCategory == "ALL" || filterCategory == "MOVIES"
    val showSeries = filterCategory == "ALL" || filterCategory == "SERIES"

    val totalResultsCount = (if (showMovies) searchMovies.size else 0) + (if (showSeries) searchSeries.size else 0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Search",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search by title, cast, genre, language, year...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CinemaRed
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCardBg,
                unfocusedContainerColor = DarkCardBg,
                focusedBorderColor = CinemaRed,
                unfocusedBorderColor = DarkSurfaceVariant,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips (All, Movies, Series)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All Titles", "MOVIES" to "Movies Only", "SERIES" to "Series Only").forEach { (key, label) ->
                FilterChip(
                    selected = filterCategory == key,
                    onClick = { viewModel.searchFilterCategory.value = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CinemaRed,
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (query.isBlank()) {
            // Suggestion chips
            Text(
                text = "Popular Searches",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            val popularKeywords = listOf("Action", "Drama", "Comedy", "Thriller", "Hindi", "English", "2024")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(popularKeywords) { keyword ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.searchQuery.value = keyword },
                        label = { Text(keyword, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurfaceVariant.copy(alpha = 0.6f),
                            labelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            EmptyContentState(
                title = "Find Your Favorite Cinema",
                message = "Search across full-length movies, web series, directors, languages, actors and release years."
            )
        } else if (totalResultsCount == 0) {
            EmptyContentState(
                title = "No Results Found",
                message = "We couldn't find any titles matching '$query'. Try checking spelling or search with different keywords."
            )
        } else {
            Text(
                text = "Found $totalResultsCount results for '$query'",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(135.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (showMovies) {
                    items(searchMovies, key = { "m_${it.id}" }) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { viewModel.selectMovie(movie.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (showSeries) {
                    items(searchSeries, key = { "s_${it.id}" }) { series ->
                        SeriesCard(
                            series = series,
                            onClick = { viewModel.selectSeries(series.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
