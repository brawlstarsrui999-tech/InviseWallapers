package com.wallapers

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * DPC receiver used only to provision an Android-managed work profile.
 * Android keeps this profile visible and controls its lifecycle.
 */
class WallapersDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)

        val admin = ComponentName(context, WallapersDeviceAdminReceiver::class.java)
        val policyManager = context.getSystemService(DevicePolicyManager::class.java)
        if (policyManager.isProfileOwnerApp(context.packageName)) {
            policyManager.setProfileName(admin, "Wallapers — отдельный профиль")
            policyManager.setProfileEnabled(admin)
        }

    }
}
