package no.modio.demo.customer.command

import io.ktor.server.config.*
import io.ktor.server.testing.*
import no.modio.demo.customer.command.infrastructure.http.restApi

fun testApp(
    commandService: CommandService,
    block: suspend ApplicationTestBuilder.() -> Unit,
) = testApplication {
    environment {
        config = MapApplicationConfig()
    }
    application {
        commonFeatures()
        restApi(commandService)
    }
    block()
}