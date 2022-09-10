package pl.mj.multicurrencyaccount.domain

import pl.mj.multicurrencyaccount.shared.Currency
import pl.mj.multicurrencyaccount.shared.Money
import java.math.BigDecimal
import java.time.Instant

class RegisterAccountCommand(val timestamp: Instant, val owner: Owner, val initialDeposit: Money)

class ExchangeMoneyCommand(val timestamp: Instant, val amount: BigDecimal,
                           val sourceCurrency: Currency, val targetCurrency: Currency,
                           val ratio: BigDecimal)