package org.gampiot.robok.core.utils.base

/*
 *  This file is part of Robok © 2024.
 *
 *  Robok is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Robok is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Robok.  If not, see <https://www.gnu.org/licenses/>.
 */ 

import android.os.Bundle
import android.os.Environment
import android.os.Build
import android.graphics.Color
import android.content.res.Configuration
import android.view.View
import android.view.WindowInsets

import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import dev.trindadedev.easyui.components.dialogs.PermissionDialog

import org.gampiot.robok.core.utils.R
import org.gampiot.robok.core.utils.requestReadWritePermissions
import org.gampiot.robok.core.utils.requestAllFilesAccessPermission
import org.gampiot.robok.core.utils.getStoragePermStatus
import org.gampiot.robok.core.utils.getBackPressedClickListener
import org.gampiot.robok.core.utils.enableEdgeToEdgeProperly
import org.gampiot.robok.core.utils.PermissionListener
import org.gampiot.robok.strings.Strings

open class RobokActivity : AppCompatActivity(), PermissionListener {

    private var permissionDialog: PermissionDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!getStoragePermStatus(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                 requestAllFilesAccessPermissionDialog()
            } else {
                requestReadWritePermissionsDialog()
            }
        }
    }

    open fun configureEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemGestureInsets.left,
                insets.systemGestureInsets.top,
                insets.systemGestureInsets.right,
                insets.systemGestureInsets.bottom
            )
            insets.consumeSystemWindowInsets()
        }

        val scrimColor = Color.TRANSPARENT
        val style = SystemBarStyle.auto(scrimColor, scrimColor)
        enableEdgeToEdge(
            statusBarStyle = style,
            navigationBarStyle = style
        )
    }

    private fun requestReadWritePermissionsDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        permissionDialog = PermissionDialog.Builder(this)
            .setIconResId(R.drawable.ic_folder_24)
            .setText(getString(Strings.warning_storage_perm_message))
            .setAllowClickListener {
                requestReadWritePermissions(this@RobokActivity, this@RobokActivity)
            }
            .setDenyClickListener {
                finish()
            }
            .build()

        permissionDialog?.show()
    }

    private fun requestAllFilesAccessPermissionDialog() {
        if (isFinishing || isDestroyed) {
            return
        }
        permissionDialog = PermissionDialog.Builder(this)
            .setIconResId(R.drawable.ic_folder_24)
            .setText(getString(Strings.warning_all_files_perm_message))
            .setAllowClickListener {
                requestAllFilesAccessPermission(this@RobokActivity, this@RobokActivity)
            }
            .setDenyClickListener {
                finish()
            }
            .build()

        permissionDialog?.show()
    }

    open fun configureToolbarNavigationBack(toolbar: MaterialToolbar) {
        toolbar.setNavigationOnClickListener(getBackPressedClickListener(onBackPressedDispatcher))
    }

    open fun isDarkMode(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onReceive(status: Boolean) {
        if (status) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    requestAllFilesAccessPermissionDialog()
                } else {
                    permissionDialog?.dismiss()
                }
            } else {
                permissionDialog?.dismiss()
            }
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(Strings.error_storage_perm_title))
                .setMessage(getString(Strings.error_storage_perm_message))
                .setCancelable(false)
                .setPositiveButton(Strings.common_word_allow) { _, _ ->
                    requestReadWritePermissionsDialog()
                }
                .show()
        }
    }
}