package pl.mj.multicurrencyaccount.domain

import java.math.BigDecimal
import java.time.Instant

sealed interface DomainEvent {val timestamp: Instant}
data class CurrencySoldEvent(override val timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal) : DomainEvent
data class CurrencyBoughtEvent(override val timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal) : DomainEvent
data class AccountRegisteredEvent(override val timestamp: Instant, val owner: Owner, val initialDeposit: BigDecimal) : DomainEvent