package pl.mj.multicurrencyaccount.infrastructure

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "events")
class StoredEvent(
        @Id
        var id: UUID,
        var accountId: String,
        var payload: String
)