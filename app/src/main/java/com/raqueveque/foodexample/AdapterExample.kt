package com.raqueveque.foodexample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterExample: RecyclerView.Adapter<AdapterExample.ViewHolder>() {
    var list: MutableList<ModelExample> = ArrayList()
    lateinit var context: Context

    fun recyclerAdapter(list: MutableList<ModelExample>, context: Context){
        this.list = list
        this.context = context
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val food = view.findViewById(R.id.description) as TextView
        val price = view.findViewById(R.id.price) as TextView

        fun bind(list: ModelExample) {
            food.text = list.food
            price.text = list.price
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.example_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}