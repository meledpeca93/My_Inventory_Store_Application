package com.uth.myinventorystoreapplication.ui.movements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.uth.myinventorystoreapplication.R
import com.uth.myinventorystoreapplication.data.db.DatabaseHelper
import com.uth.myinventorystoreapplication.data.model.Movement
import com.uth.myinventorystoreapplication.data.model.Product
import com.uth.myinventorystoreapplication.ui.adapter.MovementAdapter

class MovementsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var movementAdapter: MovementAdapter

    private lateinit var spinnerProducts: Spinner
    private lateinit var etBarcodeSearch: EditText
    private lateinit var btnSearchBarcode: Button
    private lateinit var tvSelectedStock: TextView
    private lateinit var rgType: RadioGroup
    private lateinit var rbEntrada: RadioButton
    private lateinit var rbSalida: RadioButton
    private lateinit var etQuantity: EditText
    private lateinit var btnRegister: Button
    private lateinit var rvMovements: RecyclerView
    private lateinit var tvEmptyMovements: TextView

    private var productList: List<Product> = emptyList()
    private var selectedProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movements, container, false)

        dbHelper = DatabaseHelper(requireContext())

        spinnerProducts = view.findViewById(R.id.spinnerProducts)
        etBarcodeSearch = view.findViewById(R.id.etBarcodeSearch)
        btnSearchBarcode = view.findViewById(R.id.btnSearchBarcode)
        tvSelectedStock = view.findViewById(R.id.tvSelectedStock)
        rgType = view.findViewById(R.id.rgType)
        rbEntrada = view.findViewById(R.id.rbEntrada)
        rbSalida = view.findViewById(R.id.rbSalida)
        etQuantity = view.findViewById(R.id.etQuantity)
        btnRegister = view.findViewById(R.id.btnRegister)
        rvMovements = view.findViewById(R.id.rvMovements)
        tvEmptyMovements = view.findViewById(R.id.tvEmptyMovements)

        movementAdapter = MovementAdapter(emptyList())
        rvMovements.adapter = movementAdapter

        btnSearchBarcode.setOnClickListener {
            performBarcodeSearch()
        }

        btnRegister.setOnClickListener {
            registerMovement()
        }

        spinnerProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in productList.indices) {
                    selectedProduct = productList[position]
                    updateStockDisplay()
                } else {
                    selectedProduct = null
                    tvSelectedStock.text = "Stock actual: --"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProduct = null
                tvSelectedStock.text = "Stock actual: --"
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadProductSpinner()
        loadMovementsHistory()
    }

    fun loadProductSpinner() {
        productList = dbHelper.getAllProducts()
        if (productList.isEmpty()) {
            val emptyAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("No hay productos registrados")
            )
            emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerProducts.adapter = emptyAdapter
            selectedProduct = null
            tvSelectedStock.text = "Stock actual: --"
            return
        }

        val displayList = productList.map { p ->
            val codeStr = if (p.barcode.isNotBlank()) " [Cód: ${p.barcode}]" else ""
            "${p.name}$codeStr - Stock: ${p.stock}"
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            displayList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducts.adapter = adapter

        // Re-select previously selected product if possible
        selectedProduct?.let { sel ->
            val index = productList.indexOfFirst { it.id == sel.id }
            if (index >= 0) {
                spinnerProducts.setSelection(index)
                selectedProduct = productList[index]
            }
        } ?: run {
            if (productList.isNotEmpty()) {
                spinnerProducts.setSelection(0)
                selectedProduct = productList[0]
            }
        }
        updateStockDisplay()
    }

    private fun performBarcodeSearch() {
        val barcode = etBarcodeSearch.text.toString().trim()
        if (barcode.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese un código de barra para buscar", Toast.LENGTH_SHORT).show()
            return
        }

        val foundProduct = dbHelper.getProductByBarcode(barcode)
        if (foundProduct != null) {
            val index = productList.indexOfFirst { it.id == foundProduct.id }
            if (index >= 0) {
                spinnerProducts.setSelection(index)
                selectedProduct = productList[index]
                updateStockDisplay()
                Toast.makeText(requireContext(), "Producto encontrado: ${foundProduct.name}", Toast.LENGTH_SHORT).show()
            } else {
                loadProductSpinner()
                val newIndex = productList.indexOfFirst { it.id == foundProduct.id }
                if (newIndex >= 0) {
                    spinnerProducts.setSelection(newIndex)
                    selectedProduct = productList[newIndex]
                    updateStockDisplay()
                }
            }
        } else {
            Toast.makeText(requireContext(), "No se encontró ningún producto con el código: $barcode", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateStockDisplay() {
        selectedProduct?.let { p ->
            tvSelectedStock.text = "Stock actual: ${p.stock} (Mínimo: ${p.minStock})"
        } ?: run {
            tvSelectedStock.text = "Stock actual: --"
        }
    }

    private fun registerMovement() {
        val prod = selectedProduct
        if (prod == null) {
            Toast.makeText(requireContext(), "Seleccione un producto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val qtyStr = etQuantity.text.toString().trim()
        val qty = qtyStr.toIntOrNull()
        if (qty == null || qty <= 0) {
            etQuantity.error = "Ingrese una cantidad válida mayor a 0"
            return
        }

        val type = if (rbEntrada.isChecked) DatabaseHelper.TYPE_ENTRADA else DatabaseHelper.TYPE_SALIDA

        val errorMsg = dbHelper.registerMovement(prod.id, qty, type)
        if (errorMsg != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Error en Movimiento")
                .setMessage(errorMsg)
                .setPositiveButton("Aceptar", null)
                .show()
            return
        }

        // Movement succeeded! Clear input quantity
        etQuantity.text?.clear()
        Toast.makeText(requireContext(), "Movimiento registrado exitosamente", Toast.LENGTH_SHORT).show()

        // Reload data
        loadProductSpinner()
        loadMovementsHistory()

        // Check if updated product hits stock minimum alert (Reto adicional)
        val updatedProduct = dbHelper.getProductById(prod.id)
        if (updatedProduct != null && updatedProduct.isLowStock) {
            showLowStockAlert(updatedProduct)
        }
    }

    private fun showLowStockAlert(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ ALERTA DE STOCK MÍNIMO")
            .setMessage("El producto '${product.name}' ha alcanzado o está por debajo del stock mínimo predefinido.\n\nStock actual: ${product.stock}\nStock mínimo: ${product.minStock}\n\nSe recomienda reponer el inventario de este producto.")
            .setPositiveButton("Entendido", null)
            .setIcon(R.drawable.ic_warning)
            .show()
    }

    fun loadMovementsHistory() {
        val history = dbHelper.getAllMovements()
        movementAdapter.updateData(history)

        if (history.isEmpty()) {
            tvEmptyMovements.visibility = View.VISIBLE
            rvMovements.visibility = View.GONE
        } else {
            tvEmptyMovements.visibility = View.GONE
            rvMovements.visibility = View.VISIBLE
        }
    }
}
