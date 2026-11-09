package de.bauhaupt.urlaubsschein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.data.db.DatabaseProvider
import de.bauhaupt.urlaubsschein.data.db.entity.Antrag
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun AntragDetailScreen(
    antragId: Long,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var antrag by remember { mutableStateOf<Antrag?>(null) }
    var showStornoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(antragId) {
        antrag = db.antragDao().ladeAntragNachId(antragId)
    }

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    if (showStornoDialog && antrag != null) {
        AlertDialog(
            onDismissRequest = { showStornoDialog = false },
            title = { Text("Antrag stornieren", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Möchtest du diesen Antrag wirklich stornieren?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showStornoDialog = false
                        scope.launch {
                            val updated = antrag!!.copy(status = "STORNIERT", storniertAm = java.time.Instant.now())
                            db.antragDao().update(updated)
                            antrag = updated
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ja, stornieren", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStornoDialog = false }) {
                    Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    GridBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = systemBars.calculateTopPadding(),
                    bottom = systemBars.calculateBottomPadding()
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mitscrollender Startabstand oben
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Antragsdetails", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onBack) {
                        Text("Zurück", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                if (antrag == null) {
                    Text("Antrag wird geladen...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                } else {
                    val currentAntrag = antrag!!
                    val farbe = when (currentAntrag.status) {
                        "GENEHMIGT" -> StatusGreen
                        "STORNIERT" -> Color.Red
                        else -> StatusAmber
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(currentAntrag.status, color = farbe, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Antrags-Nr:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(currentAntrag.antragsNr, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zeitraum:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text("${currentAntrag.tarifVon.format(formatter)} - ${currentAntrag.tarifBis.format(formatter)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tage:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text("${currentAntrag.tarifTage} AT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Baustelle:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(currentAntrag.baustelle, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Kostenstelle:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(currentAntrag.kostenstelle ?: "-", color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Polier:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Text(currentAntrag.polier, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Text("Status ändern", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val updated = currentAntrag.copy(status = "EINGEREICHT")
                                    db.antragDao().update(updated)
                                    antrag = updated
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusAmber),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Eingereicht", color = DarkBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val updated = currentAntrag.copy(status = "GENEHMIGT")
                                    db.antragDao().update(updated)
                                    antrag = updated
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Genehmigt", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showStornoDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Stornieren", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val pdfFiles = context.cacheDir.listFiles()?.filter { it.name.endsWith(".pdf") } ?: emptyList()
                                pdfFiles.forEach { file ->
                                    if (file.name.contains(currentAntrag.tarifVon.format(formatter))) {
                                        file.delete()
                                    }
                                }
                                db.antragDao().delete(currentAntrag)
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🗑 Antrag & zugehörige PDF löschen", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
