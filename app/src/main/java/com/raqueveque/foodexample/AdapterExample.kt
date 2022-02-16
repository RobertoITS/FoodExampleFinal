package com.raqueveque.foodexample

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.stream.Collectors

class AdapterExample: RecyclerView.Adapter<AdapterExample.ViewHolder>() {
    //Variable de lista
    var list: MutableList<ModelExample> = ArrayList()
    //Variable de lista, para resetear lista en busqueda
    var originalList: MutableList<ModelExample> = ArrayList()
    //Inicializamos el contexto
    lateinit var context: Context
    //Inicializamos el escuchador
    lateinit var mListener: OnItemClickListener

    //Inicializamos el adaptador
    fun recyclerAdapter(list: MutableList<ModelExample>, context: Context){
        this.list = list
        this.context = context
        originalList.addAll(list)
    }

    //ViewModel, lo extendemos de la clase OnItemClickListener
    class ViewHolder(view: View, listener: OnItemClickListener): RecyclerView.ViewHolder(view) {
        private val food = view.findViewById(R.id.description) as TextView
        private val price = view.findViewById(R.id.price) as TextView

        fun bind(list: ModelExample) {
            food.text = list.food
            price.text = list.price
        }

        //Click Listener
        init {
            view.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    //ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.example_card, parent, false)
        //Devolvemos el ViewHolder, con la vista (layout) y el listener
        return ViewHolder(view, mListener)
    }

    //Enlazamos los componentes del CardView con los objetos
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    //El contador de items
    override fun getItemCount(): Int {
        return list.size
    }

    //Modo de busqueda
    @SuppressLint("NotifyDataSetChanged")
    fun search(query: String){
        val long = query.length
        if (long == 0){
            list.clear()
            list.addAll(originalList)
        } else{
            val collection = list.stream().filter { i -> i.food.lowercase()
                .contains(query.lowercase()) }.collect(Collectors.toList())
            list.clear()
            list.addAll(collection)
        }
        notifyDataSetChanged()
    }

    //El click sobre los items
    interface OnItemClickListener{
        fun onItemClick (position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }
}