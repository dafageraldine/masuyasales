package com.yusuffahrudin.masuyamobileapp.listtimbangan

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.databinding.ActivityTimbanganListBinding
import com.yusuffahrudin.masuyamobileapp.util.SessionManager
import java.text.NumberFormat
import java.util.*

class ListTimbanganActivity: AppCompatActivity() {
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var kdkota: String
    val nf = NumberFormat.getInstance(Locale("pt", "BR"))
    private lateinit var sessionManager: SessionManager
    private lateinit var name: String
    private lateinit var binding: ActivityTimbanganListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimbanganListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = getString(R.string.list_timbangan)

        sessionManager = SessionManager(applicationContext)
        val user = sessionManager.userDetails
        kdkota = user[SessionManager.kdkota].toString()
        name = user[SessionManager.kunci_email].toString()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        binding.containerSo.adapter = mSectionsPagerAdapter

        binding.tabLayout.setupWithViewPager(binding.containerSo)
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_sandtime_48)
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_ok_64)
    }

    override fun onBackPressed() {
        finish()
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
                0 -> return ProsesFragment()
                else -> return SelesaiFragment()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.proses)
                1 -> return getString(R.string.selesai)
            }
            return null
        }
    }
}