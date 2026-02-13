package com.caa.initool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.caa.initool.data.EncounterDao
import com.caa.initool.data.GegnerDao
import com.caa.initool.model.Encounter
import com.caa.initool.model.Gegner
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GegnerViewModel(
    private val encounterDao: EncounterDao,
    private val gegnerDao: GegnerDao
) : ViewModel() {

    val encounters: StateFlow<List<Encounter>> = encounterDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedEncounterId = MutableStateFlow<String?>(null)
    val selectedEncounterId: StateFlow<String?> = _selectedEncounterId.asStateFlow()

    private val _showNameInput = MutableStateFlow(false)
    val showNameInput: StateFlow<Boolean> = _showNameInput.asStateFlow()

    // In-Memory-Liste für den Fall ohne Encounter
    private val _inMemoryGegner = MutableStateFlow<List<Gegner>>(emptyList())

    val gegner: StateFlow<List<Gegner>> = _selectedEncounterId
        .flatMapLatest { id ->
            if (id != null) gegnerDao.getByEncounterId(id) else _inMemoryGegner
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sortState = MutableStateFlow(SortState())
    val sortState: StateFlow<SortState> = _sortState.asStateFlow()

    private val isDbMode: Boolean get() = _selectedEncounterId.value != null

    // Cache für Würfelergebnisse pro Gegner-ID (bleibt über Screen-Wechsel erhalten)
    private val rollCache = mutableMapOf<String, Int>()

    fun getRoll(gegner: Gegner): Int {
        return rollCache.getOrPut(gegner.id) {
            if (gegner.twoD6) (1..6).random() + (1..6).random() else (1..6).random()
        }
    }

    fun clearRolls() {
        rollCache.clear()
    }

    fun sortedGegner(gegner: List<Gegner>, sortState: SortState): List<Gegner> {
        val comparator: Comparator<Gegner> = when (sortState.column) {
            SortColumn.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortColumn.INI -> compareBy { it.ini }
            SortColumn.MINUS4 -> compareBy { it.minus4 }
            SortColumn.MINUS8 -> compareBy { it.minus8 }
            SortColumn.TWOD6 -> compareBy { it.twoD6 }
        }
        return if (sortState.ascending) {
            gegner.sortedWith(comparator)
        } else {
            gegner.sortedWith(comparator.reversed())
        }
    }

    fun toggleSort(column: SortColumn) {
        _sortState.update { current ->
            if (current.column == column) {
                current.copy(ascending = !current.ascending)
            } else {
                SortState(column = column, ascending = true)
            }
        }
    }

    fun selectEncounter(id: String) {
        _selectedEncounterId.value = id
        _showNameInput.value = false
    }

    fun deselectEncounter() {
        _selectedEncounterId.value = null
        _showNameInput.value = false
    }

    fun startNewEncounter() {
        _showNameInput.value = true
        _selectedEncounterId.value = null
    }

    fun createEncounter(name: String) {
        viewModelScope.launch {
            val encounter = Encounter(name = name)
            encounterDao.insert(encounter)
            // In-Memory-Gegner in den neuen Encounter übernehmen
            val inMemory = _inMemoryGegner.value
            for (g in inMemory) {
                gegnerDao.insert(g.copy(encounterId = encounter.id))
            }
            _inMemoryGegner.value = emptyList()
            _selectedEncounterId.value = encounter.id
            _showNameInput.value = false
        }
    }

    fun deleteEncounter(id: String) {
        viewModelScope.launch {
            encounterDao.deleteById(id)
            if (_selectedEncounterId.value == id) {
                _selectedEncounterId.value = null
            }
        }
    }

    fun addGegner() {
        if (isDbMode) {
            val encounterId = _selectedEncounterId.value!!
            viewModelScope.launch {
                gegnerDao.insert(Gegner(encounterId = encounterId))
            }
        } else {
            _inMemoryGegner.update { it + Gegner() }
        }
    }

    fun duplicateGegner(id: String) {
        if (isDbMode) {
            viewModelScope.launch {
                val original = gegner.value.find { it.id == id } ?: return@launch
                val copy = buildDuplicate(original)
                gegnerDao.insert(copy)
            }
        } else {
            _inMemoryGegner.update { list ->
                val original = list.find { it.id == id } ?: return@update list
                val copy = buildDuplicate(original)
                val index = list.indexOf(original)
                list.toMutableList().apply { add(index + 1, copy) }
            }
        }
    }

    private fun buildDuplicate(original: Gegner): Gegner {
        val baseName = original.name.trimEnd().replace(Regex("\\s+\\d+$"), "")
        val existingNumbers = gegner.value
            .map { it.name.trimEnd() }
            .filter { it == baseName || it.matches(Regex("^${Regex.escape(baseName)}\\s+\\d+$")) }
            .map { name ->
                Regex("\\s+(\\d+)$").find(name)?.groupValues?.get(1)?.toIntOrNull() ?: 1
            }
        val nextNumber = (existingNumbers.maxOrNull() ?: 1) + 1
        return original.copy(
            id = UUID.randomUUID().toString(),
            name = "$baseName $nextNumber"
        )
    }

    fun removeGegner(id: String) {
        if (isDbMode) {
            viewModelScope.launch { gegnerDao.deleteById(id) }
        } else {
            _inMemoryGegner.update { list -> list.filter { it.id != id } }
        }
    }

    fun updateName(id: String, name: String) {
        if (isDbMode) {
            viewModelScope.launch {
                val g = gegner.value.find { it.id == id } ?: return@launch
                gegnerDao.update(g.copy(name = name))
            }
        } else {
            _inMemoryGegner.update { list ->
                list.map { if (it.id == id) it.copy(name = name) else it }
            }
        }
    }

    fun updateIni(id: String, ini: Int) {
        if (isDbMode) {
            viewModelScope.launch {
                val g = gegner.value.find { it.id == id } ?: return@launch
                gegnerDao.update(g.copy(ini = ini))
            }
        } else {
            _inMemoryGegner.update { list ->
                list.map { if (it.id == id) it.copy(ini = ini) else it }
            }
        }
    }

    fun toggleMinus4(id: String) {
        if (isDbMode) {
            viewModelScope.launch {
                val g = gegner.value.find { it.id == id } ?: return@launch
                gegnerDao.update(g.copy(minus4 = !g.minus4))
            }
        } else {
            _inMemoryGegner.update { list ->
                list.map { if (it.id == id) it.copy(minus4 = !it.minus4) else it }
            }
        }
    }

    fun toggleMinus8(id: String) {
        if (isDbMode) {
            viewModelScope.launch {
                val g = gegner.value.find { it.id == id } ?: return@launch
                gegnerDao.update(g.copy(minus8 = !g.minus8))
            }
        } else {
            _inMemoryGegner.update { list ->
                list.map { if (it.id == id) it.copy(minus8 = !it.minus8) else it }
            }
        }
    }

    fun toggleTwoD6(id: String) {
        if (isDbMode) {
            viewModelScope.launch {
                val g = gegner.value.find { it.id == id } ?: return@launch
                gegnerDao.update(g.copy(twoD6 = !g.twoD6))
            }
        } else {
            _inMemoryGegner.update { list ->
                list.map { if (it.id == id) it.copy(twoD6 = !it.twoD6) else it }
            }
        }
    }
}

class GegnerViewModelFactory(
    private val encounterDao: EncounterDao,
    private val gegnerDao: GegnerDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GegnerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GegnerViewModel(encounterDao, gegnerDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
