package com.caa.initool.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caa.initool.model.Gegner
import com.caa.initool.viewmodel.GegnerViewModel
import com.caa.initool.viewmodel.SortColumn
import com.caa.initool.viewmodel.SortState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GegnerListScreen(viewModel: GegnerViewModel, onNavigateToSpieler: () -> Unit) {
    val gegner by viewModel.gegner.collectAsState()
    val sortState by viewModel.sortState.collectAsState()
    val sortedGegner = viewModel.sortedGegner(gegner, sortState)
    val encounters by viewModel.encounters.collectAsState()
    val selectedEncounterId by viewModel.selectedEncounterId.collectAsState()
    val showNameInput by viewModel.showNameInput.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DSA Ini Tracker - Gegner") },
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
                FloatingActionButton(onClick = { viewModel.addGegner() }) {
                    Icon(Icons.Default.Add, contentDescription = "Gegner hinzufügen")
                }
                FloatingActionButton(onClick = onNavigateToSpieler) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Zu Spieler")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            EncounterSelector(
                encounters = encounters,
                selectedEncounterId = selectedEncounterId,
                showNameInput = showNameInput,
                onSelectEncounter = { viewModel.selectEncounter(it) },
                onDeselectEncounter = { viewModel.deselectEncounter() },
                onStartNew = { viewModel.startNewEncounter() },
                onCreateEncounter = { viewModel.createEncounter(it) },
                onDeleteEncounter = { viewModel.deleteEncounter(it) }
            )

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GegnerHeaderRow(
                        sortState = sortState,
                        onSort = { viewModel.toggleSort(it) }
                    )
                    HorizontalDivider()
                }

                items(sortedGegner, key = { it.id }) { g ->
                    GegnerRow(
                        gegner = g,
                        onNameChange = { viewModel.updateName(g.id, it) },
                        onIniChange = { viewModel.updateIni(g.id, it) },
                        onMinus4Toggle = { viewModel.toggleMinus4(g.id) },
                        onMinus8Toggle = { viewModel.toggleMinus8(g.id) },
                        onTwoD6Toggle = { viewModel.toggleTwoD6(g.id) },
                        onDuplicate = { viewModel.duplicateGegner(g.id) },
                        onDelete = { viewModel.removeGegner(g.id) }
                    )
                    HorizontalDivider()
                }

                if (gegner.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Gegner vorhanden.\nTippe + um einen Gegner hinzuzufügen.",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncounterSelector(
    encounters: List<com.caa.initool.model.Encounter>,
    selectedEncounterId: String?,
    showNameInput: Boolean,
    onSelectEncounter: (String) -> Unit,
    onDeselectEncounter: () -> Unit,
    onStartNew: () -> Unit,
    onCreateEncounter: (String) -> Unit,
    onDeleteEncounter: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = encounters.find { it.id == selectedEncounterId }?.name

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedName ?: if (showNameInput) "Neuer Encounter" else "Kein Encounter",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Kein Encounter") },
                    onClick = {
                        expanded = false
                        onDeselectEncounter()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Neuer Encounter", fontWeight = FontWeight.Bold) },
                    onClick = {
                        expanded = false
                        onStartNew()
                    }
                )
                encounters.forEach { encounter ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(encounter.name, modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        expanded = false
                                        onDeleteEncounter(encounter.id)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Encounter löschen",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelectEncounter(encounter.id)
                        }
                    )
                }
            }
        }

        if (showNameInput) {
            var nameText by remember { mutableStateOf("") }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Encounter-Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (nameText.isNotBlank()) {
                                onCreateEncounter(nameText.trim())
                            }
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            onCreateEncounter(nameText.trim())
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Encounter erstellen"
                    )
                }
            }
        }
    }
}

@Composable
private fun GegnerSortIndicator(sortState: SortState, column: SortColumn) {
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
private fun GegnerHeaderRow(sortState: SortState, onSort: (SortColumn) -> Unit) {
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
            GegnerSortIndicator(sortState, SortColumn.NAME)
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
            GegnerSortIndicator(sortState, SortColumn.INI)
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
            GegnerSortIndicator(sortState, SortColumn.MINUS4)
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
            GegnerSortIndicator(sortState, SortColumn.MINUS8)
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .width(36.dp)
                .clickable { onSort(SortColumn.TWOD6) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "2d6",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            GegnerSortIndicator(sortState, SortColumn.TWOD6)
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.width(72.dp))
    }
}

@Composable
private fun GegnerRow(
    gegner: Gegner,
    onNameChange: (String) -> Unit,
    onIniChange: (Int) -> Unit,
    onMinus4Toggle: () -> Unit,
    onMinus8Toggle: () -> Unit,
    onTwoD6Toggle: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var nameText by remember(gegner.id) { mutableStateOf(gegner.name) }

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
            value = if (gegner.ini == 0) "" else gegner.ini.toString(),
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
            checked = gegner.minus4,
            onCheckedChange = { onMinus4Toggle() },
            modifier = Modifier.width(36.dp)
        )

        Checkbox(
            checked = gegner.minus8,
            onCheckedChange = { onMinus8Toggle() },
            modifier = Modifier.width(36.dp)
        )

        Checkbox(
            checked = gegner.twoD6,
            onCheckedChange = { onTwoD6Toggle() },
            modifier = Modifier.width(36.dp)
        )

        IconButton(
            onClick = onDuplicate,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Gegner duplizieren"
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Gegner entfernen",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
