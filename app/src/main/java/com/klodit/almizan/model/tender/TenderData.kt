package com.klodit.almizan.model.tender

import com.google.gson.annotations.SerializedName

data class TenderLot(
    @SerializedName("id")            val id           : String,
    @SerializedName("aoId")          val aoId         : String,
    @SerializedName("numero")        val numero       : String,
    @SerializedName("designation")   val designation  : String,
    @SerializedName("montantEstime") val montantEstime: String?,
    @SerializedName("statut")        val statut       : String?
)

data class Tender(
    @SerializedName("id")                    val id                   : String,
    @SerializedName("reference")             val reference            : String,
    @SerializedName("objet")                 val objet                : String,
    @SerializedName("typeProcedure")         val typeProcedure        : String,
    @SerializedName("montantEstime")         val montantEstime        : String?,
    @SerializedName("datePublication")       val datePublication      : String?,
    @SerializedName("dateLimiteSoumission")  val dateLimiteSoumission : String?,
    @SerializedName("dateLimiteRetraitCdc")  val dateLimiteRetraitCdc : String?,
    @SerializedName("statut")                val statut               : String,
    @SerializedName("serviceContractantId")  val serviceContractantId : String,
    @SerializedName("wilaya")                val wilaya               : String,
    @SerializedName("secteurActivite")       val secteurActivite      : String,
    @SerializedName("createdAt")             val createdAt            : String,
    @SerializedName("lots")                  val lots                 : List<TenderLot>
)

data class TenderMeta(
    @SerializedName("total")      val total      : Int,
    @SerializedName("page")       val page       : Int,
    @SerializedName("limit")      val limit      : Int,
    @SerializedName("totalPages") val totalPages : Int
)

data class TenderListResponse(
    @SerializedName("data") val data : List<Tender>,
    @SerializedName("meta") val meta : TenderMeta
)