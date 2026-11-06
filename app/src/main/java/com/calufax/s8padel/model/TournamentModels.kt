package com.calufax.s8padel.model

enum class FixtureMode {
    /**
     * Torneo de Whist perfecto Wh(8): cada jugador juega 1 vez de compañero con cada uno
     * de los otros 7 y exactamente 2 veces en contra de cada uno de los otros 7.
     */
    BALANCED_WHIST,

    /**
     * Cruces exactos de la planilla clásica de la imagen adjunta (1 vez de compañero con cada uno).
     */
    CLASSIC_SHEET
}

data class Player(
    val slot: Int,
    val name: String
) {
    val displayName: String
        get() = name.trim().ifEmpty { "Jugador $slot" }
}

data class MatchResult(
    val teamAGames: Int,
    val teamBGames: Int
) {
    val isTeamAWinner: Boolean
        get() = teamAGames > teamBGames

    val isTeamBWinner: Boolean
        get() = teamBGames > teamAGames

    companion object {
        const val GAMES_TO_WIN = 4

        fun isValid(teamAGames: Int, teamBGames: Int): Boolean {
            val aWins = teamAGames == GAMES_TO_WIN && teamBGames in 0 until GAMES_TO_WIN
            val bWins = teamBGames == GAMES_TO_WIN && teamAGames in 0 until GAMES_TO_WIN
            return aWins || bWins
        }
    }
}

data class MatchDefinition(
    val id: Int,
    val round: Int,
    val baseCourt: Int,
    val teamA: Pair<Int, Int>,
    val teamB: Pair<Int, Int>
)

data class ScheduledMatch(
    val definition: MatchDefinition,
    val effectiveCourt: Int,
    val result: MatchResult?
) {
    val id: Int get() = definition.id
    val round: Int get() = definition.round
    val teamA: Pair<Int, Int> get() = definition.teamA
    val teamB: Pair<Int, Int> get() = definition.teamB
    val isCompleted: Boolean get() = result != null
}

data class RoundSchedule(
    val round: Int,
    val isCourtSwapped: Boolean,
    val matches: List<ScheduledMatch>
) {
    val isCompleted: Boolean
        get() = matches.all { it.isCompleted }
}

data class RoundScoreCell(
    val round: Int,
    val gamesFor: Int,
    val gamesAgainst: Int,
    val won: Boolean
)

data class PlayerStanding(
    val slot: Int,
    val name: String,
    val rank: Int,
    val isTied: Boolean,
    val tieBreakBadge: String?,
    val matchesPlayed: Int,
    val matchesWon: Int,
    val matchesLost: Int,
    val gamesFor: Int,
    val gamesAgainst: Int,
    val gamesDiff: Int,
    val roundCells: List<RoundScoreCell?>,
    val court1Count: Int,
    val court2Count: Int
)

data class TournamentState(
    val tournamentName: String = DEFAULT_TOURNAMENT_NAME,
    val fixtureMode: FixtureMode = FixtureMode.BALANCED_WHIST,
    val players: List<Player> = defaultPlayers(),
    val results: Map<Int, MatchResult> = emptyMap(),
    val swappedRounds: Set<Int> = DEFAULT_SWAPPED_ROUNDS,
    val updatedAtMillis: Long = 0L
) {
    fun playerName(slot: Int): String {
        return players.firstOrNull { it.slot == slot }?.displayName ?: "Jugador $slot"
    }

    companion object {
        const val DEFAULT_TOURNAMENT_NAME = "Super 8 Pádel"
        val DEFAULT_SWAPPED_ROUNDS: Set<Int> = setOf(2, 5, 6)

        fun defaultPlayers(): List<Player> = (1..8).map { slot ->
            Player(slot = slot, name = "Jugador $slot")
        }
    }
}
