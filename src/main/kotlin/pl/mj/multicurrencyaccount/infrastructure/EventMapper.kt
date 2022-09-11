package pl.mj.multicurrencyaccount.infrastructure

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import pl.mj.multicurrencyaccount.domain.AccountRegisteredEvent
import pl.mj.multicurrencyaccount.domain.CurrencyExchangedEvent
import pl.mj.multicurrencyaccount.domain.DomainEvent

object EventMapper {
    private val objectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .addMixIn(DomainEvent::class.java, DomainEventMixIn::class.java)

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
            JsonSubTypes.Type(value = CurrencyExchangedEvent::class, name = "CurrencyExchangedEvent"),
            JsonSubTypes.Type(value = AccountRegisteredEvent::class, name = "AccountRegisteredEvent")
    )
    private class DomainEventMixIn

    fun toJson(event: DomainEvent): String = objectMapper.writeValueAsString(event)
    fun fromJson(json: String): DomainEvent = objectMapper.readValue(json, DomainEvent::class.java)
}