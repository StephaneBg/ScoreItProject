/*
 * Copyright 2018 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbgapps.scoreit.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.sbgapps.scoreit.domain.model.Player
import com.sbgapps.scoreit.domain.model.UniversalLap
import com.sbgapps.scoreit.domain.usecase.UniversalUseCase
import com.sbgapps.scoreit.ui.base.BaseViewModel


class UniversalViewModel(private val useCase: UniversalUseCase) : BaseViewModel() {

    private val players = MutableLiveData<List<Player>>()
    private val scores = MutableLiveData<List<Int>>()
    private val laps = MutableLiveData<MutableList<UniversalLap>>()
    private var lap = MutableLiveData<UniversalLap>()

    suspend fun init() {
        useCase.execute()
    }

    fun getPlayers(): LiveData<List<Player>> {
        players.value ?: run { launchAsync { players.postValue(useCase.getPlayers()) } }
        return players
    }

    fun getScores(): LiveData<List<Int>> {
        scores.value ?: run { launchAsync { scores.postValue(useCase.getScores()) } }
        return scores
    }

    fun getLaps(): LiveData<MutableList<UniversalLap>> {
        laps.value ?: run { launchAsync { laps.postValue(useCase.getLaps()) } }
        return laps
    }

    fun onLapEditionCompleted() {
        launchAsync {
            lap.value?.let { useCase.addLap(it) }
            laps.postValue(useCase.getLaps())
            scores.postValue(useCase.getScores())
            lap.value = null
        }
    }

    fun isOnLapEdition(): Boolean = lap.value != null

    fun getLap(): LiveData<UniversalLap> {
        lap.value ?: run {
            launchAsync {
                val players = useCase.getPlayers()
                val points = ArrayList<Int>(players.size)
                for (i in 0 until players.size) points.add(0)
                lap.postValue(UniversalLap(null, points))
            }
        }
        return lap
    }

    fun setPoints(points: List<Int>) {
        lap.value?.let {
            it.clear()
            it.addAll(points)
        }
    }
}