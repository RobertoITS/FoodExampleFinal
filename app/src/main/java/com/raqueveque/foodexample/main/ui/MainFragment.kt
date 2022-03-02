package com.raqueveque.foodexample.main.ui

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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.raqueveque.foodexample.R
import com.raqueveque.foodexample.Utilities
import java.util.*
import kotlin.collections.ArrayList
import com.raqueveque.foodexample.databinding.FragmentMainBinding
import com.raqueveque.foodexample.main.adapter.FoodAdapter
import com.raqueveque.foodexample.main.constructor.Food

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var foodArrayList: ArrayList<Food> = arrayListOf()
    private lateinit var mAdapter: FoodAdapter
    private lateinit var db: FirebaseFirestore

    private lateinit var checkedList: ArrayList<Food>

    private lateinit var searchMenu: Menu
    private lateinit var itemSearch: MenuItem

    private var visible: Boolean = false
    var isToolbarShown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        //Si no le damos el valor true, el menu no se muestra
        setHasOptionsMenu(true)

        checkedList = arrayListOf()

        getData()

        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)

        setSearchToolbar()

        Utilities.setupUI(binding.rootActivity, requireContext())

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

        return binding.root
    }
    //Obtenemos los datos
    private fun getData() {
        /**Comprobamos que la lista este vacia para
         * realizar una sola vez la consulta a la base
         * de datos, evitamos la sobrecarga de carga/descarga*/
        if (foodArrayList.size == 0) {
            db = FirebaseFirestore.getInstance()
            db.collection("food").addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("FIREBASE ERROR", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
//                            foodArrayList.add(dc.document.toObject(Food::class.java))
                            val item = Food(
                                dc.document.get("image")!!.toString(),
                                dc.document.get("name")!!.toString(),
                                dc.document.get("price") as Long?,
                                //Pasamos el Id del documento
                                dc.document.id
                            )
                            foodArrayList.add(item)
                        }
                    }
                    updateListFood(foodArrayList)
                }
            })
        } else {
            updateListFood(foodArrayList)
        }
    }
    //El Buscardor
    private fun searchFood(query: String) {
        var foodName = query
        if (foodName.isNotEmpty()) foodName =
            foodName.substring(0, 1).uppercase(Locale.getDefault()) + foodName.substring(1)
                .lowercase(Locale.getDefault())
        val results: ArrayList<Food> = ArrayList()
        for (food in foodArrayList) {
            if (food.name != null && food.name!!.contains(foodName)) {
                results.add(food)
            }
        }
        updateListFood(results)
    }
    //Actualiza la lista del recycler
    @SuppressLint("NotifyDataSetChanged")
    private fun updateListFood(listFood: ArrayList<Food>) {
        mAdapter = FoodAdapter(listFood)
//        binding.recycler.isNestedScrollingEnabled = false
        binding.recycler.adapter = mAdapter
        binding.recycler.layoutManager = LinearLayoutManager(context)
        mAdapter.notifyDataSetChanged()

        /**Aqui sobreescribimos las funciones:*/
        mAdapter.setOnItemClickListener(object : FoodAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                //Enviamos los argumentos al siguiente fragment
                val action = MainFragmentDirections.actionMainFragmentToDetailFragment(
                    listFood[position].name,
                    listFood[position].price!!,
                    listFood[position].id,
                    listFood[position].image
                )
                    findNavController().navigate(action)
            }
        })
        mAdapter.setOnItemCheckListener(object : FoodAdapter.OnItemCheckListener{
            override fun onItemCheck(position: Int, checkBox: View) {
                val check = checkBox as CheckBox
                if (check.isChecked) {
                    checkedList.add(foodArrayList[position])
                    //Esta variable maneja el estado del checkbox. Ver adaptador (OnBindViewHolder)
                    listFood[position].check = true
                }
                else {
                    checkedList.remove(foodArrayList[position])
                    listFood[position].check = false
                }

            }
        })
    }

    //Inflamos el menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    //Las opciones del click a los items del menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Maneja la seleccion de items
        return when (item.itemId){
            R.id.action_status -> {
                Toast.makeText(context, checkedList.size.toString(), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_search -> {
                circleRevealAnimation(isShow = true)
                itemSearch.expandActionView()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(context, "Home Settings Click", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //Pasamos los valores del searchView
    private fun setSearchToolbar() {
        binding.searchToolbar.inflateMenu(R.menu.menu_search)
        searchMenu = binding.searchToolbar.menu
        binding.searchToolbar.setNavigationOnClickListener {
            circleRevealAnimation(isShow = false)
        }
        itemSearch = searchMenu.findItem(R.id.action_filter_search)
        MenuItemCompat.setOnActionExpandListener(itemSearch, object : MenuItemCompat.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                //Hacer algo cuando expande
                //Se esconde el titulo
                binding.toolbarLayout.isTitleEnabled = false
                return true
            }
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                circleRevealAnimation(isShow = false)
                //Se muestra el titulo
                binding.toolbarLayout.isTitleEnabled = true
                return true
            }
        })
        initSearchView()
    }
    //Inicializamos el searchView
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

        //Metodos de busqueda - SearchView
        /**Este metodo se puede implementar en la clase MainActivity, pero lo vamos
         * a hacer desde el iniciador del searchView*/
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchFood(query)
                searchView.clearFocus()
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                searchFood(newText)
                return false
            }
        })
    }
    //Animacion del searchView
    private fun circleRevealAnimation(isShow: Boolean) {
        val view = binding.searchToolbar
        var width = view.width
        //Las medidas 48 son en dp (density-independent pixels)
        width -= 1 * 48 - 48 / 2
        //Las medidas 36 son en dp (density-independent pixels)
        width -= 36
        val cx = width
        val cy = view.height / 2
        val anim: Animator = if (isShow) ViewAnimationUtils.createCircularReveal(
            view, cx, cy, 0f, width.toFloat())
            else ViewAnimationUtils.createCircularReveal(view, cx, cy, width.toFloat(), 0f)
        anim.duration = 220.toLong()
        // make the view invisible when the animation is done
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isShow) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.INVISIBLE
                }
            }
        })
        // make the view visible and start the animation
        if (isShow) {
            view.visibility = View.VISIBLE
            visible = isShow
        }
        // start the animation
        anim.start()
    }
}