package pl.mj.multicurrencyaccount.domain

import pl.mj.multicurrencyaccount.shared.Currency
import pl.mj.multicurrencyaccount.shared.Money
import java.math.BigDecimal
import java.time.Clock

const val MIN_OWNER_AGE = 18

class Account (val id: String,
               val events: List<DomainEvent>,
               private val clock: Clock) {

    private enum class Status { NEW, REGISTERED }
    private var status: Status = Status.NEW

    var pendingEvents: List<DomainEvent> = emptyList()
        private set
    var subAccounts: List<SubAccount> = listOf(SubAccount(Currency.PLN), SubAccount(Currency.USD))
        private set
    lateinit var owner: Owner
        private set

    init {
        events.forEach(this::apply)
    }

    fun register(command: RegisterAccountCommand) {
        if (status == Status.REGISTERED) {
            throw CannotRegisterAccountException("Account already registered")
        }
        if (command.owner.age(clock) < MIN_OWNER_AGE) {
            throw CannotRegisterAccountException("Owner of account must be at least $MIN_OWNER_AGE years old")
        }
        if (command.initialDeposit.currency != Currency.PLN) {
            throw CannotRegisterAccountException("Initial deposit is only accepted in PLN currency")
        }
        applyAndAppend(AccountRegisteredEvent(command.timestamp, command.owner, command.initialDeposit))
    }

    fun exchangeMoney(command: ExchangeMoneyCommand) {
        if (subAccountFor(command.sourceCurrency).balance < command.amount) {
            throw InsufficientFoundsException(command.sourceCurrency)
        }
        val from = Money(command.sourceCurrency, command.amount)
        val to = Money(command.targetCurrency, command.amount)
        applyAndAppend(MoneyExchangedEvent(command.timestamp, from, to))
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
            is AccountRegisteredEvent -> applyAccountRegistered(event)
            is MoneyExchangedEvent -> applyMoneyExchanged(event)
        }
    }

    private fun applyAccountRegistered(event: AccountRegisteredEvent) {
        this.status = Status.REGISTERED
        this.owner = event.owner
        subAccountFor(event.initialDeposit.currency).deposit(event.initialDeposit.amount)
    }

    private fun applyMoneyExchanged(event: MoneyExchangedEvent) {
        subAccountFor(event.from.currency).withdraw(event.from.amount)
        subAccountFor(event.to.currency).deposit(event.to.amount)
    }

    private fun subAccountFor(currency: Currency): SubAccount {
        return subAccounts.find { it.currency == currency } !! // TODO
    }
}

class SubAccount(val currency: Currency, var balance: BigDecimal) {
    constructor (currency: Currency): this(currency, BigDecimal.ZERO)
    fun deposit(amount: BigDecimal) { balance += amount }
    fun withdraw(amount: BigDecimal) { balance -= amount }
}