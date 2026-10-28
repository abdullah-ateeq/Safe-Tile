package com.example.safetile.model

data class RoundConfig(
    val round: Int,
    val rows: Int,
    val columns: Int,
    val dangerousCount: Int,
    val memorizeTimeMs: Long,
    val roundTimeMs: Long
) {
    val totalTiles: Int
        get() = rows * columns

    val safeTilesCount: Int
        get() = totalTiles - dangerousCount
}
