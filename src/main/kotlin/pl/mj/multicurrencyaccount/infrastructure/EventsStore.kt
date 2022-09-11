package pl.mj.multicurrencyaccount.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EventsStore : JpaRepository<StoredEvent, UUID> {
    fun findByAccountId(accountId: String): List<StoredEvent>
}