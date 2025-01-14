package com.example.madcamp3jhsj

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.madcamp3jhsj.data.AppDatabase
import com.example.madcamp3jhsj.data.User
import com.example.madcamp3jhsj.data.UserRepository
import com.example.madcamp3jhsj.databinding.ActivityMainBinding
import com.example.madcamp3jhsj.ui.dashboard.DashboardFragment
import com.example.madcamp3jhsj.ui.home.HomeFragment
import com.example.madcamp3jhsj.ui.notifications.NotificationsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var useremail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userinit()

        // Intent로 전달된 값 확인
        val fragmentId = intent.getIntExtra("FRAGMENT_ID", R.id.navigation_home)

        setInitialFragment(fragmentId)
    }
    
    fun userinit(){
        val userDao = AppDatabase.getDatabase(this).userDao()
        val repository = UserRepository(userDao)
        sharedViewModel = ViewModelProvider(
            this,
            SharedViewModelFactory(repository)
        ).get(SharedViewModel::class.java)
        val username = intent.getStringExtra("USER_NAME")
        useremail = intent.getStringExtra("USER_EMAIL") ?: ""
        if (username != null) {
            sharedViewModel.loadUserByUsername(username)
        }

        // SharedViewModel 초기화
    }
    private fun setInitialFragment(fragmentId: Int) {
        val fragment = when (fragmentId) {
            R.id.navigation_dashboard -> DashboardFragment()
            R.id.navigation_notifications -> NotificationsFragment()
            else -> {
                val homeFragment = HomeFragment()
                val bundle = Bundle().apply {
                    putString("USER_EMAIL", useremail) // 유저 이메일 전달
                    Log.e("[setInFragment]","✅ User email: ${useremail}")
                }
                homeFragment.arguments = bundle
                homeFragment
            }
        }

        // FragmentTransaction을 통해 Fragment 교체
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_activity_main, fragment) // fragment_container는 FrameLayout ID
            .commit()
    }
//    private fun setupBottomNavigation() {
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            val fragment = when (item.itemId) {
//                R.id.navigation_dashboard -> DashboardFragment()
//                R.id.navigation_notifications -> NotificationsFragment()
//                R.id.navigation_home -> HomeFragment()
//                else -> HomeFragment()
//            }
//
//            // Fragment 교체
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .commit()
//
//            true
//        }
//    }
}
