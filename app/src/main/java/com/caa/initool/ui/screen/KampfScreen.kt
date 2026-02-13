package com.caa.initool.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caa.initool.model.Gegner
import com.caa.initool.model.KampfEntry
import com.caa.initool.model.Player
import com.caa.initool.viewmodel.GegnerViewModel
import com.caa.initool.viewmodel.PlayerViewModel

private fun buildKampfEntries(
    players: List<Player>,
    gegner: List<Gegner>,
    gegnerViewModel: GegnerViewModel
): List<KampfEntry> {
    val entries = mutableListOf<KampfEntry>()

    for (p in players) {
        entries.add(KampfEntry(name = p.name, ini = p.ini, isPlayer = true, sourceId = p.id, isBaseEntry = true))
        if (p.minus4 && p.ini - 4 >= 1) {
            entries.add(KampfEntry(name = "${p.name} (-4)", ini = p.ini - 4, isPlayer = true, sourceId = p.id, isBaseEntry = false))
        }
        if (p.minus8 && p.ini - 8 >= 1) {
            entries.add(KampfEntry(name = "${p.name} (-8)", ini = p.ini - 8, isPlayer = true, sourceId = p.id, isBaseEntry = false))
        }
    }

    for (g in gegner) {
        val roll = gegnerViewModel.getRoll(g)
        val baseIni = g.ini + roll
        entries.add(KampfEntry(name = g.name, ini = baseIni, isPlayer = false, sourceId = g.id, isBaseEntry = true))
        if (g.minus4 && baseIni - 4 >= 1) {
            entries.add(KampfEntry(name = "${g.name} (-4)", ini = baseIni - 4, isPlayer = false, sourceId = g.id, isBaseEntry = false))
        }
        if (g.minus8 && baseIni - 8 >= 1) {
            entries.add(KampfEntry(name = "${g.name} (-8)", ini = baseIni - 8, isPlayer = false, sourceId = g.id, isBaseEntry = false))
        }
    }

    return entries.sortedByDescending { it.ini }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KampfScreen(
    playerViewModel: PlayerViewModel,
    gegnerViewModel: GegnerViewModel
) {
    val players by playerViewModel.players.collectAsState()
    val gegner by gegnerViewModel.gegner.collectAsState()

    val entries = remember(players, gegner) {
        buildKampfEntries(players, gegner, gegnerViewModel)
    }

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    // Reset falls Liste sich ändert
    if (selectedIndex >= entries.size) {
        selectedIndex = 0
    }

    // Auto-Scroll wenn Markierung außerhalb des sichtbaren Bereichs
    LaunchedEffect(selectedIndex) {
        val itemIndex = selectedIndex + 1 // +1 wegen Header-Item
        val isVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == itemIndex }
        if (!isVisible) {
            val firstVisible = listState.firstVisibleItemIndex
            if (itemIndex < firstVisible) {
                listState.animateScrollToItem(maxOf(0, firstVisible - 4))
            } else {
                listState.animateScrollToItem(firstVisible + 4)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DSA Ini Tracker - Kampf") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (entries.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (selectedIndex > 0) {
                                selectedIndex = selectedIndex - 1
                            }
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Vorheriger")
                    }
                    FloatingActionButton(
                        onClick = {
                            selectedIndex = (selectedIndex + 1) % entries.size
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Nächster")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ini",
                        modifier = Modifier.width(48.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Name",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                HorizontalDivider()
            }

            itemsIndexed(entries) { index, entry ->
                val isSelected = index == selectedIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isSelected) {
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            } else {
                                Modifier
                            }
                        )
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.ini.toString(),
                        modifier = Modifier.width(48.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else if (entry.isPlayer)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = entry.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else if (entry.isPlayer)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.isBaseEntry) {
                        IconButton(
                            onClick = {
                                if (entry.isPlayer) {
                                    val player = players.find { it.id == entry.sourceId }
                                    if (player != null) playerViewModel.updateIni(player.id, player.ini + 1)
                                } else {
                                    val g = gegner.find { it.id == entry.sourceId }
                                    if (g != null) gegnerViewModel.updateIni(g.id, g.ini + 1)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Initiative um 1 erhöhen",
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                if (entry.isPlayer) {
                                    val player = players.find { it.id == entry.sourceId }
                                    if (player != null) playerViewModel.updateIni(player.id, player.ini - 1)
                                } else {
                                    val g = gegner.find { it.id == entry.sourceId }
                                    if (g != null) gegnerViewModel.updateIni(g.id, g.ini - 1)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Initiative um 1 reduzieren",
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(72.dp))
                    }
                }
                HorizontalDivider()
            }

            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keine Teilnehmer vorhanden.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
