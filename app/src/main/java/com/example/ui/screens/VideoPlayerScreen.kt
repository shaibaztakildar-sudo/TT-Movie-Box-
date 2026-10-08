package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PlayerMediaArgs
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    args: PlayerMediaArgs,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isLandscape by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(args.initialPositionMs) }
    var duration by remember { mutableLongStateOf(0L) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    // User dragging / scrub state for seek bar
    var isDraggingSeek by remember { mutableStateOf(false) }
    var dragSeekPosition by remember { mutableLongStateOf(0L) }

    // Double-tap visual indicator state
    var doubleTapFeedback by remember { mutableStateOf<String?>(null) } // "LEFT" or "RIGHT"
    var feedbackTriggerCount by remember { mutableLongStateOf(0L) }

    // Dialog states
    var showQualityDialog by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf("1080p Full HD (Auto)") }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }

    var isMuted by remember { mutableStateOf(false) }
    var isSubtitlesEnabled by remember { mutableStateOf(args.subtitleUri.isNotBlank()) }
    var showNextEpisodePrompt by remember { mutableStateOf(false) }

    // ExoPlayer initialization
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Helper: Seek forward 10s
    fun seekForward10() {
        val rawDur = exoPlayer.duration
        val totalDur = if (rawDur > 0 && rawDur != C.TIME_UNSET) rawDur else duration
        val cur = exoPlayer.currentPosition
        val target = if (totalDur > 0) (cur + 10000L).coerceAtMost(totalDur) else cur + 10000L
        exoPlayer.seekTo(target)
        currentPosition = target
        doubleTapFeedback = "RIGHT"
        feedbackTriggerCount++
        viewModel.saveWatchProgress(args, target, totalDur)
    }

    // Helper: Seek backward 10s
    fun seekBackward10() {
        val rawDur = exoPlayer.duration
        val totalDur = if (rawDur > 0 && rawDur != C.TIME_UNSET) rawDur else duration
        val cur = exoPlayer.currentPosition
        val target = (cur - 10000L).coerceAtLeast(0L)
        exoPlayer.seekTo(target)
        currentPosition = target
        doubleTapFeedback = "LEFT"
        feedbackTriggerCount++
        viewModel.saveWatchProgress(args, target, totalDur)
    }

    // Hide double-tap indicator after 800ms
    LaunchedEffect(feedbackTriggerCount) {
        if (doubleTapFeedback != null) {
            delay(800)
            doubleTapFeedback = null
        }
    }

    // Set media item
    LaunchedEffect(args.videoUri) {
        if (args.videoUri.isBlank()) {
            playbackError = "Video file reference is empty. Please contact admin to upload video."
            return@LaunchedEffect
        }
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(args.videoUri))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (args.initialPositionMs > 0) {
                exoPlayer.seekTo(args.initialPositionMs)
                currentPosition = args.initialPositionMs
            }
        } catch (e: Exception) {
            playbackError = "Failed to load video: ${e.localizedMessage}"
        }
    }

    // Player event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0 && d != C.TIME_UNSET) {
                        duration = d
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    if (args.nextEpisodeId > 0) {
                        showNextEpisodePrompt = true
                    }
                }
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                val d = exoPlayer.duration
                if (d > 0 && d != C.TIME_UNSET) {
                    duration = d
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playbackError = "Playback error: ${error.localizedMessage ?: "Format not supported"}"
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            viewModel.saveWatchProgress(args, exoPlayer.currentPosition, exoPlayer.duration)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Periodic progress tracker & autosave (only updates UI position when user is NOT dragging slider)
    LaunchedEffect(isPlaying) {
        while (true) {
            if (exoPlayer.isPlaying) {
                val p = exoPlayer.currentPosition
                val d = exoPlayer.duration
                if (d > 0 && d != C.TIME_UNSET && d != duration) {
                    duration = d
                }
                if (!isDraggingSeek) {
                    currentPosition = p
                }
                viewModel.saveWatchProgress(args, p, if (duration > 0) duration else d)
            }
            delay(500)
        }
    }

    // Auto-hide controls after inactivity (paused or dragging prevents auto-hide)
    LaunchedEffect(areControlsVisible, isPlaying, isDraggingSeek) {
        if (areControlsVisible && isPlaying && !isDraggingSeek) {
            delay(4000)
            areControlsVisible = false
        }
    }

    fun handleBack() {
        viewModel.saveWatchProgress(args, exoPlayer.currentPosition, duration)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        viewModel.navigateBack()
    }

    BackHandler {
        handleBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        areControlsVisible = !areControlsVisible
                    },
                    onDoubleTap = { offset ->
                        // Left half of screen double-tap: -10s, Right half: +10s
                        if (offset.x < size.width / 2f) {
                            seekBackward10()
                        } else {
                            seekForward10()
                        }
                    }
                )
            }
    ) {
        // AndroidView rendering Media3 PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && playbackError == null) {
            CircularProgressIndicator(
                color = CinemaRed,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center)
            )
        }

        // Temporary Double-Tap Visual Indicator Overlay
        AnimatedVisibility(
            visible = doubleTapFeedback != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (doubleTapFeedback == "LEFT") {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterStart)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "↶ 10 seconds",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (doubleTapFeedback == "RIGHT") {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterEnd)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "10 seconds ↷",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Playback Error Banner
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Unable to Stream Content",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CinemaRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playbackError ?: "Unknown error",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { handleBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                    ) {
                        Text("Return Back")
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = areControlsVisible && playbackError == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { handleBack() },
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = args.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (args.subtitle.isNotBlank()) {
                            Text(
                                text = args.subtitle,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Playback Speed Button
                    IconButton(
                        onClick = { showSpeedDialog = true },
                        modifier = Modifier.testTag("player_speed_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Playback Speed",
                            tint = if (selectedSpeed != 1.0f) CinemaGold else Color.White
                        )
                    }

                    // Quality Picker Button
                    IconButton(
                        onClick = { showQualityDialog = true },
                        modifier = Modifier.testTag("player_quality_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = "Quality",
                            tint = CinemaGold
                        )
                    }

                    // Subtitle Toggle
                    IconButton(
                        onClick = { isSubtitlesEnabled = !isSubtitlesEnabled },
                        modifier = Modifier.testTag("player_subtitles_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClosedCaption,
                            contentDescription = "Subtitles",
                            tint = if (isSubtitlesEnabled) CinemaGold else TextSecondary
                        )
                    }

                    // Mute / Unmute
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            exoPlayer.volume = if (isMuted) 0f else 1f
                        }
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = Color.White
                        )
                    }
                }

                // Center Controls: Rewind -10s, Play/Pause, Forward +10s
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { seekBackward10() }
                            .padding(8.dp)
                            .testTag("player_rewind_10_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10 Seconds",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "-10s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Big Play / Pause Button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(CinemaRed)
                            .clickable {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Forward 10s Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { seekForward10() }
                            .padding(8.dp)
                            .testTag("player_forward_10_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10 Seconds",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+10s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Bottom Bar: Time Stamps, Functional Seek/Progress Bar & Fullscreen Toggle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    // Copyright stream note
                    Text(
                        text = "TT Movie Box • Free Legal Streaming • Unauthorized distribution prohibited",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress bar row: 00:15 ━━━━━━━━━━━━━ 01:45
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayPos = if (isDraggingSeek) dragSeekPosition else currentPosition
                        Text(
                            text = formatTime(displayPos),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        val maxRange = if (duration > 0) duration.toFloat() else 1f
                        val sliderVal = if (duration > 0) {
                            displayPos.toFloat().coerceIn(0f, maxRange)
                        } else 0f

                        Slider(
                            value = sliderVal,
                            onValueChange = { newPos ->
                                isDraggingSeek = true
                                val clamped = newPos.toLong().coerceIn(0L, duration.coerceAtLeast(0L))
                                dragSeekPosition = clamped
                            },
                            onValueChangeFinished = {
                                val target = dragSeekPosition.coerceIn(0L, duration.coerceAtLeast(0L))
                                exoPlayer.seekTo(target)
                                currentPosition = target
                                isDraggingSeek = false
                                viewModel.saveWatchProgress(args, target, duration)
                            },
                            valueRange = 0f..maxRange,
                            colors = SliderDefaults.colors(
                                thumbColor = CinemaRed,
                                activeTrackColor = CinemaRed,
                                inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .testTag("player_seek_bar")
                        )

                        Text(
                            text = formatTime(duration),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Fullscreen / Landscape toggle
                        IconButton(
                            onClick = {
                                isLandscape = !isLandscape
                                activity?.requestedOrientation = if (isLandscape) {
                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            },
                            modifier = Modifier.testTag("player_fullscreen_button")
                        ) {
                            Icon(
                                imageVector = if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Toggle Fullscreen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Next Episode Prompt Dialog
        if (showNextEpisodePrompt && args.nextEpisodeId > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Up Next",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CinemaGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = args.nextEpisodeTitle.ifBlank { "Next Episode" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = { handleBack() }) {
                                Text("Back to Details", color = TextSecondary)
                            }
                            Button(
                                onClick = {
                                    showNextEpisodePrompt = false
                                    viewModel.playNextEpisode(args.nextEpisodeId, args.seriesId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaRed)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Next")
                            }
                        }
                    }
                }
            }
        }

        // Playback Speed Dialog
        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text("Playback Speed", color = TextPrimary) },
                text = {
                    Column {
                        listOf(
                            0.5f to "0.5x Slow",
                            0.75f to "0.75x",
                            1.0f to "1.0x Normal",
                            1.25f to "1.25x",
                            1.5f to "1.5x Fast",
                            2.0f to "2.0x Very Fast"
                        ).forEach { (speed, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSpeed = speed
                                        exoPlayer.playbackParameters = PlaybackParameters(speed)
                                        showSpeedDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSpeed == speed,
                                    onClick = {
                                        selectedSpeed = speed
                                        exoPlayer.playbackParameters = PlaybackParameters(speed)
                                        showSpeedDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = CinemaRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = label, color = TextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpeedDialog = false }) {
                        Text("Close", color = CinemaRed)
                    }
                },
                containerColor = DarkCardBg
            )
        }

        // Quality Selection Dialog
        if (showQualityDialog) {
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                title = { Text("Select Video Quality", color = TextPrimary) },
                text = {
                    Column {
                        listOf(
                            "1080p Full HD (Auto)",
                            "720p HD",
                            "480p SD",
                            "360p Data Saver"
                        ).forEach { quality ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedQuality = quality
                                        showQualityDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedQuality == quality,
                                    onClick = {
                                        selectedQuality = quality
                                        showQualityDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = CinemaRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = quality, color = TextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQualityDialog = false }) {
                        Text("Close", color = CinemaRed)
                    }
                },
                containerColor = DarkCardBg
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
