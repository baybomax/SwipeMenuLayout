package com.android.db.swipemenulayout

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.android.db.multirecycleviewadapter.BaseAdapter
import com.android.db.multirecycleviewadapter.BaseViewHolder

class MainActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: SimpleAdapter

    private val mDataSrc = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = SimpleAdapter(R.layout.item_rv_swipemenu, mDataSrc)
        mRecyclerView.adapter = mAdapter

        (0 until 20).forEach {
            mDataSrc.add("")
        }

        mAdapter.addData(mDataSrc)
        mAdapter.notifyDataSetChanged()

    }

    private inner class SimpleAdapter(layoutIdRes: Int, dataSrc: List<String>)
        : BaseAdapter<String, BaseViewHolder>(layoutIdRes, dataSrc) {

        override fun convert(helper: BaseViewHolder, item: String?) {
            helper.getView<ViewGroup>(R.id.content)?.setOnClickListener {
                Toast.makeText(this@MainActivity, "content", Toast.LENGTH_SHORT).show()
            }

            helper.getView<TextView>(R.id.collect)?.setOnClickListener {
                Toast.makeText(this@MainActivity, "collect", Toast.LENGTH_SHORT).show()

                helper.getView<SwipeMenuLayout>(R.id.sml)?.resetStatus()
            }
        }
    }
}
