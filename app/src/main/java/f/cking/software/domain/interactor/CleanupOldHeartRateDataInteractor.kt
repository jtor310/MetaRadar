package f.cking.software.domain.interactor

import f.cking.software.data.repo.HeartRateRepository
import f.cking.software.domain.model.HeartRateAlertSettings
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CleanupOldHeartRateDataInteractor(
    private val heartRateRepository: HeartRateRepository
) {

    suspend fun execute(settings: HeartRateAlertSettings) {
        if (settings.dataRetentionDays <= 0) {
            Timber.tag(TAG).d("Data retention disabled, skipping cleanup")
            return
        }

        try {
            val retentionMillis = TimeUnit.DAYS.toMillis(settings.dataRetentionDays.toLong())
            val cutoffTime = System.currentTimeMillis() - retentionMillis

            Timber.tag(TAG).i("Cleaning up heart rate data older than ${settings.dataRetentionDays} days (before timestamp: $cutoffTime)")

            heartRateRepository.deleteReadingsOlderThan(cutoffTime)

            Timber.tag(TAG).i("Cleanup completed successfully")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to cleanup old heart rate data")
        }
    }

    companion object {
        private const val TAG = "CleanupOldHeartRateData"
    }
}
