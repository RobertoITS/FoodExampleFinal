package com.raqueveque.foodexample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.raqueveque.foodexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter: AdapterExample = AdapterExample()
    private var list: MutableList<ModelExample> = mutableListOf()

    private var searchToolbar: Toolbar? = null
    private lateinit var searchMenu: Menu
    private lateinit var itemSearch: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setList()

        setupAdapter()

        setSupportActionBar(binding.toolbar)

        setSearchToolbar()

        var isToolbarShown = false

        binding.plantDetailScrollview.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, _, _ ->
                // User scrolled past image to height of toolbar and the title text is
                // underneath the toolbar, so the toolbar should be shown.
                val shouldShowToolbar = scrollY > binding.toolbar.height
                // The new state of the toolbar differs from the previous state; update
                // appbar and toolbar attributes.
                if (isToolbarShown != shouldShowToolbar) {
                    isToolbarShown = shouldShowToolbar
                    // Use shadow animator to add elevation if toolbar is shown
                    binding.appbar.isActivated = shouldShowToolbar
                }
            }
        )


    }

    private fun setSearchToolbar() {
        binding.searchToolbar.inflateMenu(R.menu.menu_search)
        searchMenu = binding.searchToolbar.menu
        binding.searchToolbar.setNavigationOnClickListener {
            circleRevealAnimation(1, containsOverflow = true, isShow = false)
        }
        itemSearch = searchMenu.findItem(R.id.action_filter_search)
        MenuItemCompat.setOnActionExpandListener(itemSearch, object : MenuItemCompat.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                //Hacer algo cuando expande
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                circleRevealAnimation(1, containsOverflow = true, isShow = false)
                return true
            }
        })
        initSearchView()
    }

    @SuppressLint("CutPasteId", "SoonBlockedPrivateApi")
    private fun initSearchView(){
        val searchView = searchMenu.findItem(R.id.action_filter_search).actionView as SearchView
        //Activa/desactiva submit button en el teclado
        searchView.isSubmitButtonEnabled = false
        //Cambia el boton de cerrar
        val closeButton = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
        closeButton.setImageResource(R.drawable.ic_close)
        //Configuramos colores de texto y pista
        val txtSearch = searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText
        txtSearch.hint = "Buscar.."
        txtSearch.setHintTextColor(Color.GRAY)
        txtSearch.setTextColor(Color.BLACK)
        //Colocamos el cursor
        val searchTextView =
            searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as AutoCompleteTextView
        try {
            val mCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            mCursorDrawableRes.isAccessible = true
            mCursorDrawableRes[searchTextView] =
                R.drawable.search_cursor //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (e: Exception) {
            e.printStackTrace()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                callSearch(query)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                callSearch(newText)
                return true
            }

            fun callSearch(query: String) {
                //Do searching
                Log.i("query", "" + query)
            }
        })
    }

    private fun circleRevealAnimation(posFromRight: Int, containsOverflow: Boolean, isShow: Boolean) {
        var width = binding.searchToolbar.width
        if (posFromRight > 0) width -= posFromRight * resources
            .getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_button_min_width_material) - resources
            .getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_button_min_width_material) / 2
        if (containsOverflow) width -= resources
            .getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_button_min_width_overflow_material)
        val cx = width
        val cy = binding.searchToolbar.height / 2
        val anim: Animator = if (isShow) ViewAnimationUtils.createCircularReveal(
            binding.searchToolbar, cx, cy, 0f, width.toFloat())
            else ViewAnimationUtils.createCircularReveal(binding.searchToolbar, cx, cy, width.toFloat(), 0f)
        anim.duration = 220.toLong()

        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isShow) {
                    super.onAnimationEnd(animation)
                    binding.searchToolbar.visibility = View.INVISIBLE
                }
            }
        })

        // make the view visible and start the animation
        if (isShow) binding.searchToolbar.visibility = View.VISIBLE

        // start the animation
        anim.start()
    }

    private fun setList() {
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
    }

    private fun setupAdapter() {
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.isNestedScrollingEnabled = false
        adapter.recyclerAdapter(list, this)
        binding.recycler.adapter = adapter
    }
}