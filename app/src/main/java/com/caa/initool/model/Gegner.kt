package com.caa.initool.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "gegner",
    foreignKeys = [ForeignKey(
        entity = Encounter::class,
        parentColumns = ["id"],
        childColumns = ["encounterId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("encounterId")]
)
data class Gegner(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val encounterId: String = "",
    val name: String = "",
    val ini: Int = 0,
    val minus4: Boolean = false,
    val minus8: Boolean = false,
    val twoD6: Boolean = false
)
