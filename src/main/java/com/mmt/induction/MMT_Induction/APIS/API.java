package com.mmt.induction.MMT_Induction.APIS;

import io.vertx.ext.web.Router;

public class API {

  //setting no of instances...
  public static int instances = Runtime.getRuntime().availableProcessors();

  //setting global router...
  public static Router router;

  //host and port...
  public static String HOST = "localhost";
  public static int PORT = 8888;

  //eventbus address constants...
  public static String getAddress = "get";
  public static String getNameAddress = "getName";
  public static String getIDAddress = "getID";
  public static String mySqlAddress = "mysql";

  //api endpoints...
  public static String getApi="/todos";
  public static String getCheckApi = "/check";
  public static String getDeleteApi = "/delete/:id";
  public static String getInsertApi = "/insert";

  //mysql constants...
  public static String getQuery = "select id,name,checked,checked_date,checked_time from Todo where parent_id is null";
  public static String todoIDQuery = "select id from Todo where parent_id is null";
  public static String allTodoIDQuery = "select id from Todo";
}
