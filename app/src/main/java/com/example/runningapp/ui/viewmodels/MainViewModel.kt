package com.example.runningapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.classes.Run
import com.example.runningapp.other.SortType
import com.example.runningapp.repositories.MainRepository
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    val runSortedByDate = mainRepository.getAllRunsSortedByDate()
    val runSortedByCalories = mainRepository.getAllRunsSortedByCaloriesBurned()
    val runSortedByTime = mainRepository.getAllRunsSortedByTimeInMillis()
    val runSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    val runSortedByDistance = mainRepository.getAllRunsSortedByDistance()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runSortedByDate) {
            if (sortType == SortType.DATE) {
                it?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByAvgSpeed) {
            if (sortType == SortType.AVG_SPEED) {
                it?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByCalories) {
            if (sortType == SortType.CALORIES) {
                it?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByTime) {
            if (sortType == SortType.RUNNING_TIME) {
                it?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByDistance) {
            if (sortType == SortType.DISTANCE) {
                it?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runSortedByTime.value?.let { runs.value = it }
        SortType.DATE -> runSortedByDate.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortedByDate.value?.let { runs.value = it }
        SortType.CALORIES -> runSortedByCalories.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}