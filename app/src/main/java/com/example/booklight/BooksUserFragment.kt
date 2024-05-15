package com.example.booklight

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.booklight.databinding.FragmentBooksUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    public companion object{
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment{
            val fragment = BooksUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
        adapterPdfUser = AdapterPdfUser(requireContext(), ArrayList())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        Log.d(TAG, "onCreateView: Category: $category")
        if (category == "All"){
            loadAllBooks()
        }
        else{
            loadCategorizedBooks()
        }

        adapterPdfUser = AdapterPdfUser(requireContext(), ArrayList())
        binding.booksRv.adapter = adapterPdfUser

        binding.searchEt.addTextChangedListener { object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG, "onTextChanged: SEARCH EXCEPTION: ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        } }

        return binding.root
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelPdf::class.java)
                        pdfArrayList.add(model!!)
                    }
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

}