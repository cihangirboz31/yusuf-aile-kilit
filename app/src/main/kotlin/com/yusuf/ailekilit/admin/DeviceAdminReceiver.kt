package com.yusuf.ailekilit.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Aile Kilit: Cihaz yöneticisi aktif edildi.", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Aile Kilit: Cihaz yöneticisi devre dışı bırakıldı!", Toast.LENGTH_LONG).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Ebeveyn kilidi devre dışı bırakılacak. Devam etmek için PIN gereklidir."
    }
}
