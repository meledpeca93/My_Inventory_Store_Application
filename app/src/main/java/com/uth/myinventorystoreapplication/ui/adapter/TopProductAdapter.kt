package com.uth.myinventorystoreapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uth.myinventorystoreapplication.R
import com.uth.myinventorystoreapplication.data.model.Product
import java.util.Locale

class TopProductAdapter(
    private var topProducts: List<Product>
) : RecyclerView.Adapter<TopProductAdapter.TopProductViewHolder>() {

    class TopProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val tvName: TextView = itemView.findViewById(R.id.tvTopProductName)
        val tvDetail: TextView = itemView.findViewById(R.id.tvTopProductDetail)
        val tvValue: TextView = itemView.findViewById(R.id.tvTopProductValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_product, parent, false)
        return TopProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopProductViewHolder, position: Int) {
        val product = topProducts[position]
        holder.tvRank.text = "#${position + 1}"
        holder.tvName.text = product.name
        holder.tvDetail.text = String.format(Locale.getDefault(), "Stock: %d  x  L. %.2f", product.stock, product.price)
        holder.tvValue.text = String.format(Locale.getDefault(), "L. %.2f", product.totalValue)
    }

    override fun getItemCount(): Int = topProducts.size

    fun updateData(newList: List<Product>) {
        this.topProducts = newList
        notifyDataSetChanged()
    }
}
