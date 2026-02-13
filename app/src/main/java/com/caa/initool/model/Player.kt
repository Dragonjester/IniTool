package com.caa.initool.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "players")
data class Player(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val ini: Int = 0,
    val minus4: Boolean = false,
    val minus8: Boolean = false
)
