package pl.mj.multicurrencyaccount.infrastructure

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.EVERYTHING
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Component
import pl.mj.multicurrencyaccount.domain.Account
import pl.mj.multicurrencyaccount.domain.AccountRepository
import pl.mj.multicurrencyaccount.domain.DomainEvent
import java.time.Clock
import java.util.*

@Component
class AccountRepositoryImpl(private val eventsStore: EventsStore,
                            private val clock: Clock) : AccountRepository {

    private val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .activateDefaultTyping(DefaultBaseTypeLimitingValidator(), EVERYTHING, JsonTypeInfo.As.PROPERTY)

    override fun getById(id: String): Account {
        val events = eventsStore.findByAccountId(id)
                .map(this::toDomainEvent)
                .sortedWith(compareBy(DomainEvent::timestamp))
        return Account(id, events, clock)
    }

    override fun save(account: Account) {
        val events = account.pendingEvents.map { event -> toStoredEvent(account.id, event) }
        eventsStore.saveAll(events)
        account.flushPendingEvents()
    }

    private fun toDomainEvent(storedEvent: StoredEvent): DomainEvent {
        return objectMapper.readValue(storedEvent.payload, DomainEvent::class.java)
    }

    private fun toStoredEvent(accountId: String, domainEvent: DomainEvent): StoredEvent {
        val payload = objectMapper.writeValueAsString(domainEvent);
        return StoredEvent(UUID.randomUUID(), accountId, payload)
    }
}