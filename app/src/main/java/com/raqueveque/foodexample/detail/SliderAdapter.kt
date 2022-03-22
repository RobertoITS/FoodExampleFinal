package com.raqueveque.foodexample.detail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.raqueveque.foodexample.R
import com.raqueveque.foodexample.detail.SliderAdapter.*
import com.squareup.picasso.Picasso

class SliderAdapter (items: ArrayList<ImageSlider>, viewPager2: ViewPager2): RecyclerView.Adapter<SliderViewHolder>() {

    private val sliderItems: List<ImageSlider>
    //
    private val viewPager2: ViewPager2
    init {
        this.sliderItems = items
        //
        this.viewPager2 = viewPager2
    }

    class SliderViewHolder(item: View): RecyclerView.ViewHolder(item){
        val image: ImageView = item.findViewById(R.id.imageSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        return SliderViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.image_container, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = sliderItems[position]
        Picasso.get().load(item.image).into(holder.image)
        //Hasta aca la animacion:
        if (position == sliderItems.size - 2){
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int {
        return sliderItems.size
    }

    //Antes de la animacion:
    @SuppressLint("NotifyDataSetChanged")
    private val runnable = Runnable {
        items.addAll(sliderItems)
        notifyDataSetChanged()
    }

}