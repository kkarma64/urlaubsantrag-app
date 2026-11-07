package de.bauhaupt.urlaubsschein.ui.wizard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.logic.Bundesland
import de.bauhaupt.urlaubsschein.ui.components.GridBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardStep2Screen(
    viewModel: WizardViewModel,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }

    var baustelle by remember { mutableStateOf(state.baustelle) }
    var kostenstelle by remember { mutableStateOf(if (state.kostenstelle == "4400" && securePrefs.kostenstelle.isNotBlank()) securePrefs.kostenstelle else state.kostenstelle) }
    var selectedBundesland by remember {
        mutableStateOf(
            if (state.baustelleBundesland == Bundesland.HE && securePrefs.standardBundesland.isNotBlank())
                Bundesland.fromKuerzel(securePrefs.standardBundesland)
            else state.baustelleBundesland
        )
    }
    var polier by remember {
        mutableStateOf(
            if (state.polier == "Meier" && securePrefs.standardPolier.isNotBlank())
                securePrefs.standardPolier
            else state.polier
        )
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    GridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Neuer Antrag (2/3)", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            LinearProgressIndicator(
                progress = { 0.66f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("BAUSTELLE & ARBEITSORT", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)

            CustomTextField(value = baustelle, onValueChange = { baustelle = it }, label = "Baustelle")
            CustomTextField(value = kostenstelle, onValueChange = { kostenstelle = it }, label = "Kostenstelle (Standard aus Profil)")
            CustomTextField(value = polier, onValueChange = { polier = it }, label = "Polier / Verantwortlicher")

            Text("BUNDESLAND DER BAUSTELLE (für Feiertage & Arbeitsrecht)", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Box {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedBundesland.anzeigename} (${selectedBundesland.kuerzel})",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
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
                    Bundesland.entries.forEach { bl ->
                        DropdownMenuItem(
                            text = { Text(text = "${bl.anzeigename} (${bl.kuerzel})", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) },
                            onClick = {
                                selectedBundesland = bl
                                dropdownExpanded = false
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Zurück", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
                }
                Button(
                    onClick = {
                        viewModel.updateDetails(baustelle, kostenstelle, selectedBundesland, polier)
                        onNextClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier.weight(2f).height(60.dp)
                ) {
                    Text("Weiter zur Unterschrift ➔", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}
