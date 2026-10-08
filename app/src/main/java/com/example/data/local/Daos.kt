package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EpisodeEntity
import com.example.data.model.MovieEntity
import com.example.data.model.SeasonEntity
import com.example.data.model.SeriesEntity
import com.example.data.model.UserEntity
import com.example.data.model.WatchHistoryEntity
import com.example.data.model.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun getAllPublishedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id")
    fun getMovieById(id: Long): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieByIdOnce(id: Long): MovieEntity?

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND isFeatured = 1 ORDER BY createdAt DESC")
    fun getFeaturedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND isTrending = 1 ORDER BY createdAt DESC")
    fun getTrendingMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 ORDER BY createdAt DESC LIMIT :limit")
    fun getLatestMovies(limit: Int = 10): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity): Long

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: Long)

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND (title LIKE '%' || :query || '%' OR `cast` LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' OR language LIKE '%' || :query || '%' OR CAST(releaseYear AS TEXT) LIKE '%' || :query || '%')")
    fun searchMovies(query: String): Flow<List<MovieEntity>>
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun getAllPublishedSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series ORDER BY createdAt DESC")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :id")
    fun getSeriesById(id: Long): Flow<SeriesEntity?>

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getSeriesByIdOnce(id: Long): SeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity): Long

    @Update
    suspend fun updateSeries(series: SeriesEntity)

    @Query("DELETE FROM series WHERE id = :id")
    suspend fun deleteSeriesById(id: Long)

    @Query("SELECT * FROM seasons WHERE seriesId = :seriesId ORDER BY seasonNumber ASC")
    fun getSeasonsBySeries(seriesId: Long): Flow<List<SeasonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity): Long

    @Query("DELETE FROM seasons WHERE id = :seasonId")
    suspend fun deleteSeasonById(seasonId: Long)

    @Query("DELETE FROM seasons WHERE seriesId = :seriesId")
    suspend fun deleteSeasonsBySeriesId(seriesId: Long)

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId ORDER BY sortOrder ASC, episodeNumber ASC")
    fun getEpisodesBySeason(seasonId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId AND isPublished = 1 ORDER BY sortOrder ASC, episodeNumber ASC")
    fun getPublishedEpisodesBySeason(seasonId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    fun getEpisodeById(episodeId: Long): Flow<EpisodeEntity?>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisodeByIdOnce(episodeId: Long): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND seasonId = :seasonId AND episodeNumber > :currentNumber AND isPublished = 1 ORDER BY episodeNumber ASC LIMIT 1")
    suspend fun getNextEpisode(seriesId: Long, seasonId: Long, currentNumber: Int): EpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity): Long

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE id = :episodeId")
    suspend fun deleteEpisodeById(episodeId: Long)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesBySeriesId(seriesId: Long)

    @Query("SELECT * FROM series WHERE isPublished = 1 AND (title LIKE '%' || :query || '%' OR `cast` LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' OR language LIKE '%' || :query || '%' OR CAST(releaseYear AS TEXT) LIKE '%' || :query || '%')")
    fun searchSeries(query: String): Flow<List<SeriesEntity>>
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    suspend fun getAdminCount(): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUsersCount(): Int
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getUserHistory(userId: Long): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE userId = :userId AND progressMs > 3000 AND (durationMs == 0 OR (progressMs * 1.0 / durationMs) < 0.95) ORDER BY updatedAt DESC LIMIT 10")
    fun getContinueWatching(userId: Long): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(history: WatchHistoryEntity): Long

    @Query("SELECT * FROM watch_history WHERE userId = :userId AND contentId = :contentId AND contentType = :contentType LIMIT 1")
    suspend fun getProgress(userId: Long, contentId: Long, contentType: String): WatchHistoryEntity?

    @Query("DELETE FROM watch_history WHERE userId = :userId")
    suspend fun clearUserHistory(userId: Long)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getUserWatchlist(userId: Long): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND contentId = :contentId AND contentType = :contentType)")
    fun isInWatchlist(userId: Long, contentId: Long, contentType: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity): Long

    @Query("DELETE FROM watchlist WHERE userId = :userId AND contentId = :contentId AND contentType = :contentType")
    suspend fun removeFromWatchlist(userId: Long, contentId: Long, contentType: String)
}
