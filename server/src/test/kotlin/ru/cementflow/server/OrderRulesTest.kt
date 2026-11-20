package ru.cementflow.server

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderRulesTest {
    @Test
    fun `valid order calculates server price accurately`() {
        val details = OrderRules.validate(1, BigDecimal("3"), "Ирина", "+7 900 123-45-67", "Улица 1")
        assertEquals(3, details.quantity)
        assertEquals(BigDecimal("379.50"), OrderRules.total(BigDecimal("126.50"), details.quantity))
    }

    @Test
    fun `zero negative fractional and excessive quantities are rejected`() {
        listOf("0", "-1", "1.5", "10001").forEach { quantity ->
            assertThrows(BadRequest::class.java) {
                OrderRules.validate(1, BigDecimal(quantity), "Ирина", "+79001234567", "Улица 1")
            }
        }
    }

    @Test
    fun `empty and overly long contact fields are rejected`() {
        assertThrows(BadRequest::class.java) { OrderRules.validate(1, BigDecimal.ONE, " ", "123", "Адрес") }
        assertThrows(BadRequest::class.java) { OrderRules.validate(1, BigDecimal.ONE, "Имя", "123", "а".repeat(301)) }
    }

    @Test
    fun `delivery moves forward one step at a time`() {
        assertEquals(DeliveryStatus.IN_TRANSIT, DeliveryRules.next(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT))
        assertEquals(DeliveryStatus.DELIVERED, DeliveryRules.next(DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELIVERED))
        assertThrows(Conflict::class.java) { DeliveryRules.next(DeliveryStatus.WAITING, DeliveryStatus.DELIVERED) }
        assertThrows(Conflict::class.java) { DeliveryRules.next(DeliveryStatus.DELIVERED, DeliveryStatus.IN_TRANSIT) }
        assertThrows(Conflict::class.java) { DeliveryRules.next(DeliveryStatus.WAITING, DeliveryStatus.ASSIGNED) }
    }

    @Test
    fun `driver must be provided`() {
        assertThrows(BadRequest::class.java) { DeliveryRules.driver("  ") }
        assertEquals("Алексей", DeliveryRules.driver(" Алексей "))
    }
}
