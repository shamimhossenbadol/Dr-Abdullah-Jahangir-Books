package com.shamimhossen.abdullah_jahangir_books.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.shamimhossen.abdullah_jahangir_books.adapters.BooksAdapter
import com.shamimhossen.abdullah_jahangir_books.databinding.ActivityBooksBinding
import com.shamimhossen.abdullah_jahangir_books.models.BooksModel
import com.shamimhossen.abdullah_jahangir_books.services.ForegroundService

class BooksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBooksBinding
    private var booksList = mutableListOf<BooksModel>()
    private lateinit var adapter: BooksAdapter
    private var mMessageReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.share.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "https://shamimhossenbadol.com"
            )
            startActivity(Intent.createChooser(intent, null))
        }
        initializeBookList()
        initializeBroadcastReceiver()
    }

    private fun initializeBroadcastReceiver() {
        mMessageReceiver = object : BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getBooleanExtra("error", false)) {
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    stopService(Intent(this@BooksActivity, ForegroundService::class.java))
                    Toast.makeText(this@BooksActivity, "Failed to download", Toast.LENGTH_SHORT)
                        .show()
                }
                if (intent.getBooleanExtra("complete", false)) {
                    binding.recyclerView.adapter?.notifyDataSetChanged()
                    stopService(Intent(this@BooksActivity, ForegroundService::class.java))
                    Toast.makeText(this@BooksActivity, "Download Completed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        registerReceiver(mMessageReceiver, IntentFilter("timer_tracking"))
        super.onStart()
    }

    private fun initializeBookList() {
        booksList.add(BooksModel("এহ্ইয়াউস সুনান", "aheyaus_sunan", "book_01"))
        booksList.add(BooksModel("আল-মাউযূআত", "al_mawjuat", "book_02"))
        booksList.add(BooksModel("আল্লাহর পথে দাওয়াত", "allahor_pothe_dawat", "book_03"))
        booksList.add(BooksModel("বাংলাদেশে উশর বা ফসলের যাকাত", "bangladeshe_ushar", "book_04"))
        booksList.add(
            BooksModel(
                "পবিত্র বাইবেল পরিচিতি ও পর্যালোচনা", "bible_porichiti_o_porjalochona", "book_05"
            )
        )
        booksList.add(
            BooksModel(
                "সালাতুল ঈদের অতিরিক্ত তাকবীর", "eid_er_otirikto_takbir", "book_06"
            )
        )
        booksList.add(BooksModel("আল-ফিকহুল আকবার", "fiqhul_akbar", "book_07"))
        booksList.add(
            BooksModel(
                "ফিকহুস সুনানি ওয়াল আসার ১ম খণ্ড", "fiqhus_sunani_wal_asar_01", "book_08"
            )
        )
        booksList.add(
            BooksModel(
                "ফিকহুস সুনানি ওয়াল আসার ২য় খণ্ড", "fiqhus_sunani_wal_asar_02", "book_09"
            )
        )
        booksList.add(
            BooksModel(
                "ফিকহুস সুনানি ওয়াল আসার ৩য় খণ্ড", "fiqhus_sunani_wal_asar_03", "book_10"
            )
        )
        booksList.add(BooksModel("হাদীসের নামে জালিয়াতি", "hadiser_name_jaliati", "book_11"))
        booksList.add(BooksModel("ইসলামী আকীদা", "islami_akida", "book_12"))
        booksList.add(BooksModel("ইযহারুল হক (১ম খণ্ড)", "izharul_haq_01", "book_13"))
        booksList.add(BooksModel("ইযহারুল হক (২য় খণ্ড)", "izharul_haq_02", "book_14"))
        booksList.add(BooksModel("ইযহারুল হক (৩য় খণ্ড)", "izharul_haq_03", "book_15"))
        booksList.add(
            BooksModel(
                "কুরআন-সুন্নাহর আলোকে জামাআত ও ঐক্য", "jamayat_o_oikko", "book_16"
            )
        )
        booksList.add(BooksModel("জিজ্ঞাসা ও জবাব (১ম খণ্ড)", "jiggasa_o_jobab_01", "book_17"))
        booksList.add(BooksModel("জিজ্ঞাসা ও জবাব (২য় খণ্ড)", "jiggasa_o_jobab_02", "book_18"))
        booksList.add(BooksModel("জিজ্ঞাসা ও জবাব (৩য় খণ্ড)", "jiggasa_o_jobab_03", "book_19"))
        booksList.add(BooksModel("জিজ্ঞাসা ও জবাব (৪র্থ খন্ড)", "jiggasa_o_jobab_04", "book_20"))
        booksList.add(BooksModel("ইসলামের নামে জঙ্গিবাদ", "jongibad", "book_21"))
        booksList.add(BooksModel("খুতবাতুল ইসলাম", "khutbatul_islam", "book_22"))
        booksList.add(BooksModel("মিম্বারের আহবান ১", "mimbarer_ahoban_01", "book_23"))
        booksList.add(BooksModel("মিম্বারের আহবান ২", "mimbarer_ahoban_02", "book_24"))
        booksList.add(BooksModel("মুনাজাত ও নামায", "munajat_o_namaj", "book_25"))
        booksList.add(
            BooksModel(
                "কুরআন-সুন্নাহর আলোকে পোশাক, পর্দা ও দেহ-সজ্জা", "poshak_porda", "book_26"
            )
        )
        booksList.add(BooksModel("কুরবানী ও জাবীহুল্লাহ", "qurbani_zabihullah", "book_27"))
        booksList.add(BooksModel("রাহে বেলায়াত", "rahe_belayet", "book_28"))
        booksList.add(BooksModel("সহীহ মাসনূন ওযীফা", "sahih_masnun_ozifa", "book_29"))
        booksList.add(BooksModel("সালাত, দু’আ ও যিকর", "salat_dua_o_zikr", "book_30"))
        booksList.add(
            BooksModel(
                "সালাতের মধ্যে হাত বাঁধার বিধান", "salater_hat_badhar_bidhan", "book_31"
            )
        )
        booksList.add(BooksModel("কুরআন সুন্নাহর আলোকে শবে বরাত", "sobe_borat", "book_32"))
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = BooksAdapter(this, booksList)
        binding.recyclerView.adapter = adapter
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("tag", "beforeTextChanged")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("tag", "afterTextChanged")
            }

        })
        binding.search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.search.windowToken, 0)
                adapter.filter.filter(v.text)
            }
            true
        }
    }

    override fun onDestroy() {
        ForegroundService.isDownloading = false
        stopService(Intent(this@BooksActivity, ForegroundService::class.java))
        unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }
}