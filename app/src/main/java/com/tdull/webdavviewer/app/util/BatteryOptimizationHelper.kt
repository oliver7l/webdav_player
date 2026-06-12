package com.tdull.webdavviewer.app.util

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

enum class ManufacturerType {
    XIAOMI,
    HUAWEI,
    OPPO,
    VIVO,
    SAMSUNG,
    ONEPLUS,
    MEIZU,
    OTHER
}

object BatteryOptimizationHelper {
    
    fun getManufacturer(): ManufacturerType {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> ManufacturerType.XIAOMI
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> ManufacturerType.HUAWEI
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> ManufacturerType.OPPO
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> ManufacturerType.VIVO
            manufacturer.contains("samsung") -> ManufacturerType.SAMSUNG
            manufacturer.contains("oneplus") -> ManufacturerType.ONEPLUS
            manufacturer.contains("meizu") -> ManufacturerType.MEIZU
            else -> ManufacturerType.OTHER
        }
    }
    
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    
    fun requestIgnoreBatteryOptimizations(activity: Activity, requestCode: Int = 1001) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            openBatterySettings(activity)
        }
    }
    
    fun openBatterySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }
    
    fun openManufacturerSettings(context: Context): Boolean {
        val manufacturer = getManufacturer()
        return when (manufacturer) {
            ManufacturerType.XIAOMI -> openXiaomiSettings(context)
            ManufacturerType.HUAWEI -> openHuaweiSettings(context)
            ManufacturerType.OPPO -> openOppoSettings(context)
            ManufacturerType.VIVO -> openVivoSettings(context)
            ManufacturerType.SAMSUNG -> openSamsungSettings(context)
            ManufacturerType.ONEPLUS -> openOnePlusSettings(context)
            ManufacturerType.MEIZU -> openMeizuSettings(context)
            else -> {
                openBatterySettings(context)
                true
            }
        }
    }
    
    private fun openXiaomiSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.appmanager.ApplicationsDetailsActivity"
                    )
                    putExtra("package_name", context.packageName)
                }
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                openBatterySettings(context)
                true
            }
        }
    }
    
    private fun openHuaweiSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
                }
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                openBatterySettings(context)
                true
            }
        }
    }
    
    private fun openOppoSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.oppo.safe",
                        "com.oppo.safe.permission.startup.StartupAppListActivity"
                    )
                }
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                openBatterySettings(context)
                true
            }
        }
    }
    
    private fun openVivoSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                    )
                }
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                openBatterySettings(context)
                true
            }
        }
    }
    
    private fun openSamsungSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openBatterySettings(context)
            true
        }
    }
    
    private fun openOnePlusSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openBatterySettings(context)
            true
        }
    }
    
    private fun openMeizuSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.meizu.safe",
                    "com.meizu.safe.security.SHOW_APPSEC"
                )
                putExtra("packageName", context.packageName)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openBatterySettings(context)
            true
        }
    }
    
    fun getManufacturerDisplayName(manufacturer: ManufacturerType): String {
        return when (manufacturer) {
            ManufacturerType.XIAOMI -> "小米"
            ManufacturerType.HUAWEI -> "华为"
            ManufacturerType.OPPO -> "OPPO"
            ManufacturerType.VIVO -> "vivo"
            ManufacturerType.SAMSUNG -> "三星"
            ManufacturerType.ONEPLUS -> "一加"
            ManufacturerType.MEIZU -> "魅族"
            ManufacturerType.OTHER -> "其他"
        }
    }
    
    fun getManufacturerSettingsHint(manufacturer: ManufacturerType): String {
        return when (manufacturer) {
            ManufacturerType.XIAOMI -> "请在「安全中心 -> 应用管理 -> 权限 -> 自启动管理」中允许本应用自启动"
            ManufacturerType.HUAWEI -> "请在「手机管家 -> 应用启动管理」中允许本应用后台活动"
            ManufacturerType.OPPO -> "请在「手机管家 -> 应用管理 -> 自启动管理」中允许本应用自启动"
            ManufacturerType.VIVO -> "请在「i管家 -> 应用管理 -> 自启动」中允许本应用自启动"
            ManufacturerType.SAMSUNG -> "请在「设备维护 -> 电池 -> 未监视的应用」中添加本应用"
            ManufacturerType.ONEPLUS -> "请在「设置 -> 应用 -> 应用启动管理」中允许本应用后台运行"
            ManufacturerType.MEIZU -> "请在「手机管家 -> 权限管理 -> 后台管理」中允许本应用后台运行"
            ManufacturerType.OTHER -> "请在系统设置中将本应用加入电池优化白名单"
        }
    }
}
