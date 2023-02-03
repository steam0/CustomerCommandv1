package no.modio.demo.customer.command

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import no.norsktipping.ktor.core.NtNettyEngineMain
import no.norsktipping.ktor.core.infoRoutes
import no.norsktipping.ktor.core.registerProblemHandling
import no.norsktipping.ktor.core.server.plugins.callduration.CallDuration
import no.norsktipping.ktor.core.server.plugins.callid.sessionId
import no.norsktipping.ktor.core.server.plugins.calllogging.CallLogging
import no.norsktipping.ktor.core.server.plugins.calllogging.perfLog
import no.norsktipping.ktor.health.healthRoutes

fun main(args: Array<String>) {
    NtNettyEngineMain.main(args)
}

fun Application.commonFeatures() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
    install(StatusPages) {
        registerProblemHandling(ApplicationDependencies.meterRegistry)
    }
    install(ContentNegotiation) {
        json(
            ApplicationDependencies.httpJson
        )
    }
    install(CallDuration)
    install(CallLogging) {
        perfLog()
    }
    install(MicrometerMetrics) {
        registry = ApplicationDependencies.meterRegistry
        distributionStatisticConfig = DistributionStatisticConfig.Builder()
            .percentilesHistogram(true)
            .build()
    }

    healthRoutes(ApplicationDependencies.healthRegistry)
    infoRoutes()
    routing {
        get("/prometheus") {
            call.respond(ApplicationDependencies.meterRegistry.scrape())
        }
    }
}
