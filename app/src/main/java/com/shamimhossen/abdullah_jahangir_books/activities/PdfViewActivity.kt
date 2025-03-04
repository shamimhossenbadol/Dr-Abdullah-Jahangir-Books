package com.shamimhossen.abdullah_jahangir_books.activities

import android.app.Dialog
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shamimhossen.abdullah_jahangir_books.R
import com.shamimhossen.abdullah_jahangir_books.databinding.ActivityPdfViewBinding
import com.shamimhossen.abdullah_jahangir_books.databinding.PageSearchLayoutBinding
import java.io.File

class PdfViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfViewBinding
    private var isLandscapeMode = false
    private var isNightMode = false
    private var isHorizontal = false
    private var isHide = false
    private var pdfId: String? = null
    private var handler: Handler? = null
    private lateinit var lastReadPdfPageEditor: SharedPreferences.Editor
    private lateinit var settingsEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            @Suppress("DEPRECATION")
            override fun handleOnBackPressed() {
                if (isHide) hideToolbar()
                else if (isLandscapeMode) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    binding.orientation.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@PdfViewActivity, R.drawable.ic_baseline_screen_lock_landscape_24
                        )
                    )
                    handler?.removeCallbacksAndMessages(null)
                    isLandscapeMode = false
                    isHide = false
                } else this@PdfViewActivity.finish()
            }
        })
        val lastReadPdfPage = getSharedPreferences("lastRead", MODE_PRIVATE)
        lastReadPdfPageEditor = lastReadPdfPage.edit()
        pdfId = intent.getStringExtra("id")
        val settings = getSharedPreferences("settings", MODE_PRIVATE)
        settingsEditor = settings.edit()
        isLandscapeMode = settings.getBoolean("landscape", false)
        isNightMode = settings.getBoolean("night", false)
        isHorizontal = settings.getBoolean("mode", false)
        if (isHorizontal) binding.mode.rotation = 90f
        clickEvents()
        checkNightMode()
        loadPdf(lastReadPdfPage.getInt(pdfId, 0))
        binding.name.text = intent.getStringExtra("title")
    }

    @Suppress("DEPRECATION")
    private fun clickEvents() {
        binding.orientation.setOnClickListener {
            isLandscapeMode = !isLandscapeMode
            binding.orientation.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (isLandscapeMode) R.drawable.ic_baseline_screen_lock_portrait_24 else R.drawable.ic_baseline_screen_lock_landscape_24
                )
            )
            if (isLandscapeMode) {
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                hideToolbar()
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                if (handler != null) handler!!.removeCallbacksAndMessages(null)
                isHide = false
            }
        }
        binding.nightMode.setOnClickListener {
            isNightMode = !isNightMode
            settingsEditor.putBoolean("night", isNightMode).apply()
            checkNightMode()
            loadPdf(binding.pdfView.currentPage)
        }
        binding.mode.setOnClickListener {
            isHorizontal = !isHorizontal
            settingsEditor.putBoolean("mode", isHorizontal).apply()
            binding.mode.rotation = binding.mode.rotation + 90
            loadPdf(binding.pdfView.currentPage)
        }
        binding.first.setOnClickListener { loadPdf(0) }
        binding.previous.setOnClickListener {
            loadPdf(
                binding.pdfView.currentPage - 1
            )
        }
        binding.forward.setOnClickListener {
            loadPdf(
                binding.pdfView.currentPage + 1
            )
        }
        binding.last.setOnClickListener {
            loadPdf(binding.pdfView.pageCount)
        }
        binding.pageSelection.setOnClickListener {
            val searchBinding = PageSearchLayoutBinding.inflate(layoutInflater)
            searchBinding.hint.hint = "Enter value between 0 to " + binding.pdfView.pageCount
            val dialog = Dialog(this@PdfViewActivity, R.style.Theme_App_AlertDialog)
            dialog.setContentView(searchBinding.root)
            dialog.setCancelable(false)
            searchBinding.button.setOnClickListener {
                searchBinding.page.let {
                    if (it.text.toString() != "") {
                        val number = it.text.toString().toInt()
                        if (number > binding.pdfView.pageCount) {
                            it.error = "Enter a valid page number"
                        } else {
                            dialog.dismiss()
                            loadPdf(number)
                        }
                    } else {
                        dialog.dismiss()
                    }
                }
            }
            dialog.create()
            dialog.show()
        }
    }

    private fun checkNightMode() {
        val nightImage = ContextCompat.getDrawable(this, R.drawable.ic_baseline_dark_mode_24)
        val dayImage = ContextCompat.getDrawable(this, R.drawable.ic_baseline_wb_sunny_24)
        binding.nightMode.setImageDrawable(if (isNightMode) dayImage else nightImage)
        binding.pdfView.setBackgroundColor(if (isNightMode) Color.BLACK else Color.WHITE)
    }

    private fun hideToolbar() {
        handler?.removeCallbacksAndMessages(null)
        isHide = false
        binding.row1.visibility = View.VISIBLE
        binding.row2.visibility = View.VISIBLE
        binding.pageSelection.visibility = View.VISIBLE
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
            isHide = true
            binding.row1.visibility = View.GONE
            binding.row2.visibility = View.GONE
            binding.pageSelection.visibility = View.GONE
        }, 3000)
    }

    private fun loadPdf(i: Int) {
        binding.progress.visibility = View.VISIBLE
        val path = intent.getStringExtra("path")
        val file = path?.let { File(it) }
        binding.pdfView.fromFile(file).enableSwipe(true).defaultPage(i)
            .onLoad { binding.progress.visibility = View.GONE }.swipeHorizontal(isHorizontal)
            .nightMode(isNightMode).enableDoubletap(false).fitEachPage(true).load()
    }

    override fun onDestroy() {
        lastReadPdfPageEditor.putInt(pdfId, binding.pdfView.currentPage)?.apply()
        super.onDestroy()
    }
}