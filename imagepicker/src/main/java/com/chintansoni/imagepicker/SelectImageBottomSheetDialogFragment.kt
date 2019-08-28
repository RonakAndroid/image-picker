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

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.chintansoni.imagepicker.exception.ErrorCreatingFileException
import com.chintansoni.imagepicker.exception.NoAppsFoundForActionException
import com.chintansoni.imagepicker.exception.PermissionDeniedException
import com.chintansoni.imagepicker.exception.PermissionPermanentlyDeniedException
import com.chintansoni.imagepicker.util.FileUtils
import com.chintansoni.imagepicker.util.FileUtils.createImageFile
import com.chintansoni.imagepicker.util.copyInputStreamToFile
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_image_picker.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException


internal class SelectImageBottomSheetDialogFragment : BottomSheetDialogFragment(),
    EasyPermissions.PermissionCallbacks {

    private var photoFile: File? = null
    private lateinit var imageTask: ImageTask
    private lateinit var configuration: Configuration

    companion object {
        private const val RC_EXTERNAL_STORAGE = 100
        private const val BUNDLE_KEY_CONFIG = "config"
        private const val RC_TAKE_PHOTO = 101
        private const val RC_PICK_PHOTO = 102
        private const val RC_SETTINGS_ACTIVITY = 659
        internal const val TAG = "SelectImageBottomSheetDialogFragment"
        private val perms = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun setConfiguration(configuration: Configuration) {
        arguments = Bundle().apply { putParcelable(BUNDLE_KEY_CONFIG, configuration) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configuration = arguments?.getParcelable(BUNDLE_KEY_CONFIG) ?: Configuration()
        askReadExternalStoragePermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_image_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_camera.setOnClickListener {
            openCamera()
        }
        btn_gallery.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(intent, RC_PICK_PHOTO)
        } else {
            imageTask.onFailureFunc.invoke(NoAppsFoundForActionException())
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    createImageFile(requireContext())
                } catch (ex: IOException) {
                    imageTask.onFailureFunc.invoke(ex)
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val mPhotoUri = FileUtils.getUri(requireContext(), it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri)
                    startActivityForResult(takePictureIntent, RC_TAKE_PHOTO)
                } ?: imageTask.onFailureFunc.invoke(ErrorCreatingFileException())
            }
        }
    }

    fun setListener(task: ImageTask) {
        this.imageTask = task
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (!EasyPermissions.hasPermissions(requireContext(), *perms)) {
                dismiss()
            }
        } else if (requestCode == RC_TAKE_PHOTO && resultCode == RESULT_OK) {
            photoFile?.let {
                imageTask.onSuccessFunc.invoke(ImageOutput(requireContext(), it))
            }
            dismiss()
        } else if (requestCode == RC_PICK_PHOTO && resultCode == RESULT_OK) {
            data?.data?.let {
                runCatching {
                    imageTask.onSuccessFunc.invoke(
                        ImageOutput(
                            requireContext(),
                            getFileFromUri(it)
                        )
                    )
                }.onFailure(imageTask.onFailureFunc)
            }
            dismiss()
        }
    }

    @Throws(IOException::class)
    private fun getFileFromUri(uri: Uri): File {
        val file = createImageFile(requireContext())
        requireContext().contentResolver.openInputStream(uri)?.let {
            file.copyInputStreamToFile(it)
        }
        return file
    }

    // [Start] Permission Model
    private fun askReadExternalStoragePermission() {
        if (!EasyPermissions.hasPermissions(requireContext(), *perms)) {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this,
                configuration.rationaleText,
                RC_EXTERNAL_STORAGE,
                *perms
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            showPermanentlyDeniedDialog()
        } else {
            dismiss()
            imageTask.onFailureFunc(PermissionDeniedException())
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    /**
     * Opens app settings screen
     */
    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivityForResult(intent, RC_SETTINGS_ACTIVITY)
    }

    /**
     * Shows permission permanently blocked dialog
     */
    private fun showPermanentlyDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(configuration.blockedTitle)
            .setMessage(configuration.blockedText)
            .setPositiveButton(R.string.open_settings) { dialog, _ ->
                openSettings()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                dismiss()
                imageTask.onFailureFunc(PermissionPermanentlyDeniedException())
            }
            .setCancelable(false)
            .create()
            .takeIf { !requireActivity().isFinishing }?.show()
    }
}