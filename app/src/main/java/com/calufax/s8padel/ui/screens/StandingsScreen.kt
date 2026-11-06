package com.calufax.s8padel.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calufax.s8padel.domain.Super8Engine
import com.calufax.s8padel.model.PlayerStanding
import com.calufax.s8padel.ui.TournamentUiState

private enum class StandingsViewMode {
    LEADERBOARD,
    ROUND_SHEET
}

@Composable
fun StandingsScreen(
    uiState: TournamentUiState,
    modifier: Modifier = Modifier
) {
    var viewMode by rememberSaveable { mutableStateOf(StandingsViewMode.LEADERBOARD) }
    var showRules by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.hasAnyResults) {
            item(key = "podium_banner") {
                PodiumCard(
                    standings = uiState.standings,
                    isFinal = uiState.isTournamentCompleted
                )
            }
        }

        item(key = "controls_row") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = viewMode == StandingsViewMode.LEADERBOARD,
                    onClick = { viewMode = StandingsViewMode.LEADERBOARD },
                    label = { Text("Tabla general", maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                FilterChip(
                    selected = viewMode == StandingsViewMode.ROUND_SHEET,
                    onClick = { viewMode = StandingsViewMode.ROUND_SHEET },
                    label = { Text("Planilla R1–R7", maxLines = 1) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        if (viewMode == StandingsViewMode.LEADERBOARD) {
            item(key = "table_header") {
                LeaderboardHeaderRow()
            }

            items(
                items = uiState.standings,
                key = { "standing_${it.slot}" }
            ) { row ->
                LeaderboardPlayerRow(
                    standing = row,
                    hasAnyResults = uiState.hasAnyResults
                )
            }
        } else {
            item(key = "classic_sheet") {
                ClassicAmericanoSheetCard(standings = uiState.standings)
            }
        }

        item(key = "tiebreak_rules") {
            TieBreakRulesCard(
                expanded = showRules,
                onToggle = { showRules = !showRules }
            )
        }

        item(key = "bottom_space") {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PodiumCard(
    standings: List<PlayerStanding>,
    isFinal: Boolean
) {
    val top3 = standings.take(3)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFinal) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isFinal) "🏆 Podio Final del Super 8" else "⚡ Líderes en vivo",
                style = MaterialTheme.typography.titleMedium,
                color = if (isFinal) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                top3.forEach { player ->
                    val medal = when (player.rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "#${player.rank}"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(text = medal, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val dg = if (player.gamesDiff >= 0) "+${player.gamesDiff}" else "${player.gamesDiff}"
                            Text(
                                text = "${player.matchesWon} PG · DG $dg",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardHeaderRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = "POS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(38.dp)
        )
        Text(
            text = "JUGADOR",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        StatHeaderCell("PJ", 34.dp)
        StatHeaderCell("PG", 42.dp)
        StatHeaderCell("DG", 44.dp)
        StatHeaderCell("GF:GC", 54.dp)
    }
}

@Composable
private fun StatHeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun LeaderboardPlayerRow(
    standing: PlayerStanding,
    hasAnyResults: Boolean
) {
    val isLeader = hasAnyResults && standing.rank == 1
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isLeader) 1.5.dp else 1.dp,
            color = if (isLeader) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            RankBadge(rank = standing.rank, highlight = hasAnyResults)

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = standing.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isLeader) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${standing.slot}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (standing.tieBreakBadge != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = standing.tieBreakBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (standing.isTied) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            // PJ
            Text(
                text = "${standing.matchesPlayed}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(34.dp)
            )

            // PG (Puntaje principal resaltado)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "${standing.matchesWon}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // DG
            val dgText = when {
                standing.gamesDiff > 0 -> "+${standing.gamesDiff}"
                else -> "${standing.gamesDiff}"
            }
            val dgColor = when {
                standing.gamesDiff > 0 -> MaterialTheme.colorScheme.primary
                standing.gamesDiff < 0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = dgText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = dgColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(44.dp)
            )

            // GF:GC
            Text(
                text = "${standing.gamesFor}:${standing.gamesAgainst}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(54.dp)
            )
        }
    }
}

@Composable
private fun RankBadge(
    rank: Int,
    highlight: Boolean
) {
    val bgColor = if (!highlight) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        when (rank) {
            1 -> Color(0xFFFFD54F)
            2 -> Color(0xFFCFD8DC)
            3 -> Color(0xFFFFCCBC)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    }
    val textColor = if (!highlight) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        when (rank) {
            1, 2, 3 -> Color(0xFF1B1B1B)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bgColor)
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

/**
 * Réplica digital interactiva de la planilla clásica "AMERICANO PADEL 8 JUGADORES"
 * mostrando los games obtenidos por cada jugador en cada una de las 7 rondas, PG, GF y Posición.
 */
@Composable
private fun ClassicAmericanoSheetCard(standings: List<PlayerStanding>) {
    val scrollState = rememberScrollState()
    val sortedBySlot = remember(standings) { standings.sortedBy { it.slot } }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Planilla por Ronda (Games por partido)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "En verde los partidos ganados (4 games). Deslizá horizontalmente para ver todas las columnas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.horizontalScroll(scrollState)) {
                // Encabezado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    SheetCellHeader("JUGADOR", 135.dp, TextAlign.Start)
                    for (r in 1..Super8Engine.TOTAL_ROUNDS) {
                        SheetCellHeader("R$r", 36.dp)
                    }
                    SheetCellHeader("PG", 42.dp)
                    SheetCellHeader("GF", 42.dp)
                    SheetCellHeader("POS", 42.dp)
                }

                sortedBySlot.forEachIndexed { idx, player ->
                    if (idx > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = "${player.slot}. ${player.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(135.dp)
                        )

                        player.roundCells.forEach { cell ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(36.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            cell == null -> Color.Transparent
                                            cell.won -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        }
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = cell?.gamesFor?.toString() ?: "—",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (cell?.won == true) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = when {
                                        cell == null -> MaterialTheme.colorScheme.outline
                                        cell.won -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        Text(
                            text = "${player.matchesWon}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(42.dp)
                        )
                        Text(
                            text = "${player.gamesFor}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(42.dp)
                        )
                        Text(
                            text = "${player.rank}°",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(42.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetCellHeader(
    text: String,
    width: Dp,
    align: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        textAlign = align,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun TieBreakRulesCard(
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¿Cómo se ordena y desempata la tabla?",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    RuleBullet("1° Partidos Ganados (PG):", "Cada jugador suma 1 punto por cada partido ganado (al llegar a 4 games).")
                    Spacer(modifier = Modifier.height(6.dp))
                    RuleBullet("2° Diferencia de Games (DG):", "Games ganados menos games perdidos (GF - GC) a lo largo del torneo.")
                    Spacer(modifier = Modifier.height(6.dp))
                    RuleBullet("3° Games a Favor (GF):", "Mayor cantidad total de games ganados.")
                    Spacer(modifier = Modifier.height(6.dp))
                    RuleBullet("4° Cruce directo como rivales:", "En un Super 8, cada par de jugadores se enfrenta 2 veces en contra. Si 2 jugadores empatan en PG, DG y GF, desempata quién ganó más veces y sacó mejor diferencia de games en esos 2 duelos.")
                    Spacer(modifier = Modifier.height(6.dp))
                    RuleBullet("5° Igualdad absoluta:", "Si persiste empate exacto al cerrar las 7 rondas, comparten puesto o se define con 1 game / punto de oro en cancha.")
                }
            }
        }
    }
}

@Composable
private fun RuleBullet(title: String, detail: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
