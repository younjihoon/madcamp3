package com.example.madcamp3jhsj

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.madcamp3jhsj.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Intent로 전달된 값 확인
        val fragmentId = intent.getIntExtra("FRAGMENT_ID", R.id.navigation_home)

        // 초기 Fragment 설정
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navController.navigate(fragmentId)

        navView.setupWithNavController(navController)
    }
}