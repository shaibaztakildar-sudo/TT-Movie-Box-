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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.EmptyContentState
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val watchlist by viewModel.userWatchlist.collectAsState()
    val history by viewModel.userHistory.collectAsState()
    val allMovies by viewModel.allPublishedMovies.collectAsState()
    val allSeries by viewModel.allPublishedSeries.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Watchlist, 1: History
    var showAdminPasscodeDialog by remember { mutableStateOf(false) }
    var adminPasscodeInput by remember { mutableStateOf("") }
    var adminPasscodeError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "My Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkCardBorder, Color.Transparent)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CinemaRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.displayName ?: "Guest Viewer",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser?.email ?: "Sign in to save Watchlist & Progress",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CinemaGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (currentUser?.role == "ADMIN") "ADMINISTRATOR" else "100% FREE OTT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CinemaGold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentUser == null) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.Auth) },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("profile_signin_button")
                            ) {
                                Text("Sign In / Register", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("profile_logout_button")
                            ) {
                                Text("Logout", color = CinemaRed)
                            }
                        }

                        // Admin Portal Button
                        OutlinedButton(
                            onClick = {
                                if (currentUser?.role == "ADMIN") {
                                    viewModel.navigateTo(Screen.Admin)
                                } else {
                                    showAdminPasscodeDialog = true
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("profile_admin_panel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin Panel", color = CinemaGold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs: Watchlist & Watch History
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
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watchlist (${watchlist.size})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watch History (${history.size})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tab Content: Watchlist
        if (selectedTab == 0) {
            if (currentUser == null) {
                item {
                    EmptyContentState(
                        title = "Sign In to View Watchlist",
                        message = "Create an account or sign in to save movies and series to your personalized watchlist.",
                        actionLabel = "Sign In",
                        onActionClick = { viewModel.navigateTo(Screen.Auth) }
                    )
                }
            } else if (watchlist.isEmpty()) {
                item {
                    EmptyContentState(
                        title = "Your Watchlist is Empty",
                        message = "Add movies and web series to your watchlist using the Bookmark button on any title."
                    )
                }
            } else {
                items(watchlist, key = { "wl_${it.id}" }) { item ->
                    val movie = allMovies.find { it.id == item.contentId && item.contentType == "MOVIE" }
                    val series = allSeries.find { it.id == item.contentId && item.contentType == "SERIES" }

                    val title = movie?.title ?: series?.title ?: "Saved Title"
                    val poster = movie?.posterUri ?: series?.posterUri ?: ""
                    val subtitle = if (movie != null) "${movie.releaseYear} • Movie" else "${series?.releaseYear} • Series"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (movie != null) viewModel.selectMovie(movie.id)
                                else if (series != null) viewModel.selectSeries(series.id)
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(85.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E202E))
                            ) {
                                if (poster.isNotBlank()) {
                                    AsyncImage(
                                        model = poster,
                                        contentDescription = title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { viewModel.toggleWatchlist(item.contentId, item.contentType) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Tab Content: Watch History
        if (selectedTab == 1) {
            if (currentUser == null) {
                item {
                    EmptyContentState(
                        title = "Sign In to Track Progress",
                        message = "Sign in to save your viewing history and resume partially watched movies from anywhere.",
                        actionLabel = "Sign In",
                        onActionClick = { viewModel.navigateTo(Screen.Auth) }
                    )
                }
            } else if (history.isEmpty()) {
                item {
                    EmptyContentState(
                        title = "No Watch History",
                        message = "Movies and series you watch will appear here with your saved resume positions."
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear History", color = CinemaRed, fontSize = 12.sp)
                        }
                    }
                }

                items(history, key = { "h_${it.id}" }) { item ->
                    val progressRatio = if (item.durationMs > 0) {
                        (item.progressMs.toFloat() / item.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (item.contentType == "MOVIE") {
                                    viewModel.selectMovie(item.contentId)
                                } else if (item.seriesId > 0) {
                                    viewModel.selectSeries(item.seriesId)
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(85.dp)
                                    .height(55.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E202E))
                            ) {
                                if (item.posterUri.isNotBlank()) {
                                    AsyncImage(
                                        model = item.posterUri,
                                        contentDescription = item.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .align(Alignment.BottomCenter),
                                    color = CinemaRed,
                                    trackColor = Color.White.copy(alpha = 0.2f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${(progressRatio * 100).toInt()}% watched",
                                    fontSize = 11.sp,
                                    color = CinemaGold
                                )
                            }
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = CinemaRed)
                        }
                    }
                }
            }
        }
    }

    // Admin Passcode Dialog for Protected Admin Access
    if (showAdminPasscodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminPasscodeDialog = false
                adminPasscodeError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CinemaGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Access Verification", color = TextPrimary, fontSize = 17.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter the TT Movie Box Admin passcode to open the administrative panel. (Default: TT7788)",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPasscodeInput,
                        onValueChange = {
                            adminPasscodeInput = it
                            adminPasscodeError = false
                        },
                        placeholder = { Text("Admin Passcode", fontSize = 13.sp) },
                        singleLine = true,
                        isError = adminPasscodeError,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = CinemaRed,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("admin_passcode_input")
                    )
                    if (adminPasscodeError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Incorrect admin passcode. Try 'TT7788'.", color = CinemaRed, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminPasscodeInput.trim() == "TT7788" || adminPasscodeInput.trim() == "admin123") {
                            showAdminPasscodeDialog = false
                            adminPasscodeInput = ""
                            adminPasscodeError = false
                            viewModel.navigateTo(Screen.Admin)
                        } else {
                            adminPasscodeError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                ) {
                    Text("Unlock Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPasscodeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCardBg
        )
    }
}
