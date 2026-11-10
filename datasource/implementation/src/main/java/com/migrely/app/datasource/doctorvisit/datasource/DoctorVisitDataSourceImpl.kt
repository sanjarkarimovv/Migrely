package com.migrely.app.datasource.doctorvisit.datasource

import com.migrely.app.datasource.database.database
import com.migrely.app.datasource.doctorvisit.dao.DoctorVisitDao
import com.migrely.app.datasource.doctorvisit.mapper.DoctorVisitLocalMapper
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitDataModel
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitsDataModel
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class DoctorVisitDataSourceImpl(
    private val doctorVisitDao: DoctorVisitDao,
    private val doctorVisitLocalMapper: DoctorVisitLocalMapper,
    private val newId: () -> String = { UUID.randomUUID().toString() }
) : DoctorVisitDataSource {
    override fun visits(today: LocalDate): DoctorVisitsDataModel = database {
        val day = doctorVisitLocalMapper.toLocal(today)
        DoctorVisitsDataModel(
            next = doctorVisitDao.next(day)?.let(doctorVisitLocalMapper::toData),
            last = doctorVisitDao.lastDate(day)?.let(doctorVisitLocalMapper::toDate)
        )
    }

    override fun setNextVisit(date: LocalDate, today: LocalDate) =
        restoreNextVisit(DoctorVisitDataModel(newId(), date), today)

    override fun removeNextVisit(today: LocalDate): DoctorVisitDataModel? = database {
        doctorVisitDao.removeUpcoming(doctorVisitLocalMapper.toLocal(today))
            ?.let(doctorVisitLocalMapper::toData)
    }

    override fun restoreNextVisit(visit: DoctorVisitDataModel, today: LocalDate) = database {
        doctorVisitDao.replaceUpcoming(
            doctorVisitLocalMapper.toLocal(visit),
            doctorVisitLocalMapper.toLocal(today)
        )
    }

    override fun anythingLoggedBetween(
        from: Instant,
        to: Instant,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Boolean = database {
        doctorVisitDao.anythingLogged(
            from.toEpochMilli(),
            to.toEpochMilli(),
            doctorVisitLocalMapper.toLocal(fromDate),
            doctorVisitLocalMapper.toLocal(toDate)
        )
    }
}
