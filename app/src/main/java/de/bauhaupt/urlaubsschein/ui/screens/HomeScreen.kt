package de.bauhaupt.urlaubsschein.ui.screens

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import de.bauhaupt.urlaubsschein.data.db.DatabaseProvider
import de.bauhaupt.urlaubsschein.data.db.entity.Antrag
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.components.PrivacyDialog
import de.bauhaupt.urlaubsschein.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onAntragClick: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val antraege by viewModel.antraege.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val securePrefs = remember { SecurePreferences(context) }
    val fullName = "${securePrefs.vorname} ${securePrefs.name}".trim()
    val displayName = if (fullName.isNotBlank()) fullName else "Max Mustermann"
    val displayPnr = securePrefs.personalnummer.ifBlank { "1042" }

    val pdfDir = context.cacheDir
    val pdfFiles = remember(antraege) {
        pdfDir.listFiles()?.filter { it.name.endsWith(".pdf") } ?: emptyList()
    }

    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var showPrivacyDialog by remember {
        mutableStateOf(!prefs.getBoolean("privacy_accepted", false))
    }

    if (showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = {
                prefs.edit().putBoolean("privacy_accepted", true).apply()
                showPrivacyDialog = false
            }
        )
    }

    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val contentPad = PaddingValues(
        top = systemBars.calculateTopPadding() + 24.dp,
        bottom = systemBars.calculateBottomPadding() + 96.dp,
        start = 16.dp,
        end = 16.dp
    )

    GridBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLandscape) {
                // --- QUERFORMAT: 2-Spalten Dashboard ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPad),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Urlaubsschein Bau", color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("$displayName (PNr. $displayPnr)", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }
                        TextButton(onClick = { showPrivacyDialog = true }) {
                            Text("Datenschutz", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Linke Spalte (Resturlaub)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Resturlaub 2026", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 16.sp)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("18", color = MaterialTheme.colorScheme.primary, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                                        Text(" von 30 Tagen", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
                                    }
                                    Text("12 Tage genommen / genehmigt", color = StatusGreen, fontSize = 14.sp)
                                }
                            }
                        }

                        // Rechte Spalte (Meine Anträge mit integrierter PDF-Vorschau im selben Rahmen)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Meine Anträge & PDFs", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (antraege.isEmpty()) {
                                Text("Noch keine Anträge vorhanden.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 14.sp)
                            } else {
                                antraege.forEach { antrag ->
                                    val matchedPdf = pdfFiles.find {
                                        it.name.contains(antrag.tarifVon.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                                    }
                                    AntragWithPdfCard(
                                        antrag = antrag,
                                        pdfFile = matchedPdf,
                                        onAntragClick = { onAntragClick(antrag.id) },
                                        onOpenPdf = { file -> openPdf(context, file) },
                                        onDelete = {
                                            scope.launch {
                                                matchedPdf?.delete()
                                                db.antragDao().delete(antrag)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // --- HOCHFORMAT ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPad,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Urlaubsschein Bau", color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Text("$displayName (PNr. $displayPnr)", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }
                            TextButton(onClick = { showPrivacyDialog = true }) {
                                Text("Datenschutz", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Resturlaub 2026", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("18", color = MaterialTheme.colorScheme.primary, fontSize = 56.sp, fontWeight = FontWeight.Bold)
                                    Text(" von 30 Tagen", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
                                }
                                Text("12 Tage genommen / genehmigt", color = StatusGreen, fontSize = 14.sp)
                            }
                        }
                    }

                    item {
                        Text("Meine Anträge & PDFs", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }

                    if (antraege.isEmpty()) {
                        item {
                            Text("Noch keine Anträge vorhanden.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    } else {
                        items(antraege) { antrag ->
                            val matchedPdf = pdfFiles.find {
                                it.name.contains(antrag.tarifVon.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                            }
                            AntragWithPdfCard(
                                antrag = antrag,
                                pdfFile = matchedPdf,
                                onAntragClick = { onAntragClick(antrag.id) },
                                onOpenPdf = { file -> openPdf(context, file) },
                                onDelete = {
                                    scope.launch {
                                        matchedPdf?.delete()
                                        db.antragDao().delete(antrag)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun openPdf(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun AntragWithPdfCard(
    antrag: Antrag,
    pdfFile: File?,
    onAntragClick: () -> Unit,
    onOpenPdf: (File) -> Unit,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val farbe = when (antrag.status.uppercase()) {
        "GENEHMIGT" -> StatusGreen
        "STORNIERT" -> Color.Red
        "EINGEREICHT" -> StatusAmber
        else -> MaterialTheme.colorScheme.outline
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Antrag löschen", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Möchtest du diesen Antrag wirklich unwiderruflich löschen?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Löschen", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAntragClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Antrag Info
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(90.dp)
                        .background(farbe, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(antrag.status.uppercase(), color = farbe, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${antrag.tarifVon.format(formatter)} - ${antrag.tarifBis.format(formatter)}", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Nr: ${antrag.antragsNr} • ${antrag.tarifTage} Tage", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text("Baustelle: ${antrag.baustelle}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }

            // PDF-Vorschau direkt im selben Rahmen + Löschen-Button
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pdfFile != null && pdfFile.exists()) {
                    Text(
                        text = "📄 ${pdfFile.name}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = "📄 Keine PDF gefunden",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pdfFile != null && pdfFile.exists()) {
                        Button(
                            onClick = { onOpenPdf(pdfFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Öffnen", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        border = BorderStroke(1.dp, Color(0xFFFF5252)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Löschen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
