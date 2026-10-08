package com.example.data.repository

import com.example.data.local.MovieDao
import com.example.data.local.SeriesDao
import com.example.data.local.UserDao
import com.example.data.local.WatchHistoryDao
import com.example.data.local.WatchlistDao
import com.example.data.model.EpisodeEntity
import com.example.data.model.MovieEntity
import com.example.data.model.SeasonEntity
import com.example.data.model.SeriesEntity
import com.example.data.model.UserEntity
import com.example.data.model.WatchHistoryEntity
import com.example.data.model.WatchlistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class MovieRepository(private val movieDao: MovieDao) {
    fun getAllPublishedMovies(): Flow<List<MovieEntity>> = movieDao.getAllPublishedMovies()
    fun getAllMovies(): Flow<List<MovieEntity>> = movieDao.getAllMovies()
    fun getMovieById(id: Long): Flow<MovieEntity?> = movieDao.getMovieById(id)
    suspend fun getMovieByIdOnce(id: Long): MovieEntity? = movieDao.getMovieByIdOnce(id)
    fun getFeaturedMovies(): Flow<List<MovieEntity>> = movieDao.getFeaturedMovies()
    fun getTrendingMovies(): Flow<List<MovieEntity>> = movieDao.getTrendingMovies()
    fun getLatestMovies(limit: Int = 10): Flow<List<MovieEntity>> = movieDao.getLatestMovies(limit)
    suspend fun saveMovie(movie: MovieEntity): Long = withContext(Dispatchers.IO) {
        if (movie.id == 0L) {
            movieDao.insertMovie(movie)
        } else {
            movieDao.updateMovie(movie)
            movie.id
        }
    }
    suspend fun deleteMovie(id: Long) = withContext(Dispatchers.IO) {
        movieDao.deleteMovieById(id)
    }
    fun searchMovies(query: String): Flow<List<MovieEntity>> = movieDao.searchMovies(query)
}

class SeriesRepository(private val seriesDao: SeriesDao) {
    fun getAllPublishedSeries(): Flow<List<SeriesEntity>> = seriesDao.getAllPublishedSeries()
    fun getAllSeries(): Flow<List<SeriesEntity>> = seriesDao.getAllSeries()
    fun getSeriesById(id: Long): Flow<SeriesEntity?> = seriesDao.getSeriesById(id)
    suspend fun getSeriesByIdOnce(id: Long): SeriesEntity? = seriesDao.getSeriesByIdOnce(id)

    suspend fun saveSeries(series: SeriesEntity): Long = withContext(Dispatchers.IO) {
        if (series.id == 0L) {
            seriesDao.insertSeries(series)
        } else {
            seriesDao.updateSeries(series)
            series.id
        }
    }

    suspend fun deleteSeries(id: Long) = withContext(Dispatchers.IO) {
        seriesDao.deleteEpisodesBySeriesId(id)
        seriesDao.deleteSeasonsBySeriesId(id)
        seriesDao.deleteSeriesById(id)
    }

    fun getSeasons(seriesId: Long): Flow<List<SeasonEntity>> = seriesDao.getSeasonsBySeries(seriesId)

    suspend fun addSeason(seriesId: Long, seasonNumber: Int, title: String): Long = withContext(Dispatchers.IO) {
        seriesDao.insertSeason(
            SeasonEntity(
                seriesId = seriesId,
                seasonNumber = seasonNumber,
                seasonTitle = title
            )
        )
    }

    suspend fun deleteSeason(seasonId: Long) = withContext(Dispatchers.IO) {
        seriesDao.deleteSeasonById(seasonId)
    }

    fun getEpisodes(seasonId: Long, publishedOnly: Boolean = true): Flow<List<EpisodeEntity>> {
        return if (publishedOnly) {
            seriesDao.getPublishedEpisodesBySeason(seasonId)
        } else {
            seriesDao.getEpisodesBySeason(seasonId)
        }
    }

    fun getEpisodeById(episodeId: Long): Flow<EpisodeEntity?> = seriesDao.getEpisodeById(episodeId)
    suspend fun getEpisodeByIdOnce(episodeId: Long): EpisodeEntity? = seriesDao.getEpisodeByIdOnce(episodeId)

    suspend fun getNextEpisode(seriesId: Long, seasonId: Long, currentEpisodeNumber: Int): EpisodeEntity? =
        withContext(Dispatchers.IO) {
            seriesDao.getNextEpisode(seriesId, seasonId, currentEpisodeNumber)
        }

    suspend fun saveEpisode(episode: EpisodeEntity): Long = withContext(Dispatchers.IO) {
        if (episode.id == 0L) {
            seriesDao.insertEpisode(episode)
        } else {
            seriesDao.updateEpisode(episode)
            episode.id
        }
    }

    suspend fun deleteEpisode(episodeId: Long) = withContext(Dispatchers.IO) {
        seriesDao.deleteEpisodeById(episodeId)
    }

    fun searchSeries(query: String): Flow<List<SeriesEntity>> = seriesDao.searchSeries(query)
}

class AuthRepository(private val userDao: UserDao) {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
        if (user == null) {
            return@withContext Result.failure(IllegalArgumentException("No account found with this email"))
        }
        val hashed = hashPassword(password)
        if (user.passwordHash != hashed) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password"))
        }
        _currentUser.value = user
        Result.success(user)
    }

    suspend fun signup(
        displayName: String,
        email: String,
        password: String,
        role: String = "USER"
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Invalid email address"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("Email is already registered"))
        }
        val hashed = hashPassword(password)
        val newUser = UserEntity(
            email = trimmedEmail,
            passwordHash = hashed,
            displayName = displayName.ifBlank { "Movie Fan" },
            role = role
        )
        val id = userDao.insertUser(newUser)
        val created = newUser.copy(id = id)
        _currentUser.value = created
        Result.success(created)
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun getAdminCount(): Int = withContext(Dispatchers.IO) {
        userDao.getAdminCount()
    }
}

class UserActivityRepository(
    private val watchHistoryDao: WatchHistoryDao,
    private val watchlistDao: WatchlistDao
) {
    fun getUserHistory(userId: Long): Flow<List<WatchHistoryEntity>> =
        watchHistoryDao.getUserHistory(userId)

    fun getContinueWatching(userId: Long): Flow<List<WatchHistoryEntity>> =
        watchHistoryDao.getContinueWatching(userId)

    suspend fun saveWatchProgress(
        userId: Long,
        contentId: Long,
        contentType: String,
        seriesId: Long = 0,
        title: String,
        subtitle: String = "",
        posterUri: String = "",
        videoUri: String = "",
        progressMs: Long,
        durationMs: Long
    ) = withContext(Dispatchers.IO) {
        if (userId <= 0) return@withContext
        val existing = watchHistoryDao.getProgress(userId, contentId, contentType)
        val entity = WatchHistoryEntity(
            id = existing?.id ?: 0L,
            userId = userId,
            contentId = contentId,
            contentType = contentType,
            seriesId = seriesId,
            title = title,
            subtitle = subtitle,
            posterUri = posterUri,
            videoUri = videoUri,
            progressMs = progressMs,
            durationMs = durationMs,
            updatedAt = System.currentTimeMillis()
        )
        watchHistoryDao.upsertProgress(entity)
    }

    suspend fun getSavedProgress(userId: Long, contentId: Long, contentType: String): WatchHistoryEntity? =
        withContext(Dispatchers.IO) {
            if (userId <= 0) null else watchHistoryDao.getProgress(userId, contentId, contentType)
        }

    suspend fun clearHistory(userId: Long) = withContext(Dispatchers.IO) {
        watchHistoryDao.clearUserHistory(userId)
    }

    fun getUserWatchlist(userId: Long): Flow<List<WatchlistEntity>> =
        watchlistDao.getUserWatchlist(userId)

    fun isInWatchlist(userId: Long, contentId: Long, contentType: String): Flow<Boolean> =
        watchlistDao.isInWatchlist(userId, contentId, contentType)

    suspend fun toggleWatchlist(userId: Long, contentId: Long, contentType: String) =
        withContext(Dispatchers.IO) {
            if (userId <= 0) return@withContext
            val inWatchlist = watchlistDao.isInWatchlist(userId, contentId, contentType).firstOrNull() ?: false
            if (inWatchlist) {
                watchlistDao.removeFromWatchlist(userId, contentId, contentType)
            } else {
                watchlistDao.insertWatchlist(
                    WatchlistEntity(
                        userId = userId,
                        contentId = contentId,
                        contentType = contentType
                    )
                )
            }
        }
}
