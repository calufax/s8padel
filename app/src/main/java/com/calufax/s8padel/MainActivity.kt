package com.calufax.s8padel

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calufax.s8padel.domain.Super8Engine
import com.calufax.s8padel.ui.TournamentViewModel
import com.calufax.s8padel.ui.screens.MatchesScreen
import com.calufax.s8padel.ui.screens.PlayersScreen
import com.calufax.s8padel.ui.screens.StandingsScreen
import com.calufax.s8padel.ui.theme.S8PadelTheme

private enum class AppTab(val label: String) {
    MATCHES("Partidos"),
    STANDINGS("Posiciones"),
    PLAYERS("Jugadores")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            S8PadelTheme {
                S8PadelApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun S8PadelApp(
    viewModel: TournamentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(AppTab.MATCHES) }
    val context = LocalContext.current

    val titleText = uiState.tournamentState.tournamentName.trim().ifEmpty { "Super 8 Pádel" }

    val shareStandings = {
        val summary = viewModel.buildShareText()
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        context.startActivity(
            Intent.createChooser(sendIntent, "Compartir resultados del Super 8")
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "8 jugadores · 2 canchas · 7 rondas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (currentTab == AppTab.STANDINGS) {
                        IconButton(onClick = shareStandings) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir posiciones",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == AppTab.MATCHES,
                    onClick = { currentTab = AppTab.MATCHES },
                    icon = {
                        BadgedBox(
                            badge = {
                                val remaining = Super8Engine.TOTAL_MATCHES - uiState.completedMatchesCount
                                if (remaining > 0) {
                                    Badge { Text("$remaining") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsTennis,
                                contentDescription = AppTab.MATCHES.label
                            )
                        }
                    },
                    label = { Text(AppTab.MATCHES.label) }
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.STANDINGS,
                    onClick = { currentTab = AppTab.STANDINGS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = AppTab.STANDINGS.label
                        )
                    },
                    label = { Text(AppTab.STANDINGS.label) }
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.PLAYERS,
                    onClick = { currentTab = AppTab.PLAYERS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = AppTab.PLAYERS.label
                        )
                    },
                    label = { Text(AppTab.PLAYERS.label) }
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.MATCHES -> {
                MatchesScreen(
                    uiState = uiState,
                    onSelectScore = viewModel::setMatchResult,
                    onClearScore = viewModel::clearMatchResult,
                    onToggleCourtSwap = viewModel::toggleRoundCourtSwap,
                    onGoToStandings = { currentTab = AppTab.STANDINGS },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppTab.STANDINGS -> {
                StandingsScreen(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppTab.PLAYERS -> {
                PlayersScreen(
                    uiState = uiState,
                    onUpdateTournamentName = viewModel::updateTournamentName,
                    onUpdateFixtureMode = viewModel::updateFixtureMode,
                    onUpdatePlayerName = viewModel::updatePlayerName,
                    onShufflePlayers = viewModel::shufflePlayersAndResetScores,
                    onResetScoresKeepPlayers = viewModel::resetScoresKeepPlayers,
                    onResetFullTournament = viewModel::resetFullTournament,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
