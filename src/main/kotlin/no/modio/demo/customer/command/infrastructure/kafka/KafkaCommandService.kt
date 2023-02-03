package no.modio.demo.customer.command.infrastructure.kafka

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.modio.demo.customer.command.*
import no.modio.demo.customer.command.single.*
import no.modio.demo.customer.meta.Id
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.result.CommandResult
import no.modio.demo.customer.result.Problem
import no.modio.demo.customer.result.Success
import no.norsktipping.ktor.kafka.reply.KafkaReplyError
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Clock
import java.time.Instant

class KafkaCommandService(
    private val replyingKafkaProducer: ReplyingCommandProducer,
    private val commandTopic: String,
    private val clientId: String,
    private val clientVersion: String,
    private val allowedCustomerIdsToBeDeleted: List<String>?,
    private val clock: Clock = ApplicationDependencies.clock
) : CommandService {

    override suspend fun createCustomer(
        customerId: String,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, createCustomerCommand(customerId, metadata))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    override suspend fun changeName(
        customerId: String,
        stateKey: StateKey?,
        firstname: String,
        middlename: String?,
        lastname: String,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, createChangeNameCommand(stateKey, firstname, middlename, lastname, metadata))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    override suspend fun changeAddress(
        customerId: String,
        stateKey: StateKey?,
        type: String,
        address1: String?,
        address2: String?,
        postalCode: String,
        postalArea: String,
        country: String?,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, changeAddressCommand(stateKey, type, address1, address2, postalCode, postalArea, country, metadata))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    override suspend fun changeEmail(
        customerId: String,
        stateKey: StateKey?,
        email: String,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, changeEmailCommand(stateKey, email, metadata))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    override suspend fun changeBankAccountNumber(
        customerId: String,
        stateKey: StateKey?,
        bankAccountNumber: String,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, changeBankAccountNumber(stateKey, bankAccountNumber, metadata))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    override suspend fun changePhoneNumber(
        customerId: String,
        stateKey: StateKey?,
        country: String,
        number: String,
        type: String,
        metadata: Map<String, String>
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, changePhoneNumberCommand(stateKey, country, number, type, metadata))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    override suspend fun changeGender(
        customerId: String,
        stateKey: StateKey?,
        gender: String
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, changeGenderCommand(stateKey, gender))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    override suspend fun changeCustomerStatus(
        customerId: String,
        stateKey: StateKey?,
        customerStatus: String
    ): Either<CommandError, StateKey?> {
        val record = ProducerRecord(commandTopic, customerId, changeCustomerStatusCommand(stateKey, customerStatus))
        return replyingKafkaProducer.sendAndReceive(record).toStateKey()
    }

    private fun createCustomerCommand(customerId: String, metadata: Map<String, String>): Command = Command(
        CreateCustomer(customerId),
        createMetadata(metadata),
        null
    )

    private fun createChangeNameCommand(stateKey: StateKey?, firstname: String, middlename: String?, lastname: String, metadata: Map<String, String>): Command = Command(
        ChangeName(
            firstname,
            lastname,
            middlename,
            null
        ),
        createMetadata(metadata),
        stateKey?.value
    )

    private fun createMetadata(metadata: Map<String, String> = emptyMap()): Metadata = Metadata(
        Instant.now(clock).toString(),
        VENDOR_ID_NT,
        clientId,
        clientVersion,
        "Change requested by an administrator",
        metadata.map { entry -> Id(entry.key, entry.value) },
    )

    private fun changeEmailCommand(stateKey: StateKey?, email: String, metadata: Map<String, String> = emptyMap()): Command = Command(
        ChangeEmail(email),
        createMetadata(metadata),
        stateKey?.value,
    )

    private fun changeAddressCommand(stateKey: StateKey?, type: String, address1: String?, address2: String?, postalCode: String, postalArea: String, country: String?, metadata: Map<String, String> = emptyMap()): Command = Command(
        ChangeAddress(type, address1, address2, postalCode, postalArea, country),
        createMetadata(metadata),
        stateKey?.value,
    )

    private fun changeBankAccountNumber(stateKey: StateKey?, bankAccountNumber: String, metadata: Map<String, String> = emptyMap()): Command = Command(
        ChangeBankAccountNumber(bankAccountNumber),
        createMetadata(metadata),
        stateKey?.value
    )

    private fun changePhoneNumberCommand(stateKey: StateKey?, country: String, number: String, type: String, metadata: Map<String, String> = emptyMap()): Command = Command(
        ChangePhoneNumber(type, country, number),
        createMetadata(metadata),
        stateKey?.value,
    )

    private fun changeGenderCommand(stateKey: StateKey?, gender: String): Command = Command(
        ChangeGender(gender),
        createMetadata(),
        stateKey?.value,
    )

    private fun changeCustomerStatusCommand(stateKey: StateKey?, customerStatus: String): Command = Command(
        ChangeCustomerStatus(customerStatus),
        createMetadata(),
        stateKey?.value,
    )
}

fun Either<KafkaReplyError, ConsumerRecord<String, CommandResult>>.toStateKey(): Either<CommandError, StateKey?> =
    fold(
        { error ->
            when (error) {
                is KafkaReplyError.Timeout -> CommandError.Timeout(error.replyTimout)
                is KafkaReplyError.Unexpected -> CommandError.Unexpected(error.exception)
            }.left()
        },
        { record ->
            when (val result = record.value().result) {
                is Problem -> CommandError.CommandFailed(result.detail).left()
                is Success -> result.stateKey?.let { StateKey(it) }.right()
                else -> error("Unexpected result type ${result::class.java}")
            }
        }
    )

const val VENDOR_ID_NT = "Norsk-Tipping"