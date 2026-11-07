package de.bauhaupt.urlaubsschein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.data.db.DatabaseProvider
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.*
import java.time.LocalDate

@Composable
fun KontoKalenderScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }
    val db = remember { DatabaseProvider.getDatabase(context) }
    val antraege by db.antragDao().beobachteAlle().collectAsState(initial = emptyList())

    var resturlaub by remember { mutableStateOf(securePrefs.resturlaubVorjahr.toString()) }
    var jahresanspruch by remember { mutableStateOf(securePrefs.jahresanspruch.toString()) }
    var saved by remember { mutableStateOf(false) }

    val aktuellerMonat = LocalDate.now().monthValue
    val erarbeitetLaufendesJahr = (jahresanspruch.toFloatOrNull() ?: 30f) / 12f * aktuellerMonat
    val gesamtAnspruch = (resturlaub.toFloatOrNull() ?: 0f) + erarbeitetLaufendesJahr

    // Dynamische Berechnung: Nur Anträge mit Status "EINGEREICHT" oder "GENEHMIGT" zählen als genommen
    val genommen = antraege
        .filter { it.status.uppercase() == "EINGEREICHT" || it.status.uppercase() == "GENEHMIGT" }
        .sumOf { it.tarifTage.toDouble() }.toFloat()

    val verbleibend = gesamtAnspruch - genommen

    GridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Urlaubskonto & Kalender",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BRTV-Bau Urlaubskontingent & monatlicher Zuwachs (§ 5 BRTV)",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 13.sp
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ÜBERSICHT", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Resturlaub Vorjahr:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("$resturlaub Tage", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Anspruch laufendes Jahr (erarbeitet bis Monat $aktuellerMonat):", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(String.format(java.util.Locale.GERMAN, "%.1f Tage", erarbeitetLaufendesJahr), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bereits genommen/eingereicht:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(String.format(java.util.Locale.GERMAN, "%.1f Tage", genommen), color = StatusAmber, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Verfügbarer Resturlaub:", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(String.format(java.util.Locale.GERMAN, "%.1f Tage", verbleibend), color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("EINSTELLUNGEN URLAUBSKONTO", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = resturlaub,
                        onValueChange = { resturlaub = it },
                        label = { Text("Resturlaub aus Vorjahr") },
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
                        value = jahresanspruch,
                        onValueChange = { jahresanspruch = it },
                        label = { Text("Jahresurlaubsanspruch") },
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

                    Button(
                        onClick = {
                            securePrefs.resturlaubVorjahr = resturlaub.toIntOrNull() ?: 5
                            securePrefs.jahresanspruch = jahresanspruch.toIntOrNull() ?: 30
                            saved = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Konto aktualisieren", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }

                    if (saved) {
                        Text("✓ Urlaubskonto erfolgreich aktualisiert!", color = StatusGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
