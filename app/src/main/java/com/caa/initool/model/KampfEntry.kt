package com.caa.initool.model

data class KampfEntry(
    val name: String,
    val ini: Int,
    val isPlayer: Boolean,
    val sourceId: String = "",
    val isBaseEntry: Boolean = true
)
