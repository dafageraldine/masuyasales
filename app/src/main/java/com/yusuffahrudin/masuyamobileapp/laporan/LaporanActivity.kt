package com.yusuffahrudin.masuyamobileapp.laporan

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityLaporanBinding

class LaporanActivity: AppCompatActivity() {

    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var binding: ActivityLaporanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.laporan)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        binding.containerLaporan.adapter = mSectionsPagerAdapter

        binding.tabLayout.setupWithViewPager(binding.containerLaporan)
        //tab_layout?.getTabAt(0)?.setIcon(R.drawable.ic_history_pembelian_white_100)
        //tab_layout?.getTabAt(1)?.setIcon(R.drawable.ic_order_history_white_100)
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
                0 -> return PenjBrgQtyThnFragment()
                else -> return PenjBrgCustQtyThnFragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Brg Qty Per Tahun"
                1 -> return "Brg Cust Qty Per Tahun"
            }
            return null
        }
    }
}