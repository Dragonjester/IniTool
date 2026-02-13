package com.caa.initool.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caa.initool.model.Player
import com.caa.initool.viewmodel.PlayerViewModel
import com.caa.initool.viewmodel.SortColumn
import com.caa.initool.viewmodel.SortState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(viewModel: PlayerViewModel, onNavigateToKampf: () -> Unit) {
    val players by viewModel.players.collectAsState()
    val sortState by viewModel.sortState.collectAsState()
    val sortedPlayers = viewModel.sortedPlayers(players, sortState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DSA Ini Tracker - Spieler") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(onClick = { viewModel.addPlayer() }) {
                    Icon(Icons.Default.Add, contentDescription = "Spieler hinzufügen")
                }
                FloatingActionButton(onClick = onNavigateToKampf) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Zum Kampf")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HeaderRow(
                    sortState = sortState,
                    onSort = { viewModel.toggleSort(it) }
                )
                HorizontalDivider()
            }

            items(sortedPlayers, key = { it.id }) { player ->
                PlayerRow(
                    player = player,
                    onNameChange = { viewModel.updateName(player.id, it) },
                    onIniChange = { viewModel.updateIni(player.id, it) },
                    onMinus4Toggle = { viewModel.toggleMinus4(player.id) },
                    onMinus8Toggle = { viewModel.toggleMinus8(player.id) },
                    onDelete = { viewModel.removePlayer(player.id) }
                )
                HorizontalDivider()
            }

            if (players.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keine Spieler vorhanden.\nTippe + um einen Spieler hinzuzufügen.",
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

@Composable
private fun SortIndicator(sortState: SortState, column: SortColumn) {
    if (sortState.column == column) {
        Icon(
            imageVector = if (sortState.ascending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (sortState.ascending) "Aufsteigend" else "Absteigend",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HeaderRow(sortState: SortState, onSort: (SortColumn) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onSort(SortColumn.NAME) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            SortIndicator(sortState, SortColumn.NAME)
        }
        Row(
            modifier = Modifier
                .width(56.dp)
                .clickable { onSort(SortColumn.INI) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Ini",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            SortIndicator(sortState, SortColumn.INI)
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .width(36.dp)
                .clickable { onSort(SortColumn.MINUS4) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "-4",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            SortIndicator(sortState, SortColumn.MINUS4)
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .width(36.dp)
                .clickable { onSort(SortColumn.MINUS8) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "-8",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            SortIndicator(sortState, SortColumn.MINUS8)
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.width(36.dp))
    }
}

@Composable
private fun PlayerRow(
    player: Player,
    onNameChange: (String) -> Unit,
    onIniChange: (Int) -> Unit,
    onMinus4Toggle: () -> Unit,
    onMinus8Toggle: () -> Unit,
    onDelete: () -> Unit
) {
    var nameText by remember(player.id) { mutableStateOf(player.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = nameText,
            onValueChange = {
                nameText = it
                onNameChange(it)
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Name") },
            singleLine = true
        )

        OutlinedTextField(
            value = if (player.ini == 0) "" else player.ini.toString(),
            onValueChange = { text ->
                val value = text.filter { it.isDigit() || it == '-' }.toIntOrNull() ?: 0
                onIniChange(value)
            },
            modifier = Modifier.width(56.dp),
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Checkbox(
            checked = player.minus4,
            onCheckedChange = { onMinus4Toggle() },
            modifier = Modifier.width(36.dp)
        )

        Checkbox(
            checked = player.minus8,
            onCheckedChange = { onMinus8Toggle() },
            modifier = Modifier.width(36.dp)
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Spieler entfernen",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
