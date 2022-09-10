package pl.mj.multicurrencyaccount.shared

import java.math.BigDecimal

data class Money(val currency: Currency, val amount: BigDecimal)