package com.raqueveque.foodexample.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raqueveque.foodexample.R
import com.raqueveque.foodexample.main.constructor.Food
import com.squareup.picasso.Picasso

class FoodAdapter(private var foodList: ArrayList<Food>): RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    /**Creamos la funcion del checkBoxListener*/
    lateinit var mChecked: OnItemCheckListener
    interface OnItemCheckListener {
        fun onItemCheck(position: Int, checkBox: View)
    }
    fun setOnItemCheckListener(checked: OnItemCheckListener){
        mChecked = checked
    }

    /**Creamos la funcion del clickListener*/
    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    /**En el ViewHolder le pasamos al constructor primario el listener del click y el check*/
    class FoodViewHolder(itemView: View, listener: OnItemClickListener,
                         checked: OnItemCheckListener
    ):
        RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.image)
        val name: TextView = itemView.findViewById(R.id.name)
        val price: TextView = itemView.findViewById(R.id.price)
        private val checkBox: CheckBox = itemView.findViewById(R.id.isChecked)

        /**Inicializamos el click y el check*/
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
            checkBox.setOnCheckedChangeListener { _, _ ->
                checked.onItemCheck(layoutPosition, checkBox)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.example_card, parent, false)
        return FoodViewHolder(itemView, mListener, mChecked)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val item = foodList[position]
        Picasso.get().load(item.image).into(holder.image)
        holder.name.text = item.name
        holder.price.text = item.price.toString()
    }

    override fun getItemCount(): Int {
        return foodList.size
    }
}