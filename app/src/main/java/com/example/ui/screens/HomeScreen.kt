package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MovieEntity
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.EmptyContentState
import com.example.ui.components.MovieCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SeriesCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allMovies by viewModel.allPublishedMovies.collectAsState()
    val allSeries by viewModel.allPublishedSeries.collectAsState()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val trendingMovies by viewModel.trendingMovies.collectAsState()
    val latestMovies by viewModel.latestMovies.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()

    val hasAnyContent = allMovies.isNotEmpty() || allSeries.isNotEmpty()
    val heroMovie: MovieEntity? = featuredMovies.firstOrNull() ?: allMovies.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CinemaRed)
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Movie Box",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.switchTab(Screen.Search) },
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Admin) },
                        modifier = Modifier.testTag("home_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = CinemaGold
                        )
                    }
                }
            }
        }

        // Empty state if database has no content
        if (!hasAnyContent) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
                EmptyContentState(
                    title = "Welcome to TT Movie Box",
                    message = "The content library is currently empty. All movies and web series must be added and published by the admin. Tap below to access the Admin Panel and upload your first movie or series.",
                    actionLabel = "Open Admin Panel",
                    onActionClick = { viewModel.navigateTo(Screen.Admin) }
                )
            }
        } else {
            // Hero Featured Banner
            if (heroMovie != null) {
                item {
                    HeroBanner(
                        movie = heroMovie,
                        onWatchClick = { viewModel.playMovie(heroMovie) },
                        onDetailsClick = { viewModel.selectMovie(heroMovie.id) },
                        onWatchlistClick = { viewModel.toggleWatchlist(heroMovie.id, "MOVIE") }
                    )
                }
            }

            // Continue Watching (only if user has history)
            if (continueWatching.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Continue Watching")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(continueWatching, key = { it.id }) { item ->
                            ContinueWatchingCard(
                                item = item,
                                onClick = {
                                    if (item.contentType == "MOVIE") {
                                        viewModel.selectMovie(item.contentId)
                                    } else {
                                        if (item.seriesId > 0) viewModel.selectSeries(item.seriesId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Trending Now (only if trending movies exist)
            if (trendingMovies.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Trending Now")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trendingMovies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { viewModel.selectMovie(movie.id) }
                            )
                        }
                    }
                }
            }

            // Latest Added (only if latest movies exist)
            if (latestMovies.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(
                        title = "Latest Releases",
                        onSeeAllClick = { viewModel.switchTab(Screen.Movies) }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(latestMovies, key = { it.id }) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { viewModel.selectMovie(movie.id) }
                            )
                        }
                    }
                }
            }

            // Web Series (only if series exist)
            if (allSeries.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(
                        title = "Popular Web Series",
                        onSeeAllClick = { viewModel.switchTab(Screen.Series) }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allSeries, key = { it.id }) { series ->
                            SeriesCard(
                                series = series,
                                onClick = { viewModel.selectSeries(series.id) }
                            )
                        }
                    }
                }
            }

            // Genre Rows (dynamically extracted, ONLY show if content exists for that genre)
            val allGenres = allMovies.map { it.genre.split(",") }
                .flatten()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            allGenres.take(4).forEach { genre ->
                val genreMovies = allMovies.filter { it.genre.contains(genre, ignoreCase = true) }
                if (genreMovies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "$genre Movies")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(genreMovies, key = { it.id }) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { viewModel.selectMovie(movie.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Language Rows (dynamically extracted, ONLY show if content exists for that language)
            val allLanguages = allMovies.map { it.language.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            allLanguages.take(3).forEach { lang ->
                val langMovies = allMovies.filter { it.language.equals(lang, ignoreCase = true) }
                if (langMovies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = "$lang Cinema")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(langMovies, key = { it.id }) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { viewModel.selectMovie(movie.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBanner(
    movie: MovieEntity,
    onWatchClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clickable(onClick = onDetailsClick)
    ) {
        val bannerImage = movie.backdropUri.ifBlank { movie.posterUri }
        if (bannerImage.isNotBlank()) {
            AsyncImage(
                model = bannerImage,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E202E))
            )
        }

        // Dark gradients (top & bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            DarkBackground.copy(alpha = 0.85f),
                            DarkBackground
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Banner content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomStart)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CinemaRed)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "FEATURED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = movie.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${movie.releaseYear} • ${movie.durationMinutes} min • ${movie.genre} • ${movie.language}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onWatchClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("hero_watch_now_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Watch Now", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onWatchlistClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("hero_add_watchlist_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Watchlist", color = Color.White)
                }

                IconButton(
                    onClick = onDetailsClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
