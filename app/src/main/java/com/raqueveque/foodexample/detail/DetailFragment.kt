package com.raqueveque.foodexample.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import com.raqueveque.foodexample.databinding.FragmentDetailBinding
import com.raqueveque.foodexample.detail.adapter.VariationsAdapter
import com.raqueveque.foodexample.detail.constructor.VariationsExtras
import com.raqueveque.foodexample.main.constructor.Food

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var variationsList: ArrayList<VariationsExtras>
    private lateinit var db: FirebaseFirestore
    private lateinit var vAdapter: VariationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(layoutInflater, container, false)

        getData()


        return binding.root
    }

    private fun getData() {
        variationsList = arrayListOf()
        db = FirebaseFirestore.getInstance()
        db.collection("food/food_id_00/variations/").addSnapshotListener(object : EventListener<QuerySnapshot> {
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
        binding.variationRecycler.layoutManager = LinearLayoutManager(context)
        vAdapter.notifyDataSetChanged()

    }

}