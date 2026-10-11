package com.wallapers

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle

/** Selects only the user-consented managed-profile provisioning mode. */
class GetProvisioningModeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val managedProfileMode = DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE
        val allowedModes = intent.getIntArrayExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES)

        if (allowedModes != null && managedProfileMode !in allowedModes) {
            setResult(RESULT_CANCELED)
        } else {
            setResult(
                RESULT_OK,
                Intent().putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, managedProfileMode)
            )
        }
        finish()
    }
}
