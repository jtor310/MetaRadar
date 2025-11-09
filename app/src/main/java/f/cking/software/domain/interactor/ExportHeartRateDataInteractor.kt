package f.cking.software.domain.interactor

import android.content.Context
import android.net.Uri
import f.cking.software.data.repo.HeartRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportHeartRateDataInteractor(
    private val heartRateRepository: HeartRateRepository,
    private val context: Context
) {

    suspend fun execute(
        deviceAddress: String,
        fromTime: Long = 0,
        toTime: Long = Long.MAX_VALUE,
        outputUri: Uri
    ): Result {
        return withContext(Dispatchers.IO) {
            try {
                val readings = heartRateRepository.getReadingsByDevice(deviceAddress, fromTime, toTime)

                if (readings.isEmpty()) {
                    return@withContext Result.NoData
                }

                val csvContent = buildCsvContent(readings, deviceAddress)

                context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                    outputStream.flush()
                }

                Timber.tag(TAG).i("Exported ${readings.size} heart rate readings to CSV")
                Result.Success(readings.size)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to export heart rate data")
                Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun buildCsvContent(readings: List<f.cking.software.domain.model.HeartRateReading>, deviceAddress: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csv = StringBuilder()

        // Header
        csv.append("Timestamp,DateTime,DeviceAddress,HeartRate,ContactDetected,EnergyExpended,RRIntervals\n")

        // Data rows
        readings.forEach { reading ->
            csv.append("${reading.timestamp},")
            csv.append("${dateFormat.format(Date(reading.timestamp))},")
            csv.append("${reading.deviceAddress},")
            csv.append("${reading.heartRate},")
            csv.append("${reading.contactDetected ?: ""},")
            csv.append("${reading.energyExpended ?: ""},")
            csv.append("\"${reading.rrIntervals?.joinToString(";") ?: ""}\"\n")
        }

        return csv.toString()
    }

    sealed class Result {
        data class Success(val recordCount: Int) : Result()
        data class Error(val message: String) : Result()
        data object NoData : Result()
    }

    companion object {
        private const val TAG = "ExportHeartRateData"
    }
}
