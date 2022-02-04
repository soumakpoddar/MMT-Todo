//package com.mmt.induction.MMT_Induction.verticles.get;
//
//import com.mmt.induction.MMT_Induction.APIS.API;
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.Promise;
//import io.vertx.core.json.JsonArray;
//import io.vertx.ext.web.Router;
//
//public class getSenderVerticle extends AbstractVerticle {
//
//  @Override
//  public void start(Promise<Void> startPromise) throws Exception {
//
//    Router router = API.router;
//
//    router.get(API.getApi).handler(context -> {
//      vertx.eventBus().<JsonArray>request(API.getAddress, API.getQuery, reply -> {
//
//        if(reply.succeeded()) {
//          context.response().end(reply.result().body().encodePrettily());
//        }
//        else {
//          context.response().end("Failed to connect to DB...");
//          startPromise.fail(reply.cause());
//        }
//      });
//    });
//  }
//}
