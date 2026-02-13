package com.caa.initool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.caa.initool.data.PlayerDao
import com.caa.initool.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortColumn { NAME, INI, MINUS4, MINUS8, TWOD6 }

data class SortState(
    val column: SortColumn = SortColumn.INI,
    val ascending: Boolean = false
)

class PlayerViewModel(private val dao: PlayerDao) : ViewModel() {

    val players: StateFlow<List<Player>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sortState = MutableStateFlow(SortState())
    val sortState: StateFlow<SortState> = _sortState.asStateFlow()

    fun sortedPlayers(players: List<Player>, sortState: SortState): List<Player> {
        val comparator: Comparator<Player> = when (sortState.column) {
            SortColumn.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortColumn.INI -> compareBy { it.ini }
            SortColumn.MINUS4 -> compareBy { it.minus4 }
            SortColumn.MINUS8 -> compareBy { it.minus8 }
            SortColumn.TWOD6 -> compareBy { it.name } // nicht relevant für Spieler
        }
        return if (sortState.ascending) {
            players.sortedWith(comparator)
        } else {
            players.sortedWith(comparator.reversed())
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

    fun addPlayer() {
        viewModelScope.launch {
            dao.insert(Player())
        }
    }

    fun removePlayer(id: String) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun updateName(id: String, name: String) {
        viewModelScope.launch {
            val player = players.value.find { it.id == id } ?: return@launch
            dao.update(player.copy(name = name))
        }
    }

    fun updateIni(id: String, ini: Int) {
        viewModelScope.launch {
            val player = players.value.find { it.id == id } ?: return@launch
            dao.update(player.copy(ini = ini))
        }
    }

    fun toggleMinus4(id: String) {
        viewModelScope.launch {
            val player = players.value.find { it.id == id } ?: return@launch
            dao.update(player.copy(minus4 = !player.minus4))
        }
    }

    fun toggleMinus8(id: String) {
        viewModelScope.launch {
            val player = players.value.find { it.id == id } ?: return@launch
            dao.update(player.copy(minus8 = !player.minus8))
        }
    }
}

class PlayerViewModelFactory(private val dao: PlayerDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
