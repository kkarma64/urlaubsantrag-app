package de.bauhaupt.urlaubsschein.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import de.bauhaupt.urlaubsschein.data.db.entity.Arbeitsmodell
import de.bauhaupt.urlaubsschein.data.db.entity.Profil

data class ProfilMitArbeitsmodell(
    @Embedded
    val profil: Profil,

    @Relation(parentColumn = "id", entityColumn = "profil_id")
    val arbeitsmodell: Arbeitsmodell? = null
)