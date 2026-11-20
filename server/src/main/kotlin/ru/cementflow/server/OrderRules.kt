package ru.cementflow.server

import java.math.BigDecimal
import java.math.RoundingMode

class BadRequest(message: String) : RuntimeException(message)
class NotFound(message: String) : RuntimeException(message)
class Conflict(message: String) : RuntimeException(message)

enum class DeliveryStatus { WAITING, ASSIGNED, IN_TRANSIT, DELIVERED }

data class ValidOrder(val productId: Long, val quantity: Int, val customerName: String, val phone: String, val address: String)

object OrderRules {
    fun validate(productId: Long?, quantity: BigDecimal?, name: String?, phone: String?, address: String?): ValidOrder {
        if (productId == null || productId <= 0) throw BadRequest("Укажите товар")
        val count = try { quantity?.intValueExact() } catch (_: ArithmeticException) { null }
        if (count == null || count !in 1..10000) throw BadRequest("Количество должно быть целым числом от 1 до 10000")
        return ValidOrder(productId, count, text(name, "Имя", 100), text(phone, "Телефон", 100), text(address, "Адрес", 300))
    }

    fun text(value: String?, label: String, max: Int): String {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty() || trimmed.length > max) throw BadRequest("$label: заполните поле (до $max символов)")
        return trimmed
    }

    fun total(price: BigDecimal, quantity: Int): BigDecimal = price.multiply(BigDecimal.valueOf(quantity.toLong())).setScale(2, RoundingMode.HALF_UP)
}

object DeliveryRules {
    fun driver(value: String?): String = OrderRules.text(value, "Имя водителя", 100)
    fun next(current: DeliveryStatus, target: DeliveryStatus): DeliveryStatus {
        val permitted = (current == DeliveryStatus.ASSIGNED && target == DeliveryStatus.IN_TRANSIT) ||
            (current == DeliveryStatus.IN_TRANSIT && target == DeliveryStatus.DELIVERED)
        if (!permitted) throw Conflict("Недопустимый переход статуса доставки")
        return target
    }
}
