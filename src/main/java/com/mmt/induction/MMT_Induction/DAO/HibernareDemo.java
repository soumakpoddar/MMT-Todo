package com.mmt.induction.MMT_Induction.DAO;

import org.hibernate.reactive.stage.Stage;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class HibernareDemo {

  public static void main(String[] args) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
    Stage.SessionFactory sessionFactory = emf.unwrap(Stage.SessionFactory.class);

    try {

      //get all todos...
      sessionFactory.withSession(
          session -> session
            .createQuery("select id from Todo")
            .getResultList()
            .thenAccept(todos -> todos.forEach(todo -> {
              System.out.println(todo);
              })
            )
        )
        .toCompletableFuture().join();
    } finally {
      sessionFactory.close();
    }
  }
}
