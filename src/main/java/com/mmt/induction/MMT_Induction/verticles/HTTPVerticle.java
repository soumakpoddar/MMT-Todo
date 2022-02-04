//package com.mmt.induction.MMT_Induction.verticles;
//
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//import com.mmt.induction.MMT_Induction.APIS.API;
//import com.mmt.induction.MMT_Induction.DAO.MySql;
//import com.mmt.induction.MMT_Induction.models.appModel;
////import com.mmt.induction.MMT_Induction.verticles.checker.updateCheck;
//import com.mmt.induction.MMT_Induction.verticles.delete.deleteTodoVerticle;
//import com.mmt.induction.MMT_Induction.verticles.get.*;
//import com.mmt.induction.MMT_Induction.verticles.insert.insertTodoVerticle;
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.Promise;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.ext.healthchecks.HealthCheckHandler;
//import io.vertx.ext.healthchecks.Status;
//import io.vertx.ext.web.Router;
//import io.vertx.ext.web.handler.BodyHandler;
//import io.vertx.ext.web.handler.CorsHandler;
//import io.vertx.mysqlclient.MySQLPool;
//import io.vertx.core.impl.logging.Logger;
//import io.vertx.core.impl.logging.LoggerFactory;
//import java.util.HashSet;
//import java.util.Set;
//
//public class HTTPVerticle extends AbstractVerticle {
//
//  private final Logger log = LoggerFactory.getLogger(HTTPVerticle.class);
//
//  @Override
//  public void start(Promise<Void> startPromise) throws Exception {
//
//    Router router = Router.router(vertx);
//    Set<String> allowedHeaders = new HashSet<>();
//    allowedHeaders.add("x-requested-with");
//    allowedHeaders.add("Access-Control-Allow-Origin");
//    allowedHeaders.add("origin");
//    allowedHeaders.add("Content-Type");
//    allowedHeaders.add("accept");
//    allowedHeaders.add("X-PINGARUNER");
//    allowedHeaders.add("Access-Control-Allow-Headers");
//    allowedHeaders.add("authorization");
//    allowedHeaders.add("client-security-token");
//    allowedHeaders.add("token");
//    allowedHeaders.add("org");
//    allowedHeaders.add("auth");
//    allowedHeaders.add("User-Agent");
//    allowedHeaders.add("Referrer");
//    allowedHeaders.add("region");
//    allowedHeaders.add("language");
//    allowedHeaders.add("currency");
//    Set<HttpMethod> allowedMethods = new HashSet<>();
//    allowedMethods.add(HttpMethod.GET);
//    allowedMethods.add(HttpMethod.POST);
//    allowedMethods.add(HttpMethod.OPTIONS);
//    allowedMethods.add(HttpMethod.DELETE);
//    allowedMethods.add(HttpMethod.PATCH);
//    allowedMethods.add(HttpMethod.PUT);
//
//    router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
//    router.route().handler(BodyHandler.create());
//    API.router = router;
//    Injector injector = Guice.createInjector(new appModel());
//    MySql mb = injector.getInstance(MySql.class);
//
//    //health checker...❤️❤️❤️
//    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
//    registerHealthCheckHandlers(healthCheckHandler);
//    router.get("/healthcheck").handler(healthCheckHandler);
//
//    //deploying...⚡️⚡️⚡️
////    vertx.deployVerticle(getSenderVerticle.class.getName());
////    vertx.deployVerticle(getReceiverVerticle.class.getName());
////    vertx.deployVerticle(updateCheck.class.getName());
////    vertx.deployVerticle(deleteTodoVerticle.class.getName());
////    vertx.deployVerticle(insertTodoVerticle.class.getName());
//
//    //create server...💾💾💾
//    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
//      if (http.succeeded()) {
//        startPromise.complete();
//        log.info("HTTP server started on port 8888");
//      } else {
//        startPromise.fail(http.cause());
//      }
//    });
//  }
//
//  private void registerHealthCheckHandlers(HealthCheckHandler healthCheckHandler) {
//    MySQLPool pool = MySql.getConnection();
//    healthCheckHandler.register("Main-Application", checker -> checker.complete(Status.OK()));
//    healthCheckHandler.register("Database",
//      promise -> pool.getConnection(connection -> {
//        if(connection.failed()) {
//          promise.fail(connection.cause());
//        } else {
//          connection.result().close();
//          promise.complete();
//        }
//      }));
//  }
//}
