package pl.mj.multicurrencyaccount.domain

import java.math.BigDecimal
import java.time.Instant

sealed class DomainEvent(val timestamp: Instant)
class CurrencySoldEvent(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal) : DomainEvent(timestamp)
class CurrencyBoughtEvent(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal) : DomainEvent(timestamp)
class AccountRegisteredEvent(timestamp: Instant, val owner: Owner, val initialDeposit: BigDecimal) : DomainEvent(timestamp)