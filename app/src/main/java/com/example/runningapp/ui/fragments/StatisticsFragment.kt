package com.example.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.CustoMarkerView
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    private fun setupBarChart(){
        bartChart.xAxis.apply {
            position= XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor= Color.WHITE
            textColor= Color.WHITE
            setDrawGridLines(false)
        }
        bartChart.axisLeft.apply {
            axisLineColor= Color.WHITE
            textColor= Color.WHITE
            setDrawGridLines(false)
        }
        bartChart.axisRight.apply {
            axisLineColor= Color.WHITE
            textColor= Color.WHITE
            setDrawGridLines(false)
        }

        bartChart.apply {
            description.text= "Avg Speed Over Time"
            legend.isEnabled= false
        }

    }

    private fun subscribeToObservers() {
        // THIS ONE IS FOR SHOWING THE TOTAL TIME IN THE FRAGMENT
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTime = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTime
            }
        })
        // THIS ONE IS FOR SHOWING THE TOTAL DISTANCE IN THE FRAGMENT
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })
        // THIS ONE IS FOR SHOWING THE AVG SPEED IN THE FRAGMENT
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        })

        // THIS ONE IS FOR SHOWING THE CALORIES BURNED IN THE FRAGMENT
        viewModel.totalCalroiesVurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalcaloriesString = "${it}kcal"
                tvTotalCalories.text = totalcaloriesString
            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                // THIS LINE IF FOR TAKING ALL THE RUNS SORTED BY DATE AND SHOW THE AVG SPEED OF EACH ONE BY RUNNING THROUGH THE LIST AND TAKING THE AVG SPEED AND THE POSITION OF THE RUN
                val allAvgSpeeds= it.indices.map { i-> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                // THIS ONE IS FOR CREATING THE SET OF DATA AND GIVING A LABEL FOR THEM AND EDITING THE TEXT COLOR AND THE COLOR OF THE CHART
                val barDataSet= BarDataSet(allAvgSpeeds,"Avg Speed Over Time").apply {
                    valueTextColor= Color.WHITE
                    color= ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                // THIS ONE IS FOR INJECTING THE SET OF DATA THAT WE MODIFIED ABOVE AND INSERTING THEM INTO THE CHART
                bartChart.data= BarData(barDataSet)
                bartChart.marker= CustoMarkerView(it,requireContext(),R.layout.marker_view)
                // THIS IS FOR NOTIFYING THE CHART ABOUT THE CHANGES AND SHOWING THEM
                bartChart.invalidate()

            }
        })
    }
}