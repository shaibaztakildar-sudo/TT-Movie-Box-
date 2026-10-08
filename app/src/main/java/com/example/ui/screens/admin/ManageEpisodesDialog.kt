package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.EpisodeEntity
import com.example.data.model.SeriesEntity
import com.example.data.storage.UploadState
import com.example.ui.components.EmptyContentState
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun ManageEpisodesDialog(
    series: SeriesEntity,
    adminViewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    val seasons by adminViewModel.seriesRepo.getSeasons(series.id)
        .collectAsState(initial = emptyList())

    var selectedSeasonId by remember { mutableStateOf<Long?>(null) }
    var showAddSeasonDialog by remember { mutableStateOf(false) }
    var newSeasonTitle by remember { mutableStateOf("") }

    var editingEpisode by remember { mutableStateOf<EpisodeEntity?>(null) }
    var showEpisodeForm by remember { mutableStateOf(false) }

    // If season not selected yet, auto-select first
    if (selectedSeasonId == null && seasons.isNotEmpty()) {
        selectedSeasonId = seasons.first().id
    }

    val episodes by if (selectedSeasonId != null) {
        adminViewModel.seriesRepo.getEpisodes(selectedSeasonId!!, publishedOnly = false)
            .collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<EpisodeEntity>()) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Manage Seasons & Episodes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = series.title,
                            fontSize = 13.sp,
                            color = CinemaGold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Seasons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seasons",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { showAddSeasonDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CinemaRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Season", color = CinemaRed, fontSize = 12.sp)
                    }
                }

                if (seasons.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No seasons created yet", color = TextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    adminViewModel.addSeason(series.id, 1, "Season 1")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                            ) {
                                Text("Create Season 1")
                            }
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(seasons, key = { it.id }) { s ->
                            FilterChip(
                                selected = selectedSeasonId == s.id,
                                onClick = { selectedSeasonId = s.id },
                                label = { Text(s.seasonTitle) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Episodes Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Episodes (${episodes.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (selectedSeasonId != null) {
                        Button(
                            onClick = {
                                editingEpisode = null
                                showEpisodeForm = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("admin_add_episode_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Episode", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (episodes.isEmpty()) {
                    EmptyContentState(
                        title = "No Episodes In This Season",
                        message = "Tap 'Add Episode' above to upload an episode video from phone files."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(episodes, key = { it.id }) { ep ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp, 40.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DarkSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (ep.thumbnailUri.isNotBlank()) {
                                            AsyncImage(model = ep.thumbnailUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Icon(Icons.Default.VideoFile, contentDescription = null, tint = TextMuted)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "EP ${ep.episodeNumber}: ${ep.title}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${ep.durationMinutes} min • ${if (ep.isPublished) "Published" else "Draft"}",
                                            fontSize = 11.sp,
                                            color = if (ep.isPublished) StatusSuccess else CinemaGold
                                        )
                                    }

                                    IconButton(onClick = {
                                        editingEpisode = ep
                                        showEpisodeForm = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(onClick = { adminViewModel.deleteEpisode(ep) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CinemaRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Season Modal
    if (showAddSeasonDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddSeasonDialog = false },
            title = { Text("Add Season", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newSeasonTitle,
                    onValueChange = { newSeasonTitle = it },
                    label = { Text("Season Title (e.g. Season 2)") },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val titleToAdd = newSeasonTitle.ifBlank { "Season ${seasons.size + 1}" }
                        adminViewModel.addSeason(series.id, seasons.size + 1, titleToAdd)
                        newSeasonTitle = ""
                        showAddSeasonDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSeasonDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCardBg
        )
    }

    // Episode Form Dialog (Upload video, thumbnail, metadata)
    if (showEpisodeForm && selectedSeasonId != null) {
        EpisodeFormDialog(
            seriesId = series.id,
            seasonId = selectedSeasonId!!,
            initialEpisode = editingEpisode,
            nextEpisodeNumber = episodes.size + 1,
            adminViewModel = adminViewModel,
            onDismiss = { showEpisodeForm = false }
        )
    }
}

@Composable
fun EpisodeFormDialog(
    seriesId: Long,
    seasonId: Long,
    initialEpisode: EpisodeEntity?,
    nextEpisodeNumber: Int,
    adminViewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    var epNumber by remember { mutableIntStateOf(initialEpisode?.episodeNumber ?: nextEpisodeNumber) }
    var epTitle by remember { mutableStateOf(initialEpisode?.title ?: "") }
    var epDesc by remember { mutableStateOf(initialEpisode?.description ?: "") }
    var duration by remember { mutableIntStateOf(initialEpisode?.durationMinutes ?: 45) }
    var videoUri by remember { mutableStateOf(initialEpisode?.videoUri ?: "") }
    var thumbUri by remember { mutableStateOf(initialEpisode?.thumbnailUri ?: "") }
    var subUri by remember { mutableStateOf(initialEpisode?.subtitleUri ?: "") }
    var isPublished by remember { mutableStateOf(initialEpisode?.isPublished ?: true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val videoUploadState by adminViewModel.videoUploadState.collectAsState()

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadVideo(uri) { videoUri = it }
        }
    }

    val thumbPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadPoster(uri) { thumbUri = it }
        }
    }

    Dialog(
        onDismissRequest = {
            adminViewModel.resetUploadStates()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialEpisode == null) "Add Episode" else "Edit Episode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = epNumber.toString(),
                            onValueChange = { epNumber = it.toIntOrNull() ?: epNumber },
                            label = { Text("EP Number") },
                            singleLine = true,
                            colors = outlinedFieldColors(),
                            modifier = Modifier.width(90.dp)
                        )
                        OutlinedTextField(
                            value = epTitle,
                            onValueChange = { epTitle = it },
                            label = { Text("Episode Title *") },
                            singleLine = true,
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f).testTag("admin_episode_title_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = epDesc,
                        onValueChange = { epDesc = it },
                        label = { Text("Episode Description") },
                        minLines = 2,
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = duration.toString(),
                        onValueChange = { duration = it.toIntOrNull() ?: duration },
                        label = { Text("Duration (Minutes)") },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real Episode Video Upload
                    Text("Episode Video File *", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CinemaGold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (videoUri.isNotBlank()) "Video Attached" else "No video selected",
                                    fontSize = 12.sp,
                                    color = if (videoUri.isNotBlank()) StatusSuccess else TextSecondary
                                )
                                Button(
                                    onClick = { videoPicker.launch("video/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.testTag("admin_upload_episode_video_button")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick Video", fontSize = 11.sp)
                                }
                            }

                            when (val state = videoUploadState) {
                                is UploadState.Progress -> {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(progress = { state.percent / 100f }, modifier = Modifier.fillMaxWidth(), color = CinemaRed)
                                    Text("Uploading: ${state.percent}%", fontSize = 10.sp, color = CinemaGold)
                                }
                                is UploadState.Success -> {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Stored: ${state.sizeDisplay}", fontSize = 10.sp, color = StatusSuccess)
                                }
                                else -> {}
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = videoUri,
                                onValueChange = { videoUri = it },
                                label = { Text("Or Direct Stream URL") },
                                singleLine = true,
                                colors = outlinedFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Thumbnail Upload
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Episode Thumbnail", fontSize = 13.sp, color = TextPrimary)
                        Button(
                            onClick = { thumbPicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(if (thumbUri.isNotBlank()) "Thumb Attached" else "Pick Image", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Publish Episode", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isPublished,
                            onCheckedChange = { isPublished = it },
                            colors = switchColors()
                        )
                    }

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMsg ?: "", color = CinemaRed, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                Button(
                    onClick = {
                        if (epTitle.isBlank()) {
                            errorMsg = "Please enter episode title"
                            return@Button
                        }
                        if (videoUri.isBlank()) {
                            errorMsg = "Please upload or specify video URI"
                            return@Button
                        }
                        val episodeToSave = (initialEpisode ?: EpisodeEntity(
                            seriesId = seriesId,
                            seasonId = seasonId,
                            episodeNumber = epNumber,
                            title = epTitle
                        )).copy(
                            seriesId = seriesId,
                            seasonId = seasonId,
                            episodeNumber = epNumber,
                            title = epTitle.trim(),
                            description = epDesc.trim(),
                            durationMinutes = duration,
                            videoUri = videoUri.trim(),
                            thumbnailUri = thumbUri.trim(),
                            subtitleUri = subUri.trim(),
                            isPublished = isPublished,
                            sortOrder = epNumber
                        )
                        adminViewModel.saveEpisode(episodeToSave) {
                            adminViewModel.resetUploadStates()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_save_episode_button")
                ) {
                    Text(if (initialEpisode == null) "Add Episode" else "Update Episode", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
