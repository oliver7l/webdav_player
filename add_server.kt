import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tdull.webdavviewer.app.data.model.ServerConfig
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "webdav_config")

class AddServerScript {
    companion object {
        private val SERVERS_KEY = stringPreferencesKey("servers")
        private val ACTIVE_SERVER_KEY = stringPreferencesKey("active_server_id")
        
        @JvmStatic
        fun main(args: Array<String>) {
            // 模拟应用上下文
            val context = android.app.Application()
            
            // 创建服务器配置
            val serverConfig = ServerConfig(
                id = UUID.randomUUID().toString(),
                name = "My WebDAV Server",
                url = "http://115.175.3.95:9001/dav",
                username = "admin",
                password = "20098023616tl"
            )
            
            // 保存服务器配置
            val scope = kotlinx.coroutines.runBlocking {
                context.dataStore.edit { preferences ->
                    val serversJson = preferences[SERVERS_KEY] ?: "[]"
                    val servers = parseServerConfigs(serversJson).toMutableList()
                    
                    // 查找是否存在相同ID的配置
                    val existingIndex = servers.indexOfFirst { it.id == serverConfig.id }
                    if (existingIndex >= 0) {
                        servers[existingIndex] = serverConfig
                    } else {
                        servers.add(serverConfig)
                    }
                    
                    preferences[SERVERS_KEY] = serializeServerConfigs(servers)
                    // 设置为激活服务器
                    preferences[ACTIVE_SERVER_KEY] = serverConfig.id
                }
            }
            
            println("Server added successfully!")
        }
        
        private fun parseServerConfigs(json: String): List<ServerConfig> {
            return try {
                val jsonArray = JSONArray(json)
                (0 until jsonArray.length()).map { index ->
                    val jsonObject = jsonArray.getJSONObject(index)
                    ServerConfig(
                        id = jsonObject.optString("id", UUID.randomUUID().toString()),
                        name = jsonObject.optString("name", ""),
                        url = jsonObject.optString("url", ""),
                        username = jsonObject.optString("username", ""),
                        password = jsonObject.optString("password", "")
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        private fun serializeServerConfigs(configs: List<ServerConfig>): String {
            val jsonArray = JSONArray()
            configs.forEach { config ->
                val jsonObject = JSONObject().apply {
                    put("id", config.id)
                    put("name", config.name)
                    put("url", config.url)
                    put("username", config.username)
                    put("password", config.password)
                }
                jsonArray.put(jsonObject)
            }
            return jsonArray.toString()
        }
    }
}