package com.mmt.induction.MMT_Induction;

import com.mmt.induction.MMT_Induction.APIS.API;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.mmt.induction.MMT_Induction.models.*;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.hibernate.reactive.stage.Stage;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EndPoints {

  private final Logger log = LoggerFactory.getLogger(EndPoints.class);
  Vertx vertx = Vertx.vertx();
  EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

  // /todos -> get all todos...
  @Operation(summary = "Find all Todos", method = "GET", operationId = "todos",
    tags = {
      "Todo"
    },
    responses = {
      @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(
          mediaType = "application/json",
          encoding = @Encoding(contentType = "application/json"),
          schema = @Schema(name = "todos", example = "{'todos':[" +
            "{" +
            "'_id':'1'," +
            "'name':'Java'," +
            "'checked':'1'," +
            "'subTodos':[" +
            "{" +
            "'name':'JDK'" +
            "}" +
            "{" +
            "'name':'JVM'" +
            "}]" +
            "'checked_date':'2022-01-30'," +
            "'checked_time':'18:57:04.765211'," +
            "}," +
            "{" +
            "'_id':'2'," +
            "'name':'Vert.X'," +
            "'checked':'1'," +
            "'subTodos':[" +
            "{" +
            "'name':'Vert.X Core'" +
            "}" +
            "{" +
            "'name':'Vert.X Web'" +
            "}]" +
            "'checked_date':'2022-01-30'," +
            "'checked_time':'18:57:04.765211'," +
            "}," +
            "]}",
            implementation = Todos.class)
        )
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    }
  )
  public void fetchAllTodos(RoutingContext context)
  {
    fetchReceiver();

    vertx.eventBus().<JsonArray>request(API.getAddress, API.getQuery, reply -> {
      if(reply.succeeded()) {
        context.response().setStatusCode(200).end(reply.result().body().encodePrettily());
      }
      else {
        log.error("Error is \uD83D\uDD34 \uD83D\uDD34",reply.cause());
        context.response().setStatusCode(500).end(reply.cause().getMessage());
      }
    });
  }

  void fetchReceiver() {
    JsonArray data = new JsonArray();

    vertx.eventBus().<String>consumer(API.getAddress,message -> {

      vertx.executeBlocking(future -> {
        Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);
        data.clear();
        String q = message.body();

        try {
          sessionFactory.withTransaction((session,tx) ->
              session
                .createQuery(q,Object[].class)
                .getResultList()
                .thenAccept(todos ->
                  todos.forEach(todo -> {
                    JsonObject obj = new JsonObject();
                    obj.put("name",todo[1]);
                    obj.put("checked",todo[2]);

                    JsonArray d = new JsonArray();
                    session.createQuery("select name from Todo where parent_id="+todo[0])
                      .getResultList()
                      .thenAccept(names ->
                        names.forEach(name -> {
                          d.add(new JsonObject().put("name", name));
                          log.info("Sub Todos returned");
                        })
                      );

                    obj.put("subTodos",d);

                    if(todo[3]!=null && todo[4]!=null) {
                      obj.put("checked_date",todo[3]);
                      obj.put("checked_time",todo[4]);
                    }

                    data.add(obj);
                  })
                )
            )
            .toCompletableFuture().join();

          future.complete(data);

        } catch (Exception e) {
          log.error("Error is {}", e);
        }
      }, false, asyncResult -> {
        if (asyncResult.succeeded()) {
          log.info("result returned");
          message.reply(asyncResult.result());
        } else {
          log.error("Request response failed for " + API.getAddress + " : " + asyncResult.cause());
          try {
            Throwable exception = asyncResult.cause();
            if (exception != null) {
              message.fail(500, exception.getMessage());
            } else {
              message.fail(500, asyncResult.cause().getMessage());
            }
          } catch (Exception e) {
            log.error("Coreverticle execute-blocking exception ", e);
            message.fail(500, "Coreverticle execute-blocking failed");
          }
        }
      });
    });
  }

  //show Update form...
  public void showUpdateForm(RoutingContext context) {
    context.response().putHeader("content-type","text/html")
      .end("<form action=\"/check\" method=\"post\" enctype=\"multipart/form-data\">\n" +
        "  <label for=\"id\">Enter the ID of the Todo to be checked:</label><br><br>\n" +
        "  <input type=\"text\" id=\"id\" name=\"id\"><br><br>\n" +
        "  <input type=\"submit\" value=\"Submit\">\n" +
        "</form> ");
  }

  //check...
  @Operation(summary = "Check a Todo as done", method = "POST", operationId = "todo",
    description = "Check a Todo",
    tags = {
      "Todo"
    },
    requestBody = @RequestBody(
      description = "JSON object of todo",
      content = @Content(
        mediaType = "application/json",
        encoding = @Encoding(contentType = "application/json"),
        schema = @Schema(name = "todo", example = "{" +
          "'_id':'1'," +
          "'name':'Java'," +
          "'checked':'1'," +
          "'subTodos':[" +
          "{" +
          "'name':'JDK'" +
          "}" +
          "{" +
          "'name':'JVM'" +
          "}]" +
          "'checked_date':'2022-01-30'," +
          "'checked_time':'18:57:04.765211'," +
          "},", implementation = Todo.class)
      ),
      required = true
    ),
    responses = {
      @ApiResponse(responseCode = "400", description = "Bad Request."),
      @ApiResponse(responseCode = "200", description = "Todo Checked."),
      @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    }
  )
  public void checkTodos(RoutingContext context) {
    String id = context.request().getFormAttribute("id");
    getIDs();

    //check if id present in db...
      vertx.eventBus().<String>request("id","give me ID", reply -> {
        if(reply.succeeded()) {

          //setting the list...
          List<String> list = new ArrayList<String>(Arrays.asList(reply.result().body().split(",")));
          String first = list.get(0).substring(1);
          String last = list.get(list.size()-1).substring(0,list.get(list.size()-1).length()-1);
          list.set(0,first);
          list.set(list.size()-1,last);
          int i=0;
          while (i<=list.size()-1) {
            list.set(i,list.get(i).trim());
            i++;
          }

          //checking if present in database...
          if(!list.contains(id)) {
            log.error("No such ID found in DB...");
            context.response().end("No such ID found in DB...");
          } else {
            String current_date = String.valueOf(java.time.LocalDate.now());
            String current_time = String.valueOf(java.time.LocalTime.now());

            vertx.executeBlocking(promise ->
              checkTodo(id,current_date,current_time)
            );

            context.response().setStatusCode(200).end("Updated Successfully...");
          }
        } else {
          log.error("Error is {}", reply.cause());
          context.response().setStatusCode(400).end(reply.cause().getMessage());
        }
      });
  }

  private void checkTodo(String id, String current_date, String current_time) {

    Stage.SessionFactory sessionFactory1 = emf.unwrap(Stage.SessionFactory.class);

    try {
      sessionFactory1.withTransaction((session, tx) ->
        session.createQuery("SELECT t from Todo t where t.id=" +id, Todo.class)
          .getResultList()
          .thenAccept(todos -> todos.forEach(todo -> {
            todo.setChecked(1);
            todo.setChecked_date(current_date);
            todo.setChecked_time(current_time);
          })))
        .toCompletableFuture().join();

      log.info("Updated Successfully...");
    } catch (Exception e) {
      log.error("Error from checkTodo ",e);
    }
  }

  private void getIDs() {
    vertx.eventBus().consumer("id", message -> {
      List<Integer> data = new ArrayList<>();

      vertx.executeBlocking(promise -> {
        Stage.SessionFactory sessionFactory2 = emf.unwrap(Stage.SessionFactory.class);
        String q = API.todoIDQuery;

        try {
          sessionFactory2.withSession(
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

  //delete todos...
  @Operation(summary = "Delete todo by ID ", method = "DELETE", operationId = "delete/:id",
    tags = {
      "Todo"
    },
    parameters = {
      @Parameter(in = ParameterIn.PATH, name = "id",
        required = true, description = "The ID for the Todo", schema = @Schema(type = "string"))
    },
    responses = {
      @ApiResponse(responseCode = "404", description = "Not found."),
      @ApiResponse(responseCode = "200", description = "Todo deleted."),
      @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    }
  )
  public void deleteTodos(RoutingContext context) {
    String id = context.pathParam("id");
    getIDs2();

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
          context.response().setStatusCode(404).end("No such ID found in DB...");
        } else {
          vertx.executeBlocking(promise ->
            deleteTodo(id)
          );
          context.response().setStatusCode(200).end("Todo Deleted Successfully...");
        }
      } else {
        log.error("Error is ",reply.cause());
        context.response().end(reply.cause().toString());
      }
    });
  }

  private void getIDs2() {
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

  private void deleteTodo(String id) {
    Stage.SessionFactory sessionFactory3 = emf.unwrap(Stage.SessionFactory.class);
    Integer iid = Integer.parseInt(id);

    try {
      sessionFactory3.withTransaction((session,tx) ->
        session.find(Todo.class,iid)
          .thenAccept(todo -> session.remove(todo))
      ).toCompletableFuture().join();

      log.info("Todo with id: " + id + " deleted successfully...");
    } catch (Exception e) {
      log.error("Error from Delete ", e);
    } finally {
      if(sessionFactory3 != null && sessionFactory3.isOpen())
        sessionFactory3.close();
    }
  }

  //insert Todos...
//  public void showInsertForm(RoutingContext context) {
//    context.response().putHeader("content-type","text/html")
//      .end("<form action=\"/insert\" method=\"post\" enctype=\"multipart/form-data\">\n" +
//        "  <label for=\"name\">Todo name:</label><br>\n" +
//        "  <input type=\"text\" id=\"name\" name=\"name\"><br><br>\n" +
//        "  <label for=\"id\">Todo ID:</label><br>\n" +
//        "  <input type=\"text\" id=\"id\" name=\"id\"><br><br>\n" +
//        "  <label for=\"pid\">Parent ID (Put null if it is parent)</label><br>\n" +
//        "  <input type=\"text\" id=\"pid\" name=\"pid\"><br><br>\n" +
//        "  <input type=\"submit\" value=\"Submit\">\n" +
//        "</form>"
//      );
//  }

  @Operation(summary = "Add a new Todo", method = "PUT", operationId = "todo",
    description = "Add a new Todo",
    tags = {
      "Todo"
    },
    requestBody = @RequestBody(
      description = "JSON object of todo",
      content = @Content(
        mediaType = "application/json",
        encoding = @Encoding(contentType = "application/json"),
        schema = @Schema(name = "todo", example = "{" +
          "'_id':'1'," +
          "'name':'Java'," +
          "'checked':'1'," +
          "'subTodos':[" +
          "{" +
          "'name':'JDK'" +
          "}" +
          "{" +
          "'name':'JVM'" +
          "}]" +
          "'checked_date':'2022-01-30'," +
          "'checked_time':'18:57:04.765211'," +
          "},", implementation = Todo.class)
      ),
      required = true
    ),
    responses = {
      @ApiResponse(responseCode = "400", description = "Bad Request."),
      @ApiResponse(responseCode = "200", description = "Todo Created."),
      @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    }
  )
  public void addTodo(RoutingContext context) {
    String name = context.request().getFormAttribute("name");
    String id = context.request().getFormAttribute("id");
    String pid = (context.request().getFormAttribute("pid")=="")?null:context.request().getFormAttribute("pid");

    vertx.executeBlocking(promise ->
      insertTodo(name,id,pid,context)
    );
  }

  private void insertTodo(String name, String id, String pid, RoutingContext context) {
    Stage.SessionFactory sessionFactory4 = emf.unwrap(Stage.SessionFactory.class);

    try {
      Todo t = new Todo();
      t.setId(Integer.parseInt(id));
      t.setName(name);
      t.setChecked(0);
      t.setChecked_date(null);
      t.setChecked_time(null);
      t.setParent_id(Integer.parseInt(pid));

      sessionFactory4.withTransaction((session,tx) ->
        session.persist(t)
      ).toCompletableFuture().join();

      log.info("Data inserted successfully");
      context.response().end("Data inserted successfully");
    } catch (Exception e) {
      log.error("Error from insert ",e);
      context.response().end(e.getMessage());
    }
  }
}
