package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EpisodeEntity
import com.example.data.model.MovieEntity
import com.example.data.model.SeasonEntity
import com.example.data.model.SeriesEntity
import com.example.data.model.UserEntity
import com.example.data.model.WatchHistoryEntity
import com.example.data.model.WatchlistEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.data.repository.UserActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Movies : Screen()
    object Series : Screen()
    object Search : Screen()
    object Profile : Screen()
    object Auth : Screen()
    data class MovieDetails(val movieId: Long) : Screen()
    data class SeriesDetails(val seriesId: Long) : Screen()
    data class Player(val args: PlayerMediaArgs) : Screen()
    object Admin : Screen()
}

data class PlayerMediaArgs(
    val contentId: Long,
    val contentType: String, // "MOVIE" or "EPISODE"
    val seriesId: Long = 0,
    val title: String,
    val subtitle: String = "",
    val videoUri: String,
    val posterUri: String = "",
    val subtitleUri: String = "",
    val initialPositionMs: Long = 0L,
    val nextEpisodeId: Long = 0L,
    val nextEpisodeTitle: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val movieRepo = MovieRepository(database.movieDao())
    val seriesRepo = SeriesRepository(database.seriesDao())
    val authRepo = AuthRepository(database.userDao())
    val userActivityRepo = UserActivityRepository(database.watchHistoryDao(), database.watchlistDao())

    // Navigation backstack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val currentScreen: StateFlow<Screen> = _screenStack.flatMapLatest { stack ->
        flowOf(stack.lastOrNull() ?: Screen.Home)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Home)

    fun navigateTo(screen: Screen) {
        val currentList = _screenStack.value.toMutableList()
        currentList.add(screen)
        _screenStack.value = currentList
    }

    fun navigateBack(): Boolean {
        val currentList = _screenStack.value.toMutableList()
        return if (currentList.size > 1) {
            currentList.removeAt(currentList.lastIndex)
            _screenStack.value = currentList
            true
        } else {
            false
        }
    }

    fun switchTab(screen: Screen) {
        _screenStack.value = listOf(screen)
    }

    // User session
    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser

    // Published content flows
    val allPublishedMovies = movieRepo.getAllPublishedMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredMovies = movieRepo.getFeaturedMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingMovies = movieRepo.getTrendingMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMovies = movieRepo.getLatestMovies(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPublishedSeries = seriesRepo.getAllPublishedSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Activity for current user
    val continueWatching: StateFlow<List<WatchHistoryEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) userActivityRepo.getContinueWatching(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWatchlist: StateFlow<List<WatchlistEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) userActivityRepo.getUserWatchlist(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userHistory: StateFlow<List<WatchHistoryEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) userActivityRepo.getUserHistory(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query & filters
    val searchQuery = MutableStateFlow("")
    val searchFilterCategory = MutableStateFlow("ALL") // "ALL", "MOVIES", "SERIES"

    val searchMovies = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList()) else movieRepo.searchMovies(query.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchSeries = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList()) else seriesRepo.searchSeries(query.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Details state
    private val _selectedMovieId = MutableStateFlow<Long?>(null)
    val selectedMovie: StateFlow<MovieEntity?> = _selectedMovieId.flatMapLatest { id ->
        if (id != null) movieRepo.getMovieById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectMovie(movieId: Long) {
        _selectedMovieId.value = movieId
        navigateTo(Screen.MovieDetails(movieId))
    }

    private val _selectedSeriesId = MutableStateFlow<Long?>(null)
    val selectedSeries: StateFlow<SeriesEntity?> = _selectedSeriesId.flatMapLatest { id ->
        if (id != null) seriesRepo.getSeriesById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val seriesSeasons: StateFlow<List<SeasonEntity>> = _selectedSeriesId.flatMapLatest { id ->
        if (id != null) seriesRepo.getSeasons(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedSeasonId = MutableStateFlow<Long?>(null)
    val seasonEpisodes: StateFlow<List<EpisodeEntity>> = selectedSeasonId.flatMapLatest { sId ->
        if (sId != null) seriesRepo.getEpisodes(sId, publishedOnly = true) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSeries(seriesId: Long) {
        _selectedSeriesId.value = seriesId
        selectedSeasonId.value = null
        navigateTo(Screen.SeriesDetails(seriesId))
    }

    fun selectSeason(seasonId: Long) {
        selectedSeasonId.value = seasonId
    }

    // Playback launcher
    fun playMovie(movie: MovieEntity) {
        viewModelScope.launch {
            val user = currentUser.value
            val savedProgress = if (user != null) {
                userActivityRepo.getSavedProgress(user.id, movie.id, "MOVIE")?.progressMs ?: 0L
            } else 0L

            navigateTo(
                Screen.Player(
                    PlayerMediaArgs(
                        contentId = movie.id,
                        contentType = "MOVIE",
                        title = movie.title,
                        subtitle = "${movie.releaseYear} • ${movie.genre} • ${movie.language}",
                        videoUri = movie.videoUri,
                        posterUri = movie.posterUri.ifBlank { movie.backdropUri },
                        subtitleUri = movie.subtitleUri,
                        initialPositionMs = savedProgress
                    )
                )
            )
        }
    }

    fun playEpisode(episode: EpisodeEntity, series: SeriesEntity) {
        viewModelScope.launch {
            val user = currentUser.value
            val savedProgress = if (user != null) {
                userActivityRepo.getSavedProgress(user.id, episode.id, "EPISODE")?.progressMs ?: 0L
            } else 0L

            val nextEp = seriesRepo.getNextEpisode(series.id, episode.seasonId, episode.episodeNumber)

            navigateTo(
                Screen.Player(
                    PlayerMediaArgs(
                        contentId = episode.id,
                        contentType = "EPISODE",
                        seriesId = series.id,
                        title = "${series.title}: EP ${episode.episodeNumber}",
                        subtitle = episode.title,
                        videoUri = episode.videoUri,
                        posterUri = episode.thumbnailUri.ifBlank { series.posterUri },
                        subtitleUri = episode.subtitleUri,
                        initialPositionMs = savedProgress,
                        nextEpisodeId = nextEp?.id ?: 0L,
                        nextEpisodeTitle = nextEp?.let { "EP ${it.episodeNumber} - ${it.title}" } ?: ""
                    )
                )
            )
        }
    }

    fun playNextEpisode(nextEpisodeId: Long, seriesId: Long) {
        viewModelScope.launch {
            val ep = seriesRepo.getEpisodeByIdOnce(nextEpisodeId) ?: return@launch
            val ser = seriesRepo.getSeriesByIdOnce(seriesId) ?: return@launch
            playEpisode(ep, ser)
        }
    }

    fun saveWatchProgress(args: PlayerMediaArgs, progressMs: Long, durationMs: Long) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                userActivityRepo.saveWatchProgress(
                    userId = user.id,
                    contentId = args.contentId,
                    contentType = args.contentType,
                    seriesId = args.seriesId,
                    title = args.title,
                    subtitle = args.subtitle,
                    posterUri = args.posterUri,
                    videoUri = args.videoUri,
                    progressMs = progressMs,
                    durationMs = durationMs
                )
            }
        }
    }

    fun toggleWatchlist(contentId: Long, contentType: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            userActivityRepo.toggleWatchlist(user.id, contentId, contentType)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            userActivityRepo.clearHistory(user.id)
        }
    }

    fun logout() {
        authRepo.logout()
        switchTab(Screen.Home)
    }
}
