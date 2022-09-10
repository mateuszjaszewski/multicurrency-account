package pl.mj.multicurrencyaccount.domain

import pl.mj.multicurrencyaccount.shared.Currency

sealed class DomainException(override val message: String) : RuntimeException(message)

class CannotRegisterAccountException(message: String)
    : DomainException(message)

class InsufficientFoundsException(currency: Currency)
    : DomainException("Insufficient founds on $currency sub account")

class InvalidPeselException(pesel: String)
    : DomainException("Pesel $pesel is invalid")