package edu.bluejack23_2.convhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import edu.bluejack23_2.convhub.adapters.ImageAdapter
import edu.bluejack23_2.convhub.databinding.ActivityLandingBinding
import java.util.*

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        binding.loginBtn.setOnClickListener{
            var loginIntent : Intent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        binding.registerBtn.setOnClickListener{
            var registerIntent : Intent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

    }

    private fun init() {
        imageList = ArrayList()
        handler = Handler(Looper.myLooper()!!)

        imageList.add(R.drawable.carousel1)
        imageList.add(R.drawable.carousel2)
        imageList.add(R.drawable.carousel3)

        imageAdapter = ImageAdapter(imageList, binding.carousel)
        binding.carousel.adapter = imageAdapter

        binding.carousel.clipToPadding = false
        binding.carousel.clipChildren = false
        binding.carousel.offscreenPageLimit = 3
        binding.carousel.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER


        autoSlide()
    }

    private fun autoSlide() {
        val runnable = Runnable {
            var currentItem = binding.carousel.currentItem
            currentItem++
            if (currentItem >= imageList.size) {
                currentItem = 0
            }
            binding.carousel.currentItem = currentItem
        }

        handler.postDelayed(object : Runnable {
            override fun run() {
                handler.postDelayed(this, 5000)
                runnable.run()
            }
        }, 5000)
    }

}
