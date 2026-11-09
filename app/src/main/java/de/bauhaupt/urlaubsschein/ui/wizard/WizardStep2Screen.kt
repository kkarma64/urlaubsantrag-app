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
                    text = "Neuer Antrag (2/3)",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onBackClick) {
                    Text("Zurück", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            LinearProgressIndicator(
                progress = { 0.66f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
                strokeCap = StrokeCap.Round
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("BAUSTELLEN- & POLIERDATEN", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = baustelle,
                        onValueChange = { baustelle = it },
                        label = { Text("Baustelle / Objekt") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = kostenstelle,
                        onValueChange = { kostenstelle = it },
                        label = { Text("Kostenstelle") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = polier,
                        onValueChange = { polier = it },
                        label = { Text("Zuständiger Polier") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Bundesland (Feiertagsberechnung)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                }
            }

            Button(
                onClick = {
                    viewModel.updateDetails(baustelle, kostenstelle, selectedBundesland, polier)
                    onNextClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Weiter zur Unterschrift ➔", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
