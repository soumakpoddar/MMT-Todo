package com.mmt.induction.MMT_Induction.verticles.delete;

import com.mmt.induction.MMT_Induction.APIS.API;
import com.mmt.induction.MMT_Induction.models.Todo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.hibernate.reactive.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class deleteTodoVerticle extends AbstractVerticle {

  private static Logger log = LoggerFactory.getLogger(deleteTodoVerticle.class);
  EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = API.router;

    router.delete(API.getDeleteApi).handler(context -> {
      String id = context.pathParam("id");
      getIDs();

      //check if id is present in database...
      vertx.eventBus().<String>request("id","give me id",reply -> {

        if(reply.succeeded()) {
          List<String> list = new ArrayList<>(Arrays.asList(reply.result().body().split(",")));
          String first = list.get(0).substring(1);
          String last = list.get(list.size()-1).substring(0,list.get(list.size()-1).length()-1);
          list.set(0,first);
          list.set(list.size()-1,last);
          int i=0;
          while (i<=list.size()-1) {
            list.set(i,list.get(i).trim());
            i++;
          }

          if(!list.contains(id)) {
            context.response().end("No such ID found in DB...");
          } else {
            vertx.executeBlocking(promise ->
              deleteTodo(id)
            );
            context.response().end("Todo Deleted Successfully...");
          }
        } else {
          log.error("Error is ",reply.cause());
          context.response().end(reply.cause().toString());
        }
      });
    });
  }

  private void deleteTodo(String id) {
    Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);
    Integer iid = Integer.parseInt(id);

    try {
      sessionFactory.withTransaction((session,tx) ->
        session.find(Todo.class,iid)
          .thenAccept(todo -> session.remove(todo))
      ).toCompletableFuture().join();

      log.info("Todo with id {} deleted successfully...",id);
    } catch (Exception e) {
      log.error("Error from Delete ", e);
    } finally {
      if(sessionFactory != null && sessionFactory.isOpen())
        sessionFactory.close();
    }
  }

  private void getIDs() {

    vertx.eventBus().consumer("id", message -> {
      List<Integer> data = new ArrayList<>();

      vertx.executeBlocking(promise -> {
        Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);
        String q = API.allTodoIDQuery;

        try {
          sessionFactory.withSession(
            session -> session
              .createQuery(q)
              .getResultList()
              .thenAccept(ids -> ids.forEach(id -> {
                data.add((Integer) id);
              }))
          ).toCompletableFuture().join();

          promise.complete(data);
        } catch (Exception e) {
          log.error("Error due to ", e);
          message.fail(1,e.getMessage());
        }
      }, res -> message.reply(res.result().toString()));
    });
  }
}
