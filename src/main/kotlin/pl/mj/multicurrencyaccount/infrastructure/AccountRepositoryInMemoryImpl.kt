package pl.mj.multicurrencyaccount.infrastructure

import org.springframework.stereotype.Component
import pl.mj.multicurrencyaccount.domain.Account
import pl.mj.multicurrencyaccount.domain.AccountRepository
import pl.mj.multicurrencyaccount.domain.DomainEvent
import java.time.Clock

@Component
class AccountRepositoryInMemoryImpl(val clock: Clock) : AccountRepository {

    private class StoredEvent(val accountId: String, val domainEvent: DomainEvent)

    private val storedEvents: MutableList<StoredEvent> = mutableListOf()

    override fun getById(id: String): Account {
        val events = storedEvents.filter { it.accountId == id }
                .map { it.domainEvent }
                .sortedWith(compareBy(DomainEvent::timestamp))
        return Account(id, events, clock)
    }

    override fun save(account: Account) {
        storedEvents.addAll(account.pendingEvents.map { StoredEvent(account.id, it) })
        account.flushPendingEvents()
    }
}