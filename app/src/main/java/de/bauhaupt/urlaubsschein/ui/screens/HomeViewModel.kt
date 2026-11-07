package de.bauhaupt.urlaubsschein.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.bauhaupt.urlaubsschein.data.db.DatabaseProvider
import de.bauhaupt.urlaubsschein.data.db.entity.Antrag
import de.bauhaupt.urlaubsschein.data.repository.AntragRepository
import kotlinx.coroutines.flow.Flow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val repository = AntragRepository(db.antragDao(), db.statusProtokollDao(), db.vertretungDao())

    val antraege: Flow<List<Antrag>> = repository.getAllAntraege()
}