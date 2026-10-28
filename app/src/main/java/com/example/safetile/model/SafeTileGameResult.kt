package com.example.safetile.model

data class SafeTileGameResult(
    val difficulty: Difficulty,
    val roundsSurvived: Int,
    val safeTilesTapped: Int,
    val dangerTilesAvoided: Int,
    val dangerHit: Boolean,
    val perfectRounds: Int,
    val bestStreak: Int,
    val score: Int,
    val wrongTileIndex: Int? = null,
    val isNewBestScore: Boolean = false,
    val isNewBestRound: Boolean = false
)
