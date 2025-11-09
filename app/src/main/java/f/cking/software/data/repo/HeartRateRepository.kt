package f.cking.software.data.repo

import f.cking.software.data.database.AppDatabase
import f.cking.software.domain.model.HeartRateReading
import f.cking.software.domain.toData
import f.cking.software.domain.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HeartRateRepository(
    appDatabase: AppDatabase,
) {

    private val heartRateDao = appDatabase.heartRateReadingsDao()

    suspend fun saveReading(reading: HeartRateReading) {
        withContext(Dispatchers.IO) {
            heartRateDao.saveReading(reading.toData())
        }
    }

    suspend fun saveReadings(readings: List<HeartRateReading>) {
        withContext(Dispatchers.IO) {
            heartRateDao.saveReadings(readings.map { it.toData() })
        }
    }

    suspend fun getReadingsByDevice(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): List<HeartRateReading> {
        return withContext(Dispatchers.IO) {
            heartRateDao.getReadingsByDevice(deviceAddress, fromTime, toTime)
                .map { it.toDomain() }
        }
    }

    fun observeReadingsByDevice(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Flow<List<HeartRateReading>> {
        return heartRateDao.observeReadingsByDevice(deviceAddress, fromTime, toTime)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getLastNReadings(deviceAddress: String, limit: Int): List<HeartRateReading> {
        return withContext(Dispatchers.IO) {
            heartRateDao.getLastNReadings(deviceAddress, limit)
                .map { it.toDomain() }
        }
    }

    suspend fun getReadingsCount(deviceAddress: String): Int {
        return withContext(Dispatchers.IO) {
            heartRateDao.getReadingsCount(deviceAddress)
        }
    }

    suspend fun getAverageHeartRate(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Double? {
        return withContext(Dispatchers.IO) {
            heartRateDao.getAverageHeartRate(deviceAddress, fromTime, toTime)
        }
    }

    suspend fun getMinHeartRate(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Int? {
        return withContext(Dispatchers.IO) {
            heartRateDao.getMinHeartRate(deviceAddress, fromTime, toTime)
        }
    }

    suspend fun getMaxHeartRate(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Int? {
        return withContext(Dispatchers.IO) {
            heartRateDao.getMaxHeartRate(deviceAddress, fromTime, toTime)
        }
    }

    suspend fun deleteReadingsByDevice(deviceAddress: String) {
        withContext(Dispatchers.IO) {
            heartRateDao.deleteReadingsByDevice(deviceAddress)
        }
    }

    suspend fun deleteReadingsOlderThan(beforeTime: Long) {
        withContext(Dispatchers.IO) {
            heartRateDao.deleteReadingsOlderThan(beforeTime)
        }
    }

    suspend fun deleteAllReadings() {
        withContext(Dispatchers.IO) {
            heartRateDao.deleteAllReadings()
        }
    }
}
