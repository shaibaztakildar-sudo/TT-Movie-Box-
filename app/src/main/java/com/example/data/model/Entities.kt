package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val posterUri: String = "",
    val backdropUri: String = "",
    val videoUri: String = "",
    val trailerUri: String = "",
    val subtitleUri: String = "",
    val genre: String = "Action",
    val language: String = "Hindi",
    val releaseYear: Int = 2024,
    val durationMinutes: Int = 120,
    val rating: Float = 8.0f,
    val cast: String = "",
    val director: String = "",
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val posterUri: String = "",
    val backdropUri: String = "",
    val genre: String = "Drama",
    val language: String = "Hindi",
    val releaseYear: Int = 2024,
    val rating: Float = 8.2f,
    val cast: String = "",
    val director: String = "",
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "seasons",
    indices = [Index(value = ["seriesId"])]
)
data class SeasonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val seasonNumber: Int,
    val seasonTitle: String
)

@Entity(
    tableName = "episodes",
    indices = [Index(value = ["seriesId"]), Index(value = ["seasonId"])]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val seasonId: Long,
    val episodeNumber: Int,
    val title: String,
    val description: String = "",
    val thumbnailUri: String = "",
    val videoUri: String = "",
    val subtitleUri: String = "",
    val durationMinutes: Int = 45,
    val isPublished: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val role: String = "USER", // "ADMIN" or "USER"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watch_history",
    indices = [Index(value = ["userId", "contentId", "contentType"], unique = true)]
)
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val contentId: Long,
    val contentType: String, // "MOVIE" or "EPISODE"
    val seriesId: Long = 0,
    val title: String,
    val subtitle: String = "",
    val posterUri: String = "",
    val videoUri: String = "",
    val progressMs: Long = 0,
    val durationMs: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watchlist",
    indices = [Index(value = ["userId", "contentId", "contentType"], unique = true)]
)
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val contentId: Long,
    val contentType: String, // "MOVIE" or "SERIES"
    val addedAt: Long = System.currentTimeMillis()
)
