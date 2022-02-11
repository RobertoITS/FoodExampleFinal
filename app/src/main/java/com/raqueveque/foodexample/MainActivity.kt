package com.raqueveque.foodexample

import android.R
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.raqueveque.foodexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter: AdapterExample = AdapterExample()
    private var list: MutableList<ModelExample> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))
        list.add(ModelExample("Pizza", "$400"))

        setupAdapter()

        var isToolbarShown = false

        binding.toolbarLayout.title = binding.plantDetailName.text

        binding.plantDetailScrollview.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, _, _ ->

                //Controlamos el movimiento del scroll
                //pasamos la opcion de deshabilitar o no el scroll del recycler para evitar inconvenientes
                if (!v.canScrollVertically(1)){
//                    Toast.makeText(this, "Final!", Toast.LENGTH_LONG).show()
                    binding.recycler.isNestedScrollingEnabled = true
                }
                if (v.canScrollVertically(1)) {
//                    Toast.makeText(this, "Principio", Toast.LENGTH_LONG).show()
                    binding.recycler.isNestedScrollingEnabled = false
                }
//
//                binding.plantDetailScrollview.isNestedScrollingEnabled = !binding.recycler.canScrollVertically(1)

                // User scrolled past image to height of toolbar and the title text is
                // underneath the toolbar, so the toolbar should be shown.
                val shouldShowToolbar = scrollY > binding.toolbar.height

                // The new state of the toolbar differs from the previous state; update
                // appbar and toolbar attributes.
                if (isToolbarShown != shouldShowToolbar) {
                    isToolbarShown = shouldShowToolbar

                    // Use shadow animator to add elevation if toolbar is shown
                    binding.appbar.isActivated = shouldShowToolbar

                    // Show the plant name if toolbar is shown
                    binding.toolbarLayout.isTitleEnabled = shouldShowToolbar


                    if (shouldShowToolbar){
                        val colorFrom = resources.getColor(R.color.transparent)
                        val colorTo = resources.getColor(R.color.darker_gray)
                        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                        colorAnimation.duration = 250 // milliseconds

                        colorAnimation.addUpdateListener { animator ->
                            binding.toolbar.setBackgroundColor(
                                animator.animatedValue as Int
                            )
                        }
                        colorAnimation.start()
                    } else {
                        val colorFrom = ContextCompat.getColor(this, R.color.darker_gray)
                        val colorTo = ContextCompat.getColor(this, R.color.transparent)
                        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                        colorAnimation.duration = 250 // milliseconds

                        colorAnimation.addUpdateListener { animator ->
                            binding.toolbar.setBackgroundColor(
                                animator.animatedValue as Int
                            )
                        }
                        colorAnimation.start()
                    }
                }
            }
        )


    }

    private fun setupAdapter() {
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.isNestedScrollingEnabled = false
        adapter.recyclerAdapter(list, this)
        binding.recycler.adapter = adapter
    }
}