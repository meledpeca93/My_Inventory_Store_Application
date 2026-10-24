package com.uth.myinventorystoreapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.uth.myinventorystoreapplication.R
import com.uth.myinventorystoreapplication.data.db.DatabaseHelper
import com.uth.myinventorystoreapplication.data.model.Movement

class MovementAdapter(
    private var movements: List<Movement>
) : RecyclerView.Adapter<MovementAdapter.MovementViewHolder>() {

    class MovementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivDirectionIcon)
        val tvProductName: TextView = itemView.findViewById(R.id.tvMovProductName)
        val tvDate: TextView = itemView.findViewById(R.id.tvMovDate)
        val tvQty: TextView = itemView.findViewById(R.id.tvMovQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movement, parent, false)
        return MovementViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovementViewHolder, position: Int) {
        val movement = movements[position]
        holder.tvProductName.text = movement.productName
        holder.tvDate.text = movement.date

        if (movement.type == DatabaseHelper.TYPE_ENTRADA) {
            holder.ivIcon.setImageResource(R.drawable.ic_arrow_up)
            holder.tvQty.text = "+${movement.qty} (ENTRADA)"
            holder.tvQty.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.color_entrada)
            )
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_arrow_down)
            holder.tvQty.text = "-${movement.qty} (SALIDA)"
            holder.tvQty.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.color_salida)
            )
        }
    }

    override fun getItemCount(): Int = movements.size

    fun updateData(newMovements: List<Movement>) {
        this.movements = newMovements
        notifyDataSetChanged()
    }
}
