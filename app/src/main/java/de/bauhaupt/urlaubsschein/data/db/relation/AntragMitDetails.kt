package de.bauhaupt.urlaubsschein.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import de.bauhaupt.urlaubsschein.data.db.entity.Anhang
import de.bauhaupt.urlaubsschein.data.db.entity.Antrag
import de.bauhaupt.urlaubsschein.data.db.entity.StatusProtokoll
import de.bauhaupt.urlaubsschein.data.db.entity.Vertretung

data class AntragMitDetails(
    @Embedded
    val antrag: Antrag,

    @Relation(parentColumn = "id", entityColumn = "antrag_id")
    val vertretungen: List<Vertretung> = emptyList(),

    @Relation(parentColumn = "id", entityColumn = "antrag_id")
    val anhaenge: List<Anhang> = emptyList(),

    @Relation(parentColumn = "id", entityColumn = "antrag_id")
    val statusProtokoll: List<StatusProtokoll> = emptyList()
)