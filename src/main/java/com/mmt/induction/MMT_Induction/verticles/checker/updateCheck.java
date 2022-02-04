//package com.mmt.induction.MMT_Induction.verticles.checker;
//
//import com.mmt.induction.MMT_Induction.APIS.API;
//import com.mmt.induction.MMT_Induction.models.Todo;
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.Promise;
//import io.vertx.ext.web.Router;
//import org.hibernate.reactive.stage.Stage;
//import io.vertx.core.impl.logging.Logger;
//import io.vertx.core.impl.logging.LoggerFactory;
//import javax.persistence.EntityManagerFactory;
//import javax.persistence.Persistence;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class updateCheck extends AbstractVerticle {
//
//  private final Logger log = LoggerFactory.getLogger(updateCheck.class);
//  EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
//
//  @Override
//  public void start(Promise<Void> startPromise) throws Exception {
//
//    Router router = API.router;
//
//    router.get(API.getNameApi).handler(context -> {
//      context.response().putHeader("content-type","text/html")
//        .end("<form action=\"/check\" method=\"post\" enctype=\"multipart/form-data\">\n" +
//        "  <label for=\"id\">Enter the ID of the Todo to be checked:</label><br><br>\n" +
//        "  <input type=\"text\" id=\"id\" name=\"id\"><br><br>\n" +
//        "  <input type=\"submit\" value=\"Submit\">\n" +
//        "</form> ");
//    });
//
//    router.post(API.getNameApi).handler(context -> {
//      String id = context.request().getFormAttribute("id");
//      getIDs();
//
//      //check if id present in db...
//      vertx.eventBus().<String>request("id","give me ID", reply -> {
//        if(reply.succeeded()) {
//
//          //setting the list...
//          List<String> list = new ArrayList<String>(Arrays.asList(reply.result().body().split(",")));
//          String first = list.get(0).substring(1);
//          String last = list.get(list.size()-1).substring(0,list.get(list.size()-1).length()-1);
//          list.set(0,first);
//          list.set(list.size()-1,last);
//          int i=0;
//          while (i<=list.size()-1) {
//            list.set(i,list.get(i).trim());
//            i++;
//          }
//
//          //checking if present in database...
//          if(!list.contains(id)) {
//            log.error("No such ID found in DB...");
//            context.response().end("No such ID found in DB...");
//          } else {
//            String current_date = String.valueOf(java.time.LocalDate.now());
//            String current_time = String.valueOf(java.time.LocalTime.now());
//
//            vertx.executeBlocking(promise ->
//              checkTodo(id,current_date,current_time)
//            );
//
//            context.response().end("Updated Successfully...");
//          }
//        } else {
//          log.error("Error is {}", reply.cause());
//          context.response().end(reply.cause().getMessage());
//        }
//      });
//    });
//  }
//
//  private void checkTodo(String id, String current_date, String current_time) {
//
//    Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);
//
//    try {
//      sessionFactory.withTransaction((session, tx) ->
//        session.createQuery("SELECT t from Todo t where t.id=" +id, Todo.class)
//          .getResultList()
//          .thenAccept(todos -> todos.forEach(todo -> {
//            todo.setChecked(1);
//            todo.setChecked_date(current_date);
//            todo.setChecked_time(current_time);
//          })))
//        .toCompletableFuture().join();
//
//      log.info("Updated Successfully...");
//    } catch (Exception e) {
//      log.error("Error from checkTodo {}",e);
//    } finally {
//      if(sessionFactory != null && sessionFactory.isOpen())
//        sessionFactory.close();
//    }
//  }
//
//  private void getIDs() {
//
//    vertx.eventBus().consumer("id", message -> {
//      List<Integer> data = new ArrayList<>();
//
//      vertx.executeBlocking(promise -> {
//        Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);
//        String q = API.todoIDQuery;
//
//        try {
//          sessionFactory.withSession(
//            session -> session
//              .createQuery(q)
//              .getResultList()
//              .thenAccept(ids -> ids.forEach(id -> {
//                data.add((Integer) id);
//              }))
//          ).toCompletableFuture().join();
//
//          promise.complete(data);
//        } catch (Exception e) {
//          log.error("Error due to {}", e);
//          message.fail(1,e.getMessage());
//        }
//      }, res -> message.reply(res.result().toString()));
//    });
//  }
//}
