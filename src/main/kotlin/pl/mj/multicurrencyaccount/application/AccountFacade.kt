package pl.mj.multicurrencyaccount.application

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pl.mj.multicurrencyaccount.application.AccountController.*
import pl.mj.multicurrencyaccount.domain.*
import java.time.Clock
import java.time.Instant

@Component
class AccountFacade(val accountRepository: AccountRepository,
                    val currencyRatioProvider: CurrencyRatioProvider,
                    val clock: Clock) {

    @Transactional
    fun registerAccount(request: CreateAccountRequest) {
        val account = accountRepository.getById(request.owner.pesel)
        val owner = Owner(Pesel(request.owner.pesel), request.owner.firstName, request.owner.lastName)
        account.register(RegisterAccountCommand(now(), owner, request.initialDeposit))
        accountRepository.save(account)
    }

    @Transactional
    fun exchangeMoney(pesel: String, request: MoneyExchangeRequest) {
        val account = accountRepository.getById(pesel)
        val ratio = currencyRatioProvider.ratio(request.sourceCurrency, request.targetCurrency)
        val command = ExchangeMoneyCommand(now(), request.amount, request.sourceCurrency, request.targetCurrency, ratio)
        account.exchangeMoney(command)
        accountRepository.save(account)
    }

    @Transactional(readOnly = true)
    fun accountDetails(pesel: String): AccountDetailsResponse {
        val account = findRegisteredAccount(pesel)
        val owner = account.owner
        return AccountDetailsResponse(
                OwnerDto(owner.pesel.toString(), owner.firstName, owner.lastName),
                account.subAccounts.map { SubAccountDto(it.currency, it.balance) }
        )
    }

    @Transactional(readOnly = true)
    fun accountTransactions(pesel: String): AccountTransactionsResponse {
        val account = findRegisteredAccount(pesel)
        val transactions = account.events.sortedWith(compareBy(DomainEvent::timestamp).reversed()).map { event ->
            when (event) {
                is AccountRegisteredEvent -> InitialDepositTransactionDto(event.timestamp, event.initialDeposit)
                is MoneyExchangedEvent -> MoneyExchangeTransactionDto(event.timestamp, event.from, event.to)
            }
        }
        return AccountTransactionsResponse(transactions)
    }

    private fun findRegisteredAccount(pesel: String): Account {
        val account = accountRepository.getById(pesel)
        if (!account.isRegistered()) {
            throw RegisteredAccountNotFoundException()
        }
        return account
    }

    private fun now() = Instant.now(clock)

}