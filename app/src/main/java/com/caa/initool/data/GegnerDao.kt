package com.caa.initool.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.caa.initool.model.Gegner
import kotlinx.coroutines.flow.Flow

@Dao
interface GegnerDao {

    @Query("SELECT * FROM gegner WHERE encounterId = :encounterId")
    fun getByEncounterId(encounterId: String): Flow<List<Gegner>>

    @Insert
    suspend fun insert(gegner: Gegner)

    @Update
    suspend fun update(gegner: Gegner)

    @Query("DELETE FROM gegner WHERE id = :id")
    suspend fun deleteById(id: String)
}
