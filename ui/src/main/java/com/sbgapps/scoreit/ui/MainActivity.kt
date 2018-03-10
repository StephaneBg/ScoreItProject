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

package com.sbgapps.scoreit.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.sbgapps.scoreit.ui.base.BaseActivity
import com.sbgapps.scoreit.ui.ext.color
import com.sbgapps.scoreit.ui.ext.onImeActionDone
import com.sbgapps.scoreit.ui.ext.replaceFragment
import com.sbgapps.scoreit.ui.view.HeaderFragment
import com.sbgapps.scoreit.ui.view.UniversalHistoryFragment
import com.sbgapps.scoreit.ui.view.UniversalLapFragment
import com.sbgapps.scoreit.ui.viewmodel.UniversalViewModel
import com.sbgapps.scoreit.ui.viewmodel.UniversalViewModel.Mode.*
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import org.koin.android.architecture.ext.viewModel


class MainActivity : BaseActivity() {

    private val model by viewModel<UniversalViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (null == savedInstanceState)
            launch {
                model.init()
            }.invokeOnCompletion {
                replaceFragment(R.id.headerContainer, HeaderFragment.newInstance())
                replaceFragment(R.id.lapContainer, UniversalHistoryFragment.newInstance())
            }

        setSupportActionBar(toolbar)
        fab.setOnClickListener { onFabClicked() }
        decorFab()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (model.isOnHistoryMode()) menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.playerCount -> {
                showPlayerCountDialog()
                true
            }

            R.id.totals -> {
                val isShown = model.toggleShowTotal()
                val res = if (isShown) R.string.menu_action_hide_totals else R.string.menu_action_show_totals
                item.title = getString(res)
                true
            }

            R.id.clear -> {
                showClearLapDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (!model.isOnHistoryMode()) {
            model.endLapEdition()
            decorFab()
        }
        invalidateOptionsMenu()
        super.onBackPressed()
    }

    private fun onFabClicked() {
        if (model.isOnHistoryMode()) {
            model.startAdditionMode()
            replaceFragment(R.id.lapContainer, UniversalLapFragment.newInstance(), true)
        } else {
            model.onLapEditionCompleted()
            supportFragmentManager.popBackStack()
        }
        switchFab()
        invalidateOptionsMenu()
    }

    private fun switchFab() {
        fab.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
            override fun onHidden(fab: FloatingActionButton) {
                decorFab()
                fab.show()
            }
        })
    }

    private fun decorFab() {
        when (model.mode) {
            MODE_HISTORY -> {
                fab.backgroundTintList = ColorStateList.valueOf(color(R.color.orange_500))
                fab.setImageDrawable(getDrawable(R.drawable.ic_add_black_24dp))
            }
            MODE_UPDATE, MODE_ADDITION -> {
                fab.backgroundTintList = ColorStateList.valueOf(color(R.color.green_500))
                fab.setImageDrawable(getDrawable(R.drawable.ic_done_black_24dp))
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showPlayerCountDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_player_count, null) as NumberPicker
        AlertDialog.Builder(this)
                .setView(view)
                .setTitle(R.string.dialog_title_player_number)
                .setPositiveButton(R.string.button_action_ok, { _, _ ->
                    showGameNameDialog(view.value)
                })
                .setNeutralButton(R.string.button_action_cancel, null)
                .create()
                .show()
    }

    @SuppressLint("InflateParams")
    private fun showGameNameDialog(playerCount: Int) {
        val editText = layoutInflater.inflate(R.layout.dialog_game_name, null) as EditText
        val dialog = AlertDialog.Builder(this)
                .setView(editText)
                .setTitle(R.string.dialog_title_game_name)
                .setPositiveButton(R.string.button_action_ok, { _, _ ->
                    model.createGame(editText.text.toString(), playerCount)
                })
                .setNeutralButton(R.string.button_action_cancel, null)
                .create()

        editText.onImeActionDone {
            model.createGame(editText.text.toString(), playerCount)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showClearLapDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_current_game)
                .setItems(R.array.dialog_clear_actions, { _, which ->
                    when (which) {
                        0 -> model.clearLaps()
                        1 -> showGameNameDialog(model.getPlayerCount())
                    }
                })
                .create()
                .show()
    }
}