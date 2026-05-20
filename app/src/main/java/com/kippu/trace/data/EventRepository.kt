package com.kippu.trace.data

import com.kippu.trace.model.DateEvent
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<DateEvent>> = eventDao.getAllEvents()

    suspend fun insert(event: DateEvent) {
        eventDao.insertEvent(event)
    }

    suspend fun delete(event: DateEvent) {
        eventDao.deleteEvent(event)
    }

    suspend fun getEventById(id: Long): DateEvent? {
        return eventDao.getEventById(id)
    }

    suspend fun getAllEventsOnce(): List<DateEvent> {
        return eventDao.getAllEventsOnce()
    }

    suspend fun deleteAllAndInsertAll(events: List<DateEvent>) {
        eventDao.deleteAllAndInsertAll(events)
    }
}
