package pl.mj.multicurrencyaccount.domain

import java.math.BigDecimal
import java.time.Instant

sealed interface DomainEvent {val timestamp: Instant}

data class CurrencyExchangedEvent(override val timestamp: Instant,
                                  val amount: BigDecimal,
                                  val sourceCurrency: Currency,
                                  val targetCurrency: Currency,
                                  val rate: BigDecimal) : DomainEvent

data class AccountRegisteredEvent(override val timestamp: Instant,
                                  val owner: Owner,
                                  val initialDeposit: BigDecimal) : DomainEvent