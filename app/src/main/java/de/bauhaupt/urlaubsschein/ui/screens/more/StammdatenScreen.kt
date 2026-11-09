package de.bauhaupt.urlaubsschein.ui.screens.more

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.logic.Bundesland
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.StatusGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StammdatenScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }

    var vorname by remember { mutableStateOf(securePrefs.vorname) }
    var name by remember { mutableStateOf(securePrefs.name) }
    var pnr by remember { mutableStateOf(securePrefs.personalnummer) }
    var kostenstelle by remember { mutableStateOf(securePrefs.kostenstelle) }
    var standardPolier by remember { mutableStateOf(securePrefs.standardPolier) }
    var standardBundesland by remember { mutableStateOf(Bundesland.fromKuerzel(securePrefs.standardBundesland)) }
    var blDropdownExpanded by remember { mutableStateOf(false) }
    var savedMessage by remember { mutableStateOf(false) }

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
                    text = "Stammdaten",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onBack) {
                    Text("Zurück", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("PERSÖNLICHE DATEN (Verschlüsselt)", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = vorname,
                        onValueChange = { vorname = it },
                        label = { Text("Vorname") },
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
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nachname") },
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
                        value = pnr,
                        onValueChange = { pnr = it },
                        label = { Text("Personalnummer") },
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
                        label = { Text("Standard-Kostenstelle") },
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
                        value = standardPolier,
                        onValueChange = { standardPolier = it },
                        label = { Text("Standard-Polier") },
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

                    Text("Standard-Bundesland (Baustelle)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(
                            onClick = { blDropdownExpanded = true },
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
                                    text = "${standardBundesland.anzeigename} (${standardBundesland.kuerzel})",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("▼", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = blDropdownExpanded,
                            onDismissRequest = { blDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Bundesland.entries.forEach { bl ->
                                DropdownMenuItem(
                                    text = { Text(text = "${bl.anzeigename} (${bl.kuerzel})", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) },
                                    onClick = {
                                        standardBundesland = bl
                                        blDropdownExpanded = false
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
                    securePrefs.vorname = vorname
                    securePrefs.name = name
                    securePrefs.personalnummer = pnr
                    securePrefs.kostenstelle = kostenstelle
                    securePrefs.standardPolier = standardPolier
                    securePrefs.standardBundesland = standardBundesland.kuerzel
                    savedMessage = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Stammdaten speichern", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (savedMessage) {
                Text(
                    text = "✓ Stammdaten erfolgreich gespeichert!",
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
