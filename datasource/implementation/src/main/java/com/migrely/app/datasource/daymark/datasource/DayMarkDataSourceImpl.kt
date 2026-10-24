package com.migrely.app.datasource.daymark.datasource

import com.migrely.app.datasource.database.database
import com.migrely.app.datasource.daymark.dao.DayMarkDao
import com.migrely.app.datasource.daymark.mapper.DayMarkLocalMapper
import com.migrely.app.datasource.daymark.model.DayMarkDataModel
import java.time.Clock
import java.time.LocalDate

class DayMarkDataSourceImpl(
    private val dayMarkDao: DayMarkDao,
    private val dayMarkLocalMapper: DayMarkLocalMapper,
    private val clock: Clock = Clock.systemUTC()
) : DayMarkDataSource {
    override fun marksBetween(from: LocalDate, to: LocalDate): List<DayMarkDataModel> = database {
        dayMarkDao.between(dayMarkLocalMapper.toLocal(from), dayMarkLocalMapper.toLocal(to))
            .mapNotNull(dayMarkLocalMapper::toData)
    }

    override fun setPeriod(date: LocalDate, marked: Boolean) = database {
        dayMarkDao.update(dayMarkLocalMapper.toLocal(date), clock.millis()) {
            it.copy(period = marked)
        }
    }

    override fun setPainFree(date: LocalDate, marked: Boolean) = database {
        dayMarkDao.update(dayMarkLocalMapper.toLocal(date), clock.millis()) {
            it.copy(painFree = marked)
        }
    }
}
