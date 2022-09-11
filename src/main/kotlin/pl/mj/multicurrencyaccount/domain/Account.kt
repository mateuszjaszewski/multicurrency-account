package pl.mj.multicurrencyaccount.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock

const val MIN_OWNER_AGE = 18

class Account (val id: String,
               val events: List<DomainEvent>,
               private val clock: Clock) {

    private enum class Status { NEW, REGISTERED }
    private var status: Status = Status.NEW

    var pendingEvents: List<DomainEvent> = emptyList()
        private set
    var subAccounts: List<SubAccount> = initSubAccountForEachCurrency()
        private set
    lateinit var owner: Owner
        private set

    init {
        events.forEach(this::apply)
    }

    fun balance(currency: Currency) = subAccountFor(currency).balance

    fun register(command: RegisterAccountCommand) {
        if (status == Status.REGISTERED) {
            throw CannotRegisterAccountException("Account already registered for owner with pesel ${owner.pesel}")
        }
        if (command.owner.age(clock) < MIN_OWNER_AGE) {
            throw CannotRegisterAccountException("Owner of account must be at least $MIN_OWNER_AGE years old")
        }
        applyAndAppend(AccountRegisteredEvent(command.timestamp, command.owner, command.initialDeposit))
    }

    fun exchangeCurrency(command: ExchangeCurrencyCommand) {
        if (command.amount > subAccountFor(command.sourceCurrency).balance) {
            throw InsufficientFoundsException(command.sourceCurrency)
        }
        applyAndAppend(CurrencyExchangedEvent(command.timestamp, command.amount,
                command.sourceCurrency, command.targetCurrency, command.rate))
    }

    fun isRegistered() = status == Status.REGISTERED

    fun flushPendingEvents() {
        pendingEvents = emptyList()
    }

    private fun applyAndAppend(event: DomainEvent) {
        apply(event)
        pendingEvents = pendingEvents + event
    }

    private fun apply(event: DomainEvent) {
        when (event) {
            is AccountRegisteredEvent -> applyAccountRegisteredEvent(event)
            is CurrencyExchangedEvent -> applyCurrencyExchangedEvent(event)
        }
    }

    private fun applyAccountRegisteredEvent(event: AccountRegisteredEvent) {
        this.status = Status.REGISTERED
        this.owner = event.owner
        subAccountFor(Currency.PLN).deposit(event.initialDeposit)
    }

    private fun applyCurrencyExchangedEvent(event: CurrencyExchangedEvent) {
        subAccountFor(event.sourceCurrency).withdraw(event.amount)
        subAccountFor(event.targetCurrency).deposit((event.amount * event.rate).setScale(2, RoundingMode.DOWN))
    }

    private fun subAccountFor(currency: Currency): SubAccount {
        return subAccounts.find { it.currency == currency } !!
    }

    private fun initSubAccountForEachCurrency(): List<SubAccount> {
        return Currency.values().map { currency -> SubAccount(currency, BigDecimal.ZERO) }
    }

    class SubAccount(val currency: Currency, var balance: BigDecimal) {
        fun deposit(amount: BigDecimal) { balance += amount }
        fun withdraw(amount: BigDecimal) { balance -= amount }
    }

    class CannotRegisterAccountException(message: String) : DomainException(message)
    class InsufficientFoundsException(currency: Currency) : DomainException("Insufficient founds on $currency sub-account")
}