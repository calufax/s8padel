package com.calufax.s8padel.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calufax.s8padel.domain.Super8Engine
import com.calufax.s8padel.model.RoundSchedule
import com.calufax.s8padel.model.ScheduledMatch
import com.calufax.s8padel.ui.TournamentUiState
import com.calufax.s8padel.ui.theme.Court1AccentDark
import com.calufax.s8padel.ui.theme.Court1AccentLight
import com.calufax.s8padel.ui.theme.Court1ContainerDark
import com.calufax.s8padel.ui.theme.Court1ContainerLight
import com.calufax.s8padel.ui.theme.Court2AccentDark
import com.calufax.s8padel.ui.theme.Court2AccentLight
import com.calufax.s8padel.ui.theme.Court2ContainerDark
import com.calufax.s8padel.ui.theme.Court2ContainerLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    uiState: TournamentUiState,
    onSelectScore: (matchId: Int, teamAGames: Int, teamBGames: Int) -> Unit,
    onClearScore: (matchId: Int) -> Unit,
    onToggleCourtSwap: (round: Int) -> Unit,
    onGoToStandings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var editingMatchId by remember { mutableStateOf<Int?>(null) }

    val editingMatch = remember(editingMatchId, uiState.rounds) {
        if (editingMatchId == null) null
        else uiState.rounds.flatMap { it.matches }.firstOrNull { it.id == editingMatchId }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "progress_header") {
            TournamentProgressCard(
                completedMatches = uiState.completedMatchesCount,
                activeRound = uiState.activeRoundNumber,
                isCompleted = uiState.isTournamentCompleted,
                onGoToStandings = onGoToStandings
            )
        }

        item(key = "round_selector") {
            RoundJumpRow(
                rounds = uiState.rounds,
                activeRound = uiState.activeRoundNumber,
                onRoundClick = { roundNumber ->
                    coroutineScope.launch {
                        // +2 por header y barra de rondas
                        listState.animateScrollToItem(index = (roundNumber - 1) + 2)
                    }
                }
            )
        }

        items(
            items = uiState.rounds,
            key = { "round_${it.round}" }
        ) { roundSchedule ->
            RoundCard(
                roundSchedule = roundSchedule,
                isActiveRound = !uiState.isTournamentCompleted && roundSchedule.round == uiState.activeRoundNumber,
                playerName = { slot -> uiState.tournamentState.playerName(slot) },
                onMatchClick = { match -> editingMatchId = match.id },
                onSwapCourts = { onToggleCourtSwap(roundSchedule.round) }
            )
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (editingMatch != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { editingMatchId = null },
            sheetState = sheetState
        ) {
            ScorePickerSheetContent(
                match = editingMatch,
                playerName = { slot -> uiState.tournamentState.playerName(slot) },
                onPickResult = { aGames, bGames ->
                    onSelectScore(editingMatch.id, aGames, bGames)
                    editingMatchId = null
                },
                onClearResult = {
                    onClearScore(editingMatch.id)
                    editingMatchId = null
                }
            )
        }
    }
}

@Composable
private fun TournamentProgressCard(
    completedMatches: Int,
    activeRound: Int,
    isCompleted: Boolean,
    onGoToStandings: () -> Unit
) {
    val progress = completedMatches.toFloat() / Super8Engine.TOTAL_MATCHES.toFloat()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCompleted) {
                            "¡Torneo finalizado!"
                        } else {
                            "Ronda en juego: $activeRound de ${Super8Engine.TOTAL_ROUNDS}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedMatches de ${Super8Engine.TOTAL_MATCHES} partidos cargados · A 4 games",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isCompleted) {
                    Button(
                        onClick = onGoToStandings,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver tabla")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun RoundJumpRow(
    rounds: List<RoundSchedule>,
    activeRound: Int,
    onRoundClick: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(rounds, key = { it.round }) { round ->
            FilterChip(
                selected = round.round == activeRound,
                onClick = { onRoundClick(round.round) },
                label = { Text("R${round.round}") },
                leadingIcon = if (round.isCompleted) {
                    {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ronda completada",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun RoundCard(
    roundSchedule: RoundSchedule,
    isActiveRound: Boolean,
    playerName: (Int) -> String,
    onMatchClick: (ScheduledMatch) -> Unit,
    onSwapCourts: () -> Unit
) {
    val border = if (isActiveRound) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActiveRound) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ronda ${roundSchedule.round}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val statusText = when {
                        roundSchedule.isCompleted -> "Completada"
                        isActiveRound -> "En juego"
                        else -> "Pendiente"
                    }
                    val badgeContainer = when {
                        roundSchedule.isCompleted -> MaterialTheme.colorScheme.primaryContainer
                        isActiveRound -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val badgeContent = when {
                        roundSchedule.isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
                        isActiveRound -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Surface(
                        color = badgeContainer,
                        contentColor = badgeContent,
                        shape = CircleShape
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                TextButton(
                    onClick = onSwapCourts,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Invertir Cancha 1 y Cancha 2",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Canchas",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            roundSchedule.matches.forEachIndexed { index, match ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                MatchCourtItem(
                    match = match,
                    playerName = playerName,
                    onClick = { onMatchClick(match) }
                )
            }
        }
    }
}

@Composable
private fun MatchCourtItem(
    match: ScheduledMatch,
    playerName: (Int) -> String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val courtAccent = if (match.effectiveCourt == 1) {
        if (isDark) Court1AccentDark else Court1AccentLight
    } else {
        if (isDark) Court2AccentDark else Court2AccentLight
    }
    val courtBg = if (match.effectiveCourt == 1) {
        if (isDark) Court1ContainerDark else Court1ContainerLight
    } else {
        if (isDark) Court2ContainerDark else Court2ContainerLight
    }

    val result = match.result

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = if (result != null) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = courtBg,
                    contentColor = courtAccent,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "CANCHA ${match.effectiveCourt}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (result != null) "Editar resultado" else "Tocar para cargar",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (result != null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (result != null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TeamScoreRow(
                slot1 = match.teamA.first,
                name1 = playerName(match.teamA.first),
                slot2 = match.teamA.second,
                name2 = playerName(match.teamA.second),
                games = result?.teamAGames,
                isWinner = result?.isTeamAWinner == true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            )

            TeamScoreRow(
                slot1 = match.teamB.first,
                name1 = playerName(match.teamB.first),
                slot2 = match.teamB.second,
                name2 = playerName(match.teamB.second),
                games = result?.teamBGames,
                isWinner = result?.isTeamBWinner == true
            )
        }
    }
}

@Composable
private fun TeamScoreRow(
    slot1: Int,
    name1: String,
    slot2: Int,
    name2: String,
    games: Int?,
    isWinner: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            PlayerSlotInline(slot = slot1, name = name1, bold = isWinner)
            Spacer(modifier = Modifier.height(3.dp))
            PlayerSlotInline(slot = slot2, name = name2, bold = isWinner)
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (games != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isWinner) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Text(
                    text = games.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isWinner) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun PlayerSlotInline(
    slot: Int,
    name: String,
    bold: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CircleShape
        ) {
            Text(
                text = "#$slot",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ScorePickerSheetContent(
    match: ScheduledMatch,
    playerName: (Int) -> String,
    onPickResult: (teamAGames: Int, teamBGames: Int) -> Unit,
    onClearResult: () -> Unit
) {
    val teamALabel = "${playerName(match.teamA.first)} / ${playerName(match.teamA.second)}"
    val teamBLabel = "${playerName(match.teamB.first)} / ${playerName(match.teamB.second)}"
    val current = match.result

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "Ronda ${match.round} · Cancha ${match.effectiveCourt}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Seleccioná el resultado final",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Partido a 4 games (en 3-3 se juega el 7° game normal y cierra 4-3).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Columna Ganó Pareja A
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "GANA PAREJA 1",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = teamALabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                listOf(0, 1, 2, 3).forEach { loserGames ->
                    val isSelected = current?.teamAGames == 4 && current.teamBGames == loserGames
                    ScoreOptionButton(
                        label = "4  -  $loserGames",
                        selected = isSelected,
                        onClick = { onPickResult(4, loserGames) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Columna Ganó Pareja B
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "GANA PAREJA 2",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = teamBLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                listOf(0, 1, 2, 3).forEach { loserGames ->
                    val isSelected = current?.teamAGames == loserGames && current.teamBGames == 4
                    ScoreOptionButton(
                        label = "$loserGames  -  4",
                        selected = isSelected,
                        onClick = { onPickResult(loserGames, 4) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (current != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClearResult,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Borrar resultado cargado")
            }
        }
    }
}

@Composable
private fun ScoreOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
