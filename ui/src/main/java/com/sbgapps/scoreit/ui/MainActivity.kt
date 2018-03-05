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

import android.content.res.ColorStateList
import android.os.Bundle
import com.sbgapps.scoreit.ui.base.BaseActivity
import com.sbgapps.scoreit.ui.ext.color
import com.sbgapps.scoreit.ui.ext.replaceFragment
import com.sbgapps.scoreit.ui.view.HeaderFragment
import com.sbgapps.scoreit.ui.view.LapListFragment
import com.sbgapps.scoreit.ui.view.UniversalLapFragment
import com.sbgapps.scoreit.ui.viewmodel.UniversalViewModel
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
                replaceFragment(R.id.lapContainer, LapListFragment.newInstance())
            }

        fab.setOnClickListener { onFabClicked() }
    }

    private fun onFabClicked() {
        if (model.isOnLapEdition()) {
            model.onLapEditionCompleted()
            fab.backgroundTintList = ColorStateList.valueOf(color(R.color.color_accent))
            fab.setImageDrawable(getDrawable(R.drawable.ic_add_black_24dp))
            supportFragmentManager.popBackStack()
        } else {
            fab.backgroundTintList = ColorStateList.valueOf(color(R.color.color_primary))
            fab.setImageDrawable(getDrawable(R.drawable.ic_done_black_24dp))
            replaceFragment(R.id.lapContainer, UniversalLapFragment.newInstance(), true)
        }
    }
}