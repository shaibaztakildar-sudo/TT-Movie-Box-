package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyContentState
import com.example.ui.components.MovieCard
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun MoviesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allMovies by viewModel.allPublishedMovies.collectAsState()
    var selectedGenre by remember { mutableStateOf("All") }

    val genres = remember(allMovies) {
        val list = mutableListOf("All")
        allMovies.forEach { movie ->
            movie.genre.split(",").forEach { g ->
                val trimmed = g.trim()
                if (trimmed.isNotBlank() && trimmed !in list) list.add(trimmed)
            }
        }
        list
    }

    val filteredMovies = if (selectedGenre == "All") {
        allMovies
    } else {
        allMovies.filter { it.genre.contains(selectedGenre, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Movies",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Explore full-length movies, streaming in high definition",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (genres.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    FilterChip(
                        selected = selectedGenre == genre,
                        onClick = { selectedGenre = genre },
                        label = { Text(genre) },
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
        }

        if (allMovies.isEmpty()) {
            EmptyContentState(
                title = "No Movies Available",
                message = "The movie catalog is currently empty. Movies uploaded and published by the admin will appear here.",
                actionLabel = "Admin Panel",
                onActionClick = { viewModel.navigateTo(Screen.Admin) }
            )
        } else if (filteredMovies.isEmpty()) {
            EmptyContentState(
                title = "No Movies for '$selectedGenre'",
                message = "No titles match the selected genre. Try selecting another filter."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(135.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMovies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { viewModel.selectMovie(movie.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
