package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Raffle
import com.example.data.RaffleRepository
import com.example.data.TicketSold
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RaffleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RaffleRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RaffleRepository(database.raffleDao())
    }

    // List of all raffles
    val allRaffles: StateFlow<List<Raffle>> = repository.allRaffles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current active raffle ID
    private val _selectedRaffleId = MutableStateFlow<Int?>(null)
    val selectedRaffleId: StateFlow<Int?> = _selectedRaffleId.asStateFlow()

    // Active raffle entity
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRaffle: StateFlow<Raffle?> = _selectedRaffleId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getRaffleByIdFlow(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Sold tickets list for the active raffle
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeRaffleTickets: StateFlow<List<TicketSold>> = _selectedRaffleId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getTicketsSoldForRaffle(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selects active raffle
    fun selectRaffle(raffleId: Int?) {
        _selectedRaffleId.value = raffleId
    }

    // Creates new raffle
    fun createRaffle(
        title: String,
        totalNumbers: Int,
        description: String,
        ticketPrice: Double,
        prize: String,
        startFromZero: Boolean,
        onSuccess: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val raffle = Raffle(
                title = title.ifBlank { "Rifa Sin Nombre" },
                totalNumbers = if (totalNumbers <= 0) 100 else totalNumbers,
                description = description,
                ticketPrice = ticketPrice,
                prize = prize.ifBlank { "Premio Sorpresa" },
                startFromZero = startFromZero
            )
            val newId = repository.insertRaffle(raffle)
            _selectedRaffleId.value = newId.toInt()
            onSuccess(newId.toInt())
        }
    }

    // Deletes selected raffle
    fun deleteActiveRaffle() {
        val current = activeRaffle.value ?: return
        viewModelScope.launch {
            _selectedRaffleId.value = null
            repository.deleteRaffle(current)
        }
    }

    // Sells or reserves a ticket
    fun sellTicket(
        number: Int,
        clientName: String,
        clientPhone: String,
        clientEmail: String = "",
        status: String = "PENDING",
        notes: String = ""
    ) {
        val raffleId = _selectedRaffleId.value ?: return
        viewModelScope.launch {
            val ticket = TicketSold(
                raffleId = raffleId,
                number = number,
                clientName = clientName.ifBlank { "Cliente Anónimo" },
                clientPhone = clientPhone,
                clientEmail = clientEmail,
                status = status,
                notes = notes
            )
            repository.insertTicketSold(ticket)
        }
    }

    // Releases a ticket from being sold
    fun releaseTicketByNumber(number: Int) {
        val raffleId = _selectedRaffleId.value ?: return
        viewModelScope.launch {
            repository.deleteTicketByNumber(raffleId, number)
        }
    }

    // Updates a ticket's status (e.g. PAID vs PENDING)
    fun toggleTicketPaymentStatus(ticket: TicketSold) {
        viewModelScope.launch {
            val newStatus = if (ticket.status == "PAID") "PENDING" else "PAID"
            repository.updateTicketSold(ticket.copy(status = newStatus))
        }
    }

    // Helper to check if a specific number is sold and return its details
    fun getSoldTicket(number: Int): TicketSold? {
        return activeRaffleTickets.value.find { it.number == number }
    }
}
