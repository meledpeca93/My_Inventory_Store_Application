package com.uth.myinventorystoreapplication.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.uth.myinventorystoreapplication.data.model.Movement
import com.uth.myinventorystoreapplication.data.model.Product
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pulperia_inventory.db"
        private const val DATABASE_VERSION = 1

        // Tabla products
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_PROD_ID = "id"
        const val COLUMN_PROD_NAME = "name"
        const val COLUMN_PROD_PRICE = "price"
        const val COLUMN_PROD_STOCK = "stock"
        const val COLUMN_PROD_BARCODE = "barcode"
        const val COLUMN_PROD_MIN_STOCK = "min_stock"

        // Tabla movements
        const val TABLE_MOVEMENTS = "movements"
        const val COLUMN_MOV_ID = "id"
        const val COLUMN_MOV_PRODUCT_ID = "product_id"
        const val COLUMN_MOV_QTY = "qty"
        const val COLUMN_MOV_TYPE = "type"
        const val COLUMN_MOV_DATE = "date"

        const val TYPE_ENTRADA = "ENTRADA"
        const val TYPE_SALIDA = "SALIDA"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROD_NAME TEXT NOT NULL,
                $COLUMN_PROD_PRICE REAL NOT NULL,
                $COLUMN_PROD_STOCK INTEGER NOT NULL DEFAULT 0,
                $COLUMN_PROD_BARCODE TEXT,
                $COLUMN_PROD_MIN_STOCK INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        val createMovementsTable = """
            CREATE TABLE $TABLE_MOVEMENTS (
                $COLUMN_MOV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MOV_PRODUCT_ID INTEGER NOT NULL,
                $COLUMN_MOV_QTY INTEGER NOT NULL,
                $COLUMN_MOV_TYPE TEXT NOT NULL,
                $COLUMN_MOV_DATE TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_MOV_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PROD_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createProductsTable)
        db.execSQL(createMovementsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVEMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // --- OPERACIONES CRUD DE PRODUCTOS ---

    fun insertProduct(product: Product): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PROD_NAME, product.name)
            put(COLUMN_PROD_PRICE, product.price)
            put(COLUMN_PROD_STOCK, product.stock)
            put(COLUMN_PROD_BARCODE, product.barcode)
            put(COLUMN_PROD_MIN_STOCK, product.minStock)
        }
        return db.insert(TABLE_PRODUCTS, null, values)
    }

    fun updateProduct(product: Product): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PROD_NAME, product.name)
            put(COLUMN_PROD_PRICE, product.price)
            put(COLUMN_PROD_STOCK, product.stock)
            put(COLUMN_PROD_BARCODE, product.barcode)
            put(COLUMN_PROD_MIN_STOCK, product.minStock)
        }
        val rows = db.update(TABLE_PRODUCTS, values, "$COLUMN_PROD_ID = ?", arrayOf(product.id.toString()))
        return rows > 0
    }

    fun deleteProduct(id: Long): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_PRODUCTS, "$COLUMN_PROD_ID = ?", arrayOf(id.toString()))
        return rows > 0
    }

    fun getAllProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_PRODUCTS,
            null, null, null, null, null,
            "$COLUMN_PROD_NAME ASC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToProduct(c))
                } while (c.moveToNext())
            }
        }
        return list
    }

    fun getProductById(id: Long): Product? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PROD_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use { c ->
            if (c.moveToFirst()) cursorToProduct(c) else null
        }
    }

    fun getProductByBarcode(barcode: String): Product? {
        if (barcode.isBlank()) return null
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PROD_BARCODE = ?",
            arrayOf(barcode.trim()),
            null, null, null
        )
        return cursor.use { c ->
            if (c.moveToFirst()) cursorToProduct(c) else null
        }
    }

    fun searchProducts(query: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val q = "%${query.trim()}%"
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PROD_NAME LIKE ? OR $COLUMN_PROD_BARCODE LIKE ?",
            arrayOf(q, q),
            null, null,
            "$COLUMN_PROD_NAME ASC"
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToProduct(c))
                } while (c.moveToNext())
            }
        }
        return list
    }

    fun getLowStockProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PROD_STOCK <= $COLUMN_PROD_MIN_STOCK",
            null, null, null,
            "$COLUMN_PROD_STOCK ASC"
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToProduct(c))
                } while (c.moveToNext())
            }
        }
        return list
    }

    private fun cursorToProduct(c: Cursor): Product {
        val id = c.getLong(c.getColumnIndexOrThrow(COLUMN_PROD_ID))
        val name = c.getString(c.getColumnIndexOrThrow(COLUMN_PROD_NAME))
        val price = c.getDouble(c.getColumnIndexOrThrow(COLUMN_PROD_PRICE))
        val stock = c.getInt(c.getColumnIndexOrThrow(COLUMN_PROD_STOCK))
        val barcode = c.getString(c.getColumnIndexOrThrow(COLUMN_PROD_BARCODE)) ?: ""
        val minStock = c.getInt(c.getColumnIndexOrThrow(COLUMN_PROD_MIN_STOCK))
        return Product(id, name, price, stock, barcode, minStock)
    }

    // --- OPERACIONES DE MOVIMIENTOS ---

    /**
     * Registra un movimiento de inventario (ENTRADA o SALIDA)
     * y actualiza el stock del producto en la misma transacción.
     */
    fun registerMovement(productId: Long, qty: Int, type: String): String? {
        if (qty <= 0) return "La cantidad debe ser mayor a 0."
        val db = writableDatabase

        db.beginTransaction()
        try {
            val product = getProductById(productId)
                ?: return "El producto especificado no existe."

            var newStock = product.stock
            if (type == TYPE_ENTRADA) {
                newStock += qty
            } else if (type == TYPE_SALIDA) {
                if (product.stock < qty) {
                    return "Stock insuficiente. Stock actual: ${product.stock}, Solicitado: $qty."
                }
                newStock -= qty
            } else {
                return "Tipo de movimiento no válido."
            }

            // Update product stock
            val updateValues = ContentValues().apply {
                put(COLUMN_PROD_STOCK, newStock)
            }
            db.update(TABLE_PRODUCTS, updateValues, "$COLUMN_PROD_ID = ?", arrayOf(productId.toString()))

            // Insert movement record
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            val movementValues = ContentValues().apply {
                put(COLUMN_MOV_PRODUCT_ID, productId)
                put(COLUMN_MOV_QTY, qty)
                put(COLUMN_MOV_TYPE, type)
                put(COLUMN_MOV_DATE, currentDate)
            }
            db.insert(TABLE_MOVEMENTS, null, movementValues)

            db.setTransactionSuccessful()
            return null // null significa éxito
        } finally {
            db.endTransaction()
        }
    }

    fun getAllMovements(): List<Movement> {
        val list = mutableListOf<Movement>()
        val db = readableDatabase

        val query = """
            SELECT m.$COLUMN_MOV_ID, m.$COLUMN_MOV_PRODUCT_ID, m.$COLUMN_MOV_QTY, 
                   m.$COLUMN_MOV_TYPE, m.$COLUMN_MOV_DATE, p.$COLUMN_PROD_NAME
            FROM $TABLE_MOVEMENTS m
            INNER JOIN $TABLE_PRODUCTS p ON m.$COLUMN_MOV_PRODUCT_ID = p.$COLUMN_PROD_ID
            ORDER BY m.$COLUMN_MOV_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val id = c.getLong(c.getColumnIndexOrThrow(COLUMN_MOV_ID))
                    val productId = c.getLong(c.getColumnIndexOrThrow(COLUMN_MOV_PRODUCT_ID))
                    val qty = c.getInt(c.getColumnIndexOrThrow(COLUMN_MOV_QTY))
                    val type = c.getString(c.getColumnIndexOrThrow(COLUMN_MOV_TYPE))
                    val date = c.getString(c.getColumnIndexOrThrow(COLUMN_MOV_DATE))
                    val productName = c.getString(c.getColumnIndexOrThrow(COLUMN_PROD_NAME))
                    list.add(Movement(id, productId, qty, type, date, productName))
                } while (c.moveToNext())
            }
        }
        return list
    }

    // --- CÁLCULOS DE RESUMEN ---

    /**
     * Calcula el valor total del inventario: SUMA DE (precio * stock)
     */
    fun getTotalInventoryValue(): Double {
        val db = readableDatabase
        val query = "SELECT SUM($COLUMN_PROD_PRICE * $COLUMN_PROD_STOCK) FROM $TABLE_PRODUCTS"
        val cursor = db.rawQuery(query, null)
        var total = 0.0
        cursor.use { c ->
            if (c.moveToFirst()) {
                total = c.getDouble(0)
            }
        }
        return total
    }

    /**
     * Obtiene el Top 5 de productos con mayor valor total en inventario (price * stock)
     */
    fun getTop5ProductsByValue(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val query = """
            SELECT *, ($COLUMN_PROD_PRICE * $COLUMN_PROD_STOCK) AS total_val
            FROM $TABLE_PRODUCTS
            ORDER BY total_val DESC
            LIMIT 5
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToProduct(c))
                } while (c.moveToNext())
            }
        }
        return list
    }
}
