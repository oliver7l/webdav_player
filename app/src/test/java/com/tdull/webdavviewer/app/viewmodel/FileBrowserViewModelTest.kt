package com.tdull.webdavviewer.app.viewmodel

import android.app.Application
import app.cash.turbine.test
import com.tdull.webdavviewer.app.data.model.ServerConfig
import com.tdull.webdavviewer.app.data.model.WebDAVException
import com.tdull.webdavviewer.app.data.model.WebDAVResource
import com.tdull.webdavviewer.app.data.repository.ConfigRepository
import com.tdull.webdavviewer.app.data.repository.FavoritesRepository
import com.tdull.webdavviewer.app.data.repository.PlaylistRepository
import com.tdull.webdavviewer.app.data.repository.QuickAccessRepository
import com.tdull.webdavviewer.app.data.repository.WebDAVRepository
import com.tdull.webdavviewer.app.data.repository.DirectoryHistoryRepository
import com.tdull.webdavviewer.app.data.remote.ConnectionManager
import com.tdull.webdavviewer.app.util.NetworkMonitor
import com.tdull.webdavviewer.app.util.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * FileBrowserViewModel 单元测试
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class FileBrowserViewModelTest {

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockWebDavRepository: WebDAVRepository

    @Mock
    private lateinit var mockConfigRepository: ConfigRepository

    @Mock
    private lateinit var mockNetworkMonitor: NetworkMonitor

    @Mock
    private lateinit var mockFavoritesRepository: FavoritesRepository

    @Mock
    private lateinit var mockPlaylistRepository: PlaylistRepository

    @Mock
    private lateinit var mockQuickAccessRepository: QuickAccessRepository

    @Mock
    private lateinit var mockDirectoryHistoryRepository: DirectoryHistoryRepository

    @Mock
    private lateinit var mockConnectionManager: ConnectionManager

    private lateinit var viewModel: FileBrowserViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // 默认配置
        whenever(mockConfigRepository.servers).thenReturn(flowOf(emptyList()))
        whenever(mockConfigRepository.activeServer).thenReturn(flowOf(null))
        whenever(mockNetworkMonitor.networkStatus).thenReturn(flowOf(NetworkStatus(isAvailable = true)))
        whenever(mockNetworkMonitor.isNetworkAvailable()).thenReturn(true)
        whenever(mockFavoritesRepository.favorites).thenReturn(flowOf(emptyList()))
        whenever(mockPlaylistRepository.playlists).thenReturn(flowOf(emptyList()))
        whenever(mockQuickAccessRepository.quickAccessItems).thenReturn(flowOf(emptyList()))
        val mockConnectionState = MutableStateFlow(com.tdull.webdavviewer.app.data.remote.ConnectionState())
        whenever(mockConnectionManager.connectionState).thenReturn(mockConnectionState)

        viewModel = FileBrowserViewModel(
            application = mockApplication,
            webDavRepository = mockWebDavRepository,
            configRepository = mockConfigRepository,
            networkMonitor = mockNetworkMonitor,
            favoritesRepository = mockFavoritesRepository,
            playlistRepository = mockPlaylistRepository,
            quickAccessRepository = mockQuickAccessRepository,
            directoryHistoryRepository = mockDirectoryHistoryRepository,
            connectionManager = mockConnectionManager
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ========== 初始状态测试 ==========

    @Test
    fun `initial state is correct`() = runTest {
        val initialState = viewModel.uiState.value
        
        assertFalse(initialState.isLoading)
        assertTrue(initialState.files.isEmpty())
        assertNull(initialState.error)
        assertNull(initialState.errorInfo)
        assertFalse(initialState.isConnected)
        assertNull(initialState.currentServer)
        assertTrue(initialState.isNetworkAvailable)
    }

    @Test
    fun `initial path is root`() = runTest {
        assertEquals("/", viewModel.currentPath.value)
    }

    // ========== selectServer 测试 ==========

    @Test
    fun `selectServer updates currentServer and connects`() = runTest {
        val config = ServerConfig(
            id = "test-id",
            name = "Test Server",
            url = "https://example.com"
        )
        val files = listOf(
            WebDAVResource(path = "/folder", name = "folder", isDirectory = true),
            WebDAVResource(path = "/file.txt", name = "file.txt", isDirectory = false)
        )

        whenever(mockConnectionManager.connect(config)).thenReturn(true)
        whenever(mockWebDavRepository.listFiles("/")).thenReturn(Result.success(files))

        viewModel.selectServer(config)

        viewModel.uiState.test {
            val finalState = awaitItem()
            assertEquals(config, finalState.currentServer)
            assertTrue(finalState.isConnected)
            assertEquals(2, finalState.files.size)
            assertNull(finalState.error)
        }
    }

    @Test
    fun `selectServer handles connection failure`() = runTest {
        val config = ServerConfig(
            name = "Test Server",
            url = "https://example.com"
        )

        whenever(mockConnectionManager.connect(config)).thenReturn(false)

        viewModel.selectServer(config)

        viewModel.uiState.test {
            val finalState = awaitItem()
            assertEquals(config, finalState.currentServer)
            assertFalse(finalState.isConnected)
            assertNotNull(finalState.error)
            assertNotNull(finalState.errorInfo)
        }
    }

    @Test
    fun `selectServer shows error when network unavailable`() = runTest {
        val config = ServerConfig(
            name = "Test Server",
            url = "https://example.com"
        )

        whenever(mockNetworkMonitor.isNetworkAvailable()).thenReturn(false)

        viewModel.selectServer(config)

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isConnected)
        assertNotNull(finalState.error)
    }

    // ========== navigateTo 测试 ==========

    @Test
    fun `navigateTo updates currentPath and loads files`() = runTest {
        val files = listOf(
            WebDAVResource(path = "/subfolder/file.txt", name = "file.txt", isDirectory = false)
        )
        whenever(mockWebDavRepository.listFiles("/subfolder")).thenReturn(Result.success(files))
        whenever(mockWebDavRepository.listFiles("/"))
            .thenReturn(Result.success(emptyList()))

        // 先连接服务器
        val config = ServerConfig(name = "Test", url = "https://example.com")
        whenever(mockConnectionManager.connect(config)).thenReturn(true)
        whenever(mockConnectionManager.isConnectedToServer(any())).thenReturn(true)
        viewModel.selectServer(config)

        viewModel.navigateTo("/subfolder")

        assertEquals("/subfolder", viewModel.currentPath.value)
        verify(mockWebDavRepository).listFiles("/subfolder")
    }

    // ========== navigateUp 测试 ==========

    @Test
    fun `navigateUp returns to previous path`() = runTest {
        whenever(mockWebDavRepository.listFiles(any())).thenReturn(Result.success(emptyList()))

        // 先连接服务器
        val config = ServerConfig(name = "Test", url = "https://example.com")
        whenever(mockConnectionManager.connect(config)).thenReturn(true)
        whenever(mockConnectionManager.isConnectedToServer(any())).thenReturn(true)
        viewModel.selectServer(config)

        // 导航到子目录
        viewModel.navigateTo("/subfolder")
        assertEquals("/subfolder", viewModel.currentPath.value)

        // 返回上级
        viewModel.navigateUp()
        assertEquals("/", viewModel.currentPath.value)
    }

    @Test
    fun `navigateUp stays at root when already at root`() = runTest {
        whenever(mockWebDavRepository.listFiles(any())).thenReturn(Result.success(emptyList()))

        val config = ServerConfig(name = "Test", url = "https://example.com")
        whenever(mockConnectionManager.connect(config)).thenReturn(true)
        whenever(mockConnectionManager.isConnectedToServer(any())).thenReturn(true)
        viewModel.selectServer(config)

        viewModel.navigateUp()

        assertEquals("/", viewModel.currentPath.value)
    }

    // ========== refresh 测试 ==========

    @Test
    fun `refresh reloads current directory`() = runTest {
        whenever(mockWebDavRepository.listFiles("/"))
            .thenReturn(Result.success(emptyList()))
        val config = ServerConfig(name = "Test", url = "https://example.com")
        whenever(mockConnectionManager.connect(config)).thenReturn(true)
        whenever(mockConnectionManager.isConnectedToServer(any())).thenReturn(true)
        viewModel.selectServer(config)

        viewModel.refresh()

        verify(mockWebDavRepository, times(2)).listFiles("/")
    }

    // ========== getStreamUrl 测试 ==========

    @Test
    fun `getStreamUrl returns correct URL`() {
        val expectedUrl = "https://example.com/video.mp4"
        whenever(mockWebDavRepository.getStreamUrl("/video.mp4")).thenReturn(expectedUrl)

        val result = viewModel.getStreamUrl("/video.mp4")

        assertEquals(expectedUrl, result)
    }

    // ========== clearError 测试 ==========

    @Test
    fun `clearError removes error from state`() = runTest {
        val config = ServerConfig(name = "Test", url = "https://example.com")
        whenever(mockConnectionManager.connect(config)).thenReturn(false)

        viewModel.selectServer(config)
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.errorInfo)
    }
}
