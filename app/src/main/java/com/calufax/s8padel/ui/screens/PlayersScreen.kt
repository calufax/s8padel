package com.calufax.s8padel.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calufax.s8padel.model.FixtureMode
import com.calufax.s8padel.ui.TournamentUiState

private enum class ConfirmDialogType {
    SHUFFLE_WITH_RESULTS,
    SWITCH_FIXTURE_WITH_RESULTS,
    RESET_SCORES,
    RESET_ALL
}

@Composable
fun PlayersScreen(
    uiState: TournamentUiState,
    onUpdateTournamentName: (String) -> Unit,
    onUpdateFixtureMode: (FixtureMode) -> Unit,
    onUpdatePlayerName: (slot: Int, name: String) -> Unit,
    onShufflePlayers: () -> Unit,
    onResetScoresKeepPlayers: () -> Unit,
    onResetFullTournament: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var activeDialog by remember { mutableStateOf<ConfirmDialogType?>(null) }
    var pendingFixtureMode by remember { mutableStateOf<FixtureMode?>(null) }

    val courtCountsBySlot = remember(uiState.standings) {
        uiState.standings.associate { it.slot to (it.court1Count to it.court2Count) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "tournament_title_card") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Configuración del torneo",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectAllOnFocusTextField(
                        value = uiState.tournamentState.tournamentName,
                        onValueChange = onUpdateTournamentName,
                        label = { Text("Nombre del torneo") },
                        placeholder = { Text("Super 8 Pádel") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        trailingIcon = {
                            if (uiState.tournamentState.tournamentName.isNotEmpty()) {
                                IconButton(onClick = { onUpdateTournamentName("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar nombre del torneo"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Armado de cruces (7 rondas)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentMode = uiState.tournamentState.fixtureMode
                        FilterChip(
                            selected = currentMode == FixtureMode.BALANCED_WHIST,
                            onClick = {
                                if (currentMode != FixtureMode.BALANCED_WHIST) {
                                    if (uiState.hasAnyResults) {
                                        pendingFixtureMode = FixtureMode.BALANCED_WHIST
                                        activeDialog = ConfirmDialogType.SWITCH_FIXTURE_WITH_RESULTS
                                    } else {
                                        onUpdateFixtureMode(FixtureMode.BALANCED_WHIST)
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = "Super 8 Balanceado",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                        FilterChip(
                            selected = currentMode == FixtureMode.CLASSIC_SHEET,
                            onClick = {
                                if (currentMode != FixtureMode.CLASSIC_SHEET) {
                                    if (uiState.hasAnyResults) {
                                        pendingFixtureMode = FixtureMode.CLASSIC_SHEET
                                        activeDialog = ConfirmDialogType.SWITCH_FIXTURE_WITH_RESULTS
                                    } else {
                                        onUpdateFixtureMode(FixtureMode.CLASSIC_SHEET)
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = "Planilla clásica",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                    Text(
                        text = if (uiState.tournamentState.fixtureMode == FixtureMode.BALANCED_WHIST) {
                            "1 vez de compañero y exactamente 2 veces en contra de cada rival."
                        } else {
                            "Cruces exactos de la hoja impresa clásica (1 vez con cada compañero)."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "players_header_and_shuffle") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Los 8 Jugadores",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Se guarda automáticamente al escribir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        if (uiState.hasAnyResults) {
                            activeDialog = ConfirmDialogType.SHUFFLE_WITH_RESULTS
                        } else {
                            onShufflePlayers()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sortear orden")
                }
            }
        }

        items(count = 8, key = { index -> "player_slot_${index + 1}" }) { index ->
            val slot = index + 1
            val player = uiState.tournamentState.players.firstOrNull { it.slot == slot }
            val rawName = player?.name ?: ""
            val courts = courtCountsBySlot[slot] ?: (4 to 3)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$slot",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                SelectAllOnFocusTextField(
                    value = rawName,
                    onValueChange = { onUpdatePlayerName(slot, it) },
                    placeholder = { Text("Jugador $slot") },
                    singleLine = true,
                    supportingText = {
                        Text("Cancha 1: ${courts.first} part. · Cancha 2: ${courts.second} part.")
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = if (slot < 8) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = { focusManager.clearFocus() }
                    ),
                    trailingIcon = {
                        if (rawName.isNotEmpty()) {
                            IconButton(onClick = { onUpdatePlayerName(slot, "") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar jugador $slot"
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item(key = "reset_actions") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Gestión del torneo",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toda la información queda guardada localmente en tu teléfono aunque cierres la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { activeDialog = ConfirmDialogType.RESET_SCORES },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reiniciar partidos (mantener nombres)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { activeDialog = ConfirmDialogType.RESET_ALL },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nuevo torneo desde cero")
                    }
                }
            }
        }

        item(key = "bottom_pad") {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    when (activeDialog) {
        ConfirmDialogType.SHUFFLE_WITH_RESULTS -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("¿Sortear jugadores y reiniciar resultados?") },
                text = {
                    Text("Ya hay partidos cargados. Si sorteás el orden de los 8 jugadores ahora, los resultados actuales se pondrán en cero para comenzar con los nuevos cruces.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onShufflePlayers()
                            activeDialog = null
                        }
                    ) {
                        Text("Sortear y reiniciar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        ConfirmDialogType.SWITCH_FIXTURE_WITH_RESULTS -> {
            AlertDialog(
                onDismissRequest = {
                    pendingFixtureMode = null
                    activeDialog = null
                },
                title = { Text("¿Cambiar formato de cruces?") },
                text = {
                    Text("Cambiar el formato de cruces reiniciará los resultados cargados hasta el momento, manteniendo los nombres de los 8 jugadores.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingFixtureMode?.let(onUpdateFixtureMode)
                            pendingFixtureMode = null
                            activeDialog = null
                        }
                    ) {
                        Text("Cambiar y reiniciar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            pendingFixtureMode = null
                            activeDialog = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        ConfirmDialogType.RESET_SCORES -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("¿Reiniciar resultados de partidos?") },
                text = {
                    Text("Se borrarán los resultados de las 7 rondas, conservando los nombres actuales de los 8 jugadores.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetScoresKeepPlayers()
                            activeDialog = null
                        }
                    ) {
                        Text("Reiniciar partidos")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        ConfirmDialogType.RESET_ALL -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("¿Comenzar torneo desde cero?") },
                text = {
                    Text("Se borrarán todos los resultados y se restablecerán los nombres por defecto (Jugador 1 al 8).")
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            onResetFullTournament()
                            activeDialog = null
                        }
                    ) {
                        Text("Borrar todo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        null -> Unit
    }
}

@Composable
private fun SelectAllOnFocusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange.Zero))
    }
    var wasFocused by remember { mutableStateOf(false) }
    var justFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = if (wasFocused) {
                    TextRange(0, value.length)
                } else {
                    TextRange(value.length)
                }
            )
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            if (justFocused && newValue.text == textFieldValue.text) {
                // Al tocar el campo por primera vez, el gesto del puntero de Compose intenta
                // colapsar el cursor en la posición tocada. Si acabamos de enfocar y el texto
                // no cambió, forzamos a seleccionar todo el contenido para que el usuario
                // pueda tipear por encima de inmediato.
                justFocused = false
                textFieldValue = newValue.copy(
                    selection = TextRange(0, newValue.text.length)
                )
            } else {
                justFocused = false
                textFieldValue = newValue
                if (newValue.text != value) {
                    onValueChange(newValue.text)
                }
            }
        },
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier.onFocusChanged { focusState ->
            if (focusState.isFocused) {
                if (!wasFocused) {
                    wasFocused = true
                    justFocused = true
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                }
            } else {
                wasFocused = false
                justFocused = false
            }
        }
    )
}
