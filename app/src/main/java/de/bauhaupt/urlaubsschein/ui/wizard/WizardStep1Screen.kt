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
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
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

            Text("URLAUBSART WÄHLEN", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    UrlaubsArtButton(
                        text = "Tarifurlaub (BRTV Bau)",
                        isSelected = state.urlaubsArt == "Tarifurlaub",
                        onClick = { viewModel.setUrlaubsArt("Tarifurlaub") },
                        modifier = Modifier.weight(1f)
                    )
                    UrlaubsArtButton(
                        text = "Sonderurlaub",
                        isSelected = state.urlaubsArt == "Sonderurlaub",
                        onClick = { viewModel.setUrlaubsArt("Sonderurlaub") },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                UrlaubsArtButton(
                    text = "Tarifurlaub (BRTV Bau)",
                    isSelected = state.urlaubsArt == "Tarifurlaub",
                    onClick = { viewModel.setUrlaubsArt("Tarifurlaub") }
                )
                UrlaubsArtButton(
                    text = "Sonderurlaub",
                    isSelected = state.urlaubsArt == "Sonderurlaub",
                    onClick = { viewModel.setUrlaubsArt("Sonderurlaub") }
                )
            }

            // Dropdown + Info-Button + Zeitraum bei Sonderurlaub
            if (state.urlaubsArt == "Sonderurlaub") {
                Text("GRUND FÜR SONDERURLAUB", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Box {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (state.sonderurlaubGrund.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.sonderurlaubGrund.isNotBlank()) state.sonderurlaubGrund else "Grund auswählen...",
                                color = if (state.sonderurlaubGrund.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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

                if (state.sonderurlaubGrund == SonderurlaubGründe.EIGENER) {
                    OutlinedTextField(
                        value = state.sonderurlaubGrund,
                        onValueChange = { viewModel.setSonderurlaubGrund(it) },
                        label = { Text("Bitte Grund eingeben") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                TextButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("ℹ Informationen zu Sonderurlaub", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("ZEITRAUM FÜR SONDERURLAUB", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VON (erster Urlaubstag)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 10.sp)
                        DateCard(date = state.vonDatum.format(dateFormatter)) {
                            activeDatePicker = Triple(0, true, state.vonDatum)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BIS (letzter Urlaubstag)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 10.sp)
                        DateCard(date = state.bisDatum.format(dateFormatter)) {
                            activeDatePicker = Triple(0, false, state.bisDatum)
                        }
                    }
                }
            } else {
                // Tarifurlaub: Bis zu 3 Zeiträume
                Text("ZEITRÄUME (Bis zu 3 Zeiträume für Tarifurlaub)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                state.urlaubsPerioden.forEachIndexed { index, periode ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Zeitraum ${index + 1} (${periode.tage} AT)", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (state.urlaubsPerioden.size > 1) {
                                    TextButton(onClick = { viewModel.removeUrlaubsperiode(index) }) {
                                        Text("Entfernen", color = Color(0xFFFF5252), fontSize = 12.sp)
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("VON (erster Urlaubstag)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
                                    DateCard(date = periode.von.format(dateFormatter)) {
                                        activeDatePicker = Triple(index, true, periode.von)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("BIS (letzter Urlaubstag)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 10.sp)
                                    DateCard(date = periode.bis.format(dateFormatter)) {
                                        activeDatePicker = Triple(index, false, periode.bis)
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.urlaubsPerioden.size < 3) {
                    OutlinedButton(
                        onClick = { viewModel.addUrlaubsperiode() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("+ Weiteren Zeitraum hinzufügen (${state.urlaubsPerioden.size}/3)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gesamte Urlaubstage: ${state.berechneteTage} AT",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nächster Arbeitstag (Rückkehr): ${state.rueckkehrDatum.format(rueckkehrFormatter)}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }

            Button(
                onClick = onNextClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("Weiter zu Details ➔", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Ausreichend Abstand am unteren Rand, damit Buttons nicht abgeschnitten werden
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Active Date Picker Dialog
    activeDatePicker?.let { (index, isVon, currentDate) ->
        val periode = if (state.urlaubsArt == "Sonderurlaub") {
            null
        } else {
            state.urlaubsPerioden.getOrNull(index)
        }
        val startD = periode?.von ?: state.vonDatum
        val endD = periode?.bis ?: state.bisDatum

        CustomDatePickerDialog(
            initialDate = currentDate,
            startDate = startD,
            endDate = endD,
            onDateSelected = { date ->
                if (state.urlaubsArt == "Sonderurlaub") {
                    if (isVon) {
                        viewModel.setVonDatum(date)
                    } else {
                        viewModel.setBisDatum(date)
                    }
                } else {
                    if (isVon) {
                        viewModel.updateUrlaubsperiodeVon(index, date)
                    } else {
                        viewModel.updateUrlaubsperiodeBis(index, date)
                    }
                }
                activeDatePicker = null
            },
            onDismiss = { activeDatePicker = null }
        )
    }

    // Info-Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    "Sonderurlaub im Bauhauptgewerbe",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("§ 4 BRTV-Bau / RTV-Angestellte", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mögliche Gründe und Anzahl der Tage:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Eigene Eheschließung: 3 Tage", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text("• Geburt des Kindes: 2 Tage", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text("• Tod naher Angehöriger: 2 Tage", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text("• Wohnungswechsel (1x/Jahr): 2 Tage", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text("• Erkrankung Familienmitglied: 1 Tag", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text("• Arztbesuche/Behördengänge: tatsächliche Zeit", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showDetailsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Detaillierte Informationen", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Schließen", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Details-Dialog
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = {
                Text(
                    "Detaillierte Regelungen",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Wichtige Zusatzregeln:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Ehrenämter und öffentliche Pflichten: Für die Wahrnehmung von gesetzlich auferlegten Pflichten (z. B. Vorladungen als Zeuge vor Gericht, Ausübung von öffentlichen Ehrenämtern) besteht ebenfalls ein Anspruch auf Freistellung.\n\n" +
                                "• Keine betrieblichen Gründe: Der Sonderurlaub aus familiären Gründen wird gewährt, sofern dem keine schwerwiegenden betrieblichen Gründe entgegenstehen.\n\n" +
                                "• Attestpflicht: Bei Erkrankung von im gemeinsamen Haushalt lebenden Familienmitgliedern ist ein ärztliches Attest vorzulegen.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Zurück", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Ausführlicher Datenschutzhinweis-Dialog
    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false })
    }
}

@Composable
fun UrlaubsArtButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DateCard(date: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = date, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("📅", fontSize = 16.sp)
        }
    }
}
