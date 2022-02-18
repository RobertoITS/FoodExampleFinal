package com.raqueveque.foodexample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.raqueveque.foodexample.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding

//    private lateinit var foodArrayList: ArrayList<Food>
//    private lateinit var mAdapter: FoodAdapter
//    private lateinit var db: FirebaseFirestore
//
//    private lateinit var searchMenu: Menu
//    private lateinit var itemSearch: MenuItem
//
//    private var vsble: Boolean = false
//    var isToolbarShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
//        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        getData()
//
//        setSupportActionBar(binding.toolbar)
//
//        setSearchToolbar()
//
//        Utilities.setupUI(findViewById(R.id.rootActivity), this)
//
//        binding.plantDetailScrollview.setOnScrollChangeListener(
//            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, _, _ ->
//                // User scrolled past image to height of toolbar and the title text is
//                // underneath the toolbar, so the toolbar should be shown.
//                val shouldShowToolbar = scrollY > binding.toolbar.height
//                // The new state of the toolbar differs from the previous state; update
//                // appbar and toolbar attributes.
//                if (isToolbarShown != shouldShowToolbar) {
//                    isToolbarShown = shouldShowToolbar
//                    // Use shadow animator to add elevation if toolbar is shown
//                    binding.appbar.isActivated = shouldShowToolbar
//                }
//            }
//        )
    }
}