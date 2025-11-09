package f.cking.software.domain.interactor

import f.cking.software.data.repo.HeartRateRepository
import f.cking.software.domain.model.HeartRateReading
import kotlinx.coroutines.flow.Flow

class GetHeartRateHistoryInteractor(
    private val heartRateRepository: HeartRateRepository
) {

    suspend fun execute(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): List<HeartRateReading> {
        return heartRateRepository.getReadingsByDevice(deviceAddress, fromTime, toTime)
    }

    fun observe(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): Flow<List<HeartRateReading>> {
        return heartRateRepository.observeReadingsByDevice(deviceAddress, fromTime, toTime)
    }

    suspend fun getLastN(deviceAddress: String, limit: Int): List<HeartRateReading> {
        return heartRateRepository.getLastNReadings(deviceAddress, limit)
    }

    suspend fun getStatistics(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE
    ): HeartRateStatistics {
        val average = heartRateRepository.getAverageHeartRate(deviceAddress, fromTime, toTime)
        val min = heartRateRepository.getMinHeartRate(deviceAddress, fromTime, toTime)
        val max = heartRateRepository.getMaxHeartRate(deviceAddress, fromTime, toTime)
        val count = heartRateRepository.getReadingsCount(deviceAddress)

        return HeartRateStatistics(
            average = average,
            min = min,
            max = max,
            count = count
        )
    }

    data class HeartRateStatistics(
        val average: Double?,
        val min: Int?,
        val max: Int?,
        val count: Int
    )
}
