package com.renatizzi.photovideomanager.domain.hub

/**
 * LOCK #6/#8: etichetta sede Catalogo su PC Windows.
 * Non usare il modello del telefono precompilato in Avvio (es. «S25 Ultra…»).
 */
object PcSeatDeviceModel {
    fun resolve(
        typedModel: String,
        phoneSuggested: String,
        pcComputerName: String = "",
    ): String {
        val typed = typedModel.trim()
        val phone = phoneSuggested.trim()
        val pc = pcComputerName.trim()
        return when {
            typed.isNotBlank() &&
                (phone.isBlank() || !typed.equals(phone, ignoreCase = true)) -> typed
            pc.isNotBlank() -> pc
            else -> CatalogSeatKindLabels.shortDevice(CatalogSeatKind.PC_NOTEBOOK)
        }
    }
}
