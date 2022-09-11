package pl.mj.multicurrencyaccount.domain

sealed class DomainException(override val message: String) : RuntimeException(message)
