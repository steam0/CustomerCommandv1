package no.modio.demo.customer.command.infrastructure.http

import arrow.core.Either
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import no.modio.demo.customer.command.ApplicationDependencies.meterRegistry
import no.modio.demo.customer.command.ApplicationDependencies.slugToProblemType
import no.modio.demo.customer.command.CommandError
import no.modio.demo.customer.command.CommandService
import no.modio.demo.customer.command.StateKey
import no.modio.demo.customer.command.createKafkaCommandService
import no.norsktipping.ktor.core.apiRespond
import java.util.*
import kotlin.reflect.KClass

fun Application.restApi(
    commandService: CommandService = createKafkaCommandService()
) {
    suspend fun ApplicationCall.commandApiRespond(result: Either<CommandError, Any>) =
        apiRespond(meterRegistry, { it.toProblem(slugToProblemType) }, result)

    routing {
        route("internal") {
            post("/create-customer") {
                val customerId = UUID.randomUUID().toString()
                val result = commandService.createCustomer(customerId, call.buildMetadataMap())

                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }

            post("{customerId}/name") {
                val customerId = call.parameters.getOrFail("customerId")
                val body = call.receive<ChangeNameBody>()
                val result = commandService.changeName(
                    customerId,
                    body.stateKey,
                    body.firstname,
                    body.middlename,
                    body.lastname,
                    metadata = call.buildMetadataMap()
                )
                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }

            post("{customerId}/address") {
                val customerId = call.parameters.getOrFail("customerId")
                val body = call.receive<ChangeAddressBody>()
                val result = commandService.changeAddress(
                    customerId,
                    body.stateKey,
                    body.type,
                    body.address1,
                    body.address2,
                    body.postalCode,
                    body.postalArea,
                    body.country,
                    metadata = call.buildMetadataMap()
                )
                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }

            post("{customerId}/bankAccountNumber") {
                val customerId = call.parameters.getOrFail("customerId")
                val body = call.receive<ChangeBankAccountNumberBody>()
                val result = commandService.changeBankAccountNumber(
                    customerId,
                    body.stateKey,
                    body.bankAccountNumber,
                    metadata = call.buildMetadataMap()
                )
                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }

            post("{customerId}/email") {
                val customerId = call.parameters.getOrFail("customerId")
                val body = call.receive<ChangeEmailBody>()
                val result = commandService.changeEmail(
                    customerId,
                    body.stateKey,
                    body.email,
                    metadata = call.buildMetadataMap()
                )
                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }

            post("{customerId}/phoneNumber") {
                val customerId = call.parameters.getOrFail("customerId")
                val body = call.receive<ChangePhoneNumberBody>()
                val result = commandService.changePhoneNumber(
                    customerId,
                    body.stateKey,
                    body.country,
                    body.number,
                    body.type,
                    metadata = call.buildMetadataMap()
                )
                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }

            post("{customerId}/gender") {
                val customerId = call.parameters.getOrFail("customerId")
                val body = call.receive<ChangeGenderBody>()
                val result = commandService.changeGender(customerId, body.stateKey, body.gender)
                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }

            post("{customerId}/customer-status") {
                val customerId = call.parameters.getOrFail("customerId")
                val body = call.receive<ChangeCustomerStatusBody>()
                val result = commandService.changeCustomerStatus(customerId, body.stateKey, body.customerStatus)
                call.commandApiRespond(result.map { CommandSuccess(customerId, it) })
            }
        }
    }
}

fun ApplicationCall.buildMetadataMap(): Map<String, String> {
    return buildMap {

        callId?.let {
            put("sessionId", it)
        }
    }
}

@Serializable
data class CommandSuccess(
    val customerId: String,
    val newStateKey: StateKey?,
)

@Serializable
data class ChangeEmailBody(
    val email: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangeGenderBody(
    val gender: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangeAddressBody(
    val type: String,
    val address1: String? = null,
    val address2: String? = null,
    val postalCode: String,
    val postalArea: String,
    val country: String? = null,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangeNameBody(
    val firstname: String,
    val middlename: String? = null,
    val lastname: String,
    val stateKey: StateKey? = null,
)


@Serializable
data class ChangeBankAccountNumberBody(
    val bankAccountNumber: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangePhoneNumberBody(
    val type: String,
    val country: String,
    val number: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangeBuypassStatusBody(
    val buypassStatus: Boolean,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangeFregStatusBody(
    val fregStatus: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangePoliticallyExposedStatusBody(
    val politicallyExposedPerson: Boolean,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangeCustomerStatusBody(
    val customerStatus: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangePlayerCardBody(
    val cardId: String,
    val type: String,
    val playercardNumber: String,
    val status: String,
    val expireDate: String,
    val firstUsed: String? = null,
    val stateKey: StateKey? = null,
)

@Serializable
data class RemovePlayerCardBody(
    val cardId: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ChangeIdBody(
    val type: String,
    val value: String,
    val stateKey: StateKey? = null,
)

@Serializable
data class ConfirmCustomerDataBody(
    val stateKey: StateKey? = null,
)

@Serializable
data class DeleteCustomerBody(
    val stateKey: StateKey? = null,
)

suspend inline fun <reified T : Any> ApplicationCall.receiveJsonOrNull(): T? = receiveJsonOrNull(T::class)
suspend inline fun <reified T : Any> ApplicationCall.receiveJsonOrNull(type: KClass<T>): T? {
    return try {
        kotlin.runCatching { receiveNullable<T>() }.getOrNull()
    } catch (cause: Exception) {
        application.log.debug("Conversion failed, null returned", cause)
        null
    }
}
