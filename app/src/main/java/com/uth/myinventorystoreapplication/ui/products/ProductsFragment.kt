package com.uth.myinventorystoreapplication.ui.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uth.myinventorystoreapplication.R
import com.uth.myinventorystoreapplication.data.db.DatabaseHelper
import com.uth.myinventorystoreapplication.data.model.Product
import com.uth.myinventorystoreapplication.ui.adapter.ProductAdapter

class ProductsFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ProductAdapter
    private lateinit var rvProducts: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tvEmptyProducts: TextView
    private lateinit var fabAddProduct: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        dbHelper = DatabaseHelper(requireContext())
        rvProducts = view.findViewById(R.id.rvProducts)
        etSearch = view.findViewById(R.id.etSearch)
        tvEmptyProducts = view.findViewById(R.id.tvEmptyProducts)
        fabAddProduct = view.findViewById(R.id.fabAddProduct)

        adapter = ProductAdapter(
            products = emptyList(),
            onEditClick = { product -> showAddEditProductDialog(product) },
            onDeleteClick = { product -> showDeleteConfirmationDialog(product) }
        )
        rvProducts.adapter = adapter

        fabAddProduct.setOnClickListener {
            showAddEditProductDialog(null)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadProducts(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        loadProducts(etSearch.text.toString())
    }

    fun loadProducts(query: String = "") {
        val products = if (query.isBlank()) {
            dbHelper.getAllProducts()
        } else {
            dbHelper.searchProducts(query)
        }

        adapter.updateData(products)

        if (products.isEmpty()) {
            tvEmptyProducts.visibility = View.VISIBLE
            rvProducts.visibility = View.GONE
        } else {
            tvEmptyProducts.visibility = View.GONE
            rvProducts.visibility = View.VISIBLE
        }
    }

    private fun showAddEditProductDialog(productToEdit: Product?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_product, null)
        val tvTitle: TextView = dialogView.findViewById(R.id.tvDialogTitle)
        val etName: EditText = dialogView.findViewById(R.id.etProductName)
        val etPrice: EditText = dialogView.findViewById(R.id.etProductPrice)
        val etStock: EditText = dialogView.findViewById(R.id.etProductStock)
        val etBarcode: EditText = dialogView.findViewById(R.id.etProductBarcode)
        val etMinStock: EditText = dialogView.findViewById(R.id.etProductMinStock)

        if (productToEdit != null) {
            tvTitle.text = getString(R.string.edit_product)
            etName.setText(productToEdit.name)
            etPrice.setText(productToEdit.price.toString())
            etStock.setText(productToEdit.stock.toString())
            etBarcode.setText(productToEdit.barcode)
            etMinStock.setText(productToEdit.minStock.toString())
        } else {
            tvTitle.text = getString(R.string.add_product)
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save, null)
            .setNegativeButton(R.string.btn_cancel, null)

        val dialog = builder.create()
        dialog.show()

        // Override listener to handle custom validation without closing immediately on error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = etName.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val stockStr = etStock.text.toString().trim()
            val barcode = etBarcode.text.toString().trim()
            val minStockStr = etMinStock.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Ingrese el nombre del producto"
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price <= 0) {
                etPrice.error = "Ingrese un precio válido mayor a 0"
                return@setOnClickListener
            }

            val stock = stockStr.toIntOrNull()
            if (stock == null || stock < 0) {
                etStock.error = "Ingrese un stock válido (>= 0)"
                return@setOnClickListener
            }

            val minStock = if (minStockStr.isBlank()) 0 else (minStockStr.toIntOrNull() ?: 0)
            if (minStock < 0) {
                etMinStock.error = "El stock mínimo no puede ser negativo"
                return@setOnClickListener
            }

            if (productToEdit == null) {
                // Nuevo producto
                val newProduct = Product(
                    name = name,
                    price = price,
                    stock = stock,
                    barcode = barcode,
                    minStock = minStock
                )
                val id = dbHelper.insertProduct(newProduct)
                if (id > 0) {
                    Toast.makeText(requireContext(), "Producto registrado correctamente", Toast.LENGTH_SHORT).show()
                    loadProducts(etSearch.text.toString())
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar el producto", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Actualizar producto
                val updatedProduct = productToEdit.copy(
                    name = name,
                    price = price,
                    stock = stock,
                    barcode = barcode,
                    minStock = minStock
                )
                val success = dbHelper.updateProduct(updatedProduct)
                if (success) {
                    Toast.makeText(requireContext(), "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
                    loadProducts(etSearch.text.toString())
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar el producto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        val message = getString(R.string.confirm_delete_msg, product.name)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(message)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                val success = dbHelper.deleteProduct(product.id)
                if (success) {
                    Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                    loadProducts(etSearch.text.toString())
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar el producto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
}
