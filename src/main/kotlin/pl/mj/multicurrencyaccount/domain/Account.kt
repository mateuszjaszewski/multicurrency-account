package pl.mj.multicurrencyaccount.domain

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

    fun buyCurrency(buy: BuyCurrencyCommand) {
        if (buy.amount * buy.rate > subAccountFor(Currency.PLN).balance) {
            throw InsufficientFoundsException(Currency.PLN)
        }
        applyAndAppend(CurrencyBoughtEvent(buy.timestamp, buy.currency, buy.amount, buy.rate))
    }

    fun sellCurrency(sell: SellCurrencyCommand) {
        if (sell.amount > subAccountFor(sell.currency).balance) {
            throw InsufficientFoundsException(sell.currency)
        }
        applyAndAppend(CurrencySoldEvent(sell.timestamp, sell.currency, sell.amount, sell.rate))
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
            is CurrencySoldEvent -> applyCurrencySoldEvent(event)
            is CurrencyBoughtEvent -> applyCurrencyBoughtEvent(event)
        }
    }

    private fun applyAccountRegisteredEvent(event: AccountRegisteredEvent) {
        this.status = Status.REGISTERED
        this.owner = event.owner
        subAccountFor(Currency.PLN).deposit(event.initialDeposit)
    }

    private fun applyCurrencySoldEvent(sold: CurrencySoldEvent) {
        subAccountFor(sold.currency).withdraw(sold.amount)
        subAccountFor(Currency.PLN).deposit(sold.amount * sold.rate)
    }

    private fun applyCurrencyBoughtEvent(bought: CurrencyBoughtEvent) {
        subAccountFor(Currency.PLN).withdraw(bought.amount * bought.rate)
        subAccountFor(bought.currency).deposit(bought.amount)
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
    class InsufficientFoundsException(currency: Currency) : DomainException("Insufficient founds on $currency sub account")
}