package com.kippu.trace.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kippu.trace.data.AppDatabase
import com.kippu.trace.data.EventRepository
import com.kippu.trace.model.DateEvent
import com.kippu.trace.utils.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository
    val allEvents: StateFlow<List<DateEvent>>

    init {
        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        allEvents = repository.allEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )
    }

    fun addEvent(event: DateEvent) {
        viewModelScope.launch {
            repository.insert(event)
        }
    }

    fun deleteEvent(event: DateEvent) {
        viewModelScope.launch {
            repository.delete(event)
        }
    }

    fun exportBackup(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val events = withContext(Dispatchers.IO) {
                    repository.getAllEventsOnce()
                }
                withContext(Dispatchers.IO) {
                    BackupManager.exportToZip(getApplication(), events, uri).getOrThrow()
                }
                onResult(true, "数据备份成功")
            } catch (e: Exception) {
                onResult(false, "备份失败: ${e.localizedMessage ?: "未知错误"}")
            }
        }
    }

    fun importBackup(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val events = withContext(Dispatchers.IO) {
                    BackupManager.importFromZip(getApplication(), uri).getOrThrow()
                }
                withContext(Dispatchers.IO) {
                    repository.deleteAllAndInsertAll(events)
                }
                onResult(true, "数据恢复成功，已导入 ${events.size} 条记录")
            } catch (e: Exception) {
                onResult(false, "恢复失败: ${e.localizedMessage ?: "未知错误"}")
            }
        }
    }
}
