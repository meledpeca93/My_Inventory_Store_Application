package com.uth.myinventorystoreapplication

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.uth.myinventorystoreapplication.data.db.DatabaseHelper
import com.uth.myinventorystoreapplication.data.model.Product
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseHelperTest {

    private lateinit var dbHelper: DatabaseHelper

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("pulperia_inventory.db")
        dbHelper = DatabaseHelper(context)
    }

    @After
    fun tearDown() {
        dbHelper.close()
    }

    @Test
    fun testProductCRUDAndBarcode() {
        // 1. Insert product
        val p1 = Product(
            name = "Refresco Coca Cola 2L",
            price = 45.0,
            stock = 20,
            barcode = "740123456789",
            minStock = 5
        )
        val id1 = dbHelper.insertProduct(p1)
        assertTrue(id1 > 0)

        // 2. Read product by ID and Barcode
        val fetched = dbHelper.getProductById(id1)
        assertNotNull(fetched)
        assertEquals("Refresco Coca Cola 2L", fetched?.name)
        assertEquals(45.0, fetched?.price ?: 0.0, 0.001)
        assertEquals(20, fetched?.stock)
        assertEquals("740123456789", fetched?.barcode)

        val fetchedByBarcode = dbHelper.getProductByBarcode("740123456789")
        assertNotNull(fetchedByBarcode)
        assertEquals(id1, fetchedByBarcode?.id)

        // 3. Update product
        val updatedP1 = fetched!!.copy(price = 50.0, stock = 25)
        val updated = dbHelper.updateProduct(updatedP1)
        assertTrue(updated)

        val fetchedAfterUpdate = dbHelper.getProductById(id1)
        assertEquals(50.0, fetchedAfterUpdate?.price ?: 0.0, 0.001)
        assertEquals(25, fetchedAfterUpdate?.stock)

        // 4. Delete product
        val deleted = dbHelper.deleteProduct(id1)
        assertTrue(deleted)
        assertNull(dbHelper.getProductById(id1))
    }

    @Test
    fun testMovementsAndStockUpdates() {
        val p1 = Product(name = "Harina Maseca 1kg", price = 22.0, stock = 10, barcode = "111222333", minStock = 5)
        val id = dbHelper.insertProduct(p1)

        // Entrada (+5)
        val err1 = dbHelper.registerMovement(id, 5, DatabaseHelper.TYPE_ENTRADA)
        assertNull(err1)

        val prodAfterEntrada = dbHelper.getProductById(id)
        assertEquals(15, prodAfterEntrada?.stock)

        // Salida (-8)
        val err2 = dbHelper.registerMovement(id, 8, DatabaseHelper.TYPE_SALIDA)
        assertNull(err2)

        val prodAfterSalida = dbHelper.getProductById(id)
        assertEquals(7, prodAfterSalida?.stock)

        // Verify movement history
        val history = dbHelper.getAllMovements()
        assertEquals(2, history.size)
        assertEquals("Harina Maseca 1kg", history[0].productName)

        // Test Salida exceeds stock (stock is 7, request 10) -> Should fail with error message
        val err3 = dbHelper.registerMovement(id, 10, DatabaseHelper.TYPE_SALIDA)
        assertNotNull(err3)
        assertTrue(err3!!.contains("Stock insuficiente"))

        // Stock should remain 7
        val prodAfterFailedSalida = dbHelper.getProductById(id)
        assertEquals(7, prodAfterFailedSalida?.stock)
    }

    @Test
    fun testInventorySummaryAndTop5() {
        // Insert 6 products with different values
        val p1 = Product(name = "Arroz 1lb", price = 15.0, stock = 100, minStock = 10) // Value: 1500
        val p2 = Product(name = "Frijoles 1lb", price = 20.0, stock = 50, minStock = 5)  // Value: 1000
        val p3 = Product(name = "Aceite 1L", price = 60.0, stock = 30, minStock = 8)     // Value: 1800
        val p4 = Product(name = "Azúcar 1lb", price = 12.0, stock = 80, minStock = 15)  // Value: 960
        val p5 = Product(name = "Leche 1L", price = 30.0, stock = 40, minStock = 10)    // Value: 1200
        val p6 = Product(name = "Café 200g", price = 80.0, stock = 5, minStock = 10)     // Value: 400 (Low Stock Alert!)

        dbHelper.insertProduct(p1)
        dbHelper.insertProduct(p2)
        dbHelper.insertProduct(p3)
        dbHelper.insertProduct(p4)
        dbHelper.insertProduct(p5)
        dbHelper.insertProduct(p6)

        // Total inventory value = 1500 + 1000 + 1800 + 960 + 1200 + 400 = 6860
        val totalVal = dbHelper.getTotalInventoryValue()
        assertEquals(6860.0, totalVal, 0.01)

        // Top 5 products by value
        val top5 = dbHelper.getTop5ProductsByValue()
        assertEquals(5, top5.size)
        assertEquals("Aceite 1L", top5[0].name)     // 1800
        assertEquals("Arroz 1lb", top5[1].name)      // 1500
        assertEquals("Leche 1L", top5[2].name)      // 1200
        assertEquals("Frijoles 1lb", top5[3].name)   // 1000
        assertEquals("Azúcar 1lb", top5[4].name)     // 960

        // Low stock alerts (stock <= minStock) -> p6 (Café 200g: stock 5 <= minStock 10)
        val lowStockList = dbHelper.getLowStockProducts()
        assertEquals(1, lowStockList.size)
        assertEquals("Café 200g", lowStockList[0].name)
    }
}
