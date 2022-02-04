package com.mmt.induction.MMT_Induction.verticles.get;

import com.mmt.induction.MMT_Induction.APIS.API;
import com.mmt.induction.MMT_Induction.DAO.MySql;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLPool;

//send all the available todos only name...
public class getNameReceiverVerticle extends AbstractVerticle {

  private static MySQLPool db = MySql.getConnection();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    startPromise.complete();

    vertx.eventBus().<String>consumer(API.getNameAddress, message -> {
      db.query(message.body())
        .execute()
        .onFailure(error -> {
          JsonArray arr = new JsonArray();
//          JsonObject obj = new JsonObject().put("name","Failed to fetch data...");
//          arr.add(obj);
          //reply with failure...
          message.reply(arr);
        })
        .onSuccess(result -> {
          var response = new JsonArray();

          result.forEach(row -> {
            JsonObject data = new JsonObject()
              .put("name",row.getValue("name"))
              ;
            response.add(data);
          });

          message.reply(response);
        })
      ;
    });
  }
}
