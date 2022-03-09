package com.raqueveque.foodexample.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.*
import com.raqueveque.foodexample.databinding.FragmentDetailBinding
import com.raqueveque.foodexample.detail.adapter.VariationsAdapter
import com.raqueveque.foodexample.detail.constructor.VariationsExtras

class DetailFragment : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private val sliderHandler = Handler()

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var variationsList: ArrayList<VariationsExtras>
    private lateinit var imagesList: ArrayList<ImageSlider>
    private lateinit var db: FirebaseFirestore
    private lateinit var vAdapter: VariationsAdapter

    private val args: DetailFragmentArgs by navArgs()

    private var quantity = 1
    private var price: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(layoutInflater, container, false)

        getData()

        initFragment()

        quantityPicker()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Se tiene que colocar una vez que se crea la vista
        viewPager2 = binding.viewPagerImage

        viewPager2.adapter = SliderAdapter(imagesList, viewPager2)

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

    @SuppressLint("SetTextI18n")
    private fun initFragment() {
        binding.name.text = args.name
        binding.price.text = "$${args.price}"
    }

    private fun getData() {
        variationsList = arrayListOf()
        imagesList = arrayListOf()
        db = FirebaseFirestore.getInstance()
        db.collection("food/${args.id}/variations/").get().addOnSuccessListener {
            for (dc in it){
                variationsList.add(dc.toObject(VariationsExtras::class.java))
            }
            updateList(variationsList)
        }
        db.collection("food/${args.id}/images").get().addOnSuccessListener {
            if (it.isEmpty){
                Toast.makeText(context, "Vacio", Toast.LENGTH_SHORT).show()
            }
            for (dc in it){
                val image = dc.toObject(ImageSlider::class.java)
                imagesList.add(image)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(variationsList: ArrayList<VariationsExtras>) {
        vAdapter = VariationsAdapter(variationsList)
        binding.variationRecycler.isNestedScrollingEnabled = false
        binding.variationRecycler.adapter = vAdapter
        /**Usamos el gridlayoutmanager para agrandar el layout y que se ajuste a la pantalla
         * La cantidad de filas la define el tamaño de la lista, en modo horizontal*/
        GridLayoutManager(context, variationsList.size, RecyclerView.HORIZONTAL, false).apply {
            binding.variationRecycler.layoutManager = this
        }
        vAdapter.notifyDataSetChanged()

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
}