package com.example.runningapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.runningapp.repositories.MainRepository
import javax.inject.Inject

class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    val totalTimeRun= mainRepository.getTotalTimeInMillis()
    val totalAvgSpeed= mainRepository.getTotalAvgSpeed()
    val totalDistance= mainRepository.getTotalDistance()
    val totalCalroiesVurned= mainRepository.getTotalCaloriesBurned()

}