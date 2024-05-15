package com.example.booklight

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.booklight.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfEditBinding

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }

    private var bookId = ""

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryTitleArrayList: ArrayList<String>

    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    Log.d(TAG, "onDataChange: Loading book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var title = ""
    private var description = ""
    private fun validateData() {
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        if (title.isEmpty()){
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show()
        }
        else if (selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Pick category", Toast.LENGTH_SHORT).show()
        }
        else{
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info")

        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated")
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "updatePdf: Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray){dialog, position->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children){

                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}