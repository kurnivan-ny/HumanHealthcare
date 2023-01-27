package com.kurnivan_ny.humanhealthcare.viewPager

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.kurnivan_ny.humanhealthcare.R
import com.kurnivan_ny.humanhealthcare.databinding.ActivityOnBoardingBinding
import com.kurnivan_ny.humanhealthcare.sign.signin.MasukActivity
import com.kurnivan_ny.humanhealthcare.sign.signup.DaftarActivity
import com.kurnivan_ny.humanhealthcare.utils.Preferences

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding:ActivityOnBoardingBinding

    private lateinit var adapter: ImageSliderAdapter
    private val list = ArrayList<ImageData>()
    private lateinit var dots: ArrayList<TextView>

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private lateinit var preference: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable{
            var index = 0
            override fun run() {
                if (index == list.size)
                    index = 0
                Log.e("Runnable","$index")
                binding.viewPager.setCurrentItem(index)
                index++
                handler.postDelayed(this, 2000)
            }
        }

        list.add(
            ImageData(
                R.drawable.pantau_glukosa
            )
        )

        list.add(
            ImageData(
                R.drawable.pantau_kolesterol
            )
        )

        adapter = ImageSliderAdapter(list)
        binding.viewPager.adapter = adapter
        dots = ArrayList()
        setIndicator()

        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                selectedDot(position)
                super.onPageSelected(position)
            }
        })

        handler.post(runnable)


        preference = Preferences(this)

        if (preference.getValuesString("onboarding").equals("1")){
            preference.setValuesString("onboarding", "1")
            finishAffinity()

            var intent = Intent(this@OnBoardingActivity, MasukActivity::class.java)
            startActivity(intent)
        }

        binding.btnMasuk.setOnClickListener {
            preference.setValuesString("onboarding", "1")
            finishAffinity()

            var intent = Intent(this@OnBoardingActivity, MasukActivity::class.java)
            startActivity(intent)
        }

        binding.btnDaftar.setOnClickListener {
            preference.setValuesString("onboarding", "1")
            finishAffinity()

            var intent = Intent(this@OnBoardingActivity, DaftarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectedDot(position: Int) {
        for(i in 0 until list.size){
            if (i == position){
                dots[i].setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                dots[i].setTextColor(ContextCompat.getColor(this, R.color.customColorHint))
            }
        }

    }

    private fun setIndicator() {
        for (i in 0 until list.size) {
            dots.add(TextView(this))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dots[i].text = Html.fromHtml("&#9679", Html.FROM_HTML_MODE_LEGACY).toString()
            } else {
                dots[i].text = Html.fromHtml("&#9679")
            }
            dots[i].textSize = 18f
            binding.dotsIndicator.addView(dots[i])
        }
    }

//    override fun onStart() {
//        super.onStart()
//        handler.post(runnable)
//    }

    override fun onStop(){
        super.onStop()
        handler.removeCallbacks(runnable)
    }
}