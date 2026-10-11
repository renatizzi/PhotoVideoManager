package com.renatizzi.photovideomanager.domain.policy

import com.renatizzi.photovideomanager.domain.port.SyncPort

class DisabledSyncPort : SyncPort {
    override val isEnabled: Boolean = false
}
