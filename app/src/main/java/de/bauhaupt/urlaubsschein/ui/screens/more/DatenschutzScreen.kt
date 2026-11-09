package de.bauhaupt.urlaubsschein.ui.screens.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.ui.components.GridBackground

@Composable
fun DatenschutzScreen(onBack: () -> Unit) {
    var showFullText by remember { mutableStateOf(false) }

    GridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Datenschutz",
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
                    if (!showFullText) {
                        // --- KURZFASSUNG ---
                        Text(
                            text = "Datenschutz – kurz erklärt",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Diese App erstellt Urlaubsanträge als PDF gemäß BRTV-Bau. Dafür werden Formulardaten wie Name, Personalnummer, Baustelle und Zeitraum verarbeitet.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Deine Stammdaten (Name, Personalnummer, Unterschrift, PIN-Hash) werden lokal und verschlüsselt auf deinem Gerät gespeichert.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Das PDF wird lokal erzeugt und über deinen eigenen E-Mail-Client an das Lohnbüro gesendet. Es findet keine Datenübertragung an Server oder Dritte statt.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )

                        Button(
                            onClick = { showFullText = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Vollständige Informationen anzeigen", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // --- LANGFASSUNG ---
                        Text(
                            text = "Datenschutz & Sicherheit (DSGVO)",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "1. Verantwortlicher & Anbieter",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Verantwortlich für die Bereitstellung dieser lokalen Anwendung ist der jeweilige Anwender bzw. Arbeitnehmer im Bauhauptgewerbe. Es findet keine zentrale Datenverarbeitung auf externen Cloud-Servern statt.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "2. Zweck der App",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Erstellung und Verwaltung von Urlaubsanträgen sowie Berechnung des Urlaubskontos nach BRTV-Bau.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "3. Datenerfassung, Verschlüsselung & Backup",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alle Profildaten, Unterschriften, Sicherheits-PINs und Anträge werden mittels AES-256 (EncryptedSharedPreferences / SQLCipher) verschlüsselt ausschließlich lokal auf deinem Endgerät gespeichert. Bei Nutzung der Backup-Export-Funktion entscheidest du selbst, wo die verschlüsselte JSON-Sicherungsdatei abgelegt wird.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "4. E-Mail-Versand",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Der Versand der erstellten PDF-Dokumente erfolgt direkt über deinen auf dem Gerät installierten E-Mail-Client an das von dir konfigurierte Lohnbüro. Die App selbst hat keinen Zugriff auf Zugangsdaten oder Postfächer.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "5. Speicherdauer",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Die lokal gespeicherten Daten verbleiben so lange auf deinem Gerät, bis du sie in den App-Einstellungen löschst, ein Backup wiederherstellst oder die App deinstallierst.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "6. Deine Rechte (DSGVO)",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Du hast das Recht auf Auskunft, Berichtigung, Löschung und Einschränkung der Verarbeitung deiner lokal gespeicherten Daten gemäß DSGVO.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Stand: November 2026",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        TextButton(
                            onClick = { showFullText = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("← Zurück zur Kurzfassung", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}