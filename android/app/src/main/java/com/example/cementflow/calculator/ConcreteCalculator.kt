package com.example.cementflow.calculator

import java.math.BigDecimal
import java.math.RoundingMode

/** Возвращает стоимость в рублях с округлением до копеек или null при неверных данных. */
fun calculateConcreteCost(volumeText: String, pricePerCubicMeter: BigDecimal): BigDecimal? {
    val normalized = volumeText.trim().replace(',', '.')
    if (normalized.length > 100 || !normalized.matches(Regex("[0-9]+(\\.[0-9]+)?"))) return null
    val volume = normalized.toBigDecimalOrNull() ?: return null
    if (volume.signum() <= 0 || pricePerCubicMeter.signum() <= 0) return null
    return volume.multiply(pricePerCubicMeter).setScale(2, RoundingMode.HALF_UP)
}
