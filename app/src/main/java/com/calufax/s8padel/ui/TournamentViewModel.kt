package com.calufax.s8padel.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.calufax.s8padel.data.TournamentRepository
import com.calufax.s8padel.domain.Super8Engine
import com.calufax.s8padel.model.FixtureMode
import com.calufax.s8padel.model.MatchResult
import com.calufax.s8padel.model.Player
import com.calufax.s8padel.model.PlayerStanding
import com.calufax.s8padel.model.RoundSchedule
import com.calufax.s8padel.model.TournamentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TournamentUiState(
    val tournamentState: TournamentState,
    val rounds: List<RoundSchedule>,
    val standings: List<PlayerStanding>,
    val completedMatchesCount: Int,
    val activeRoundNumber: Int
) {
    val isTournamentCompleted: Boolean
        get() = completedMatchesCount == Super8Engine.TOTAL_MATCHES

    val hasAnyResults: Boolean
        get() = completedMatchesCount > 0
}

class TournamentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TournamentRepository(application)

    // Scope de persistencia independiente de la UI para que ningún guardado se cancele al cerrar la app.
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _uiState: MutableStateFlow<TournamentUiState> = MutableStateFlow(
        buildUiState(repository.loadImmediate())
    )
    val uiState: StateFlow<TournamentUiState> = _uiState.asStateFlow()

    fun updateTournamentName(newName: String) {
        val sanitized = newName.take(50)
        mutateAndSave { current ->
            current.copy(tournamentName = sanitized)
        }
    }

    fun updateFixtureMode(mode: FixtureMode) {
        mutateAndSave { current ->
            if (current.fixtureMode == mode) {
                current
            } else {
                current.copy(
                    fixtureMode = mode,
                    results = emptyMap()
                )
            }
        }
    }

    fun updatePlayerName(slot: Int, newName: String) {
        if (slot !in 1..8) return
        val sanitized = newName.take(28)
        mutateAndSave { current ->
            val updatedPlayers = (1..8).map { s ->
                val existing = current.players.firstOrNull { it.slot == s }
                    ?: Player(slot = s, name = "Jugador $s")
                if (s == slot) existing.copy(name = sanitized) else existing
            }
            current.copy(players = updatedPlayers)
        }
    }

    /**
     * Sortea aleatoriamente el orden de los 8 jugadores y reinicia los resultados
     * para que los cruces de las 7 rondas reflejen las nuevas ubicaciones.
     */
    fun shufflePlayersAndResetScores() {
        mutateAndSave { current ->
            val currentNames = (1..8).map { slot ->
                current.players.firstOrNull { it.slot == slot }?.name ?: "Jugador $slot"
            }.shuffled()

            val shuffledPlayers = (1..8).map { slot ->
                Player(slot = slot, name = currentNames[slot - 1])
            }
            current.copy(
                players = shuffledPlayers,
                results = emptyMap()
            )
        }
    }

    fun setMatchResult(matchId: Int, teamAGames: Int, teamBGames: Int) {
        if (matchId !in 1..Super8Engine.TOTAL_MATCHES) return
        if (!MatchResult.isValid(teamAGames, teamBGames)) return

        mutateAndSave { current ->
            val updatedResults = current.results.toMutableMap()
            updatedResults[matchId] = MatchResult(teamAGames = teamAGames, teamBGames = teamBGames)
            current.copy(results = updatedResults)
        }
    }

    fun clearMatchResult(matchId: Int) {
        if (matchId !in 1..Super8Engine.TOTAL_MATCHES) return
        mutateAndSave { current ->
            val updatedResults = current.results.toMutableMap()
            updatedResults.remove(matchId)
            current.copy(results = updatedResults)
        }
    }

    fun toggleRoundCourtSwap(round: Int) {
        if (round !in 1..Super8Engine.TOTAL_ROUNDS) return
        mutateAndSave { current ->
            val updatedSwaps = current.swappedRounds.toMutableSet()
            if (round in updatedSwaps) {
                updatedSwaps.remove(round)
            } else {
                updatedSwaps.add(round)
            }
            current.copy(swappedRounds = updatedSwaps)
        }
    }

    fun resetScoresKeepPlayers() {
        mutateAndSave { current ->
            current.copy(
                results = emptyMap(),
                swappedRounds = TournamentState.DEFAULT_SWAPPED_ROUNDS
            )
        }
    }

    fun resetFullTournament() {
        mutateAndSave {
            TournamentState()
        }
    }

    fun buildShareText(): String {
        val current = _uiState.value
        return Super8Engine.buildShareSummary(
            state = current.tournamentState,
            standings = current.standings
        )
    }

    private fun mutateAndSave(transform: (TournamentState) -> TournamentState) {
        val nextTournamentState = transform(_uiState.value.tournamentState).copy(
            updatedAtMillis = System.currentTimeMillis()
        )
        _uiState.value = buildUiState(nextTournamentState)
        persistenceScope.launch {
            repository.save(nextTournamentState)
        }
    }

    private fun buildUiState(state: TournamentState): TournamentUiState {
        val rounds = Super8Engine.buildSchedule(state)
        val standings = Super8Engine.calculateStandings(state)
        val activeRound = rounds.firstOrNull { !it.isCompleted }?.round ?: Super8Engine.TOTAL_ROUNDS
        return TournamentUiState(
            tournamentState = state,
            rounds = rounds,
            standings = standings,
            completedMatchesCount = state.results.size,
            activeRoundNumber = activeRound
        )
    }
}
