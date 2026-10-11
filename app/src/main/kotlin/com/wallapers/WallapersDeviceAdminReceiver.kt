package com.wallapers

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * DPC receiver used only to provision an Android-managed work profile.
 * Android keeps this profile visible and controls its lifecycle.
 */
class WallapersDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        configureManagedProfile(context)
    }
}

internal fun configureManagedProfile(context: Context): Boolean {
    val admin = ComponentName(context, WallapersDeviceAdminReceiver::class.java)
    val policyManager = context.getSystemService(DevicePolicyManager::class.java)
    if (!policyManager.isProfileOwnerApp(context.packageName)) return false

    policyManager.setProfileName(admin, "Wallapers — отдельный профиль")
    val cloneRequestFilter = IntentFilter(ACTION_CLONE_APP).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
    }
    policyManager.clearCrossProfileIntentFilters(admin)
    policyManager.addCrossProfileIntentFilter(
        admin,
        cloneRequestFilter,
        DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
    )
    policyManager.setProfileEnabled(admin)
    return true
}
