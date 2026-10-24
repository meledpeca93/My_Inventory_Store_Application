package com.uth.myinventorystoreapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.uth.myinventorystoreapplication.R
import com.uth.myinventorystoreapplication.data.model.Product
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvAlertBadge: TextView = itemView.findViewById(R.id.tvAlertBadge)
        val tvBarcode: TextView = itemView.findViewById(R.id.tvBarcode)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.tvName.text = product.name
        holder.tvPrice.text = String.format(Locale.getDefault(), "Precio: L. %.2f", product.price)
        holder.tvStock.text = String.format(Locale.getDefault(), "Stock: %d (Mínimo: %d)", product.stock, product.minStock)

        if (product.barcode.isNotBlank()) {
            holder.tvBarcode.text = "Cód: ${product.barcode}"
            holder.tvBarcode.visibility = View.VISIBLE
        } else {
            holder.tvBarcode.text = "Sin Cód. Barra"
            holder.tvBarcode.visibility = View.VISIBLE
        }

        // Reto adicional: Alerta si el stock alcanza el mínimo predefinido
        if (product.isLowStock) {
            holder.tvAlertBadge.text = "¡STOCK CRÍTICO!"
            holder.tvAlertBadge.visibility = View.VISIBLE
        } else {
            holder.tvAlertBadge.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener { onEditClick(product) }
        holder.btnDelete.setOnClickListener { onDeleteClick(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        this.products = newProducts
        notifyDataSetChanged()
    }
}
