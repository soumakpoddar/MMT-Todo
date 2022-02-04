package com.mmt.induction.MMT_Induction.DAO;

import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class TodoDAO {

  private static final Logger log = LoggerFactory.getLogger(TodoDAO.class);
  static EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
  static Vertx vertx = Vertx.vertx();
}
