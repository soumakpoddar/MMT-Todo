package com.mmt.induction.MMT_Induction.verticles.insert;

import com.mmt.induction.MMT_Induction.APIS.API;
import com.mmt.induction.MMT_Induction.models.Todo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.hibernate.reactive.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class insertTodoVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(insertTodoVerticle.class);
  EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = API.router;

    router.get(API.getInsertApi).handler(context -> {
      context.response().putHeader("content-type","text/html")
        .end("<form action=\"/insert\" method=\"post\" enctype=\"multipart/form-data\">\n" +
          "  <label for=\"name\">Todo name:</label><br>\n" +
          "  <input type=\"text\" id=\"name\" name=\"name\"><br><br>\n" +
          "  <label for=\"id\">Todo ID:</label><br>\n" +
          "  <input type=\"text\" id=\"id\" name=\"id\"><br><br>\n" +
          "  <label for=\"pid\">Parent ID (Put null if it is parent)</label><br>\n" +
          "  <input type=\"text\" id=\"pid\" name=\"pid\"><br><br>\n" +
          "  <input type=\"submit\" value=\"Submit\">\n" +
          "</form>");
    });

    router.post(API.getInsertApi).handler(context -> {
      String name = context.request().getFormAttribute("name");
      String id = context.request().getFormAttribute("id");
      String pid = (context.request().getFormAttribute("pid")=="null")?null:context.request().getFormAttribute("pid");

      vertx.executeBlocking(promise ->
        insertTodo(name,id,pid,context)
      );
    });
  }

  private void insertTodo(String name, String id, String pid, RoutingContext context) {
    Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);

    try {
      Todo t = new Todo();
      t.setId(Integer.parseInt(id));
      t.setName(name);
      t.setChecked(0);
      t.setChecked_date(null);
      t.setChecked_time(null);
      t.setParent_id(Integer.parseInt(pid));

      sessionFactory.withTransaction((session,tx) ->
        session.persist(t)
      ).toCompletableFuture().join();

      log.info("Data inserted successfully");
      context.response().end("Data inserted successfully");
    } catch (Exception e) {
      log.error("Error from insert ",e);
      context.response().end(e.getMessage());
    } finally {
      if(sessionFactory!=null && sessionFactory.isOpen())
        sessionFactory.close();
    }
  }
}
