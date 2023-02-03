package no.modio.demo.customer.command.infrastructure.kafka

import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import no.modio.demo.customer.command.Command
import no.modio.demo.customer.command.single.*
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.result.CommandResult
import no.modio.demo.customer.result.Success
import no.modio.demo.customer.command.ReplyingCommandProducer
import no.modio.demo.customer.command.StateKey
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*

class KafkaCommandServiceTest : FreeSpec({

    "change address" {
        val now = Instant.now()
        val fixedClock = Clock.fixed(now, ZoneId.systemDefault())
        val customerId = UUID.randomUUID().toString()
        val stateKey = UUID.randomUUID().toString()
        val newStateKey = UUID.randomUUID().toString()
        val slot = slot<ProducerRecord<String, Command>>()
        val mockedReplyingProducer: ReplyingCommandProducer = mockk {
            coEvery { sendAndReceive(capture(slot)) } returns
                    ConsumerRecord(
                        "nt.customer.internal.command.result",
                        0,
                        0,
                        customerId,
                        CommandResult(Success(newStateKey))
                    ).right()
        }
        val service = KafkaCommandService(
            mockedReplyingProducer,
            "nt.customer.internal.command",
            "TEST_CLIENT_ID",
            "TEST_CLIENT_VERSION",
            emptyList(),
            fixedClock,
        )
        val result = service.changeAddress(
            customerId,
            StateKey(stateKey),
            "Default",
            "address1",
            "address2",
            "postalCode",
            "postalArea",
            "country"
        )
        result.shouldBeRight() shouldBe StateKey(newStateKey)
        slot.captured shouldBe ProducerRecord(
            "nt.customer.internal.command",
            customerId,
            Command(
                ChangeAddress(
                    "Default",
                    "address1",
                    "address2",
                    "postalCode",
                    "postalArea",
                    "country"
                ),
                Metadata(
                    now.toString(),
                    VENDOR_ID_NT,
                    "TEST_CLIENT_ID",
                    "TEST_CLIENT_VERSION",
                    "Change requested by an administrator",
                    emptyList(),
                ),
                stateKey,
            )
        )
    }

    "change phone number" {
        val now = Instant.now()
        val fixedClock = Clock.fixed(now, ZoneId.systemDefault())
        val customerId = UUID.randomUUID().toString()
        val stateKey = UUID.randomUUID().toString()
        val newStateKey = UUID.randomUUID().toString()
        val slot = slot<ProducerRecord<String, Command>>()
        val mockedReplyingProducer: ReplyingCommandProducer = mockk {
            coEvery { sendAndReceive(capture(slot)) } returns
                    ConsumerRecord(
                        "nt.customer.internal.command.result",
                        0,
                        0,
                        customerId,
                        CommandResult(Success(newStateKey))
                    ).right()
        }
        val service = KafkaCommandService(
            mockedReplyingProducer,
            "nt.customer.internal.command",
            "TEST_CLIENT_ID",
            "TEST_CLIENT_VERSION",
            emptyList(),
            fixedClock,
        )
        val result = service.changePhoneNumber(
            customerId,
            StateKey(stateKey),
            "47",
            "23232323",
            "Mobile",
        )
        result.shouldBeRight() shouldBe StateKey(newStateKey)
        slot.captured shouldBe ProducerRecord(
            "nt.customer.internal.command",
            customerId,
            Command(
                ChangePhoneNumber("Mobile", "47", "23232323"),
                Metadata(
                    now.toString(),
                    VENDOR_ID_NT,
                    "TEST_CLIENT_ID",
                    "TEST_CLIENT_VERSION",
                    "Change requested by an administrator",
                    emptyList(),
                ),
                stateKey,
            )
        )
    }

    "change email" {
        val now = Instant.now()
        val fixedClock = Clock.fixed(now, ZoneId.systemDefault())
        val customerId = UUID.randomUUID().toString()
        val stateKey = UUID.randomUUID().toString()
        val newStateKey = UUID.randomUUID().toString()
        val slot = slot<ProducerRecord<String, Command>>()
        val mockedReplyingProducer: ReplyingCommandProducer = mockk {
            coEvery { sendAndReceive(capture(slot)) } returns
                    ConsumerRecord(
                        "nt.customer.internal.command.result",
                        0,
                        0,
                        customerId,
                        CommandResult(Success(newStateKey))
                    ).right()
        }
        val service = KafkaCommandService(
            mockedReplyingProducer,
            "nt.customer.internal.command",
            "TEST_CLIENT_ID",
            "TEST_CLIENT_VERSION",
            emptyList(),
            fixedClock,
        )
        val result = service.changeEmail(
            customerId,
            StateKey(stateKey),
            "test@norsk-tipping.no"
        )
        result.shouldBeRight() shouldBe StateKey(newStateKey)
        slot.captured shouldBe ProducerRecord(
            "nt.customer.internal.command",
            customerId,
            Command(
                ChangeEmail("test@norsk-tipping.no"),
                Metadata(
                    now.toString(),
                    VENDOR_ID_NT,
                    "TEST_CLIENT_ID",
                    "TEST_CLIENT_VERSION",
                    "Change requested by an administrator",
                    emptyList(),
                ),
                stateKey,
            )
        )
    }

    "change bank account number" {
        val now = Instant.now()
        val fixedClock = Clock.fixed(now, ZoneId.systemDefault())
        val customerId = UUID.randomUUID().toString()
        val stateKey = UUID.randomUUID().toString()
        val newStateKey = UUID.randomUUID().toString()
        val slot = slot<ProducerRecord<String, Command>>()
        val mockedReplyingProducer: ReplyingCommandProducer = mockk {
            coEvery { sendAndReceive(capture(slot)) } returns
                    ConsumerRecord(
                        "nt.customer.internal.command.result",
                        0,
                        0,
                        customerId,
                        CommandResult(Success(newStateKey))
                    ).right()
        }
        val service = KafkaCommandService(
            mockedReplyingProducer,
            "nt.customer.internal.command",
            "TEST_CLIENT_ID",
            "TEST_CLIENT_VERSION",
            emptyList(),
            fixedClock,
        )
        val result = service.changeBankAccountNumber(
            customerId,
            StateKey(stateKey),
            "1324"
        )
        result.shouldBeRight() shouldBe StateKey(newStateKey)
        slot.captured shouldBe ProducerRecord(
            "nt.customer.internal.command",
            customerId,
            Command(
                ChangeBankAccountNumber("1324"),
                Metadata(
                    now.toString(),
                    VENDOR_ID_NT,
                    "TEST_CLIENT_ID",
                    "TEST_CLIENT_VERSION",
                    "Change requested by an administrator",
                    emptyList(),
                ),
                stateKey,
            )
        )
    }

    "change customer status" {
        val now = Instant.now()
        val fixedClock = Clock.fixed(now, ZoneId.systemDefault())
        val customerId = UUID.randomUUID().toString()
        val stateKey = UUID.randomUUID().toString()
        val newStateKey = UUID.randomUUID().toString()
        val slot = slot<ProducerRecord<String, Command>>()
        val mockedReplyingProducer: ReplyingCommandProducer = mockk {
            coEvery { sendAndReceive(capture(slot)) } returns
                    ConsumerRecord(
                        "nt.customer.internal.command.result",
                        0,
                        0,
                        customerId,
                        CommandResult(Success(newStateKey))
                    ).right()
        }
        val service = KafkaCommandService(
            mockedReplyingProducer,
            "nt.customer.internal.command",
            "TEST_CLIENT_ID",
            "TEST_CLIENT_VERSION",
            emptyList(),
            fixedClock,
        )
        val result = service.changeCustomerStatus(
            customerId,
            StateKey(stateKey),
            "Active"
        )
        result.shouldBeRight() shouldBe StateKey(newStateKey)
        slot.captured shouldBe ProducerRecord(
            "nt.customer.internal.command",
            customerId,
            Command(
                ChangeCustomerStatus("Active"),
                Metadata(
                    now.toString(),
                    VENDOR_ID_NT,
                    "TEST_CLIENT_ID",
                    "TEST_CLIENT_VERSION",
                    "Change requested by an administrator",
                    emptyList(),
                ),
                stateKey,
            )
        )
    }
})
