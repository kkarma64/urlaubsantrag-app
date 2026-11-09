package de.bauhaupt.urlaubsschein.ui.wizard

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.ui.components.CustomDatePickerDialog
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.components.PrivacyDialog
import de.bauhaupt.urlaubsschein.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardStep1Screen(
    viewModel: WizardViewModel,
    onNextClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy", Locale.GERMAN)
    val rueckkehrFormatter = DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy", Locale.GERMAN)

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Triple: (periodIndex, isVon, currentDate)
    var activeDatePicker by remember { mutableStateOf<Triple<Int, Boolean, LocalDate>?>(null) }

    GridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Neuer Antrag (1/3)",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showPrivacyDialog = true }) {
                    Text("🔒 Datenschutz", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
            }

            LinearProgressIndicator(
                progress = { 0.33f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
                strokeCap = StrokeCap.Round
            )

            // --- URLAUBSART AUSWAHL ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("URLAUBSART WÄHLEN", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setUrlaubsArt("Tarifurlaub") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.urlaubsArt == "Tarifurlaub") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Tarifurlaub",
                                color = if (state.urlaubsArt == "Tarifurlaub") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.setUrlaubsArt("Sonderurlaub") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.urlaubsArt == "Sonderurlaub") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Sonderurlaub",
                                color = if (state.urlaubsArt == "Sonderurlaub") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (state.urlaubsArt == "Sonderurlaub") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Grund für Sonderurlaub:", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Box {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.sonderurlaubGrund.ifBlank { "Bitte Grund wählen..." },
                                        color = if (state.sonderurlaubGrund.isBlank()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text("▼", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                SonderurlaubGründe.alle().forEach { grund ->
                                    DropdownMenuItem(
                                        text = { Text(text = grund, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) },
                                        onClick = {
                                            viewModel.setSonderurlaubGrund(grund)
                                            dropdownExpanded = false
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- ZEITRÄUME (BIS ZU 3) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("URLAUBSZEITRÄUME", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${state.urlaubsPerioden.size} / 3", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                    }

                    state.urlaubsPerioden.forEachIndexed { index, periode ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Zeitraum ${index + 1}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (state.urlaubsPerioden.size > 1) {
                                        TextButton(onClick = { viewModel.removeUrlaubsperiode(index) }) {
                                            Text("Entfernen", color = Color.Red, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { activeDatePicker = Triple(index, true, periode.von) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Von", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(periode.von.format(dateFormatter), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { activeDatePicker = Triple(index, false, periode.bis) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Bis", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(periode.bis.format(dateFormatter), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Text(
                                    text = "Dauer: ${periode.tage} Arbeitstage",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (state.urlaubsPerioden.size < 3) {
                        OutlinedButton(
                            onClick = { viewModel.addUrlaubsperiode() },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("+ Weiteren Zeitraum hinzufügen (Splitten)", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- BERECHNUNG & RÜCKKEHR ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("URLAUBSANSPRUCH & RÜCKKEHR", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gesamt Urlaubstage:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text("${state.berechneteTage} Arbeitstage", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Erster Arbeitstag nach Urlaub:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text(state.rueckkehrDatum.format(rueckkehrFormatter), color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    TextButton(onClick = { showInfoDialog = true }) {
                        Text("ℹ️ Wie werden Feiertage & Samstage berechnet?", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }

            Button(
                onClick = onNextClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Weiter zu Details ➔", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Berechnungshinweis", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    "Gemäß BRTV-Bau werden Arbeitstage von Montag bis Freitag gezählt. Gesetzliche Feiertage des gewählten Bundeslandes sowie Samstage und Sonntage werden automatisch als arbeitsfrei berücksichtigt.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Verstanden", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    activeDatePicker?.let { (index, isVon, currentDate) ->
        CustomDatePickerDialog(
            initialDate = currentDate,
            onDateSelected = { selectedDate ->
                if (isVon) {
                    viewModel.updateUrlaubsperiodeVon(index, selectedDate)
                } else {
                    viewModel.updateUrlaubsperiodeBis(index, selectedDate)
                }
                activeDatePicker = null
            },
            onDismiss = { activeDatePicker = null }
        )
    }
}
