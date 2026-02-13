package com.caa.initool.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "encounters")
data class Encounter(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = ""
)
