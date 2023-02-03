package no.modio.demo.customer.command.infrastructure.http

import io.ktor.http.*
import no.modio.demo.customer.command.CommandError
import no.norsktipping.ktor.core.Problem
import org.slf4j.LoggerFactory
import java.net.URI


private val log = LoggerFactory.getLogger("no.norsktipping.customer.command.infrastructure.http.ErrorMapping")

fun problemMapperFactory(slugToProblemType: (String) -> URI): (Any?) -> Problem = { value ->
    when (value) {
        is Problem -> value
        is CommandError -> value.toProblem(slugToProblemType)
        else -> {
            log.warn("no specific problem mapper found for {}", value)
            Problem.fromStatus(
                HttpStatusCode.InternalServerError,
                slugToProblemType("unknown")
            )
        }
    }
}

fun CommandError.toProblem(slugToProblemType: (String) -> URI): Problem = when (this) {
    is CommandError.CommandFailed -> Problem.fromStatus(
        HttpStatusCode.BadRequest,
        slugToProblemType("failed-command"),
        "Command failed",
        detail
    )
    is CommandError.Timeout -> Problem.fromStatus(
        HttpStatusCode.GatewayTimeout,
        slugToProblemType("timeout"),
        "Request timed out"
    )
    is CommandError.Unexpected -> Problem.fromThrowable(
        HttpStatusCode.InternalServerError,
        slugToProblemType("unexpected"),
        exception
    )
}