package com.raqueveque.foodexample.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raqueveque.foodexample.databinding.VariationsCardBinding
import com.raqueveque.foodexample.detail.constructor.VariationsExtras


@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
class VariationsAdapter(private var list: ArrayList<VariationsExtras>): RecyclerView.Adapter<VariationsAdapter.VariationsExtrasViewHolder>() {

    /**Creamos los listener para el check*/
    lateinit var mChecked: OnItemSingleCheckListener
    interface OnItemSingleCheckListener{
        fun onItemSingleCheck(position: Int, radioButton: View)
    }
    fun setOnSingleItemCheckListener(check: OnItemSingleCheckListener){
        mChecked = check
    }

    /**El ViewHolder, le pasamos como constructor primario el item (usamos viewBinding) y el listener*/
    class VariationsExtrasViewHolder(val binding: VariationsCardBinding, check: OnItemSingleCheckListener): RecyclerView.ViewHolder(binding.root) {

        /**Inicializamos las funciones del listener*/
        init {
            binding.radioButton.setOnCheckedChangeListener { _, _ ->
                check.onItemSingleCheck(layoutPosition, binding.radioButton)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariationsExtrasViewHolder {
        val binding = VariationsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VariationsExtrasViewHolder(binding, mChecked)
    }

    override fun onBindViewHolder(holder: VariationsExtrasViewHolder, position: Int) {
        val binding = holder.binding
        binding.price.text = list[position].price.toString()
        binding.radioButton.text = list[position].type
        binding.radioButton.tag = position
        /**Damos el check al primer item*/
        if (position == 0) binding.radioButton.isChecked = true
    }

    override fun getItemCount(): Int {
        return list.size
    }
}