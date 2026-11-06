package com.calufax.s8padel

import com.calufax.s8padel.data.TournamentJsonSerializer
import com.calufax.s8padel.domain.Super8Engine
import com.calufax.s8padel.model.FixtureMode
import com.calufax.s8padel.model.MatchResult
import com.calufax.s8padel.model.Player
import com.calufax.s8padel.model.TournamentState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Super8EngineTest {

    @Test
    fun `balanced whist fixture guarantees every pair partners once and faces each other twice`() {
        val schedule = Super8Engine.buildSchedule(
            TournamentState(fixtureMode = FixtureMode.BALANCED_WHIST)
        )
        assertEquals(7, schedule.size)

        val partnerCounts = mutableMapOf<Pair<Int, Int>, Int>()
        val opponentCounts = mutableMapOf<Pair<Int, Int>, Int>()

        fun canonical(a: Int, b: Int): Pair<Int, Int> = if (a < b) a to b else b to a

        for (round in schedule) {
            assertEquals(2, round.matches.size)
            assertEquals(listOf(1, 2), round.matches.map { it.effectiveCourt })

            val playersInRound = round.matches.flatMap {
                listOf(it.teamA.first, it.teamA.second, it.teamB.first, it.teamB.second)
            }
            assertEquals((1..8).toList(), playersInRound.sorted())

            for (match in round.matches) {
                val pA = canonical(match.teamA.first, match.teamA.second)
                val pB = canonical(match.teamB.first, match.teamB.second)
                partnerCounts[pA] = (partnerCounts[pA] ?: 0) + 1
                partnerCounts[pB] = (partnerCounts[pB] ?: 0) + 1

                for (a in listOf(match.teamA.first, match.teamA.second)) {
                    for (b in listOf(match.teamB.first, match.teamB.second)) {
                        val opp = canonical(a, b)
                        opponentCounts[opp] = (opponentCounts[opp] ?: 0) + 1
                    }
                }
            }
        }

        // Existen C(8, 2) = 28 pares posibles
        assertEquals(28, partnerCounts.size)
        assertEquals(28, opponentCounts.size)
        assertTrue(partnerCounts.values.all { it == 1 })
        assertTrue(opponentCounts.values.all { it == 2 })
    }

    @Test
    fun `classic sheet fixture guarantees every pair partners once and balances courts`() {
        val state = TournamentState(fixtureMode = FixtureMode.CLASSIC_SHEET)
        val schedule = Super8Engine.buildSchedule(state)
        assertEquals(7, schedule.size)

        val partnerCounts = mutableMapOf<Pair<Int, Int>, Int>()
        fun canonical(a: Int, b: Int): Pair<Int, Int> = if (a < b) a to b else b to a

        for (round in schedule) {
            for (match in round.matches) {
                val pA = canonical(match.teamA.first, match.teamA.second)
                val pB = canonical(match.teamB.first, match.teamB.second)
                partnerCounts[pA] = (partnerCounts[pA] ?: 0) + 1
                partnerCounts[pB] = (partnerCounts[pB] ?: 0) + 1
            }
        }

        assertEquals(28, partnerCounts.size)
        assertTrue(partnerCounts.values.all { it == 1 })

        val standings = Super8Engine.calculateStandings(state)
        assertTrue(standings.all { it.court1Count in 2..5 && it.court2Count in 2..5 })
    }

    @Test
    fun `match result validation accepts only 4-0 to 4-3 scores`() {
        for (loser in 0..3) {
            assertTrue(MatchResult.isValid(4, loser))
            assertTrue(MatchResult.isValid(loser, 4))
        }
        assertFalse(MatchResult.isValid(4, 4))
        assertFalse(MatchResult.isValid(5, 3))
        assertFalse(MatchResult.isValid(3, 2))
        assertFalse(MatchResult.isValid(4, -1))
    }

    @Test
    fun `initial standings start with rank 1 for all 8 players`() {
        val standings = Super8Engine.calculateStandings(TournamentState())
        assertEquals(8, standings.size)
        assertTrue(standings.all { it.rank == 1 && it.matchesWon == 0 && it.gamesDiff == 0 })
    }

    @Test
    fun `tie breakers resolve by PG then DG then GF then opponent head-to-head`() {
        val baseClassic = TournamentState(fixtureMode = FixtureMode.CLASSIC_SHEET)

        // Ronda 1:
        // Match 1: (1,8) vs (7,2) -> 4 - 1 (1 y 8 ganan +3 DG; 7 y 2 pierden -3 DG)
        // Match 2: (6,3) vs (5,4) -> 4 - 3 (6 y 3 ganan +1 DG; 5 y 4 pierden -1 DG)
        val stateR1 = baseClassic.copy(
            results = mapOf(
                1 to MatchResult(4, 1),
                2 to MatchResult(4, 3)
            )
        )
        val standingsR1 = Super8Engine.calculateStandings(stateR1)

        assertEquals(setOf(1, 8), standingsR1.take(2).map { it.slot }.toSet())
        assertEquals(1, standingsR1[0].rank)
        assertEquals(1, standingsR1[1].rank)

        assertEquals(setOf(3, 6), standingsR1.subList(2, 4).map { it.slot }.toSet())
        assertEquals(3, standingsR1[2].rank)
        assertEquals(3, standingsR1[3].rank)

        // Desempate por 3° criterio (Games a Favor GF):
        val stateGfTieBreak = baseClassic.copy(
            results = mapOf(
                1 to MatchResult(2, 4),
                4 to MatchResult(3, 4),
                5 to MatchResult(1, 4),
                6 to MatchResult(4, 3)
            )
        )
        val standingsGf = Super8Engine.calculateStandings(stateGfTieBreak)
        val pos7 = standingsGf.indexOfFirst { it.slot == 7 }
        val pos1 = standingsGf.indexOfFirst { it.slot == 1 }
        assertTrue("Jugador 7 debe superar a Jugador 1 por mayor GF (11 vs 10)", pos7 < pos1)

        // Desempate por 4° criterio (Cruce directo en contra):
        val stateH2H = baseClassic.copy(
            results = mapOf(
                1 to MatchResult(0, 4),
                4 to MatchResult(4, 2),
                5 to MatchResult(1, 4),
                6 to MatchResult(1, 4),
                7 to MatchResult(4, 1),
                8 to MatchResult(4, 1)
            )
        )
        val standingsH2H = Super8Engine.calculateStandings(stateH2H)
        val s7 = standingsH2H.first { it.slot == 7 }
        val s1 = standingsH2H.first { it.slot == 1 }
        assertEquals(2, s7.matchesWon)
        assertEquals(2, s1.matchesWon)
        assertEquals(0, s7.gamesDiff)
        assertEquals(0, s1.gamesDiff)
        assertEquals(10, s7.gamesFor)
        assertEquals(10, s1.gamesFor)
        assertTrue("Jugador 7 debe superar a Jugador 1 por cruce rival 2-0", s7.rank < s1.rank)
        assertEquals("Cruce rival (2-0)", s7.tieBreakBadge)
    }

    @Test
    fun `json serializer round-trips state with accents quotes fixture mode and results intact`() {
        val original = TournamentState(
            tournamentName = "Super 8 \"Nocturno\" - Club Norte",
            fixtureMode = FixtureMode.CLASSIC_SHEET,
            players = listOf(
                Player(1, "Martín \"Tincho\""),
                Player(2, "Sebastián"),
                Player(3, "Nico \\ Zurdo"),
                Player(4, "Agustín"),
                Player(5, "Julián"),
                Player(6, "Fede"),
                Player(7, "Tomás"),
                Player(8, "Gonzalo")
            ),
            results = mapOf(
                1 to MatchResult(4, 2),
                2 to MatchResult(3, 4),
                14 to MatchResult(4, 0)
            ),
            swappedRounds = setOf(2, 4, 6),
            updatedAtMillis = 1793966000000L
        )

        val json = TournamentJsonSerializer.encode(original)
        val restored = TournamentJsonSerializer.decode(json)

        assertEquals(original, restored)
    }
}
