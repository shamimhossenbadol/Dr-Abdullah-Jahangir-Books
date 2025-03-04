package com.shamimhossen.abdullah_jahangir_books.activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shamimhossen.abdullah_jahangir_books.adapters.VideoAdapter
import com.shamimhossen.abdullah_jahangir_books.adapters.VideoAdapter.ItemClick
import com.shamimhossen.abdullah_jahangir_books.api.VideoApiService
import com.shamimhossen.abdullah_jahangir_books.databinding.ActivityLecturesBinding
import com.shamimhossen.abdullah_jahangir_books.models.Videos
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LecturesActivity : AppCompatActivity(), ItemClick {
    private lateinit var binding: ActivityLecturesBinding
    private val playlistId = "UULFWuvzUF7ZcRCAsWswzjNLbw"
    private val apiKey = "AIzaSyAdskpiz5l4iZPDU7PMxECfoOBPXEeTNAo"
    private var retry = 0
    private var pageToken: String? = null
    private val videoList = mutableListOf<String>()
    private var mPlayer: YouTubePlayer? = null
    private var youtubeVideoId: String? = null
    private var currentPos = 0
    private var totalDu = 0
    private var isFullScreen = false
    private lateinit var adapter: VideoAdapter
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestVideos(null)

        binding.recyclerViewId.layoutManager = GridLayoutManager(this@LecturesActivity, 2)
        adapter = VideoAdapter(this@LecturesActivity, videoList)
        binding.recyclerViewId.adapter = adapter
        binding.recyclerViewId.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItem = layoutManager.childCount
                val totalItem = layoutManager.itemCount
                val scrolledOut = layoutManager.findFirstVisibleItemPosition()
                if (visibleItem + scrolledOut >= totalItem && !isLoading) {
                    if (pageToken != null) requestVideos(pageToken)
                }
            }
        })

        lifecycle.addObserver(binding.normalScreenView)
        val options: IFramePlayerOptions = IFramePlayerOptions.Builder().controls(0).build()
        val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                mPlayer = youTubePlayer
            }

            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                binding.normalVideoProgress.progress = second.toInt()
                currentPos = second.toInt()
                binding.normalCurrentDu.text =
                    String.format("%02d:%02d", (second / 60).toInt(), (second % 60).toInt())
                binding.fullscreenVideoProgress.progress = second.toInt()
                binding.fullscreenCurrentDu.text =
                    String.format("%02d:%02d", (second / 60).toInt(), (second % 60).toInt())
            }

            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                binding.normalVideoProgress.max = duration.toInt()
                totalDu = duration.toInt()
                binding.normalTotalDu.text =
                    String.format("%02d:%02d", (duration / 60).toInt(), (duration % 60).toInt())
                binding.fullscreenVideoProgress.max = duration.toInt()
                binding.fullscreenTotalDu.text =
                    String.format("%02d:%02d", (duration / 60).toInt(), (duration % 60).toInt())
            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer, loadedFraction: Float
            ) {
                binding.normalVideoProgress.secondaryProgress = (loadedFraction * 100).toInt()
                binding.fullscreenVideoProgress.secondaryProgress = (loadedFraction * 100).toInt()
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState
            ) {
                if (state == PlayerConstants.PlayerState.PAUSED) {
                    binding.normalPlayIcon.visibility = View.VISIBLE
                    binding.fullscreenPlayIcon.visibility = View.VISIBLE
                }
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    binding.normalPlayIcon.visibility = View.GONE
                    binding.fullscreenPlayIcon.visibility = View.GONE
                }
                if (state == PlayerConstants.PlayerState.ENDED) {
                    binding.normalScreen.visibility = View.GONE
                    binding.fullscreen.visibility = View.GONE
                    if (isFullScreen) mPlayer?.toggleFullscreen()
                }
            }
        }
        binding.normalPlayIcon.setOnClickListener {
            mPlayer?.play()
        }
        binding.fullscreenPlayIcon.setOnClickListener {
            mPlayer?.play()
        }
        binding.normalScreenView.initialize(listener, options)
        binding.normalBackward.setOnClickListener {
            if (currentPos != 0 && currentPos > 30) mPlayer?.seekTo((currentPos - 30).toFloat())
        }
        binding.fullscreenBackward.setOnClickListener {
            if (currentPos != 0 && currentPos > 30) mPlayer?.seekTo((currentPos - 30).toFloat())
        }
        binding.normalForward.setOnClickListener {
            if (currentPos != 0 && currentPos < totalDu - 30) mPlayer?.seekTo((currentPos + 30).toFloat())
        }
        binding.fullscreenForward.setOnClickListener {
            if (currentPos != 0 && currentPos < totalDu - 30) mPlayer?.seekTo((currentPos + 30).toFloat())
        }
        binding.normalScreenView.addFullscreenListener(object : FullscreenListener {
            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                binding.normalScreen.visibility = View.GONE
                binding.fullscreen.visibility = View.VISIBLE
                binding.fullscreenView.addView(fullscreenView)
                isFullScreen = true
                hideSystemUI()
            }

            override fun onExitFullscreen() {
                binding.normalScreen.visibility = View.VISIBLE
                binding.fullscreen.visibility = View.GONE
                binding.fullscreenView.removeAllViews()
                isFullScreen = false
                showSystemUI()
            }

        })
        binding.fullscreenButton.setOnClickListener {
            mPlayer?.toggleFullscreen()
        }
        binding.normalButton.setOnClickListener {
            mPlayer?.toggleFullscreen()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullScreen) {
                    mPlayer?.toggleFullscreen()
                } else {
                    this@LecturesActivity.finish()
                }
            }
        })
    }

    private fun requestVideos(
        nextPageToken: String?
    ) {
        isLoading = true
        binding.progress.visibility = View.VISIBLE
        val videoApiService = RetrofitClient.createVideoApiService()
        videoApiService.getVideos("snippet", 50, playlistId, apiKey, nextPageToken)
            .enqueue(object : Callback<Videos> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<Videos>, response: Response<Videos>
                ) {
                    binding.progress.visibility = View.GONE
                    isLoading = false
                    if (response.isSuccessful) {
                        pageToken = response.body()?.nextPageToken.toString()
                        val list = response.body()?.items
                        for (item in list!!) {
                            videoList.add(item.snippet.resourceId.videoId)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<Videos>, t: Throwable) {
                    binding.progress.visibility = View.GONE
                    if (retry < 3) {
                        retry++
                        requestVideos(nextPageToken)
                    } else this@LecturesActivity.finish()
                }
            })
    }

    private object RetrofitClient {
        private const val BASE_URL = "https://youtube.googleapis.com/"
        fun createVideoApiService(): VideoApiService {
            val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()
            return retrofit.create(VideoApiService::class.java)
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        supportActionBar?.hide()
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        supportActionBar?.show()
    }

    override fun onClick(videoId: String) {
        youtubeVideoId = videoId
        mPlayer?.loadOrCueVideo(lifecycle, youtubeVideoId!!, 0f)
        binding.normalScreen.visibility = View.VISIBLE
    }
}