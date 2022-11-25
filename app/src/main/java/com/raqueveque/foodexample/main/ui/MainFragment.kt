package com.raqueveque.foodexample.main.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.ahmadhamwi.tabsync.TabbedListMediator
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.raqueveque.foodexample.IOnBackPressed
import com.raqueveque.foodexample.OrderFood
import com.raqueveque.foodexample.R
import com.raqueveque.foodexample.Utilities
import com.raqueveque.foodexample.databinding.FragmentMainBinding
import com.raqueveque.foodexample.detail.ImageSlider
import com.raqueveque.foodexample.detail.SliderAdapter
import com.raqueveque.foodexample.detail.adapter.ExtrasAdapter
import com.raqueveque.foodexample.detail.adapter.VariationsAdapter
import com.raqueveque.foodexample.detail.constructor.VariationsExtras
import com.raqueveque.foodexample.main.adapter.CategoriesAdapter
import com.raqueveque.foodexample.main.adapter.FoodAdapter
import com.raqueveque.foodexample.main.constructor.Category
import com.raqueveque.foodexample.main.constructor.Food
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class MainFragment : Fragment(), IOnBackPressed {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    //Instancia a la base de datos
    private lateinit var db: FirebaseFirestore

    //El menu del searchview
    private lateinit var searchMenu: Menu
    private lateinit var itemSearch: MenuItem

    //El viewPager2 (pasa las imagenes)
    private lateinit var viewPager2: ViewPager2
    private val sliderHandler = Handler()

    /**Inicializamos las listas y sus adaptadores*/
    //Lista de variedades y su adaptador
    private lateinit var variationsList: ArrayList<VariationsExtras>
    private lateinit var vAdapter: VariationsAdapter
    //Lista de imagenes y su adaptador
    private lateinit var imagesList: ArrayList<ImageSlider>
    private lateinit var iAdapter: SliderAdapter
    //Lista de extras y su adaptador
    private lateinit var extrasList: ArrayList<VariationsExtras>
    private lateinit var eAdapter: ExtrasAdapter
    //Lista de comida principal y su adaptador
    private var foodArrayList: ArrayList<Food> = arrayListOf()
    private var foodCategoryList: ArrayList<Food> = arrayListOf()
    private var categories: ArrayList<Category> = arrayListOf()
    private lateinit var fAdapter: CategoriesAdapter
    //Lista de pedidos
    private var orderList: ArrayList<OrderFood> = arrayListOf()

    //Lista de los chequeados
    private var checkedList: ArrayList<Food> = arrayListOf()

    //Manejo de las cantidades
    private var quantity = 1
    private var price: Long = 0

    //Obtenemos el ID de la coleccion a la que accedemos
    private var id: String? = null

    //Guardamos la posicion actual del item en la lista principal
    private var actualPos: Int = 0

    /**Variables de tipo BOOLEAN**/
    //Controla la rotacion del fabbutton
    var rotate = false
    //Controla la expancion del appbar de la pantalla de inicio
    var misAppbarExpand = true
    //Controla la expancion del appbar de la pantalla de detalles
    var misAppbar2Expand = true
    //Controla la visibilidad del searchToolbar
    private var visible: Boolean = false
    //Controla la visibilidad de los botones en el panel de inicio
    private var isShown = false
    //Controla la revelacion del layout de detalles
    private var isRevealed = false

    private var collapsedScale: Float = 0f
    private var expandedScale: Float = 0f

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        //Inicializamos las decoraciones del recycler aca, sino se ejecutan una y otra vez
        val dividerItemDecoration = DividerItemDecoration(binding.recyclerMain.context, LinearLayoutManager.VERTICAL)
        binding.recyclerMain.layoutManager = LinearLayoutManager(context)
        binding.recyclerMain.addItemDecoration(dividerItemDecoration)

        //Si no le damos el valor true, el menu no se muestra
        setHasOptionsMenu(true)

        binding.toolbarDetail.setNavigationOnClickListener {
            revealLayoutFun()
        }

        checkedList = arrayListOf()

        //Obtenemos los datos
        getData()

        //Colocamos el actionbar principal
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbarMain)

        setSearchToolbar()

        //Esconder el teclado al tocar otras vistas
        Utilities.setupUI(binding.rootActivity, requireContext())

        quantityPicker()

        val fabRotate = AnimationUtils.loadAnimation(context, R.animator.fab_rotate)
        val fabRotateOriginPosition = AnimationUtils.loadAnimation(context, R.animator.fab_rotate_origin_position)

        binding.add.setOnClickListener {
            if (!rotate) {
                rotate = true
                it.startAnimation(fabRotate)
                it.background.setTint(Color.parseColor("#FF0000"))
                it.animate().scaleX(0.8f).scaleY(0.8f)
            }
            else {
                rotate = false
                it.startAnimation(fabRotateOriginPosition)
                it.background.setTint(Color.parseColor("#FF03DAC5"))
                it.animate().scaleX(1f).scaleY(1f)
            }
        }

        //Listener del cambio en el scroll del appbar
        //Lo que se hace aca es tomar el valor del rango de scroll del appbar y el desplazamiento vertical
        //del NestedScrollView, para esconder o mostrar las vistas
        binding.appbarDetail.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            val scrollRange = appBarLayout.totalScrollRange
            val fraction = 1f * (scrollRange + verticalOffset) / scrollRange
            if (fraction < 0.35 && misAppbar2Expand) {
                //Hide here
                misAppbar2Expand = false
                //Se cambia la escala de la vista
                binding.buttonsPanel.animate().scaleX(0f).scaleY(0f)
            }
            if (fraction > 0.27 && !misAppbar2Expand) {
                //Show here
                misAppbar2Expand = true
                binding.buttonsPanel.animate().scaleX(1f).scaleY(1f)
            }
        })
        //La misma funcionalidad, pero con el appbarmain
        binding.appbarMain.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            //Zoom Image
            val maxScroll = appBarLayout.totalScrollRange

            val scrollPercent = abs(verticalOffset).toFloat() / maxScroll.toFloat()

            if (collapsedScale == 0f) {

                val photo: Drawable = binding.detailImage.drawable

                val bitmapWidth = photo.intrinsicWidth
                val bitmapHeight = photo.intrinsicHeight

                collapsedScale = binding.detailImage.width.toFloat() / bitmapWidth.toFloat()
                expandedScale =  binding.detailImage.height.toFloat() / bitmapHeight.toFloat()

                scalePhotoImage(binding.detailImage, expandedScale)
            }else {
                scalePhotoImage(binding.detailImage, collapsedScale + (expandedScale - collapsedScale) * (1f - scrollPercent))
            }

            val scrollRange = appBarLayout.totalScrollRange
            val fraction = 1f * (scrollRange + verticalOffset) / scrollRange
            if (fraction < 0.5 && misAppbarExpand) {
                //Hide here
                misAppbarExpand = false
                //Se cambia la escala de la vista
                binding.fb.animate().scaleX(0f).scaleY(0f)
                binding.ins.animate().scaleX(0f).scaleY(0f)
                binding.tw.animate().scaleX(0f).scaleY(0f)
            }
            if (fraction > 0.8 && !misAppbarExpand) {
                //Show here
                misAppbarExpand = true
                binding.fb.animate().scaleX(1f).scaleY(1f)
                binding.ins.animate().scaleX(1f).scaleY(1f)
                binding.tw.animate().scaleX(1f).scaleY(1f)
            }
        })

        binding.logoFront.setOnClickListener {
            if (!isShown) {
                isShown = true
                binding.fb.animate().translationX(resources.getInteger(R.integer.value_190__).toFloat()).translationY(resources.getInteger(R.integer.value_190__).toFloat())
                binding.ins.animate().translationX(0f).translationY(resources.getInteger(R.integer.value_252__).toFloat())
                binding.tw.animate().translationX(resources.getInteger(R.integer.value_190).toFloat()).translationY(resources.getInteger(R.integer.value_190__).toFloat())
            }
            else {
                isShown = false
                binding.fb.animate().translationX(0f).translationY(0f)
                binding.ins.animate().translationX(0f).translationY(0f)
                binding.tw.animate().translationX(0f).translationY(0f)
            }
        }

        binding.appbarMain.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener{ appBarLayout, verticalOffset ->

        })

        return binding.root
    }

    private fun scalePhotoImage(photoView: ImageView, scale: Float) {
        val photo = photoView.drawable
        val bitmapWidth = photo.intrinsicWidth
        val bitmapHeight = photo.intrinsicHeight
        val offsetX = (photoView.width - bitmapWidth) / 2f
        val offsetY = (photoView.height - bitmapHeight) / 2f
        val centerX = photoView.width / 2f
        val centerY = photoView.height / 2f
        val imageMatrix = Matrix()
        imageMatrix.setScale(scale, scale, centerX, centerY)
        imageMatrix.preTranslate(offsetX, offsetY)
        photoView.imageMatrix = imageMatrix
    }

    //--------------------------------------LAYOUT PRINCIPAL-------------------------------------//
        //Obtenemos los datos principales
        private fun getData() {
            /**Comprobamos que la lista este vacia para
             * realizar una sola vez la consulta a la base
             * de datos, evitamos la sobrecarga de carga/descarga*/
            if (foodCategoryList.size == 0) {
                db = FirebaseFirestore.getInstance()
                db.collection("food").addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                        if (error != null) {
                            Log.e("FIREBASE ERROR", error.message.toString())
                            return
                        }
                        for (dc: DocumentChange in value?.documentChanges!!) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                val item = Food(
                                    dc.document.get("category").toString(), //Obtenemos la categoria
                                    dc.document.get("image").toString(),
                                    dc.document.get("name").toString(),
                                    dc.document.get("price") as Long?,
                                    //Pasamos el Id del documento
                                    dc.document.id
                                )
                                foodArrayList.add(item)
                            }
                        }
                        ordenarLista(foodArrayList) //Pasamos la lista para ordenarla
                        updateListFood(categories)
                    }
                })
            } else {
                updateListFood(categories)
            }
        }

    //Ver tab layout
    private fun ordenarLista(foodArrayList: ArrayList<Food>) {
        var category = ""
        var index = 0
        for (n in foodArrayList){
            if (category == n.category) break //Retorna si la categoria ya se agrego
            else { //Sino, continuamos con la siguiente
                category = n.category.toString()
                val list: ArrayList<Food> = arrayListOf()
                for (m in foodArrayList){
                    if (m.category == category && !foodCategoryList.contains(m)) list.add(m)
                }
                categories.add(Category(category, list))
            }
        }

    }

    //El Buscardor
        private fun searchFood(query: String) {
            var foodName = query
            if (foodName.isNotEmpty()) foodName =
                foodName.substring(0, 1).uppercase(Locale.getDefault()) + foodName.substring(1)
                    .lowercase(Locale.getDefault())
            val results: ArrayList<Food> = ArrayList()
            for (food in foodCategoryList) {
                if (food.name != null && food.name!!.contains(foodName)) {
                    results.add(food)
                }
            }
            //updateListFood(results)
        }
        //Actualiza la lista del recycler
        @SuppressLint("NotifyDataSetChanged")
        private fun updateListFood(listFood: ArrayList<Category>) {
            fAdapter = CategoriesAdapter(requireContext(), listFood)
    //        binding.recycler.isNestedScrollingEnabled = false
            binding.recyclerMain.adapter = fAdapter
            fAdapter.notifyDataSetChanged()

//            /**Aqui sobreescribimos las funciones:*/
//            fAdapter.setOnItemClickListener(object : FoodAdapter.OnItemClickListener{
//                override fun onItemClick(position: Int) {
//                    id = foodCategoryList[position].id
//                    actualPos = position
//                    binding.variationShimmer.visibility = View.VISIBLE
//                    binding.variationShimmer.startShimmer()
//                    binding.imageShimmer.visibility = View.VISIBLE
//                    binding.imageShimmer.startShimmer()
//                    binding.extrasShimmer.visibility = View.VISIBLE
//                    binding.extrasShimmer.startShimmer()
//                    revealLayoutFun()
//                }
//            })
//            fAdapter.setOnItemCheckListener(object : FoodAdapter.OnItemCheckListener{
//                override fun onItemCheck(position: Int, checkBox: View) {
//                    val check = checkBox as CheckBox
//                    if (check.isChecked) {
//                        checkedList.add(foodCategoryList[position])
//                        //Esta variable maneja el estado del checkbox. Ver adaptador (OnBindViewHolder)
//                        listFood[position].check = true
//                    }
//                    else {
//                        checkedList.remove(foodCategoryList[position])
//                        listFood[position].check = false
//                    }
//
//                }
//            })

            //Ver tab layout
            // Ver tab layout
            for (n in categories){
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(n.category))
            }
            Toast.makeText(context, categories.indices.toList().toString(), Toast.LENGTH_SHORT).show()
            TabbedListMediator(
                binding.recyclerMain, binding.tabLayout, categories.indices.toList()
            ).attach()
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
                    binding.toolbarLayoutMain.isTitleEnabled = false
                    return true
                }
                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    circleRevealAnimation(isShow = false)
                    //Se muestra el titulo
                    binding.toolbarLayoutMain.isTitleEnabled = true
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
                    //if (query.isNotEmpty())//
                    searchFood(query)
                    searchView.clearFocus()
                    return false
                }
                override fun onQueryTextChange(newText: String): Boolean {
                    //if (newText.isNotEmpty())
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
    //--------------------------------------LAYOUT PRINCIPAL-------------------------------------//




    //--------------------------------------LAYOUT DETALLES-------------------------------------//
        //Revela el segundo layout de detalles desde el centro
        private fun revealLayoutFun() {

            val mRevealLayout = binding.detailLayout

            // based on the boolean value the
            // reveal layout should be toggled
            if (!isRevealed) {

                // get the right and bottom side
                // lengths of the reveal layout
                val x: Int = mRevealLayout.right / 2
                val y: Int = mRevealLayout.bottom / 2

                // here the starting radius of the reveal
                // layout is 0 when it is not visible
                val startRadius = 0

                // make the end radius should
                // match the while parent view
                val endRadius = kotlin.math.hypot(
                    mRevealLayout.width.toDouble(),
                    mRevealLayout.height.toDouble()
                ).toInt()

                // create the instance of the ViewAnimationUtils to
                // initiate the circular reveal animation
                val anim = ViewAnimationUtils.createCircularReveal(
                    mRevealLayout, x, y,
                    startRadius.toFloat(), endRadius.toFloat()
                )

                // make the invisible reveal layout to visible
                // so that upon revealing it can be visible to user
                mRevealLayout.visibility = View.VISIBLE
                // now start the reveal animation
                anim.start()

                // set the boolean value to true as the reveal
                // layout is visible to the user
                isRevealed = true

                getImages()
                getVariations()
                getExtras()

                binding.collapsingToolbarDetail.title = foodCategoryList[actualPos].name.toString()

            } else {

                // get the right and bottom side lengths
                // of the reveal layout
                val x: Int = mRevealLayout.right / 2
                val y: Int = mRevealLayout.bottom / 2

                // here the starting radius of the reveal layout is its full width
                val startRadius: Int = kotlin.math.max(mRevealLayout.width, mRevealLayout.height)

                // and the end radius should be zero at this
                // point because the layout should be closed
                val endRadius = 0

                // create the instance of the ViewAnimationUtils
                // to initiate the circular reveal animation
                val anim = ViewAnimationUtils.createCircularReveal(
                    mRevealLayout, x, y,
                    startRadius.toFloat(), endRadius.toFloat()
                )

                // now as soon as the animation is ending, the reveal
                // layout should also be closed
                anim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        mRevealLayout.visibility = View.GONE

                        //resetea la posicion del appbar y el nestedscrollview
                        binding.appbarDetail.setExpanded(true)
                        binding.detailScroll.scrollTo(0, 0)

                        quantity = 1
                        binding.quantity.setText(quantity.toString())

                        binding.price.text = ""

                        //Limpiamos las listas
                        variationsList.clear()
                        extrasList.clear()
                        imagesList.clear()
                        updateVariations(variationsList)
                        updateExtras(extrasList)
                        updateImages(imagesList)
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })

                // start the closing animation
                anim.start()

                // set the boolean variable to false
                // as the reveal layout is invisible
                isRevealed = false
            }
        }

        //Se cancela la funcion de atras a menos se cumpla la condicion
        override fun onBackPressed(): Boolean {
            return if (!binding.detailLayout.isVisible){
                true
            } else {
                revealLayoutFun()
                false
            }
        }

        //Una vez creada la vista, implementamos el viewPager2 y su recycler
        @SuppressLint("NotifyDataSetChanged")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            //Se tiene que colocar una vez que se crea la vista
            viewPager2 = binding.imageSliderViewPager

            viewPager2.clipToPadding = false
            viewPager2.clipChildren = false
            viewPager2.offscreenPageLimit = 3
            viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(30))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - kotlin.math.abs(position)
                page.scaleY = 0.85f + r * 0.25f
            }

            viewPager2.setPageTransformer(compositePageTransformer)
            //Hasta aca, ver las animaciones

            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    sliderHandler.removeCallbacks(sliderRunnable)
                    sliderHandler.postDelayed(sliderRunnable, 3000)
                }
            })
        }

        //Variable tipo Runnable para ejecutar el codigo siempre y cuando este activo
        private val sliderRunnable = Runnable {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }

        override fun onPause() {
            super.onPause()
            sliderHandler.postDelayed(sliderRunnable, 3000)
        }

        override fun onResume() {
            super.onResume()
            sliderHandler.postDelayed(sliderRunnable, 3000)
        }

        //Funcion para manejar la cantidad de pedidos
        @SuppressLint("SetTextI18n")
        private fun quantityPicker() {
            //Colocamos por defecto 1 en el edittext
            binding.quantity.setText("1")
            //El contenido del edittext lo pasamos a INT
            quantity = Integer.parseInt(binding.quantity.text.toString())
            //Sumamos o restamos pedidos
            binding.less.setOnClickListener {
                if (quantity != 1)
                    quantity -= 1
                binding.quantity.setText(quantity.toString())
                val total = quantity * price
                binding.price.text = "$$total"
            }
            binding.more.setOnClickListener {
                quantity += 1
                binding.quantity.setText(quantity.toString())
                val total = quantity * price
                binding.price.text = "$$total"
            }
        }

        //Obtenemos las imagenes
        private fun getImages(){
            imagesList = arrayListOf()
            db.collection("food/${id}/images/").get().addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty){
                    Log.e("FIREBASE", "Lista vacía")
                } else {
                    for (dc in snapshot){
                        val image = dc.toObject(ImageSlider::class.java)
                        imagesList.add(image)
                    }
                    updateImages(imagesList)
                }
            }
        }
        //Actualizamos las listas de imagen
        @SuppressLint("NotifyDataSetChanged")
        private fun updateImages(imagesList: ArrayList<ImageSlider>) {
            //El adaptador
            iAdapter = SliderAdapter(imagesList, viewPager2)
            viewPager2.adapter = iAdapter
            iAdapter.notifyDataSetChanged()

            //Paramos el shimmer
            binding.imageShimmer.stopShimmer()
            binding.imageShimmer.visibility = View.GONE
        }

        //Obtenemos los extras
        private fun getExtras(){
            extrasList = arrayListOf()
            db.collection("food/${id}/extra/").get().addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty){
                    Log.e("FIREBASE", "Lista vacía")
                } else {
                    for (dc in snapshot) {
                        extrasList.add(dc.toObject(VariationsExtras::class.java))
                    }
                    updateExtras(extrasList)
                }
            }
        }
        //Actualizamos la lista de extras
        @SuppressLint("NotifyDataSetChanged")
        private fun updateExtras(extrasList: ArrayList<VariationsExtras>) {
            //Adaptador
            eAdapter = ExtrasAdapter(extrasList)
            binding.others.adapter = eAdapter
            //Para evitar el error del gridlayout (lista en 0 valores), colocamos la condicion:
            if (extrasList.size == 0) {
                binding.others.layoutManager = LinearLayoutManager(context)
            } else {
                /**Usamos el gridlayoutmanager para agrandar el layout y que se ajuste a la pantalla
                 * La cantidad de filas la define el tamaño de la lista, en modo horizontal*/
                GridLayoutManager(context, extrasList.size / 2, RecyclerView.VERTICAL, false).apply {
                    binding.others.layoutManager = this
                }
            }
            eAdapter.notifyDataSetChanged()
            //Paramos el shimmer
            binding.extrasShimmer.stopShimmer()
            binding.extrasShimmer.visibility = View.GONE
        }

        //Obtenemos las variedades
        private fun getVariations() {
            variationsList = arrayListOf()
            db.collection("food/${id}/variations/").get().addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty){
                    Log.e("FIREBASE", "Lista vacía")
                } else {
                    for (dc in snapshot) {
                        variationsList.add(dc.toObject(VariationsExtras::class.java))
                    }
                    updateVariations(variationsList)
                }
            }
        }
        //Actualizamos la lista de variedades:
        @SuppressLint("NotifyDataSetChanged")
        private fun updateVariations(variationsList: ArrayList<VariationsExtras>) {
            //El adaptador
            vAdapter = VariationsAdapter(variationsList)
            binding.variationRecycler.adapter = vAdapter

            //Desactivamos el scroll
            binding.variationRecycler.isNestedScrollingEnabled = false

            if (variationsList.isEmpty()){
                binding.variationRecycler.layoutManager = LinearLayoutManager(context)
            } else {
                /**Usamos el gridlayoutmanager para agrandar el layout y que se ajuste a la pantalla
                 * La cantidad de filas la define el tamaño de la lista, en modo horizontal*/
                GridLayoutManager(context, variationsList.size, RecyclerView.HORIZONTAL, false).apply {
                    binding.variationRecycler.layoutManager = this
                }
            }
            vAdapter.notifyDataSetChanged()

            //Paramos el shimmer
            binding.variationShimmer.stopShimmer()
            binding.variationShimmer.visibility = View.GONE

            /**Aqui sobreescribimos la funcion creada en el adapter
             * usando interface:*/
            vAdapter.setOnSingleItemCheckListener(object : VariationsAdapter.OnItemSingleCheckListener{
                var lastChecked: RadioButton? = null
                var lastCheckedPos = 0
                @SuppressLint("SetTextI18n")
                override fun onItemSingleCheck(position: Int, radioButton: View) {
                    //Obtenemos el radioButton actual
                    val rb = radioButton as RadioButton
                    //Pasamos el "tag" de ese rB, y dejamos constancia de la ultima
                    //posicion de donde se hizo "click"
                    val clickedPos = (rb.tag as Int).toInt()
                    if (rb.isChecked) {
                        if (lastChecked != null) {
                            //Aqui, si se cumplen las condiciones, el ultimo tocado
                            //deja de esta checkeado
                            lastChecked!!.isChecked = false
                        }
                        //Actualizamos los datos
                        lastChecked = rb
                        lastCheckedPos = clickedPos
                    } else lastChecked = null
                    price = variationsList[position].price!!.toLong()
                    binding.price.text = "$${variationsList[position].price!! * quantity}"
                }
            })
        }
    //--------------------------------------LAYOUT DETALLES-------------------------------------//


    //---------------------------------------LAYOUT CARRITO--------------------------------------//
    //Revela carrito desde esquina inferior derecha
    private fun revealLayout2Fun() {
        val mRevealLayout = binding.detailLayout

        // based on the boolean value the
        // reveal layout should be toggled
        if (!isRevealed) {

            // get the right and bottom side
            // lengths of the reveal layout
            val x: Int = mRevealLayout.right
            val y: Int = mRevealLayout.bottom

            // here the starting radius of the reveal
            // layout is 0 when it is not visible
            val startRadius = 0

            // make the end radius should match
            // the while parent view
            val endRadius = kotlin.math.hypot(
                mRevealLayout.width.toDouble(),
                mRevealLayout.height.toDouble()
            ).toInt()


            // create the instance of the ViewAnimationUtils to
            // initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                mRevealLayout,
                x,
                y,
                startRadius.toFloat(),
                endRadius.toFloat()
            )

            // make the invisible reveal layout to visible
            // so that upon revealing it can be visible to user
            mRevealLayout.visibility = View.VISIBLE
            // now start the reveal animation
            anim.start()

            // set the boolean value to true as the reveal
            // layout is visible to the user
            isRevealed = true

        } else {

            // get the right and bottom side lengths
            // of the reveal layout
            val x: Int = mRevealLayout.right
            val y: Int = mRevealLayout.bottom

            // here the starting radius of the reveal layout is its full width
            val startRadius: Int = kotlin.math.max(mRevealLayout.width, mRevealLayout.height)

            // and the end radius should be zero
            // at this point because the layout should be closed
            val endRadius = 0

            // create the instance of the ViewAnimationUtils to
            // initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                mRevealLayout,
                x,
                y,
                startRadius.toFloat(),
                endRadius.toFloat()
            )

            // now as soon as the animation is ending, the reveal
            // layout should also be closed
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    mRevealLayout.visibility = View.GONE
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })

            // start the closing animation
            anim.start()

            // set the boolean variable to false
            // as the reveal layout is invisible
            isRevealed = false
        }
        db.collection("food/food_00_id/extra/").get().addOnSuccessListener {
            if (it.isEmpty){
                Toast.makeText(context, "vacio", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //---------------------------------------LAYOUT CARRITO--------------------------------------//

}