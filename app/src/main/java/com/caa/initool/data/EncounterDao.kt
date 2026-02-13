package com.caa.initool.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.caa.initool.model.Encounter
import kotlinx.coroutines.flow.Flow

@Dao
interface EncounterDao {

    @Query("SELECT * FROM encounters")
    fun getAll(): Flow<List<Encounter>>

    @Insert
    suspend fun insert(encounter: Encounter)

    @Query("DELETE FROM encounters WHERE id = :id")
    suspend fun deleteById(id: String)
}
