package com.example.booklight

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.booklight.databinding.ActivityPdfDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding

    private var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId);
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"

                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"
                    val date = MyApplication.formatTimestamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title",
                        binding.pdfView, binding.progressBar, binding.pagesTv)

                    MyApplication.loadPdfSize("$url", "$title", binding.sizeTv)

                    binding.titleTv.text = title
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}