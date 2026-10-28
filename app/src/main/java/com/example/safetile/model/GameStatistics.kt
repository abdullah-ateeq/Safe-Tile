package com.example.safetile.model

data class GameStatistics(
    val bestScore: Int = 0,
    val bestRound: Int = 0,
    val bestStreak: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalSafeTilesTapped: Int = 0
)
