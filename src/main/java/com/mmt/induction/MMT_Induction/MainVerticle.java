package com.mmt.induction.MMT_Induction;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mmt.induction.MMT_Induction.APIS.DataConstants;
import com.mmt.induction.MMT_Induction.DAO.MySql;
import com.mmt.induction.MMT_Induction.models.appModel;
import generator.OpenApiRoutePublisher;
import generator.Required;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.mysqlclient.MySQLPool;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.util.*;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class MainVerticle extends AbstractVerticle {

  public static final String APPLICATION_JSON = "application/json";
  private static final int PORT = DataConstants.PORT;
  private static final String HOST = DataConstants.HOST;
  private HttpServer server;
  private EndPoints endPoints;
  private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    endPoints = new EndPoints();

    server = vertx.createHttpServer(createOptions());
    server.requestHandler(configurationRouter()); //::accept not there....
    server.listen(result -> {
      if (result.succeeded()) {
        log.info("Server started on port {} \uD83D\uDFE2 \uD83D\uDFE2",PORT);
        startPromise.complete();
      } else {
        startPromise.fail(result.cause());
      }
    });
  }

  private Router configurationRouter() throws NoSuchFieldException {
    Router router = Router.router(vertx);
    router.route().consumes(APPLICATION_JSON);
    router.route().produces(APPLICATION_JSON);
    router.route().handler(BodyHandler.create());
    Injector injector = Guice.createInjector(new appModel());
    MySql mb = injector.getInstance(MySql.class);

    //health checker...❤️❤️❤️
    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
    registerHealthCheckHandlers(healthCheckHandler);
    router.get("/healthcheck").handler(healthCheckHandler);

    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("auth");
    allowedHeaders.add("Content-Type");
    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.PUT);

    router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

    router.route().handler(context -> {
      context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
      context.next();
    });
    router.route().failureHandler(ErrorHandler.create(vertx,true));

    // Routing section...
    router.get(DataConstants.getApi).handler(endPoints::fetchAllTodos);
    router.get(DataConstants.getCheckApi).handler(endPoints::showUpdateForm);
    router.post(DataConstants.getCheckApi).handler(endPoints::checkTodos);
    router.delete(DataConstants.getDeleteApi).handler(endPoints::deleteTodos);
    router.put(DataConstants.getInsertApi).handler(endPoints::addTodo);

//    for(Route r : router.getRoutes()) {
//      Field f = r.getClass().getDeclaredField("methods");
//    }

    OpenAPI openAPIDoc = OpenApiRoutePublisher.publishOpenApiSpec(
      router,
      "spec",
      "Vertx Swagger Auto Generation",
      "1.0.0",
      "http://" + HOST + ":" + PORT + "/"
    );

    // Tagging section. This is where we can group end point operations; The tag name is then used in the end point annotation
    openAPIDoc.addTagsItem( new io.swagger.v3.oas.models.tags.Tag().name("Todo").description("Todo Operations"));

    // Generate the SCHEMA section of Swagger, using the definitions in the Model folder
    ImmutableSet<ClassPath.ClassInfo> modelClasses = getClassesInPackage("com.mmt.induction.MMT_Induction.models");
    Map<String, Object> map = new HashMap<String, Object>();

    for(ClassPath.ClassInfo modelClass : modelClasses) {

      Field[] fields = FieldUtils.getFieldsListWithAnnotation(modelClass.load(), Required.class).toArray(new
        Field[0]);
      List<String> requiredParameters = new ArrayList<String>();

      for(Field requiredField : fields){
        requiredParameters.add(requiredField.getName());
      }

      fields = modelClass.load().getDeclaredFields();

      for (Field field : fields) {
        mapParameters(field, map);
      }

      openAPIDoc.schema(modelClass.getSimpleName(),
        new Schema()
          .title(modelClass.getSimpleName())
          .type("object")
          .required(requiredParameters)
          .properties(map)
      );

      map = new HashMap<String, Object>();
    }

    // Serve the Swagger JSON spec out on /swagger
    router.get("/swagger").handler(res -> {
      res.response()
        .setStatusCode(200)
        .end(Json.pretty(openAPIDoc));
    });

    // Serve the Swagger UI out on /doc/index.html
    router.route("/doc/*").handler(StaticHandler.create().setCachingEnabled(false).setWebRoot("webroot/node_modules/swagger-ui-dist"));

    return router;
  }

  private void registerHealthCheckHandlers(HealthCheckHandler healthCheckHandler) {
    MySQLPool pool = MySql.getConnection();
    healthCheckHandler.register("Main-Application", checker -> checker.complete(Status.OK()));
    healthCheckHandler.register("Database",
      promise -> pool.getConnection(connection -> {
        if(connection.failed()) {
          promise.fail(connection.cause());
        } else {
          connection.result().close();
          promise.complete();
        }
      }));
  }

  private void mapParameters(Field field, Map<String, Object> map) {
    Class type = field.getType();
    Class componentType = field.getType().getComponentType();

    if (isPrimitiveOrWrapper(type)) {
      Schema primitiveSchema = new Schema();
      primitiveSchema.type(field.getType().getSimpleName());
      map.put(field.getName(), primitiveSchema);
    } else {
      HashMap<String, Object> subMap = new HashMap<String, Object>();

      if(isPrimitiveOrWrapper(componentType)){
        HashMap<String, Object> arrayMap = new HashMap<String, Object>();
        arrayMap.put("type", componentType.getSimpleName() + "[]");
        subMap.put("type", arrayMap);
      } else {
        subMap.put("$ref", "#/components/schemas/" + componentType.getSimpleName());
      }

      map.put(field.getName(), subMap);
    }
  }

  private Boolean isPrimitiveOrWrapper(Class type) {
    return type.equals(Double.class) ||
      type.equals(Float.class) ||
      type.equals(Long.class) ||
      type.equals(Integer.class) ||
      type.equals(Short.class) ||
      type.equals(Character.class) ||
      type.equals(Byte.class) ||
      type.equals(Boolean.class) ||
      type.equals(String.class);
  }

  private ImmutableSet<ClassPath.ClassInfo> getClassesInPackage(String pckgname) {
    try {
      ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
      ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(pckgname);
      return classes;

    } catch (Exception e) {
      return null;
    }
  }

  private HttpServerOptions createOptions() {
    HttpServerOptions options = new HttpServerOptions();
    options.setHost(HOST);
    options.setPort(PORT);
    return options;
  }
}
