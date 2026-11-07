package de.bauhaupt.urlaubsschein

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.objects.AppDialogConfig
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.ui.navigation.MainScaffold
import de.bauhaupt.urlaubsschein.ui.screens.AuthViewModel
import de.bauhaupt.urlaubsschein.ui.screens.HelpScreen
import de.bauhaupt.urlaubsschein.ui.screens.LockScreen
import de.bauhaupt.urlaubsschein.ui.screens.OnboardingScreen
import de.bauhaupt.urlaubsschein.ui.screens.PasswortEinrichtenScreen
import de.bauhaupt.urlaubsschein.ui.screens.SplashScreen
import de.bauhaupt.urlaubsschein.ui.theme.ThemeViewModel
import de.bauhaupt.urlaubsschein.ui.theme.UrlaubsscheinBauTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var backgroundTimestamp = 0L
    private var isAppInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Locale.setDefault(Locale.GERMAN) // Startet die Kalenderwoche mit Montag
        enableEdgeToEdge()

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_PAUSE -> {
                    backgroundTimestamp = System.currentTimeMillis()
                    isAppInBackground = true
                }
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()

            val darkModeSetting by themeViewModel.isDarkMode.collectAsState(initial = null)
            val onboardingDone by authViewModel.onboardingAbgeschlossen.collectAsState(initial = null)
            val passwordHash by authViewModel.passwortHash.collectAsState(initial = null)

            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = darkModeSetting ?: systemDark

            val navController = rememberNavController()

            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                        if (isAppInBackground) {
                            isAppInBackground = false
                            val elapsed = System.currentTimeMillis() - backgroundTimestamp
                            CoroutineScope(Dispatchers.IO).launch {
                                val appPrefs = AppPreferencesRepository(applicationContext)
                                val timeoutMs = appPrefs.lockTimeout.first()
                                val passHash = appPrefs.passwortHash.first()

                                if (!passHash.isNullOrBlank() && (timeoutMs == 0L || elapsed > timeoutMs)) {
                                    withContext(Dispatchers.Main) {
                                        try {
                                            navController.navigate("lock") {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            UrlaubsscheinBauTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (onboardingDone == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        val startDest = when {
                            onboardingDone != true -> "onboarding"
                            passwordHash.isNullOrBlank() -> "passwort_einrichten"
                            else -> "lock"
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startDest
                        ) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    onOnboardingAbgeschlossen = {
                                        navController.navigate("passwort_einrichten") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("passwort_einrichten") {
                                PasswortEinrichtenScreen(
                                    onPasswortEingerichtet = {
                                        navController.navigate("splash") {
                                            popUpTo("passwort_einrichten") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("lock") {
                                LockScreen(
                                    onUnlocked = {
                                        navController.navigate("splash") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("splash") {
                                SplashScreen(
                                    onStartClick = {
                                        navController.navigate("main_scaffold") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    },
                                    onHelpClick = {
                                        navController.navigate("help_screen_splash")
                                    }
                                )
                            }
                            composable("help_screen_splash") {
                                HelpScreen(onBack = { navController.popBackStack() })
                            }
                            composable("main_scaffold") {
                                MainScaffold()
                            }
                        }
                    }
                }
            }
        }

        // Automatische Update-Prüfung beim Start
        checkForUpdates("https://github.com/kkarma64/urlaubsantrag-app/releases/download/v1.0/version.json", false)
    }

    fun triggerManualUpdateCheck() {
        checkForUpdates("https://github.com/kkarma64/urlaubsantrag-app/releases/download/v1.0/version.json", true)
    }

    private fun checkForUpdates(jsonUrl: String, manualCheck: Boolean) {
        if (manualCheck) {
            runOnUiThread {
                Toast.makeText(this, "Suche nach Updates...", Toast.LENGTH_SHORT).show()
            }
        }
        @Suppress("DEPRECATION")
        AsyncTask.execute {
            try {
                val url = URL(jsonUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonString)

                    val remoteVersionCode = jsonObject.optInt("versionCode", 0)
                    val apkUrl = jsonObject.optString("apkUrl", "")

                    val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageName, 0).versionCode
                    }

                    runOnUiThread {
                        if (remoteVersionCode > currentVersionCode && apkUrl.isNotBlank()) {
                            showUpdateDialog(apkUrl)
                        } else if (manualCheck) {
                            Toast.makeText(this, "Du hast bereits die neueste Version!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (manualCheck) {
                    runOnUiThread {
                        Toast.makeText(this, "Keine Verbindung zum Update-Server.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (manualCheck) {
                    runOnUiThread {
                        Toast.makeText(this, "Fehler bei der Update-Prüfung.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showUpdateDialog(apkUrl: String) {
        try {
            val dialogConfig = AppDialogConfig(
                this,
                "Neue Version verfügbar!",
                "Es gibt ein Update für die Urlaubsantrags-App. Möchtest du es jetzt installieren?",
                "Jetzt aktualisieren",
                "Später",
                null
            )

            AlertDialog.Builder(this)
                .setTitle(dialogConfig.title)
                .setMessage(dialogConfig.description)
                .setPositiveButton(dialogConfig.btnUpdate) { _, _ ->
                    AppUpdater(this, apkUrl).start()
                }
                .setNegativeButton(dialogConfig.btnDismiss, null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
