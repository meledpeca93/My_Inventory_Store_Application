package com.uth.myinventorystoreapplication

import com.uth.myinventorystoreapplication.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseHelperUnitTest {

    @Test
    fun testProductTotalValueCalculation() {
        val product = Product(
            id = 1,
            name = "Refresco",
            price = 25.5,
            stock = 10,
            barcode = "123456",
            minStock = 5
        )

        assertEquals(255.0, product.totalValue, 0.001)
        assertFalse(product.isLowStock)
    }

    @Test
    fun testProductLowStockThreshold() {
        val lowStockProduct = Product(
            id = 2,
            name = "Jabón",
            price = 15.0,
            stock = 3,
            barcode = "654321",
            minStock = 5
        )

        assertTrue(lowStockProduct.isLowStock)
    }
}
