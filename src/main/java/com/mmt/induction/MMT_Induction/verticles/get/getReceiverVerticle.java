//package com.mmt.induction.MMT_Induction.verticles.get;
//
//import com.mmt.induction.MMT_Induction.APIS.API;
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.eventbus.ReplyException;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import org.hibernate.reactive.stage.Stage;
//import io.vertx.core.impl.logging.Logger;
//import io.vertx.core.impl.logging.LoggerFactory;
//import javax.persistence.EntityManagerFactory;
//import javax.persistence.Persistence;
//
//public class getReceiverVerticle extends AbstractVerticle {
//
//  private final Logger log = LoggerFactory.getLogger(getReceiverVerticle.class);
//  EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
//
//  @Override
//  public void start() throws Exception {
//    JsonArray data = new JsonArray();
//
//    vertx.eventBus().<String>consumer(API.getAddress,message -> {
//
//      vertx.executeBlocking(future -> {
//        Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);
//        data.clear();
//        String q = message.body();
//
//        try {
//          sessionFactory.withTransaction((session,tx) ->
//            session
//              .createQuery(q,Object[].class)
//              .getResultList()
//              .thenAccept(todos ->
//                todos.forEach(todo -> {
//                  JsonObject obj = new JsonObject();
//                  obj.put("name",todo[1]);
//                  obj.put("checked",todo[2]);
//
//                  JsonArray d = new JsonArray();
//                  session.createQuery("select name from Todo where parent_id="+todo[0])
//                    .getResultList()
//                    .thenAccept(names ->
//                      names.forEach(name -> {
//                        d.add(new JsonObject().put("name", name));
//                        log.info("Sub Todos returned");
//                      })
//                    );
//
//                  obj.put("subTodos",d);
//
//                  if(todo[3]!=null && todo[4]!=null) {
//                    obj.put("checked_date",todo[3]);
//                    obj.put("checked_time",todo[4]);
//                  }
//
//                  data.add(obj);
//                })
//              )
//            )
//            .toCompletableFuture().join();
//
//          future.complete(data);
//
//        } catch (Exception e) {
//          log.error("Error is {}", e);
//        }
//      }, false, asyncResult -> {
//        if (asyncResult.succeeded()) {
//          log.info("result returned");
//          message.reply(asyncResult.result());
//        } else {
//          log.error("Request response failed for " + API.getAddress + " : " + asyncResult.cause());
//          try {
//            ReplyException exception = (ReplyException) asyncResult.cause();
//            if (exception != null) {
//              message.fail(exception.failureCode(), exception.getMessage());
//            } else {
//              message.fail(500, asyncResult.cause().getMessage());
//            }
//          } catch (Exception e) {
//            log.error("Coreverticle execute-blocking exception {}", e);
//            message.fail(500, "Coreverticle execute-blocking failed");
//          }
//        }
//      });
//    });
//  }
//}
