package com.tdull.webdavviewer.app.util

import org.junit.Assert.*
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `formatFileSize returns correct format for bytes`() {
        assertEquals("0.0 B", FormatUtils.formatFileSize(0))
        assertEquals("512.0 B", FormatUtils.formatFileSize(512))
        assertEquals("1023.0 B", FormatUtils.formatFileSize(1023))
    }

    @Test
    fun `formatFileSize returns correct format for kilobytes`() {
        assertEquals("1.0 KB", FormatUtils.formatFileSize(1024))
        assertEquals("1.5 KB", FormatUtils.formatFileSize(1536))
        assertEquals("1023.0 KB", FormatUtils.formatFileSize(1024 * 1023))
    }

    @Test
    fun `formatFileSize returns correct format for megabytes`() {
        assertEquals("1.0 MB", FormatUtils.formatFileSize(1024 * 1024))
        assertEquals("512.0 MB", FormatUtils.formatFileSize(1024 * 1024 * 512))
    }

    @Test
    fun `formatFileSize returns correct format for gigabytes`() {
        assertEquals("1.0 GB", FormatUtils.formatFileSize(1024L * 1024 * 1024))
        assertEquals("2.5 GB", FormatUtils.formatFileSize(1024L * 1024 * 1024 * 5 / 2))
    }

    @Test
    fun `formatFileSize returns correct format for terabytes`() {
        assertEquals("1.0 TB", FormatUtils.formatFileSize(1024L * 1024 * 1024 * 1024))
    }

    @Test
    fun `formatFileSize handles negative values`() {
        assertEquals("未知", FormatUtils.formatFileSize(-1))
        assertEquals("未知", FormatUtils.formatFileSize(-100))
    }

    @Test
    fun `formatDate returns correct format`() {
        val timestamp = 1609452800000L // 2021-01-01 00:00:00 UTC
        val result = FormatUtils.formatDate(timestamp)
        assertNotNull(result)
        assertTrue(result.contains("-"))
        assertTrue(result.contains(":"))
    }

    @Test
    fun `formatDateTime returns correct format`() {
        val timestamp = 1609452800000L // 2021-01-01 00:00:00 UTC
        val result = FormatUtils.formatDateTime(timestamp)
        assertNotNull(result)
        assertTrue(result.contains("-"))
        assertTrue(result.contains(":"))
        assertTrue(result.contains(":")) // Should have seconds
    }
}
