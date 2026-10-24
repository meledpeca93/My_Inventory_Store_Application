package com.uth.myinventorystoreapplication.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.uth.myinventorystoreapplication.R
import com.uth.myinventorystoreapplication.data.db.DatabaseHelper
import com.uth.myinventorystoreapplication.ui.adapter.ProductAdapter
import com.uth.myinventorystoreapplication.ui.adapter.TopProductAdapter
import java.util.Locale

class SummaryFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var tvTotalValue: TextView
    private lateinit var tvTotalProductsCount: TextView
    private lateinit var tvLowStockCount: TextView
    private lateinit var rvTop5Products: RecyclerView
    private lateinit var tvEmptyTop5: TextView
    private lateinit var rvLowStockAlerts: RecyclerView
    private lateinit var tvNoAlerts: TextView

    private lateinit var topProductAdapter: TopProductAdapter
    private lateinit var lowStockAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_summary, container, false)

        dbHelper = DatabaseHelper(requireContext())

        tvTotalValue = view.findViewById(R.id.tvTotalInventoryValue)
        tvTotalProductsCount = view.findViewById(R.id.tvTotalProductsCount)
        tvLowStockCount = view.findViewById(R.id.tvLowStockCount)
        rvTop5Products = view.findViewById(R.id.rvTop5Products)
        tvEmptyTop5 = view.findViewById(R.id.tvEmptyTop5)
        rvLowStockAlerts = view.findViewById(R.id.rvLowStockAlerts)
        tvNoAlerts = view.findViewById(R.id.tvNoAlerts)

        topProductAdapter = TopProductAdapter(emptyList())
        rvTop5Products.adapter = topProductAdapter

        lowStockAdapter = ProductAdapter(
            products = emptyList(),
            onEditClick = {},
            onDeleteClick = {}
        )
        rvLowStockAlerts.adapter = lowStockAdapter

        return view
    }

    override fun onResume() {
        super.onResume()
        loadSummaryData()
    }

    fun loadSummaryData() {
        // 1. Valor total del inventario
        val totalValue = dbHelper.getTotalInventoryValue()
        tvTotalValue.text = String.format(Locale.getDefault(), "L. %,.2f", totalValue)

        // 2. Métricas rápidas
        val allProducts = dbHelper.getAllProducts()
        tvTotalProductsCount.text = "Total Productos: ${allProducts.size}"

        val lowStockProducts = dbHelper.getLowStockProducts()
        tvLowStockCount.text = "Alertas Críticas: ${lowStockProducts.size}"

        // 3. Top 5 productos con mayor valor
        val top5 = dbHelper.getTop5ProductsByValue()
        topProductAdapter.updateData(top5)
        if (top5.isEmpty()) {
            tvEmptyTop5.visibility = View.VISIBLE
            rvTop5Products.visibility = View.GONE
        } else {
            tvEmptyTop5.visibility = View.GONE
            rvTop5Products.visibility = View.VISIBLE
        }

        // 4. Alertas de stock mínimo
        lowStockAdapter.updateData(lowStockProducts)
        if (lowStockProducts.isEmpty()) {
            tvNoAlerts.visibility = View.VISIBLE
            rvLowStockAlerts.visibility = View.GONE
        } else {
            tvNoAlerts.visibility = View.GONE
            rvLowStockAlerts.visibility = View.VISIBLE
        }
    }
}
