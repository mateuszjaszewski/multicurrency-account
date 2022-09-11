package pl.mj.multicurrencyaccount.domain

import java.math.BigDecimal
import java.time.Instant

data class RegisterAccountCommand(val timestamp: Instant,
                                  val owner: Owner,
                                  val initialDeposit: BigDecimal)

data class ExchangeCurrencyCommand(val timestamp: Instant,
                                   val amount: BigDecimal,
                                   val sourceCurrency: Currency,
                                   val targetCurrency: Currency, val rate: BigDecimal)