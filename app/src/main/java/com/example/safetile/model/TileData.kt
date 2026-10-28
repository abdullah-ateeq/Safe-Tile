package com.example.safetile.model

data class TileData(
    val index: Int,
    val row: Int,
    val column: Int,
    val isDangerous: Boolean,
    val state: TileState,
    val tapped: Boolean = false,
    val isWronglyTapped: Boolean = false
)
