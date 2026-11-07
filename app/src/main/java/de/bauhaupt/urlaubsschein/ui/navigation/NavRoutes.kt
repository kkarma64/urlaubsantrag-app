package de.bauhaupt.urlaubsschein.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Home : NavRoutes("home")
    object WizardStep1 : NavRoutes("wizard_step1")
    object WizardStep2 : NavRoutes("wizard_step2")
    object WizardStep3 : NavRoutes("wizard_step3") // NEU
}