package de.bauhaupt.urlaubsschein.ui.components

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PrivacyDialog(
    onDismiss: () -> Unit
) {
    var showFull by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Dialog nicht durch Klick außerhalb schließen */ },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (showFull) "Datenschutz & Sicherheit (DSGVO)" else "Datenschutz – kurz erklärt",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!showFull) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showFull = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Vollständige Informationen anzeigen", fontWeight = FontWeight.Bold)
                    }
                } else {
                    PrivacySection(
                        title = "1. Verantwortlicher & Anbieter",
                        content = "Verantwortlich für die Bereitstellung dieser lokalen Anwendung ist der jeweilige Anwender bzw. Arbeitnehmer im Bauhauptgewerbe. Es findet keine zentrale Datenverarbeitung auf externen Cloud-Servern statt."
                    )
                    PrivacySection(
                        title = "2. Zweck der App",
                        content = "Erstellung und Verwaltung von Urlaubsanträgen sowie Berechnung des Urlaubskontos nach BRTV-Bau."
                    )
                    PrivacySection(
                        title = "3. Datenerfassung, Verschlüsselung & Backup",
                        content = "Alle Profildaten, Unterschriften, Sicherheits-PINs und Anträge werden mittels AES-256 (EncryptedSharedPreferences / SQLCipher) verschlüsselt ausschließlich lokal auf deinem Endgerät gespeichert. Bei Nutzung der Backup-Export-Funktion entscheidest du selbst, wo die verschlüsselte JSON-Sicherungsdatei (z.B. im Download-Ordner) abgelegt wird."
                    )
                    PrivacySection(
                        title = "4. E-Mail-Versand",
                        content = "Der Versand der erstellten PDF-Dokumente erfolgt direkt über deinen auf dem Gerät installierten E-Mail-Client an das von dir konfigurierte Lohnbüro. Die App selbst hat keinen Zugriff auf Zugangsdaten oder Postfächer."
                    )
                    PrivacySection(
                        title = "5. Speicherdauer",
                        content = "Die lokal gespeicherten Daten verbleiben so lange auf deinem Gerät, bis du sie in den App-Einstellungen löschst, ein Backup wiederherstellst oder die App deinstallierst."
                    )
                    PrivacySection(
                        title = "6. Deine Rechte (DSGVO)",
                        content = "Du hast das Recht auf Auskunft, Berichtigung, Löschung und Einschränkung der Verarbeitung deiner lokal gespeicherten Daten gemäß DSGVO."
                    )

                    Text(
                        text = "Stand: ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMAN))}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { showFull = false }) {
                        Text("← Zurück zur Kurzfassung", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Verstanden",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp
        )
    }
}
