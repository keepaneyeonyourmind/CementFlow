package com.example.cementflow.calculator

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConcreteCalculatorTest {
    @Test
    fun `calculates cost for a fractional volume`() {
        assertEquals(BigDecimal("9375.00"), calculateConcreteCost("2,5", BigDecimal("3750")))
    }

    @Test
    fun `rounds cost to kopecks using half up`() {
        assertEquals(BigDecimal("1.01"), calculateConcreteCost("0.005", BigDecimal("201")))
    }

    @Test
    fun `invalid or non-positive volume is rejected`() {
        listOf("", "0", "-2", "abc", "1,2,3", "1e3").forEach {
            assertNull("Volume $it should be rejected", calculateConcreteCost(it, BigDecimal("3750")))
        }
    }

    @Test
    fun `non-positive price is rejected`() {
        assertNull(calculateConcreteCost("1", BigDecimal.ZERO))
        assertNull(calculateConcreteCost("1", BigDecimal("-5")))
    }
}

