package pl.mj.multicurrencyaccount.domain

import java.math.BigDecimal
import java.time.Instant

data class RegisterAccountCommand(val timestamp: Instant, val owner: Owner, val initialDeposit: BigDecimal)
data class BuyCurrencyCommand(val timestamp: Instant, val amount: BigDecimal, val currency: Currency, val rate: BigDecimal)
data class SellCurrencyCommand(val timestamp: Instant, val amount: BigDecimal, val currency: Currency, val rate: BigDecimal)