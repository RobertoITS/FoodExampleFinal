package com.raqueveque.foodexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.raqueveque.foodexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val adapter: AdapterExample = AdapterExample()
    var list: MutableList<ModelExample> = mutableListOf()
    var state: Boolean = false
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
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

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

                    if (scrollY == 100){
                        state = true
                    }

                }
            }
        )
    }
    fun setupAdapter(){
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = object : LinearLayoutManager(this){
            override fun canScrollVertically(): Boolean {
                return state
            }
        }
        adapter.recyclerAdapter(list, this)
        binding.recycler.adapter = adapter
    }
}