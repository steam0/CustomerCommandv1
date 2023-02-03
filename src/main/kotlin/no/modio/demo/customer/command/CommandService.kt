package no.modio.demo.customer.command

import arrow.core.Either
import kotlinx.serialization.Serializable
import java.time.Duration

interface CommandService {

    suspend fun createCustomer(
        customerId: String,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?>

    suspend fun changeName(
        customerId: String,
        stateKey: StateKey?,
        firstname: String,
        middlename: String?,
        lastname: String,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?>

    suspend fun changeAddress(
        customerId: String,
        stateKey: StateKey?,
        type: String,
        address1: String?,
        address2: String?,
        postalCode: String,
        postalArea: String,
        country: String?,
        metadata: Map<String, String> = emptyMap()
    ): Either<CommandError, StateKey?>

    suspend fun changeEmail(
        customerId: String,
        stateKey: StateKey?,
        email: String,
        metadata: Map<String, String> = emptyMap()
    ): Either<CommandError, StateKey?>

    suspend fun changeBankAccountNumber(
        customerId: String,
        stateKey: StateKey?,
        bankAccountNumber: String,
        metadata: Map<String, String> = emptyMap()
    ): Either<CommandError, StateKey?>

    suspend fun changePhoneNumber(
        customerId: String,
        stateKey: StateKey?,
        country: String,
        number: String,
        type: String = "Mobile",
        metadata: Map<String, String> = emptyMap()
    ): Either<CommandError, StateKey?>

    suspend fun changeGender(
        customerId: String,
        stateKey: StateKey?,
        gender: String
    ): Either<CommandError, StateKey?>

    suspend fun changeCustomerStatus(
        customerId: String,
        stateKey: StateKey?,
        customerStatus: String
    ): Either<CommandError, StateKey?>
}

sealed interface CommandError {
    data class CommandFailed(val detail: String) : CommandError
    data class Timeout(val replyTimout: Duration) : CommandError
    data class Unexpected(val exception: Exception) : CommandError
}

@JvmInline
@Serializable
value class StateKey(val value: String)
