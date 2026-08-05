package us.zoom.uitoolkitsample

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LaunchActivity : AppCompatActivity() {
    private val pages = listOf(
        R.drawable.intro_image1,
        R.drawable.intro_image2,
        R.drawable.intro_image3,
        R.drawable.intro_image4,
        R.drawable.intro_image5,
        R.drawable.intro_image6
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        val pager = findViewById<ViewPager2>(R.id.pager)
        val btnCreate = findViewById<MaterialButton>(R.id.btnCreate)
        val btnJoin = findViewById<MaterialButton>(R.id.btnJoin)

        pager.adapter = LaunchPagerAdapter(pages)

        btnCreate.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java).apply {
                putExtra(JoinActivity.EXTRA_MODE, JoinActivity.MODE_CREATE)
            })
        }

        btnJoin.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java).apply {
                putExtra(JoinActivity.EXTRA_MODE, JoinActivity.MODE_JOIN)
            })
        }
    }

    inner class LaunchPagerAdapter(
        private val images: List<Int>
    ) : RecyclerView.Adapter<LaunchPagerAdapter.VH>() {

        inner class VH(val image: ImageView) : RecyclerView.ViewHolder(image)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val iv = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
            }
            return VH(iv)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.image.setImageResource(images[position])
        }

        override fun getItemCount() = images.size
    }
}