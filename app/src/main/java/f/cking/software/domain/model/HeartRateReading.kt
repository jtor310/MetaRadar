package f.cking.software.domain.model

data class HeartRateReading(
    val id: Long? = null,
    val deviceAddress: String,
    val heartRate: Int,
    val timestamp: Long,
    val contactDetected: Boolean?,
    val energyExpended: Int?,
    val rrIntervals: List<Int>?,
)
