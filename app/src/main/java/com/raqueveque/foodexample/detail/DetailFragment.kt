package com.raqueveque.foodexample.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.contentValuesOf
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.raqueveque.foodexample.databinding.FragmentDetailBinding
import com.raqueveque.foodexample.detail.adapter.VariationsAdapter
import com.raqueveque.foodexample.detail.constructor.VariationsExtras
import com.squareup.picasso.Picasso

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var variationsList: ArrayList<VariationsExtras>
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
            binding.price.text = total.toString()
        }
        binding.more.setOnClickListener {
            quantity += 1
            binding.quantity.setText(quantity.toString())
            val total = quantity * price
            binding.price.text = total.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initFragment() {
        Picasso.get().load(args.image).into(binding.image)
        binding.name.text = args.name
        binding.price.text = "$${args.price}"
    }

    private fun getData() {
        variationsList = arrayListOf()
        db = FirebaseFirestore.getInstance()
        db.collection("food/${args.id}/variations/").addSnapshotListener(object : EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null){
                    Log.e("FIREBASE ERROR", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!){
                    if (dc.type == DocumentChange.Type.ADDED){
                        variationsList.add(dc.document.toObject(VariationsExtras::class.java))
                    }
                }
                updateList(variationsList)
            }
        })
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