package com.raqueveque.foodexample.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raqueveque.foodexample.R
import com.raqueveque.foodexample.detail.constructor.VariationsExtras

class VariationsAdapter(private var list: ArrayList<VariationsExtras>): RecyclerView.Adapter<VariationsAdapter.VariationsExtrasViewHolder>() {

    class VariationsExtrasViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
        val price: TextView = itemView.findViewById(R.id.price)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariationsExtrasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.variations_card, parent, false)
        return VariationsExtrasViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VariationsExtrasViewHolder, position: Int) {
        val item = list[position]
        holder.price.text = item.price.toString()
        holder.radioButton.text = item.type
    }

    override fun getItemCount(): Int {
        return list.size
    }
}