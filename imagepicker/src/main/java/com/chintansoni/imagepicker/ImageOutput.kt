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

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.chintansoni.imagepicker.util.BitmapDecodeAsyncTask
import java.io.File

/*
 * Created by Birju Vachhani on 26 July 2019
 * Copyright © 2019 image-picker. All rights reserved.
 */

class ImageOutput internal constructor(context: Context, private val file: File) {

    private val fileUri: Uri by lazy {
        FileProvider.getUriForFile(context, context.packageName + ".imagepicker.fileprovider", file)
    }

    fun asUri(): Uri = fileUri

    fun asBitmap(func: (Bitmap?) -> Unit) {
        BitmapDecodeAsyncTask(func).execute(file)
    }

    fun asFile(): File {
        return file
    }
}