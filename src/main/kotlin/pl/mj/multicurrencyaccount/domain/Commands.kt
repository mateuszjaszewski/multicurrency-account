package pl.mj.multicurrencyaccount.domain

import java.math.BigDecimal
import java.time.Instant

class RegisterAccountCommand(val timestamp: Instant, val owner: Owner, val initialDeposit: BigDecimal)

class ExchangeMoneyCommand(val timestamp: Instant, val amount: BigDecimal,
                           val sourceCurrency: Currency, val targetCurrency: Currency,
                           val ratio: BigDecimal)

class BuyCurrencyCommand(val timestamp: Instant, val amount: BigDecimal, val currency: Currency, val rate: BigDecimal)
class SellCurrencyCommand(val timestamp: Instant, val amount: BigDecimal, val currency: Currency, val rate: BigDecimal)