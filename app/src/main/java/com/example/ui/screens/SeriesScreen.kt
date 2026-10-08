package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyContentState
import com.example.ui.components.SeriesCard
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun SeriesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allPublishedSeries.collectAsState()
    var selectedLanguage by remember { mutableStateOf("All") }

    val languages = remember(allSeries) {
        val list = mutableListOf("All")
        allSeries.forEach { s ->
            val trimmed = s.language.trim()
            if (trimmed.isNotBlank() && trimmed !in list) list.add(trimmed)
        }
        list
    }

    val filteredSeries = if (selectedLanguage == "All") {
        allSeries
    } else {
        allSeries.filter { it.language.equals(selectedLanguage, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Web Series",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Binge-worthy original series, multi-season dramas and comedies",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (languages.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(languages) { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        label = { Text(lang) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaRed,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (allSeries.isEmpty()) {
            EmptyContentState(
                title = "No Web Series Available",
                message = "The web series library is currently empty. Seasons and episodes added and published by the admin will appear here.",
                actionLabel = "Admin Panel",
                onActionClick = { viewModel.navigateTo(Screen.Admin) }
            )
        } else if (filteredSeries.isEmpty()) {
            EmptyContentState(
                title = "No Series for '$selectedLanguage'",
                message = "No series match the selected filter. Try another language."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(135.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSeries, key = { it.id }) { series ->
                    SeriesCard(
                        series = series,
                        onClick = { viewModel.selectSeries(series.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
