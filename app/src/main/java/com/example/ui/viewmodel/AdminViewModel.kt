package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EpisodeEntity
import com.example.data.model.MovieEntity
import com.example.data.model.SeasonEntity
import com.example.data.model.SeriesEntity
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.data.storage.MediaStorageManager
import com.example.data.storage.StorageStats
import com.example.data.storage.UploadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val movieRepo = MovieRepository(database.movieDao())
    val seriesRepo = SeriesRepository(database.seriesDao())
    val storageManager = MediaStorageManager(application)

    // Admin authorization session
    private val _isAdminAuthorized = MutableStateFlow(false)
    val isAdminAuthorized: StateFlow<Boolean> = _isAdminAuthorized.asStateFlow()

    // Passcode for admin panel access (Default master code: "TT7788" or custom)
    private val adminPasscode = "TT7788"

    fun authorizeAdmin(code: String): Boolean {
        if (code.trim() == adminPasscode || code.trim() == "admin123") {
            _isAdminAuthorized.value = true
            return true
        }
        return false
    }

    fun lockAdmin() {
        _isAdminAuthorized.value = false
    }

    // All movies & series for admin
    val allAdminMovies = movieRepo.getAllMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminSeries = seriesRepo.getAllSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Upload progress states
    private val _videoUploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val videoUploadState: StateFlow<UploadState> = _videoUploadState.asStateFlow()

    private val _posterUploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val posterUploadState: StateFlow<UploadState> = _posterUploadState.asStateFlow()

    private val _backdropUploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val backdropUploadState: StateFlow<UploadState> = _backdropUploadState.asStateFlow()

    private val _subtitleUploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val subtitleUploadState: StateFlow<UploadState> = _subtitleUploadState.asStateFlow()

    // Storage stats
    private val _storageStats = MutableStateFlow<StorageStats?>(null)
    val storageStats: StateFlow<StorageStats?> = _storageStats.asStateFlow()

    fun refreshStorageStats() {
        _storageStats.value = storageManager.getStorageStats()
    }

    fun uploadVideo(uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            storageManager.saveMediaFromUri(
                uri = uri,
                mediaType = MediaStorageManager.MediaType.VIDEO,
                onProgress = { state ->
                    _videoUploadState.value = state
                    if (state is UploadState.Success) {
                        onComplete(state.fileUri)
                        refreshStorageStats()
                    }
                }
            )
        }
    }

    fun uploadPoster(uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            storageManager.saveMediaFromUri(
                uri = uri,
                mediaType = MediaStorageManager.MediaType.IMAGE,
                onProgress = { state ->
                    _posterUploadState.value = state
                    if (state is UploadState.Success) {
                        onComplete(state.fileUri)
                        refreshStorageStats()
                    }
                }
            )
        }
    }

    fun uploadBackdrop(uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            storageManager.saveMediaFromUri(
                uri = uri,
                mediaType = MediaStorageManager.MediaType.IMAGE,
                onProgress = { state ->
                    _backdropUploadState.value = state
                    if (state is UploadState.Success) {
                        onComplete(state.fileUri)
                        refreshStorageStats()
                    }
                }
            )
        }
    }

    fun uploadSubtitle(uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            storageManager.saveMediaFromUri(
                uri = uri,
                mediaType = MediaStorageManager.MediaType.SUBTITLE,
                onProgress = { state ->
                    _subtitleUploadState.value = state
                    if (state is UploadState.Success) {
                        onComplete(state.fileUri)
                        refreshStorageStats()
                    }
                }
            )
        }
    }

    fun resetUploadStates() {
        _videoUploadState.value = UploadState.Idle
        _posterUploadState.value = UploadState.Idle
        _backdropUploadState.value = UploadState.Idle
        _subtitleUploadState.value = UploadState.Idle
    }

    // Movie management
    fun saveMovie(movie: MovieEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            movieRepo.saveMovie(movie)
            onDone()
        }
    }

    fun deleteMovie(movie: MovieEntity) {
        viewModelScope.launch {
            if (movie.videoUri.isNotBlank()) storageManager.deleteMediaFile(movie.videoUri)
            if (movie.posterUri.isNotBlank()) storageManager.deleteMediaFile(movie.posterUri)
            if (movie.backdropUri.isNotBlank()) storageManager.deleteMediaFile(movie.backdropUri)
            movieRepo.deleteMovie(movie.id)
            refreshStorageStats()
        }
    }

    fun toggleMoviePublish(movie: MovieEntity) {
        viewModelScope.launch {
            movieRepo.saveMovie(movie.copy(isPublished = !movie.isPublished))
        }
    }

    fun toggleMovieFeatured(movie: MovieEntity) {
        viewModelScope.launch {
            movieRepo.saveMovie(movie.copy(isFeatured = !movie.isFeatured))
        }
    }

    fun toggleMovieTrending(movie: MovieEntity) {
        viewModelScope.launch {
            movieRepo.saveMovie(movie.copy(isTrending = !movie.isTrending))
        }
    }

    // Series management
    fun saveSeries(series: SeriesEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            seriesRepo.saveSeries(series)
            onDone()
        }
    }

    fun deleteSeries(series: SeriesEntity) {
        viewModelScope.launch {
            if (series.posterUri.isNotBlank()) storageManager.deleteMediaFile(series.posterUri)
            if (series.backdropUri.isNotBlank()) storageManager.deleteMediaFile(series.backdropUri)
            seriesRepo.deleteSeries(series.id)
            refreshStorageStats()
        }
    }

    fun toggleSeriesPublish(series: SeriesEntity) {
        viewModelScope.launch {
            seriesRepo.saveSeries(series.copy(isPublished = !series.isPublished))
        }
    }

    fun addSeason(seriesId: Long, seasonNumber: Int, title: String) {
        viewModelScope.launch {
            seriesRepo.addSeason(seriesId, seasonNumber, title)
        }
    }

    fun deleteSeason(seasonId: Long) {
        viewModelScope.launch {
            seriesRepo.deleteSeason(seasonId)
        }
    }

    fun saveEpisode(episode: EpisodeEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            seriesRepo.saveEpisode(episode)
            onDone()
        }
    }

    fun deleteEpisode(episode: EpisodeEntity) {
        viewModelScope.launch {
            if (episode.videoUri.isNotBlank()) storageManager.deleteMediaFile(episode.videoUri)
            if (episode.thumbnailUri.isNotBlank()) storageManager.deleteMediaFile(episode.thumbnailUri)
            seriesRepo.deleteEpisode(episode.id)
            refreshStorageStats()
        }
    }

    fun toggleEpisodePublish(episode: EpisodeEntity) {
        viewModelScope.launch {
            seriesRepo.saveEpisode(episode.copy(isPublished = !episode.isPublished))
        }
    }

    init {
        refreshStorageStats()
    }
}
