package com.example.springrsocket.client

import io.netty.util.ResourceLeakDetector
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.stereotype.Component

private val log = LoggerFactory.getLogger(ClientApplication::class.java)

fun main(args: Array<String>) {
  System.setProperty("spring.profiles.active", "client")
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)
  runApplication<ClientApplication>(*args)
}

@SpringBootApplication
class ClientApplication {

  class Controller {

    // with @ConnectMapping commented out, it reproduces the bug
    // with @ConnectMapping enabled, it works OK

    // @ConnectMapping
    fun handle(setupData: String) {
      log.info("Setup: $setupData")
    }

  }

}

@Component
class Client : ApplicationRunner {

  override fun run(args: ApplicationArguments) {
    repeat(3) { requestId ->
      val requester = RSocketRequester.builder()
        .setupData("Setup data")
        .rsocketConnector { connector ->
          connector.acceptor(
            RSocketMessageHandler.responder(
              RSocketStrategies.create(),
              ClientApplication.Controller()
            )
          )
        }
        .tcp("localhost", 7000)

      log.info("Sending request")

      val response = requester
        .route("route")
        .data("data $requestId")
        .retrieveMono(String::class.java)
        .block()

      log.info("Response: $response")

      // don't know why, but with dispose() the issue on server is reproduced sooner
      // it seems that the disconnect from the server helps to reproduce the issue
      // without dispose() several client runs are needed to reproduce the issue
      requester.dispose()

      System.gc()
    }
  }

}
