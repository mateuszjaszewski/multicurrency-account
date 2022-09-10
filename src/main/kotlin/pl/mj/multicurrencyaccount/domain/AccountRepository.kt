package pl.mj.multicurrencyaccount.domain

interface AccountRepository {
    fun getById(id: String): Account
    fun save(account: Account)
}