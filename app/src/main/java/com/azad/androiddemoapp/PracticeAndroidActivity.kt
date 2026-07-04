package com.azad.androiddemoapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.azad.androiddemoapp.Util.logFlowC1
import com.azad.androiddemoapp.Util.logFlowC2
import com.azad.androiddemoapp.Util.logFlowC3
import com.azad.androiddemoapp.databinding.ActivityPracticeAndroidBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class PracticeAndroidActivity : AppCompatActivity() {
   lateinit var binding: ActivityPracticeAndroidBinding
   lateinit var liveData: MutableLiveData<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPracticeAndroidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        liveData = MutableLiveData<Int>()
        produceLivedata()

            liveData.observe(this@PracticeAndroidActivity){
                it.toString().logFlowC1()
        }
        liveData.observe(this@PracticeAndroidActivity){
            it.toString().logFlowC2()
        }
        liveData.observe(this@PracticeAndroidActivity){
            it.toString().logFlowC3()
        }
        liveData.observe(this@PracticeAndroidActivity){
            it.toString().logFlowC3()
        }


        GlobalScope.launch {
            val  flow = produceStateFlow()

            launch {
                flow.collect {
                    it.toString().logFlowC1()
                }
            }
            delay(2000)

            launch {
                flow.collect {
                    it.toString().logFlowC2()
                }
            }
         delay(1000)
            launch {
                flow.collect {
                    it.toString().logFlowC3()
                }
            }



        }


    }
    fun produceFlow(): Flow<Int> {
        return flow {
            repeat(5){ i ->
                emit(i)
                delay(1000)
            } }

    }
    fun produceStateFlow(): StateFlow<Int> {
        val stateFlow = MutableStateFlow(0)
        GlobalScope.launch {
            repeat(5){ i ->
                stateFlow.value = i
                delay(1000)
            }
        }
        return stateFlow
    }
    fun produceSharedFlow(): SharedFlow<Int> {
        val sharedFlow = MutableSharedFlow<Int>()
        GlobalScope.launch {
            repeat(5){ i ->
                sharedFlow.emit(i)
                delay(1000)
            }
        }
        return sharedFlow
    }
    fun produceLivedata(): MutableLiveData<Int>{
        GlobalScope.launch {
            repeat(5){ i ->
                liveData.postValue(i)
               // liveData.value = i
                delay(1000)
            }
        }
        return liveData
    }


    }
