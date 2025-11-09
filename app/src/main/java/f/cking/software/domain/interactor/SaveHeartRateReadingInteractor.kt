package f.cking.software.domain.interactor

import f.cking.software.data.repo.HeartRateRepository
import f.cking.software.domain.model.HeartRateReading

class SaveHeartRateReadingInteractor(
    private val heartRateRepository: HeartRateRepository
) {

    suspend fun execute(reading: HeartRateReading) {
        heartRateRepository.saveReading(reading)
    }

    suspend fun executeBatch(readings: List<HeartRateReading>) {
        heartRateRepository.saveReadings(readings)
    }
}
