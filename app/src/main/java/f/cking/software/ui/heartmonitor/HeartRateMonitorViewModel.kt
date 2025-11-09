package f.cking.software.ui.heartmonitor

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import f.cking.software.data.helpers.BleScannerHelper
import f.cking.software.data.repo.DevicesRepository
import f.cking.software.domain.interactor.ParseHeartRateMeasurement
import f.cking.software.domain.interactor.SaveHeartRateReadingInteractor
import f.cking.software.domain.model.DeviceData
import f.cking.software.domain.model.HeartRateReading
import f.cking.software.fromBase64
import f.cking.software.utils.navigation.BackCommand
import f.cking.software.utils.navigation.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class HeartRateMonitorViewModel(
    private val address: String,
    private val router: Router,
    private val devicesRepository: DevicesRepository,
    private val bleScannerHelper: BleScannerHelper,
    private val parseHeartRateMeasurement: ParseHeartRateMeasurement,
    private val saveHeartRateReadingInteractor: SaveHeartRateReadingInteractor,
) : ViewModel() {

    var deviceState: DeviceData? by mutableStateOf(null)
    var connectionStatus: ConnectionStatus by mutableStateOf(ConnectionStatus.DISCONNECTED)
    var heartRateData: HeartRateData by mutableStateOf(HeartRateData())
    var error: String? by mutableStateOf(null)

    private var connectionJob: Job? = null
    private var currentGatt: BluetoothGatt? = null
    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null

    sealed class ConnectionStatus {
        data object DISCONNECTED : ConnectionStatus()
        data object CONNECTING : ConnectionStatus()
        data class CONNECTED(val gatt: BluetoothGatt) : ConnectionStatus()
        data object MONITORING : ConnectionStatus()
        data object DISCONNECTING : ConnectionStatus()
        data class ERROR(val message: String) : ConnectionStatus()
    }

    data class HeartRateData(
        val currentHeartRate: Int? = null,
        val sensorContactDetected: Boolean? = null,
        val energyExpended: Int? = null,
        val rrIntervals: List<Int>? = null,
        val timestamp: Long = System.currentTimeMillis(),
        val history: List<HeartRateReading> = emptyList()
    )

    init {
        viewModelScope.launch {
            devicesRepository.observeDevice(address).collect { device ->
                deviceState = device
            }
        }
    }

    fun back() {
        router.navigate(BackCommand)
    }

    fun startMonitoring() {
        if (connectionStatus is ConnectionStatus.MONITORING) {
            Timber.tag(TAG).w("Already monitoring heart rate")
            return
        }

        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            bleScannerHelper.connectToDevice(address)
                .catch { e ->
                    Timber.tag(TAG).e(e, "Error connecting to device $address")
                    error = "Connection failed: ${e.message}"
                    connectionStatus = ConnectionStatus.ERROR(e.message ?: "Unknown error")
                }
                .collect { event ->
                    handleConnectionEvent(event)
                }
        }
    }

    fun stopMonitoring() {
        currentGatt?.let { gatt ->
            heartRateCharacteristic?.let { characteristic ->
                bleScannerHelper.enableCharacteristicNotification(gatt, characteristic, false)
            }
            bleScannerHelper.disconnect(gatt)
        }
        connectionJob?.cancel()
        connectionStatus = ConnectionStatus.DISCONNECTING
    }

    private fun handleConnectionEvent(event: BleScannerHelper.DeviceConnectResult) {
        when (event) {
            is BleScannerHelper.DeviceConnectResult.Connecting -> {
                Timber.tag(TAG).d("Connecting to device...")
                connectionStatus = ConnectionStatus.CONNECTING
                error = null
            }

            is BleScannerHelper.DeviceConnectResult.Connected -> {
                Timber.tag(TAG).d("Connected! Discovering services...")
                currentGatt = event.gatt
                connectionStatus = ConnectionStatus.CONNECTED(event.gatt)
                bleScannerHelper.discoverServices(event.gatt)
            }

            is BleScannerHelper.DeviceConnectResult.AvailableServices -> {
                Timber.tag(TAG).d("Services discovered, looking for Heart Rate Service...")
                val heartRateService = event.services.firstOrNull { service ->
                    service.uuid.toString().uppercase().contains("180D")
                }

                if (heartRateService == null) {
                    Timber.tag(TAG).e("Heart Rate Service not found")
                    error = "This device does not have a Heart Rate Service"
                    connectionStatus = ConnectionStatus.ERROR("No Heart Rate Service")
                    bleScannerHelper.disconnect(event.gatt)
                    return
                }

                val heartRateMeasurement = heartRateService.characteristics.firstOrNull { char ->
                    char.uuid.toString().uppercase().contains("2A37")
                }

                if (heartRateMeasurement == null) {
                    Timber.tag(TAG).e("Heart Rate Measurement characteristic not found")
                    error = "Heart Rate Measurement characteristic not found"
                    connectionStatus = ConnectionStatus.ERROR("No HR Measurement")
                    bleScannerHelper.disconnect(event.gatt)
                    return
                }

                heartRateCharacteristic = heartRateMeasurement

                // Enable notifications
                val enabled = bleScannerHelper.enableCharacteristicNotification(
                    event.gatt,
                    heartRateMeasurement,
                    true
                )

                if (enabled) {
                    Timber.tag(TAG).d("Heart rate notifications enabled successfully")
                    connectionStatus = ConnectionStatus.MONITORING
                    error = null
                } else {
                    Timber.tag(TAG).e("Failed to enable heart rate notifications")
                    error = "Failed to enable notifications"
                    connectionStatus = ConnectionStatus.ERROR("Notification failed")
                    bleScannerHelper.disconnect(event.gatt)
                }
            }

            is BleScannerHelper.DeviceConnectResult.CharacteristicNotification -> {
                if (event.characteristic.uuid.toString().uppercase().contains("2A37")) {
                    val data = parseHeartRateMeasurement.execute(event.valueEncoded64.fromBase64())
                    if (data != null) {
                        Timber.tag(TAG).d("Heart rate received: ${data.heartRate} BPM")

                        val timestamp = System.currentTimeMillis()

                        // Create domain model for database
                        val reading = HeartRateReading(
                            deviceAddress = address,
                            heartRate = data.heartRate,
                            timestamp = timestamp,
                            contactDetected = data.sensorContactDetected,
                            energyExpended = data.energyExpended,
                            rrIntervals = data.rrIntervals
                        )

                        // Save to database
                        viewModelScope.launch {
                            try {
                                saveHeartRateReadingInteractor.execute(reading)
                                Timber.tag(TAG).d("Heart rate saved to database")
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e, "Failed to save heart rate to database")
                            }
                        }

                        // Update UI state (for session history)
                        val newReading = HeartRateReading(
                            deviceAddress = address,
                            heartRate = data.heartRate,
                            timestamp = timestamp,
                            contactDetected = data.sensorContactDetected,
                            energyExpended = data.energyExpended,
                            rrIntervals = data.rrIntervals
                        )

                        // Keep last 100 readings for in-memory history
                        val updatedHistory = (heartRateData.history + newReading).takeLast(100)

                        heartRateData = HeartRateData(
                            currentHeartRate = data.heartRate,
                            sensorContactDetected = data.sensorContactDetected,
                            energyExpended = data.energyExpended,
                            rrIntervals = data.rrIntervals,
                            timestamp = timestamp,
                            history = updatedHistory
                        )
                    }
                }
            }

            is BleScannerHelper.DeviceConnectResult.Disconnected -> {
                Timber.tag(TAG).d("Disconnected from device")
                connectionStatus = ConnectionStatus.DISCONNECTED
                currentGatt = null
                heartRateCharacteristic = null
            }

            is BleScannerHelper.DeviceConnectResult.DisconnectedWithError -> {
                Timber.tag(TAG).e("Disconnected with error: ${event.errorCode}")
                error = "Connection error: ${event.errorCode}"
                connectionStatus = ConnectionStatus.ERROR("Disconnected with error")
                currentGatt = null
                heartRateCharacteristic = null
            }

            else -> {
                // Ignore other events
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }

    companion object {
        private const val TAG = "HeartRateMonitorVM"
    }
}
