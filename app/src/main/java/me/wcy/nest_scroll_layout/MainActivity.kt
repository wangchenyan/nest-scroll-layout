package me.wcy.nest_scroll_layout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val mDataList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nest_scroll.setOnScrollListener(onScrollListener)

        mDataList.add(R.drawable.alipay2)
        mDataList.add(R.drawable.alipay3)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = Adapter()
    }

    private val onScrollListener = object : NestScrollLayout.OnScrollListener {
        override fun onScroll(scrollY: Int, dy: Int, percent: Float) {
            if (percent <= 0.1) {
                title1.alpha = 1 - percent * 10 / 1
                title2.alpha = 0f
            } else {
                title1.alpha = 0f
                title2.alpha = (percent - 0.1f) * 10 / 9
            }

            scan.alpha = 1 - percent
            scan.translationY = scrollY / 2f
        }
    }

    private inner class Adapter : RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val image = ImageView(parent.context)
            val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            image.layoutParams = lp
            image.adjustViewBounds = true
            return VH(image)
        }

        override fun getItemCount(): Int {
            return mDataList.size
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(mDataList[position])
        }

    }

    private class VH constructor(val item: ImageView) : RecyclerView.ViewHolder(item) {
        fun bind(resId: Int) {
            item.setImageResource(resId)
        }
    }
}
