package com.shamimhossen.abdullah_jahangir_books.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shamimhossen.abdullah_jahangir_books.R
import com.shamimhossen.abdullah_jahangir_books.activities.PdfViewActivity
import com.shamimhossen.abdullah_jahangir_books.databinding.BooksBinding
import com.shamimhossen.abdullah_jahangir_books.models.BooksModel
import com.shamimhossen.abdullah_jahangir_books.services.ForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class BooksAdapter(
    private val context: Context, private val booksList: MutableList<BooksModel>
) : RecyclerView.Adapter<BooksAdapter.ViewHolder>(), Filterable {
    private val developer = "https://shamimhossenbadol.com"
    private val appName = "khandaker_abdullah_jahangir"
    private var downloading = ""
    private var bookFiltered = booksList

    class ViewHolder(val binding: BooksBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BooksBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return bookFiltered.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booksModel = bookFiltered[position]
        holder.binding.image.load("file:///android_asset/books/${booksModel.fileName}.jpg")
        if (booksModel.fileName == downloading && ForegroundService.isDownloading) {
            holder.binding.animation.visibility = View.VISIBLE
        } else holder.binding.animation.visibility = View.GONE
        holder.binding.root.setOnClickListener {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/" + booksModel.fileName + ".pdf"
            )
            if (file.exists()) {
                val intent = Intent(context, PdfViewActivity::class.java)
                intent.putExtra("path", file.absolutePath)
                intent.putExtra("title", booksModel.title)
                intent.putExtra("id", booksModel.id)
                context.startActivity(intent)
            } else {
                startDownLoading(booksModel, holder, booksModel.fileName)
            }
        }
        val file = File(
            context.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/" + booksModel.fileName + ".pdf"
        )
        if (file.exists()) {
            holder.binding.status.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
        } else {
            holder.binding.status.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
        }
    }

    private fun startDownLoading(item: BooksModel, holder: ViewHolder, fileName: String) {
        val pdfLinks = "$developer/apps_files/$appName/${item.fileName}.pdf"
        if (!ForegroundService.isDownloading) {
            object : Thread(Runnable {
                try {
                    val startIntent = Intent(context, ForegroundService::class.java)
                    startIntent.putExtra("name", item.title)
                    startIntent.putExtra("link", pdfLinks)
                    startIntent.putExtra("fileName", item.fileName)
                    startIntent.putExtra("id", item.id)
                    ContextCompat.startForegroundService(context, startIntent)
                    downloading = fileName
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.binding.animation.visibility = View.VISIBLE
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }) {}.start()
        } else {
            Toast.makeText(
                context,
                "Click a little bit later.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                bookFiltered = if (constraint.toString().isEmpty()) booksList
                else {
                    val filtered: MutableList<BooksModel> = ArrayList()
                    for (row in booksList) {
                        if (row.title.contains(constraint.toString())) {
                            filtered.add(row)
                        } else if (row.fileName.contains(constraint.toString())) {
                            filtered.add(row)
                        }
                    }
                    filtered
                }
                val filterResults = FilterResults()
                filterResults.values = bookFiltered
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                val filteredResults = results.values
                if (filteredResults is List<*>) {
                    bookFiltered = filteredResults.filterIsInstance<BooksModel>().toMutableList()
                    notifyDataSetChanged()
                }
            }
        }
    }
}