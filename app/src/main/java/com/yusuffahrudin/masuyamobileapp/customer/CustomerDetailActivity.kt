package com.yusuffahrudin.masuyamobileapp.customer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.customer.CustomerDetailFragment.Companion.EXTRA_CUSTOMER
import com.yusuffahrudin.masuyamobileapp.data.Customer
import com.yusuffahrudin.masuyamobileapp.salesorder.CreateSOActivity

class CustomerDetailActivity : AppCompatActivity() {
    private var customer: Customer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = getString(R.string.info_customer)
        setContentView(R.layout.activity_customer_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        customer = intent.getParcelableExtra(EXTRA_CUSTOMER)
        val mViewPager = findViewById<ViewPager>(R.id.container_info_brg)
        mViewPager.offscreenPageLimit = 3
        mViewPager.adapter = mSectionsPagerAdapter
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.setupWithViewPager(mViewPager)
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_user_conference_white_100)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_history_pembelian_white_100)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_report_white_100)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_create_sales_order) {
            val intent = Intent(this, CreateSOActivity::class.java)
            intent.putExtra(EXTRA_CUSTOMER, customer)
            startActivity(intent)
            finish()
        }
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> CustomerDetailFragment()
                1 -> CustomerHistoryFragment()
                else -> EvaluasiCustomerFragment()
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.detail)
                1 -> return getString(R.string.history_penjualan)
                2 -> return getString(R.string.evaluasi)
            }
            return null
        }
    }
}