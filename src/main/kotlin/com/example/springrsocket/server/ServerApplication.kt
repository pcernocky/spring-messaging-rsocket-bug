package com.example.springrsocket.server

import io.netty.util.ResourceLeakDetector
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.util.DefaultPayload
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.rsocket.server.RSocketServerCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Controller

private val log = LoggerFactory.getLogger(ServerApplication::class.java)

fun main(args: Array<String>) {
  System.setProperty("spring.profiles.active", "server")
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)
  runApplication<ServerApplication>(*args)
  Thread.sleep(Long.MAX_VALUE)
}

@SpringBootApplication
class ServerApplication

@Controller
class Server {

  // with @ConnectMapping commented out, it reproduces the bug
  // with @ConnectMapping enabled, it works OK

//  @ConnectMapping
  fun handleConnect(setupData: String) {
    log.info("Connected: $setupData")
  }

  @MessageMapping("route")
  fun handle(data: String): String {
    log.info("Received $data")
    System.gc()
    return "Hello $data"
  }

}
