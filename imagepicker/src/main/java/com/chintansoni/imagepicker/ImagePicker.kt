/*
 * Copyright 2019 Chintan Soni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chintansoni.imagepicker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.chintansoni.imagepicker.SelectImageBottomSheetDialogFragment.Companion.TAG

class ImagePicker(func: Configuration.() -> Unit = {}) {

    private val configuration = Configuration()
    private val selectImageBottomSheetDialogFragment = SelectImageBottomSheetDialogFragment()

    init {
        configuration.apply(func)
    }

    fun show(fragmentActivity: FragmentActivity, onSuccess: (ImageOutput) -> Unit): ImageTask {
        return show(fragmentActivity.supportFragmentManager, onSuccess)
    }

    fun show(fragment: Fragment, onSuccess: (ImageOutput) -> Unit): ImageTask {
        return show(fragment.childFragmentManager, onSuccess)
    }

    private fun show(
        fragmentManager: FragmentManager,
        onSuccess: (ImageOutput) -> Unit
    ): ImageTask {
        val task = ImageTask().apply { onSuccess(onSuccess) }
        selectImageBottomSheetDialogFragment.apply {
            setConfiguration(configuration)
            setListener(task)
        }.show(fragmentManager, TAG)
        return task
    }

    fun configure(func: Configuration.() -> Unit) {
        configuration.apply(func)
    }

    fun clearCache() {

    }
}