package no.modio.demo.customer.command

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.modio.demo.customer.result.CommandResult
import kotlinx.serialization.json.Json
import no.modio.demo.customer.command.infrastructure.http.problemMapperFactory
import no.modio.demo.customer.command.infrastructure.kafka.KafkaCommandService
import no.norsktipping.ktor.core.concurrent.executeRunnable
import no.norsktipping.ktor.core.config.getRequiredString
import no.norsktipping.ktor.core.config.getRequiredUrl
import no.norsktipping.ktor.core.host.HostName
import no.norsktipping.ktor.health.CachedHealthRegistry
import no.norsktipping.ktor.health.HealthRegistry
import no.norsktipping.ktor.kafka.reply.ReplyingKafkaProducer
import no.norsktipping.ktor.kafka.reply.replyingKafkaProducer
import no.norsktipping.ktor.kafka.tryGetKafkaSecurityConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.net.URI
import java.time.Clock

object ApplicationDependencies {
    val meterRegistry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT).apply {
        LogbackMetrics().bindTo(this)
    }
    val healthRegistry: HealthRegistry = CachedHealthRegistry()
    val httpJson: Json = Json
    val slugToProblemType = { slug: String ->
        URI.create("https://api.norsk-tipping.no/CustomerCommand/v1/problems/$slug")
    }
    val problemMapper = problemMapperFactory(slugToProblemType)
    val clock: Clock = Clock.systemUTC()
}


typealias ReplyingCommandProducer = ReplyingKafkaProducer<String, Command, String, CommandResult>

fun Application.createKafkaCommandService(
    replyingKafkaProducer: ReplyingCommandProducer = createAndStartReplyingCommandProducer()
): KafkaCommandService = KafkaCommandService(
    replyingKafkaProducer,
    environment.config.getRequiredString("kafka.topics.command"),
    environment.config.getRequiredString("clientId"),
    environment.config.getRequiredString("clientVersion"),
    getAllowableCustomerIdsToDelete()
)

fun Application.createAndStartReplyingCommandProducer(): ReplyingCommandProducer {
    val producerConfig = environment.config.config("kafka")
    val applicationName = producerConfig.getRequiredString("applicationName")
    val clientId = "$applicationName-$HostName"
    val securityConfig = producerConfig.tryGetKafkaSecurityConfig("security")
    val commandResultTopic = producerConfig.getRequiredString("topics.commandResult")
    val producer: ReplyingCommandProducer = replyingKafkaProducer {
        common {
            security(securityConfig)
            bootstrapServers(producerConfig.property("bootstrapServers").getList())
            schemaRegistryUrl(producerConfig.getRequiredUrl("schemaRegistry").toString())
        }
        producer {
            clientId(clientId)
            keySerializer<StringSerializer>()
            valueSerializer<SpecificAvroSerializer<in Command>>()
            acks("0")
        }
        replyTopic(commandResultTopic) {
            keyDeserializer<StringDeserializer>()
            valueDeserializer<SpecificAvroDeserializer<in CommandResult>>()
        }
        meterRegistry = ApplicationDependencies.meterRegistry
    }
    // Start consumer poll loops
    executeRunnable(runnable = producer.pollLoop)
    return producer
}
fun Application.getAllowableCustomerIdsToDelete(): List<String>? {
    val customerIds = environment.config.tryGetString("admin.customerIds")
    return customerIds?.split(",")
}