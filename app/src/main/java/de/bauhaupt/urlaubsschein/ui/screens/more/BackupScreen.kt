package de.bauhaupt.urlaubsschein.ui.screens.more

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.MainActivity
import de.bauhaupt.urlaubsschein.data.db.DatabaseProvider
import de.bauhaupt.urlaubsschein.logic.BackupManager
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.StatusGreen
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportMergeDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val success = BackupManager.exportBackup(context, db, it)
                snackbarMessage = if (success) "✓ Backup erfolgreich exportiert!" else "Fehler beim Exportieren des Backups."
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingImportUri = it
            showImportMergeDialog = true
        }
    }

    if (showImportMergeDialog) {
        AlertDialog(
            onDismissRequest = { showImportMergeDialog = false },
            title = { Text("Backup importieren", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Möchtest du bestehende Daten mit dem Backup zusammenführen oder sollen bestehende Daten überschrieben werden?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showImportMergeDialog = false
                        pendingImportUri?.let { uri ->
                            scope.launch {
                                val success = BackupManager.importBackup(context, db, uri, merge = true)
                                snackbarMessage = if (success) "✓ Backup erfolgreich zusammengeführt!" else "Fehler: Ungültiges Backup-Format."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Zusammenführen", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportMergeDialog = false
                        pendingImportUri?.let { uri ->
                            scope.launch {
                                val success = BackupManager.importBackup(context, db, uri, merge = false)
                                snackbarMessage = if (success) "✓ Backup erfolgreich importiert (überschrieben)!" else "Fehler: Ungültiges Backup-Format."
                            }
                        }
                    }
                ) {
                    Text("Überschreiben", color = Color.Red)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    text = "Backup & Updates",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onBack) {
                    Text("Zurück", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            // --- APP-AKTUALISIERUNG ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("APP-AKTUALISIERUNG", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Prüfe, ob eine neuere Version der App verfügbar ist.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = {
                            (context as? MainActivity)?.triggerManualUpdateCheck()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manuell nach Update suchen", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- DATEN SICHERN & WIEDERHERSTELLEN ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("DATEN SICHERN & WIEDERHERSTELLEN", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Text(
                        "Du kannst deine Anträge als JSON-Datei exportieren oder ein bestehendes Backup wieder einlesen.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                exportLauncher.launch("urlaubsschein_backup_${LocalDate.now()}.json")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Backup exportieren", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Backup importieren", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (snackbarMessage != null) {
                        Text(
                            text = snackbarMessage!!,
                            color = if (snackbarMessage!!.startsWith("✓")) StatusGreen else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
