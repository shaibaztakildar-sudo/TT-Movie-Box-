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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.SeriesEntity
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AddEditSeriesDialog(
    initialSeries: SeriesEntity? = null,
    adminViewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialSeries?.title ?: "") }
    var description by remember { mutableStateOf(initialSeries?.description ?: "") }
    var genre by remember { mutableStateOf(initialSeries?.genre ?: "Drama, Crime") }
    var language by remember { mutableStateOf(initialSeries?.language ?: "Hindi") }
    var releaseYear by remember { mutableIntStateOf(initialSeries?.releaseYear ?: 2024) }
    var rating by remember { mutableFloatStateOf(initialSeries?.rating ?: 8.2f) }
    var cast by remember { mutableStateOf(initialSeries?.cast ?: "") }
    var director by remember { mutableStateOf(initialSeries?.director ?: "") }
    var posterUri by remember { mutableStateOf(initialSeries?.posterUri ?: "") }
    var backdropUri by remember { mutableStateOf(initialSeries?.backdropUri ?: "") }
    var isPublished by remember { mutableStateOf(initialSeries?.isPublished ?: true) }

    var validationError by remember { mutableStateOf<String?>(null) }

    val posterPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadPoster(uri) { posterUri = it }
        }
    }

    val backdropPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            adminViewModel.uploadBackdrop(uri) { backdropUri = it }
        }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialSeries == null) "Create Web Series" else "Edit Web Series",
                        fontSize = 20.sp,
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
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Series Title *") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_series_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Series Description *") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("admin_series_desc_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Poster & Backdrop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Series Poster", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(60.dp, 80.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable { posterPicker.launch("image/*") },
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
                                    onClick = { posterPicker.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Pick Poster", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Series Banner", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(90.dp, 55.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable { backdropPicker.launch("image/*") },
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
                                    onClick = { backdropPicker.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Pick Banner", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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

                    OutlinedTextField(
                        value = cast,
                        onValueChange = { cast = it },
                        label = { Text("Cast") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Publish Series", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isPublished,
                            onCheckedChange = { isPublished = it },
                            colors = switchColors()
                        )
                    }

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = validationError ?: "", color = CinemaRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            validationError = "Please enter series title"
                            return@Button
                        }
                        val seriesToSave = (initialSeries ?: SeriesEntity(
                            title = title,
                            description = description
                        )).copy(
                            title = title.trim(),
                            description = description.trim(),
                            posterUri = posterUri.trim(),
                            backdropUri = backdropUri.trim(),
                            genre = genre.trim(),
                            language = language.trim(),
                            releaseYear = releaseYear,
                            rating = rating,
                            cast = cast.trim(),
                            director = director.trim(),
                            isPublished = isPublished
                        )
                        adminViewModel.saveSeries(seriesToSave) {
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_save_series_button")
                ) {
                    Text(if (initialSeries == null) "Create Series" else "Update Series", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
