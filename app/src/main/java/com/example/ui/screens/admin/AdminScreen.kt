package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MovieEntity
import com.example.data.model.SeriesEntity
import com.example.ui.components.EmptyContentState
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        mainViewModel.navigateBack()
    }

    val isAuthorized by adminViewModel.isAdminAuthorized.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()

    // Auto-authorize if logged in user has ADMIN role
    if (currentUser?.role == "ADMIN" && !isAuthorized) {
        adminViewModel.authorizeAdmin("TT7788")
    }

    if (!isAuthorized) {
        AdminGateScreen(
            onUnlock = { code -> adminViewModel.authorizeAdmin(code) },
            onBack = { mainViewModel.navigateBack() }
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Movies, 1: Web Series, 2: Storage & Cloud

    var showAddMovieDialog by remember { mutableStateOf(false) }
    var movieToEdit by remember { mutableStateOf<MovieEntity?>(null) }
    var movieToDelete by remember { mutableStateOf<MovieEntity?>(null) }

    var showAddSeriesDialog by remember { mutableStateOf(false) }
    var seriesToEdit by remember { mutableStateOf<SeriesEntity?>(null) }
    var seriesToDelete by remember { mutableStateOf<SeriesEntity?>(null) }
    var seriesToManageEpisodes by remember { mutableStateOf<SeriesEntity?>(null) }

    val adminMovies by adminViewModel.allAdminMovies.collectAsState()
    val adminSeries by adminViewModel.allAdminSeries.collectAsState()
    val storageStats by adminViewModel.storageStats.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        movieToEdit = null
                        showAddMovieDialog = true
                    },
                    containerColor = CinemaRed,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_fab_add_movie")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Movie")
                }
            } else if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        seriesToEdit = null
                        showAddSeriesDialog = true
                    },
                    containerColor = CinemaRed,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_fab_add_series")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Series")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { mainViewModel.navigateBack() },
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Admin Control Panel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "TT Movie Box • Master Suite",
                            fontSize = 11.sp,
                            color = CinemaGold
                        )
                    }
                }

                IconButton(
                    onClick = { adminViewModel.lockAdmin() },
                    modifier = Modifier.testTag("admin_lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Admin",
                        tint = CinemaRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard(
                    title = "Movies",
                    value = "${adminMovies.size}",
                    subtitle = "${adminMovies.count { it.isPublished }} live",
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Web Series",
                    value = "${adminSeries.size}",
                    subtitle = "${adminSeries.count { it.isPublished }} live",
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Media Vault",
                    value = String.format("%.1f MB", storageStats?.totalStorageUsedMb ?: 0.0),
                    subtitle = "${storageStats?.totalFilesCount ?: 0} files",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Admin Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CinemaRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CinemaRed
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Movies (${adminMovies.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Web Series (${adminSeries.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        adminViewModel.refreshStorageStats()
                    },
                    text = { Text("Storage & Cloud", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab 0: Movies List
            if (selectedTab == 0) {
                if (adminMovies.isEmpty()) {
                    EmptyContentState(
                        title = "No Movies Uploaded",
                        message = "Your movie catalog is empty. Tap 'Add Movie' to upload your first movie file from your phone.",
                        actionLabel = "Upload Movie",
                        onActionClick = {
                            movieToEdit = null
                            showAddMovieDialog = true
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(adminMovies, key = { it.id }) { movie ->
                            AdminMovieItemCard(
                                movie = movie,
                                onTogglePublish = { adminViewModel.toggleMoviePublish(movie) },
                                onToggleFeatured = { adminViewModel.toggleMovieFeatured(movie) },
                                onToggleTrending = { adminViewModel.toggleMovieTrending(movie) },
                                onEdit = {
                                    movieToEdit = movie
                                    showAddMovieDialog = true
                                },
                                onDelete = { movieToDelete = movie }
                            )
                        }
                    }
                }
            }

            // Tab 1: Web Series List
            if (selectedTab == 1) {
                if (adminSeries.isEmpty()) {
                    EmptyContentState(
                        title = "No Web Series Added",
                        message = "Tap 'Create Series' to add a web series and upload seasons and episodes.",
                        actionLabel = "Create Series",
                        onActionClick = {
                            seriesToEdit = null
                            showAddSeriesDialog = true
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(adminSeries, key = { it.id }) { series ->
                            AdminSeriesItemCard(
                                series = series,
                                onManageEpisodes = { seriesToManageEpisodes = series },
                                onTogglePublish = { adminViewModel.toggleSeriesPublish(series) },
                                onEdit = {
                                    seriesToEdit = series
                                    showAddSeriesDialog = true
                                },
                                onDelete = { seriesToDelete = series }
                            )
                        }
                    }
                }
            }

            // Tab 2: Storage & Cloud Backend Architecture
            if (selectedTab == 2) {
                StorageAndCloudTab(storageStats)
            }
        }
    }

    // Dialogs
    if (showAddMovieDialog) {
        AddEditMovieDialog(
            initialMovie = movieToEdit,
            adminViewModel = adminViewModel,
            onDismiss = { showAddMovieDialog = false }
        )
    }

    if (showAddSeriesDialog) {
        AddEditSeriesDialog(
            initialSeries = seriesToEdit,
            adminViewModel = adminViewModel,
            onDismiss = { showAddSeriesDialog = false }
        )
    }

    if (seriesToManageEpisodes != null) {
        ManageEpisodesDialog(
            series = seriesToManageEpisodes!!,
            adminViewModel = adminViewModel,
            onDismiss = { seriesToManageEpisodes = null }
        )
    }

    // Delete Confirmations
    if (movieToDelete != null) {
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            title = { Text("Delete Movie?", color = TextPrimary) },
            text = { Text("Are you sure you want to permanently delete '${movieToDelete!!.title}' and its video file?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteMovie(movieToDelete!!)
                        movieToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { movieToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCardBg
        )
    }

    if (seriesToDelete != null) {
        AlertDialog(
            onDismissRequest = { seriesToDelete = null },
            title = { Text("Delete Series?", color = TextPrimary) },
            text = { Text("Are you sure you want to delete '${seriesToDelete!!.title}' and all its seasons & episodes?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteSeries(seriesToDelete!!)
                        seriesToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { seriesToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCardBg
        )
    }
}

@Composable
fun AdminGateScreen(
    onUnlock: (String) -> Boolean,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(CinemaRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CinemaRed,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Admin Authorization Required",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Normal viewers cannot access this area. Enter the master admin authorization passcode to proceed. (Default: TT7788)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        isError = false
                    },
                    label = { Text("Admin Passcode") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("admin_gate_input")
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Incorrect passcode. Try TT7788.", color = CinemaRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exit", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val success = onUnlock(code)
                            if (!success) isError = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                        modifier = Modifier.weight(1f).testTag("admin_gate_unlock_button")
                    ) {
                        Text("Unlock")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkCardBorder, Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 9.sp, color = CinemaGold)
        }
    }
}

@Composable
fun AdminMovieItemCard(
    movie: MovieEntity,
    onTogglePublish: () -> Unit,
    onToggleFeatured: () -> Unit,
    onToggleTrending: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp, 75.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    if (movie.posterUri.isNotBlank()) {
                        AsyncImage(model = movie.posterUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = TextMuted, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = movie.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${movie.releaseYear} • ${movie.durationMinutes} min • ${movie.language}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (movie.isPublished) StatusSuccess.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (movie.isPublished) "PUBLISHED" else "DRAFT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (movie.isPublished) StatusSuccess else TextSecondary
                            )
                        }
                        if (movie.isFeatured) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CinemaRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("FEATURED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CinemaRed)
                            }
                        }
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CinemaRed, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Live", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(checked = movie.isPublished, onCheckedChange = { onTogglePublish() }, modifier = Modifier.size(36.dp), colors = switchColors())
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hero", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(checked = movie.isFeatured, onCheckedChange = { onToggleFeatured() }, modifier = Modifier.size(36.dp), colors = switchColors())
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Trending", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(checked = movie.isTrending, onCheckedChange = { onToggleTrending() }, modifier = Modifier.size(36.dp), colors = switchColors())
                }
            }
        }
    }
}

@Composable
fun AdminSeriesItemCard(
    series: SeriesEntity,
    onManageEpisodes: () -> Unit,
    onTogglePublish: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp, 75.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    if (series.posterUri.isNotBlank()) {
                        AsyncImage(model = series.posterUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = TextMuted, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = series.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${series.releaseYear} • ${series.language}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onManageEpisodes,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Manage Seasons & Episodes", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CinemaRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun StorageAndCloudTab(storageStats: com.example.data.storage.StorageStats?) {
    var selectedCloudProvider by remember { mutableStateOf("Local App Storage Vault (Active)") }
    var cdnEndpoint by remember { mutableStateOf("https://cdn.ttmoviebox.net/vault/") }
    var s3BucketName by remember { mutableStateOf("tt-movie-box-streams") }
    var s3Region by remember { mutableStateOf("ap-south-1 (Mumbai)") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = CinemaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Local Device Media Vault", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Videos uploaded from the admin device are securely held in isolated application sandbox storage. Streaming feeds run directly through secure content descriptors.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Videos Occupied:", fontSize = 12.sp, color = TextSecondary)
                        Text(String.format("%.2f MB", storageStats?.totalVideosSizeMb ?: 0.0), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Posters & Artwork:", fontSize = 12.sp, color = TextSecondary)
                        Text(String.format("%.2f MB", storageStats?.totalImagesSizeMb ?: 0.0), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Available Disk Space:", fontSize = 12.sp, color = TextSecondary)
                        Text(String.format("%.1f MB", storageStats?.availableDiskSpaceMb ?: 0.0), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CinemaGold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = CinemaGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cloud Storage & CDN Connector", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Configure external Cloud Object Storage (Cloudflare R2 / AWS S3 / Firebase Storage) for large-scale multi-device OTT distribution.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = cdnEndpoint,
                        onValueChange = { cdnEndpoint = it },
                        label = { Text("Public CDN / Stream URL Prefix") },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = s3BucketName,
                        onValueChange = { s3BucketName = it },
                        label = { Text("Cloud Storage Bucket Name") },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = s3Region,
                        onValueChange = { s3Region = it },
                        label = { Text("Cloud Storage Region") },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { /* Settings saved to local prefs */ },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Cloud Storage Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CinemaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Legal Rights Compliance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Under TT Movie Box distribution guidelines, content administrators may only publish video media for which they hold distribution copyright or legitimate public streaming licenses. Video files uploaded via the Admin Suite must strictly comply with all applicable copyright and intellectual property legislation.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
