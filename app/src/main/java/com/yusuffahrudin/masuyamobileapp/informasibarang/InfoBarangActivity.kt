package com.yusuffahrudin.masuyamobileapp.informasibarang

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.yusuffahrudin.masuyamobileapp.R

class InfoBarangActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.informasi_barang)
        setContentView(R.layout.activity_info_barang)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        val mViewPager = findViewById<ViewPager>(R.id.container_info_brg)
        mViewPager.offscreenPageLimit = 5
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.currentItem = 0
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(mViewPager)
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_stok_white_50)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_detail_white_64)
        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_barcode_reader_white_50)
        tabLayout.getTabAt(3)?.setIcon(R.drawable.ic_report_white_100)
        tabLayout.getTabAt(4)?.setIcon(R.drawable.ic_history_pembelian_white_100)
        tabLayout.getTabAt(5)?.setIcon(R.drawable.ic_order_history_white_100)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return StokBarangFragment()
                1 -> return DetailBarangFragment()
                2 -> return BarcodeBarangFragment()
                3 -> return EvaluasiBarangFragment()
                4 -> return HistoryPenjualanFragment()
                else -> return HistoryPembelianFragment()
            }
        }

        override fun getCount(): Int {
            return 6
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return null
        }
    }
}