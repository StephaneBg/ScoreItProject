/*
 * Copyright 2019 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbgapps.scoreit.data.solver

import com.sbgapps.scoreit.data.model.BeloteBonus
import com.sbgapps.scoreit.data.model.CoincheLapData
import com.sbgapps.scoreit.data.model.PlayerPosition
import com.sbgapps.scoreit.data.source.DataStore

class CoincheSolver(private val dataStore: DataStore) {

    fun getResults(lap: CoincheLapData): List<Int> {
        var (bidderPts, counterPts) = computeResults(lap)
        if (isWon(bidderPts, counterPts, lap.bidPoints)) {
            // Deal succeeded
            bidderPts += lap.bidPoints
            bidderPts *= lap.coincheBid.coefficient
        } else {
            // Deal failed
            bidderPts = 0
            counterPts = if (250 == lap.bidPoints) 500 else 160 + lap.bidPoints
            counterPts *= lap.coincheBid.coefficient
        }

        return if (PlayerPosition.ONE == lap.bidder) {
            listOf(bidderPts, counterPts)
        } else {
            listOf(counterPts, bidderPts)
        }
    }

    fun getDisplayResults(lap: CoincheLapData): Pair<List<String>, Boolean> {
        val (bidderPts, counterPts) = computeResults(lap)
        return getResults(lap).map { it.toString() } to isWon(bidderPts, counterPts, lap.bidPoints)
    }

    private fun isWon(bidderPts: Int, counterPts: Int, bidPoints: Int): Boolean =
        bidderPts >= bidPoints && bidderPts > counterPts

    private fun computeResults(lap: CoincheLapData): Pair<Int, Int> {
        val points = IntArray(2)
        val results = IntArray(2)

        if (lap.points >= 158) {
            points[0] = lap.points
            points[1] = 0
        } else {
            points[0] = lap.points
            points[1] = 162 - lap.points
        }

        if (PlayerPosition.ONE == lap.scorer) {
            results[PlayerPosition.ONE.index] = points[0]
            results[PlayerPosition.TWO.index] = points[1]
        } else {
            results[PlayerPosition.ONE.index] = points[1]
            results[PlayerPosition.TWO.index] = points[0]
        }

        // Add bonuses
        for ((player, bonus) in lap.bonuses) results[player.index] += bonus.points

        return if (PlayerPosition.ONE == lap.bidder) {
            results[PlayerPosition.ONE.index] to results[PlayerPosition.TWO.index]
        } else {
            results[PlayerPosition.TWO.index] to results[PlayerPosition.ONE.index]
        }
    }

    fun computeScores(laps: List<CoincheLapData>): List<Int> {
        val scores = MutableList(2) { 0 }
        laps.map { getResults(it) }.forEach { points ->
            for (player in 0 until 2) scores[player] += points[player]
        }
        return scores.map { getPointsForDisplay(it) }
    }

    fun getPointsForDisplay(points: Int): Int = if (dataStore.isCoincheScoreRounded()) roundPoint(points) else points

    fun canIncrement(lap: CoincheLapData): Pair<Boolean, Boolean> = (lap.bidPoints <= 990) to (lap.points < 150)

    fun canDecrement(lap: CoincheLapData): Pair<Boolean, Boolean> = (lap.bidPoints >= 110) to (lap.points >= 20)

    fun getAvailableBonuses(lap: CoincheLapData): List<BeloteBonus> {
        val currentBonuses = lap.bonuses.map { it.bonus }
        val bonuses = mutableListOf<BeloteBonus>()
        if (!currentBonuses.contains(BeloteBonus.BELOTE)) bonuses.add(BeloteBonus.BELOTE)
        bonuses.add(BeloteBonus.RUN_3)
        bonuses.add(BeloteBonus.RUN_4)
        bonuses.add(BeloteBonus.RUN_5)
        bonuses.add(BeloteBonus.FOUR_NORMAL)
        bonuses.add(BeloteBonus.FOUR_NINE)
        bonuses.add(BeloteBonus.FOUR_JACK)
        return bonuses
    }

    private fun roundPoint(score: Int): Int = when (score) {
        162, 160 -> 160
        250 -> score
        else -> (score + 5) / 10 * 10
    }
}
