package com.calufax.s8padel.domain

import com.calufax.s8padel.model.FixtureMode
import com.calufax.s8padel.model.MatchDefinition
import com.calufax.s8padel.model.MatchResult
import com.calufax.s8padel.model.PlayerStanding
import com.calufax.s8padel.model.RoundSchedule
import com.calufax.s8padel.model.RoundScoreCell
import com.calufax.s8padel.model.ScheduledMatch
import com.calufax.s8padel.model.TournamentState

/**
 * Motor matemático del torneo Super 8 / Americano de 8 jugadores.
 *
 * Soporta dos variantes de cruces en 7 rondas y 2 canchas simultáneas (14 partidos):
 * 1) [FixtureMode.BALANCED_WHIST] (Whist perfecto Wh(8), por defecto):
 *    - Cada jugador juega exactamente 1 vez de compañero con cada uno de los otros 7.
 *    - Cada jugador juega exactamente 2 veces en contra de cada uno de los otros 7.
 * 2) [FixtureMode.CLASSIC_SHEET] (Planilla clásica de la imagen):
 *    - Respeta literalmente las combinaciones de la hoja clásica de club (1 vez de compañero con cada uno),
 *      rotando las canchas en rondas 2, 5 y 6 por defecto para que nadie quede fijo las 7 rondas en Cancha 1.
 */
object Super8Engine {

    /**
     * Torneo de Whist cíclico perfecto Wh(8) sobre Z_7 U {8}:
     * Parejas y rivales 100% simétricos (1 vez compañero, 2 veces rival para los 28 pares).
     */
    val WHIST_BALANCED_FIXTURE: List<MatchDefinition> = listOf(
        // Ronda 1
        MatchDefinition(id = 1, round = 1, baseCourt = 1, teamA = 1 to 8, teamB = 2 to 4),
        MatchDefinition(id = 2, round = 1, baseCourt = 2, teamA = 3 to 7, teamB = 5 to 6),
        // Ronda 2
        MatchDefinition(id = 3, round = 2, baseCourt = 1, teamA = 2 to 8, teamB = 3 to 5),
        MatchDefinition(id = 4, round = 2, baseCourt = 2, teamA = 4 to 1, teamB = 6 to 7),
        // Ronda 3
        MatchDefinition(id = 5, round = 3, baseCourt = 1, teamA = 3 to 8, teamB = 4 to 6),
        MatchDefinition(id = 6, round = 3, baseCourt = 2, teamA = 5 to 2, teamB = 7 to 1),
        // Ronda 4
        MatchDefinition(id = 7, round = 4, baseCourt = 1, teamA = 4 to 8, teamB = 5 to 7),
        MatchDefinition(id = 8, round = 4, baseCourt = 2, teamA = 6 to 3, teamB = 1 to 2),
        // Ronda 5
        MatchDefinition(id = 9, round = 5, baseCourt = 1, teamA = 5 to 8, teamB = 6 to 1),
        MatchDefinition(id = 10, round = 5, baseCourt = 2, teamA = 7 to 4, teamB = 2 to 3),
        // Ronda 6
        MatchDefinition(id = 11, round = 6, baseCourt = 1, teamA = 6 to 8, teamB = 7 to 2),
        MatchDefinition(id = 12, round = 6, baseCourt = 2, teamA = 1 to 5, teamB = 3 to 4),
        // Ronda 7
        MatchDefinition(id = 13, round = 7, baseCourt = 1, teamA = 7 to 8, teamB = 1 to 3),
        MatchDefinition(id = 14, round = 7, baseCourt = 2, teamA = 2 to 6, teamB = 4 to 5)
    )

    /**
     * Cruces exactos de la planilla "AMERICANO PADEL 8 JUGADORES" subida como referencia.
     */
    val CLASSIC_SHEET_FIXTURE: List<MatchDefinition> = listOf(
        // Ronda 1
        MatchDefinition(id = 1, round = 1, baseCourt = 1, teamA = 1 to 8, teamB = 7 to 2),
        MatchDefinition(id = 2, round = 1, baseCourt = 2, teamA = 6 to 3, teamB = 5 to 4),
        // Ronda 2
        MatchDefinition(id = 3, round = 2, baseCourt = 1, teamA = 8 to 5, teamB = 4 to 6),
        MatchDefinition(id = 4, round = 2, baseCourt = 2, teamA = 3 to 7, teamB = 2 to 1),
        // Ronda 3
        MatchDefinition(id = 5, round = 3, baseCourt = 1, teamA = 2 to 8, teamB = 1 to 3),
        MatchDefinition(id = 6, round = 3, baseCourt = 2, teamA = 7 to 4, teamB = 6 to 5),
        // Ronda 4
        MatchDefinition(id = 7, round = 4, baseCourt = 1, teamA = 8 to 6, teamB = 5 to 7),
        MatchDefinition(id = 8, round = 4, baseCourt = 2, teamA = 4 to 1, teamB = 3 to 2),
        // Ronda 5
        MatchDefinition(id = 9, round = 5, baseCourt = 1, teamA = 3 to 8, teamB = 2 to 4),
        MatchDefinition(id = 10, round = 5, baseCourt = 2, teamA = 1 to 5, teamB = 7 to 6),
        // Ronda 6
        MatchDefinition(id = 11, round = 6, baseCourt = 1, teamA = 8 to 7, teamB = 6 to 1),
        MatchDefinition(id = 12, round = 6, baseCourt = 2, teamA = 5 to 2, teamB = 4 to 3),
        // Ronda 7
        MatchDefinition(id = 13, round = 7, baseCourt = 1, teamA = 4 to 8, teamB = 5 to 3),
        MatchDefinition(id = 14, round = 7, baseCourt = 2, teamA = 2 to 6, teamB = 1 to 7)
    )

    const val TOTAL_ROUNDS = 7
    const val TOTAL_MATCHES = 14

    fun fixtureFor(mode: FixtureMode): List<MatchDefinition> = when (mode) {
        FixtureMode.BALANCED_WHIST -> WHIST_BALANCED_FIXTURE
        FixtureMode.CLASSIC_SHEET -> CLASSIC_SHEET_FIXTURE
    }

    fun buildSchedule(state: TournamentState): List<RoundSchedule> {
        val fixture = fixtureFor(state.fixtureMode)
        return (1..TOTAL_ROUNDS).map { round ->
            val swapped = round in state.swappedRounds
            val matches = fixture
                .filter { it.round == round }
                .map { def ->
                    val effectiveCourt = if (!swapped) {
                        def.baseCourt
                    } else {
                        if (def.baseCourt == 1) 2 else 1
                    }
                    ScheduledMatch(
                        definition = def,
                        effectiveCourt = effectiveCourt,
                        result = state.results[def.id]
                    )
                }
                .sortedBy { it.effectiveCourt }

            RoundSchedule(
                round = round,
                isCourtSwapped = swapped,
                matches = matches
            )
        }
    }

    private data class RawStats(
        val slot: Int,
        val name: String,
        val matchesPlayed: Int,
        val matchesWon: Int,
        val matchesLost: Int,
        val gamesFor: Int,
        val gamesAgainst: Int,
        val roundCells: List<RoundScoreCell?>,
        val court1Count: Int,
        val court2Count: Int
    ) {
        val gamesDiff: Int get() = gamesFor - gamesAgainst
    }

    private data class HeadToHeadComparison(
        val slotAWins: Int,
        val slotBWins: Int,
        val slotAGames: Int,
        val slotBGames: Int
    ) {
        val winsDiff: Int get() = slotAWins - slotBWins
        val gamesDiff: Int get() = slotAGames - slotBGames
    }

    /**
     * Calcula la tabla de posiciones en tiempo real aplicando los criterios de desempate:
     * 1) Mayor cantidad de Partidos Ganados (PG).
     * 2) Mejor Diferencia de Games (DG = GF - GC).
     * 3) Mayor cantidad de Games a Favor (GF).
     * 4) Entre 2 jugadores empatados en (PG, DG, GF): balance en los partidos donde se enfrentaron
     *    en contra (victorias entre sí y diferencia de games en esos cruces).
     * 5) Si persiste igualdad exacta, comparten la misma posición numérica (empate técnico).
     */
    fun calculateStandings(state: TournamentState): List<PlayerStanding> {
        val schedule = buildSchedule(state)
        val allMatches = schedule.flatMap { it.matches }

        val rawList = (1..8).map { slot ->
            var played = 0
            var won = 0
            var lost = 0
            var gf = 0
            var ga = 0
            var c1 = 0
            var c2 = 0

            val cells = (1..TOTAL_ROUNDS).map { round ->
                val match = allMatches.first { m ->
                    m.round == round && m.involves(slot)
                }
                if (match.effectiveCourt == 1) c1++ else c2++

                val res = match.result
                if (res == null) {
                    null
                } else {
                    val onTeamA = match.isOnTeamA(slot)
                    val myGames = if (onTeamA) res.teamAGames else res.teamBGames
                    val oppGames = if (onTeamA) res.teamBGames else res.teamAGames
                    val isWin = myGames > oppGames

                    played++
                    if (isWin) won++ else lost++
                    gf += myGames
                    ga += oppGames

                    RoundScoreCell(
                        round = round,
                        gamesFor = myGames,
                        gamesAgainst = oppGames,
                        won = isWin
                    )
                }
            }

            RawStats(
                slot = slot,
                name = state.playerName(slot),
                matchesPlayed = played,
                matchesWon = won,
                matchesLost = lost,
                gamesFor = gf,
                gamesAgainst = ga,
                roundCells = cells,
                court1Count = c1,
                court2Count = c2
            )
        }

        val sortedGlobal = rawList.sortedWith(
            compareByDescending<RawStats> { it.matchesWon }
                .thenByDescending { it.gamesDiff }
                .thenByDescending { it.gamesFor }
                .thenBy { it.slot }
        )

        data class GlobalKey(val pg: Int, val dg: Int, val gf: Int)

        val grouped = LinkedHashMap<GlobalKey, MutableList<RawStats>>()
        for (item in sortedGlobal) {
            val key = GlobalKey(item.matchesWon, item.gamesDiff, item.gamesFor)
            grouped.getOrPut(key) { mutableListOf() }.add(item)
        }

        val anyMatchesPlayed = state.results.isNotEmpty()
        val resultStandings = mutableListOf<PlayerStanding>()
        var nextPosition = 1

        val buckets = grouped.values.toList()
        for (bucketIndex in buckets.indices) {
            val bucket = buckets[bucketIndex]

            if (bucket.size == 1) {
                val player = bucket.first()
                val badge = computeGlobalTieBreakBadge(
                    player = player,
                    nextBucketFirst = buckets.getOrNull(bucketIndex + 1)?.firstOrNull()
                )
                resultStandings.add(
                    player.toStanding(
                        rank = nextPosition,
                        isTied = false,
                        tieBreakBadge = if (anyMatchesPlayed) badge else null
                    )
                )
                nextPosition += 1
            } else if (bucket.size == 2) {
                val first = bucket[0]
                val second = bucket[1]
                val h2h = compareAsOpponents(first.slot, second.slot, allMatches)

                when {
                    h2h.winsDiff > 0 || (h2h.winsDiff == 0 && h2h.gamesDiff > 0) -> {
                        val reason = if (h2h.winsDiff > 0) {
                            "Cruce rival (${h2h.slotAWins}-${h2h.slotBWins})"
                        } else {
                            "Cruce rival (+${h2h.gamesDiff} games)"
                        }
                        resultStandings.add(
                            first.toStanding(
                                rank = nextPosition,
                                isTied = false,
                                tieBreakBadge = reason
                            )
                        )
                        resultStandings.add(
                            second.toStanding(
                                rank = nextPosition + 1,
                                isTied = false,
                                tieBreakBadge = null
                            )
                        )
                        nextPosition += 2
                    }

                    h2h.winsDiff < 0 || (h2h.winsDiff == 0 && h2h.gamesDiff < 0) -> {
                        val reason = if (h2h.winsDiff < 0) {
                            "Cruce rival (${h2h.slotBWins}-${h2h.slotAWins})"
                        } else {
                            "Cruce rival (+${-h2h.gamesDiff} games)"
                        }
                        resultStandings.add(
                            second.toStanding(
                                rank = nextPosition,
                                isTied = false,
                                tieBreakBadge = reason
                            )
                        )
                        resultStandings.add(
                            first.toStanding(
                                rank = nextPosition + 1,
                                isTied = false,
                                tieBreakBadge = null
                            )
                        )
                        nextPosition += 2
                    }

                    else -> {
                        val sharedRank = nextPosition
                        for (p in bucket.sortedBy { it.slot }) {
                            resultStandings.add(
                                p.toStanding(
                                    rank = sharedRank,
                                    isTied = anyMatchesPlayed,
                                    tieBreakBadge = if (anyMatchesPlayed) "Empate exacto" else null
                                )
                            )
                        }
                        nextPosition += bucket.size
                    }
                }
            } else {
                val sharedRank = nextPosition
                for (p in bucket.sortedBy { it.slot }) {
                    resultStandings.add(
                        p.toStanding(
                            rank = sharedRank,
                            isTied = anyMatchesPlayed,
                            tieBreakBadge = if (anyMatchesPlayed) "Empate múltiple" else null
                        )
                    )
                }
                nextPosition += bucket.size
            }
        }

        return resultStandings
    }

    private fun computeGlobalTieBreakBadge(
        player: RawStats,
        nextBucketFirst: RawStats?
    ): String? {
        if (player.matchesPlayed == 0) return null
        if (nextBucketFirst != null && nextBucketFirst.matchesWon == player.matchesWon) {
            return if (player.gamesDiff > nextBucketFirst.gamesDiff) {
                "Desempate DG"
            } else if (player.gamesFor > nextBucketFirst.gamesFor) {
                "Desempate GF"
            } else {
                null
            }
        }
        return null
    }

    private fun compareAsOpponents(
        slotA: Int,
        slotB: Int,
        matches: List<ScheduledMatch>
    ): HeadToHeadComparison {
        var aWins = 0
        var bWins = 0
        var aGames = 0
        var bGames = 0

        for (match in matches) {
            val res = match.result ?: continue
            val aInTeamA = match.isOnTeamA(slotA)
            val aInTeamB = match.isOnTeamB(slotA)
            val bInTeamA = match.isOnTeamA(slotB)
            val bInTeamB = match.isOnTeamB(slotB)

            if (aInTeamA && bInTeamB) {
                aGames += res.teamAGames
                bGames += res.teamBGames
                if (res.teamAGames > res.teamBGames) aWins++ else bWins++
            } else if (aInTeamB && bInTeamA) {
                aGames += res.teamBGames
                bGames += res.teamAGames
                if (res.teamBGames > res.teamAGames) aWins++ else bWins++
            }
        }

        return HeadToHeadComparison(
            slotAWins = aWins,
            slotBWins = bWins,
            slotAGames = aGames,
            slotBGames = bGames
        )
    }

    private fun ScheduledMatch.involves(slot: Int): Boolean =
        isOnTeamA(slot) || isOnTeamB(slot)

    private fun ScheduledMatch.isOnTeamA(slot: Int): Boolean =
        teamA.first == slot || teamA.second == slot

    private fun ScheduledMatch.isOnTeamB(slot: Int): Boolean =
        teamB.first == slot || teamB.second == slot

    private fun RawStats.toStanding(
        rank: Int,
        isTied: Boolean,
        tieBreakBadge: String?
    ): PlayerStanding = PlayerStanding(
        slot = slot,
        name = name,
        rank = rank,
        isTied = isTied,
        tieBreakBadge = tieBreakBadge,
        matchesPlayed = matchesPlayed,
        matchesWon = matchesWon,
        matchesLost = matchesLost,
        gamesFor = gamesFor,
        gamesAgainst = gamesAgainst,
        gamesDiff = gamesDiff,
        roundCells = roundCells,
        court1Count = court1Count,
        court2Count = court2Count
    )

    fun buildShareSummary(
        state: TournamentState,
        standings: List<PlayerStanding> = calculateStandings(state)
    ): String {
        val completed = state.results.size
        val statusLabel = if (completed == TOTAL_MATCHES) {
            "TORNEO FINALIZADO (14/14 partidos)"
        } else {
            "EN JUEGO ($completed/$TOTAL_MATCHES partidos)"
        }

        return buildString {
            appendLine("🎾 *${state.tournamentName.trim().ifEmpty { "Super 8 Pádel" }}*")
            appendLine("📊 $statusLabel")
            appendLine()
            appendLine("*POSICIONES:*")
            for (row in standings) {
                val medal = when (row.rank) {
                    1 -> if (completed > 0) "🥇" else "1."
                    2 -> if (completed > 0) "🥈" else "2."
                    3 -> if (completed > 0) "🥉" else "3."
                    else -> "${row.rank}."
                }
                val dgFormatted = if (row.gamesDiff >= 0) "+${row.gamesDiff}" else "${row.gamesDiff}"
                appendLine(
                    "$medal *${row.name}* — *${row.matchesWon} PG* (${row.matchesPlayed} PJ) | DG $dgFormatted (${row.gamesFor}-${row.gamesAgainst})"
                )
            }
            if (completed > 0) {
                appendLine()
                append("_Desempate: 1° PG · 2° Dif. Games · 3° Games a favor · 4° Cruce rival_")
            }
        }.trim()
    }
}
