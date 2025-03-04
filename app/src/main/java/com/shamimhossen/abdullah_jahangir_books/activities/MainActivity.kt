package com.shamimhossen.abdullah_jahangir_books.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.shamimhossen.abdullah_jahangir_books.R
import com.shamimhossen.abdullah_jahangir_books.databinding.ActivityMainBinding
import com.shamimhossen.abdullah_jahangir_books.databinding.DonationPageBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.books.setOnClickListener {
            startActivity(Intent(this@MainActivity, BooksActivity::class.java))
        }
        binding.lectures.setOnClickListener {
            startActivity(Intent(this@MainActivity, LecturesActivity::class.java))
        }
        val preferences = getSharedPreferences("settings", MODE_PRIVATE)
        var counter = preferences.getInt("counter", 0)
        if (counter == 4) {
            Handler(Looper.getMainLooper()).postDelayed({ showDonationDialog() }, 2000)
            preferences.edit().putInt("counter", 0).apply()
        } else preferences.edit().putInt("counter", ++counter).apply()

    }

    private fun showDonationDialog() {
        val builder = AlertDialog.Builder(this@MainActivity, R.style.FullscreenAlertDialog)
        val donationBinding = DonationPageBinding.inflate(layoutInflater)
        donationBinding.payment.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://shop.bkash.com/learn-to-know01630133873/paymentlink/default-payment")
                )
            )
        }
        builder.setView(donationBinding.root)
        builder.setCancelable(false)
        val dialog: Dialog = builder.create()
        donationBinding.close.setOnClickListener { dialog.dismiss() }
        if (!isFinishing) dialog.show()
    }
}