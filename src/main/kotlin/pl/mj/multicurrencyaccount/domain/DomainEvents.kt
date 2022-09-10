package pl.mj.multicurrencyaccount.domain

import pl.mj.multicurrencyaccount.shared.Money
import java.time.Instant

sealed class DomainEvent(val timestamp: Instant)

class MoneyExchangedEvent(timestamp: Instant, val from: Money, val to: Money)
    : DomainEvent(timestamp)

class AccountRegisteredEvent(timestamp: Instant, val owner: Owner, val initialDeposit: Money)
    : DomainEvent(timestamp)