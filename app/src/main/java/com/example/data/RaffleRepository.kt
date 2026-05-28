package com.example.data

import kotlinx.coroutines.flow.Flow

class RaffleRepository(private val raffleDao: RaffleDao) {
    val allRaffles: Flow<List<Raffle>> = raffleDao.getAllRaffles()

    fun getRaffleByIdFlow(id: Int): Flow<Raffle?> {
        return raffleDao.getRaffleByIdFlow(id)
    }

    suspend fun getRaffleById(id: Int): Raffle? {
        return raffleDao.getRaffleById(id)
    }

    fun getTicketsSoldForRaffle(raffleId: Int): Flow<List<TicketSold>> {
        return raffleDao.getTicketsSoldForRaffle(raffleId)
    }

    suspend fun insertRaffle(raffle: Raffle): Long {
        return raffleDao.insertRaffle(raffle)
    }

    suspend fun deleteRaffle(raffle: Raffle) {
        raffleDao.deleteRaffle(raffle)
    }

    suspend fun insertTicketSold(ticketSold: TicketSold): Long {
        return raffleDao.insertTicketSold(ticketSold)
    }

    suspend fun deleteTicketSold(ticketId: Int) {
        raffleDao.deleteTicketSold(ticketId)
    }

    suspend fun deleteTicketByNumber(raffleId: Int, number: Int) {
        raffleDao.deleteTicketByNumber(raffleId, number)
    }

    suspend fun updateTicketSold(ticketSold: TicketSold) {
        raffleDao.updateTicketSold(ticketSold)
    }
}
