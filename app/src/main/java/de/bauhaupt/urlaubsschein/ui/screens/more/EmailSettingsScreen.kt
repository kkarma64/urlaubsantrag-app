package de.bauhaupt.urlaubsschein.ui.screens.more

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
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.StatusGreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun EmailSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val appPrefs = remember { AppPreferencesRepository(context) }
    val securePrefs = remember { SecurePreferences(context) }
    val scope = rememberCoroutineScope()

    var empfaengerEmail by remember { mutableStateOf("") }
    var polierEmail by remember { mutableStateOf(securePrefs.standardPolierEmail) }
    var betreffFormat by remember { mutableStateOf("Urlaubsschein {Name} {Zeitraum}") }
    var dateinameFormat by remember { mutableStateOf("Urlaubsschein_{Name}_{Datum}.pdf") }
    var savedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        empfaengerEmail = appPrefs.empfaengerEmail.first() ?: "lohnbuero@nameunternehmen.de"
        betreffFormat = appPrefs.betreffFormat.first()
        dateinameFormat = appPrefs.dateinameFormat.first()
    }

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
                    text = "E-Mail-Einstellungen",
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
                    Text("LOHNBÜRO & POLIER E-MAIL", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = empfaengerEmail,
                        onValueChange = { empfaengerEmail = it },
                        label = { Text("E-Mail Lohnbüro (An)") },
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
                        value = polierEmail,
                        onValueChange = { polierEmail = it },
                        label = { Text("E-Mail Standard-Polier (CC)") },
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
                        value = betreffFormat,
                        onValueChange = { betreffFormat = it },
                        label = { Text("E-Mail-Betreff Format") },
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
                        value = dateinameFormat,
                        onValueChange = { dateinameFormat = it },
                        label = { Text("PDF-Dateiname Format") },
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

                    Text(
                        "Verfügbare Platzhalter:\n{Name} = Name des Mitarbeiters\n{Zeitraum} = Urlaubszeitraum\n{Datum} = Erstellungsdatum",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = {
                    securePrefs.standardPolierEmail = polierEmail
                    scope.launch {
                        appPrefs.setEmpfaengerEmail(empfaengerEmail)
                        appPrefs.setBetreffFormat(betreffFormat)
                        appPrefs.setDateinameFormat(dateinameFormat)
                    }
                    savedMessage = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("E-Mail-Einstellungen speichern", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (savedMessage) {
                Text(
                    text = "✓ E-Mail-Einstellungen erfolgreich gespeichert!",
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
