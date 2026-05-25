package com.example.overloadtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.overloadtracker.data.AppDatabase
import com.example.overloadtracker.ui.MainScreen
import com.example.overloadtracker.ui.WorkoutViewModel
import com.example.overloadtracker.ui.theme.OverloadTrackerTheme // Kendi projenin teması, hata verirse importu yenile

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Veritabanını ve DAO'yu başlatıyoruz
        val database = AppDatabase.getDatabase(applicationContext)
        val workoutDao = database.workoutDao()

        // 2. ViewModel'ı DAO ile oluşturacak Factory'yi tanımlıyoruz
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return WorkoutViewModel(workoutDao) as T
                }
                throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
            }
        }

        // 3. ViewModel nesnesini Factory aracılığıyla üretiyoruz
        val viewModel = ViewModelProvider(this, viewModelFactory)[WorkoutViewModel::class.java]


        setContent {
            OverloadTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // DashboardScreen yerine artık yeni navigasyonlu ana ekranımızı çağırıyoruz
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}