package pl.mj.multicurrencyaccount.infrastructure

import org.springframework.stereotype.Component
import pl.mj.multicurrencyaccount.domain.*
import pl.mj.multicurrencyaccount.infrastructure.EventMapper.fromJson
import pl.mj.multicurrencyaccount.infrastructure.EventMapper.toJson
import java.time.Clock
import java.util.*

@Component
class AccountRepositoryImpl(private val eventsStore: EventsStore,
                            private val clock: Clock) : AccountRepository {

    override fun getById(id: String): Account {
        val events = eventsStore.findByAccountId(id)
                .map { storedEvent -> fromJson(storedEvent.payload) }
                .sortedWith(compareBy(DomainEvent::timestamp))
        return Account(id, events, clock)
    }

    override fun save(account: Account) {
        val events = account.pendingEvents
                .map { domainEvent -> StoredEvent(UUID.randomUUID(), account.id, toJson(domainEvent)) }
        eventsStore.saveAll(events)
        account.flushPendingEvents()
    }
}