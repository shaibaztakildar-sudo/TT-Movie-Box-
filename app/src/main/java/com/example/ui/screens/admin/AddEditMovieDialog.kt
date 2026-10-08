package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.MovieEntity
import com.example.data.storage.UploadState
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AddEditMovieDialog(
    initialMovie: MovieEntity? = null,
    adminViewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialMovie?.title ?: "") }
    var description by remember { mutableStateOf(initialMovie?.description ?: "") }
    var genre by remember { mutableStateOf(initialMovie?.genre ?: "Action, Drama") }
    var language by remember { mutableStateOf(initialMovie?.language ?: "Hindi") }
    var releaseYear by remember { mutableIntStateOf(initialMovie?.releaseYear ?: 2024) }
    var durationMinutes by remember { mutableIntStateOf(initialMovie?.durationMinutes ?: 120) }
    var rating by remember { mutableFloatStateOf(initialMovie?.rating ?: 8.0f) }
    var cast by remember { mutableStateOf(initialMovie?.cast ?: "") }
    var director by remember { mutableStateOf(initialMovie?.director ?: "") }

    var videoUri by remember { mutableStateOf(initialMovie?.videoUri ?: "") }
    var posterUri by remember { mutableStateOf(initialMovie?.posterUri ?: "") }
    var backdropUri by remember { mutableStateOf(initialMovie?.backdropUri ?: "") }
    var trailerUri by remember { mutableStateOf(initialMovie?.trailerUri ?: "") }
    var subtitleUri by remember { mutableStateOf(initialMovie?.subtitleUri ?: "") }

    var isFeatured by remember { mutableStateOf(initialMovie?.isFeatured ?: false) }
    var isTrending by remember { mutableStateOf(initialMovie?.isTrending ?: false) }
    var isPublished by remember { mutableStateOf(initialMovie?.isPublished ?: true) }
    var hasLegalRightCertified by remember { mutableStateOf(initialMovie != null) }

    var validationError by remember { mutableStateOf<String?>(null) }

    val videoUploadState by adminViewModel.videoUploadState.collectAsState()
    val posterUploadState by adminViewModel.posterUploadState.collectAsState()
    val backdropUploadState by adminViewModel.backdropUploadState.collectAsState()
    val subtitleUploadState by adminViewModel.subtitleUploadState.collectAsState()

    // Real Android file pickers
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadVideo(uri) { savedPath ->
                videoUri = savedPath
            }
        }
    }

    val posterPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadPoster(uri) { savedPath ->
                posterUri = savedPath
            }
        }
    }

    val backdropPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadBackdrop(uri) { savedPath ->
                backdropUri = savedPath
            }
        }
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadSubtitle(uri) { savedPath ->
                subtitleUri = savedPath
            }
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
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialMovie == null) "Add New Movie" else "Edit Movie",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = {
                            adminViewModel.resetUploadStates()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Movie Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Movie Title *") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_movie_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Movie Synopsis / Description *") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_movie_desc_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // VIDEO UPLOAD SECTION (Mandatory feature from prompt)
                    Text(
                        text = "Movie Video File *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload movie file directly from phone files/gallery (MP4, MKV, WEBM). Securely copied to media vault.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VideoFile,
                                        contentDescription = null,
                                        tint = if (videoUri.isNotBlank()) StatusSuccess else CinemaRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (videoUri.isNotBlank()) "Video Attached" else "No video selected",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        if (videoUri.isNotBlank()) {
                                            Text(
                                                text = videoUri.substringAfterLast("/"),
                                                fontSize = 10.sp,
                                                color = TextSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { videoPickerLauncher.launch("video/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.testTag("admin_upload_movie_video_button")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload Video", fontSize = 12.sp)
                                }
                            }

                            // Progress Indicator
                            when (val state = videoUploadState) {
                                is UploadState.Progress -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Uploading: ${state.percent}% (${state.bytesCopied / (1024 * 1024)} MB)",
                                        fontSize = 11.sp,
                                        color = CinemaGold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { state.percent / 100f },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = CinemaRed
                                    )
                                }
                                is UploadState.Success -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Stored successfully: ${state.sizeDisplay}", fontSize = 11.sp, color = StatusSuccess)
                                    }
                                }
                                is UploadState.Error -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = StatusError, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(state.message, fontSize = 11.sp, color = StatusError)
                                    }
                                }
                                UploadState.Idle -> {}
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Optional direct stream URL field
                            OutlinedTextField(
                                value = videoUri,
                                onValueChange = { videoUri = it },
                                label = { Text("Or Direct Video URI / Stream URL") },
                                singleLine = true,
                                shape = RoundedCornerShape(6.dp),
                                colors = outlinedFieldColors(),
                                modifier = Modifier.fillMaxWidth().testTag("admin_video_uri_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // POSTER & BACKDROP UPLOAD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Poster Upload
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Movie Poster", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(60.dp, 80.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable { posterPickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (posterUri.isNotBlank()) {
                                        AsyncImage(model = posterUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { posterPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.testTag("admin_upload_poster_button")
                                ) {
                                    Text("Pick Poster", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        // Backdrop Upload
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Backdrop Banner", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(90.dp, 55.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable { backdropPickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (backdropUri.isNotBlank()) {
                                        AsyncImage(model = backdropUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { backdropPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.testTag("admin_upload_backdrop_button")
                                ) {
                                    Text("Pick Banner", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Genre & Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = genre,
                            onValueChange = { genre = it },
                            label = { Text("Genre") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = language,
                            onValueChange = { language = it },
                            label = { Text("Language") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Year, Duration, Rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = releaseYear.toString(),
                            onValueChange = { releaseYear = it.toIntOrNull() ?: releaseYear },
                            label = { Text("Year") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durationMinutes.toString(),
                            onValueChange = { durationMinutes = it.toIntOrNull() ?: durationMinutes },
                            label = { Text("Minutes") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rating.toString(),
                            onValueChange = { rating = it.toFloatOrNull() ?: rating },
                            label = { Text("Rating (1-10)") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cast & Director
                    OutlinedTextField(
                        value = cast,
                        onValueChange = { cast = it },
                        label = { Text("Cast (e.g. Shah Rukh Khan, Deepika Padukone)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = director,
                        onValueChange = { director = it },
                        label = { Text("Director") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Trailer & Subtitles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = trailerUri,
                            onValueChange = { trailerUri = it },
                            label = { Text("Trailer Video URI (Optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { subtitlePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = CinemaGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (subtitleUri.isNotBlank()) "Sub Added" else "Add SRT", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Switches: Featured, Trending, Publish
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Featured on Home Hero", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = isFeatured,
                            onCheckedChange = { isFeatured = it },
                            colors = switchColors()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trending Now Section", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = isTrending,
                            onCheckedChange = { isTrending = it },
                            colors = switchColors()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Publish (Visible to Users)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isPublished,
                            onCheckedChange = { isPublished = it },
                            colors = switchColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // LEGAL RIGHT CERTIFICATION (Mandate from prompt)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasLegalRightCertified,
                            onCheckedChange = { hasLegalRightCertified = it },
                            colors = CheckboxDefaults.colors(checkedColor = CinemaRed),
                            modifier = Modifier.testTag("admin_legal_checkbox")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I certify that I hold the legal distribution and streaming rights for this movie title.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = validationError ?: "",
                            color = CinemaRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Bottom Save Action
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            validationError = "Please enter movie title"
                            return@Button
                        }
                        if (videoUri.isBlank()) {
                            validationError = "Please upload or provide video file URI"
                            return@Button
                        }
                        if (!hasLegalRightCertified) {
                            validationError = "Please certify legal streaming rights"
                            return@Button
                        }

                        val movieToSave = (initialMovie ?: MovieEntity(
                            title = title,
                            description = description
                        )).copy(
                            title = title.trim(),
                            description = description.trim(),
                            posterUri = posterUri.trim(),
                            backdropUri = backdropUri.trim(),
                            videoUri = videoUri.trim(),
                            trailerUri = trailerUri.trim(),
                            subtitleUri = subtitleUri.trim(),
                            genre = genre.trim(),
                            language = language.trim(),
                            releaseYear = releaseYear,
                            durationMinutes = durationMinutes,
                            rating = rating,
                            cast = cast.trim(),
                            director = director.trim(),
                            isFeatured = isFeatured,
                            isTrending = isTrending,
                            isPublished = isPublished
                        )

                        adminViewModel.saveMovie(movieToSave) {
                            adminViewModel.resetUploadStates()
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_save_movie_button")
                ) {
                    Text(
                        text = if (initialMovie == null) "Save & Add Movie" else "Update Movie",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DarkSurfaceVariant,
    unfocusedContainerColor = DarkSurfaceVariant,
    focusedBorderColor = CinemaRed,
    unfocusedBorderColor = DarkCardBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

@Composable
fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = CinemaRed,
    uncheckedThumbColor = TextMuted,
    uncheckedTrackColor = DarkSurfaceVariant
)
