package de.bauhaupt.urlaubsschein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.ui.components.GridBackground

@Composable
fun HelpScreen(onBack: () -> Unit) {
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
                    text = "Hilfe & Bedienung",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onBack) {
                    Text("Zurück", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            HelpCard(
                title = "1. Wozu ist diese App da?",
                content = "Diese App ersetzt den Papier-Urlaubsschein im Bauhauptgewerbe. Sie erstellt rechtssichere Urlaubsanträge nach BRTV-Bau als PDF, verwaltet dein Urlaubskonto und ermöglicht den direkten E-Mail-Versand an das Lohnbüro."
            )

            HelpNavCard(
                title = "2. Die 4 Hauptbereiche (Navigation)",
                items = listOf(
                    "Home" to "Startbildschirm und Willkommensübersicht.",
                    "Anträge" to "Zeigt deine erstellten Anträge, den Resturlaub, die integrierte PDF-Vorschau mit Ein-Zeilen-Kürzung, den aktuellen Status (Entwurf, Eingereicht, Genehmigt, Storniert, mit Sicherheitsabfrage zum Stornieren) sowie die Löschfunktion.",
                    "Konto & Kalender" to "Verwaltung des Urlaubskontos (Resturlaub aus Vorjahr, Jahresanspruch und monatlicher Zuwachs nach § 5 BRTV).",
                    "Mehr" to "Stammdaten, Unterschriften-Verwaltung (inkl. Höhen- und Größenregler für Bilddateien und Zeichnungen), E-Mail- & Dateinamen-Formatierung mit Platzhaltern ({Name}, {Zeitraum}, {Datum}), Backup-Export/Import (JSON), Timeout-Sperrbildschirm, App-Updates, Design (Hell-/Dunkelmodus) und diese Hilfe."
                )
            )

            HelpNavCard(
                title = "3. Urlaub beantragen in 3 Schritten",
                items = listOf(
                    "Schritt 1" to "Wähle die Urlaubsart (Tarifurlaub mit bis zu 3 Zeiträumen oder Sonderurlaub mit Grund, inkl. halben Tagen). Der Kalender startet mit Montag, hebt Wochenenden und Feiertage des gewählten Bundeslandes hervor und markiert den Zeitraum durchgehend.",
                    "Schritt 2" to "Trage Baustelle, Kostenstelle, Polier und das Bundesland der Baustelle ein (wichtig für Feiertags- und Arbeitszeitregeln).",
                    "Schritt 3" to "Nutze deine gespeicherte Profil-Unterschrift oder zeichne direkt auf dem Bildschirm. Die App prüft vor dem Absenden automatisch auf Überschneidungen mit bestehenden Anträgen."
                )
            )

            HelpCard(
                title = "4. E-Mail-Versand & Outbox",
                content = "Beim Absenden wird der Antrag zunächst als Entwurf gespeichert und die PDF erzeugt. Anschließend öffnet sich dein E-Mail-Client mit Empfänger und Betreff. Nach erfolgreichem Absenden wird der Status automatisch auf „Eingereicht“ gesetzt."
            )

            HelpCard(
                title = "5. Sicherheit, Backup & Datenschutz",
                content = "Deine Daten und dein PIN-Passwort werden verschlüsselt und zu 100% lokal auf deinem Gerät gespeichert. Die App verfügt über einen konfigurierbaren Auto-Lock-Timeout (Sofort, 15s, 60s, 5 Min., Unendlich) sowie eine Backup-Funktion zum Sichern und Wiederherstellen deiner Daten als JSON."
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HelpCard(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = content, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        }
    }
}

@Composable
fun HelpNavCard(title: String, items: List<Pair<String, String>>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            items.forEach { (boldTerm, description) ->
                Text(
                    text = buildAnnotatedString {
                        append("• ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(boldTerm)
                        }
                        append(": ")
                        append(description)
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        }
    }
}