package com.mmt.induction.MMT_Induction.DAO;

import com.mmt.induction.MMT_Induction.APIS.DataConstants;
import com.mmt.induction.MMT_Induction.annotations.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import javax.inject.Inject;

public class MySql extends AbstractVerticle {
  private static String host;
  private static Integer port;
  private static String database;
  private static String user;
  private static String password;

  @Inject
  public MySql(@Host String host, @Port Integer port, @Database String database, @User String user, @Password String password) {
    this.host = host;
    this.port = port;
    this.database = database;
    this.user = user;
    this.password = password;
  }

  public static MySQLPool getConnection() {
    final var conectOptions = new MySQLConnectOptions()
      .setHost(host)
      .setPort(port)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password);

    var poolOptions = new PoolOptions()
      .setMaxSize(DataConstants.instances);

    var vertx = Vertx.vertx();

    MySQLPool db = MySQLPool.pool(vertx , conectOptions , poolOptions);

    return db;
  }

//  public static Stage.SessionFactory getConnection1() {
//    EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
//    Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);
//    return sessionFactory;
//  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    final var conectOptions = new MySQLConnectOptions()
      .setHost(host)
      .setPort(port)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password);

    var poolOptions = new PoolOptions()
      .setMaxSize(5);

    MySQLPool db = MySQLPool.pool(vertx , conectOptions , poolOptions);

    if(db==null) {
      System.out.println("Cannot connect...");
      startPromise.fail("Cannot connect");
    }
    else {
      vertx.eventBus().publish(DataConstants.mySqlAddress , db);
      startPromise.complete();
    }
  }
}
