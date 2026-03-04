package com.tdull.webdavviewer.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

/**
 * 主屏幕容器
 * 包含导航栏的通用容器，用于主要功能页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContainer(
    navController: NavHostController,
    currentRoute: String,
    title: String,
    topBar: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = topBar ?: {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            AppNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            content()
        }
    }
}
