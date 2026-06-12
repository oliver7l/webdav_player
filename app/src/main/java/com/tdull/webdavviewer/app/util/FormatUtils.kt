package com.tdull.webdavviewer.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    fun formatFileSize(size: Long): String {
        if (size < 0) return "未知"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var unitIndex = 0
        var fileSize = size.toDouble()
        
        while (fileSize >= 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", fileSize, units[unitIndex])
    }
    
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
}
