package com.mmt.induction.MMT_Induction.verticles.get;

import com.mmt.induction.MMT_Induction.APIS.API;
import com.mmt.induction.MMT_Induction.DAO.MySql;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLPool;

public class getIdReceiverVerticle extends AbstractVerticle {

  private static MySQLPool db = MySql.getConnection();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    vertx.eventBus().<String>consumer(API.getIDAddress, message -> {

      getData(startPromise, message);
    });
  }

  private void getData(Promise<Void> startPromise, Message<String> message) {
    db.query(message.body())
      .execute()
      .onFailure(error -> {
        JsonArray data = new JsonArray();
        data.add(error.getMessage());
        message.reply(data);
        startPromise.fail(error.getMessage());
      })
      .onSuccess(result -> {
        JsonArray data = new JsonArray();

        result.forEach(row -> {
          data.add(new JsonObject().put(row.getValue("id").toString(),"id"));
        });

        message.reply(data);
        startPromise.complete();
      })
    ;
  }
}
