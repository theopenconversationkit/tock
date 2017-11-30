package fr.vsct.tock.bot.connector.slack

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions


//fun main(args: Array<String>) {
//    val options = VertxOptions()
//    options.maxEventLoopExecuteTime = 45000000000L
//    Vertx.vertx(options).deployVerticle(App)
//}
//
//object App: AbstractVerticle() {
//
//
//    override fun start(future: Future<Void>) {
//        val router = Controller(vertx).buildRouter()
//        vertx.createHttpServer().requestHandler(router::accept).listen(8080)
//    }
//}