package com.mmt.induction.MMT_Induction.models;

import com.google.inject.AbstractModule;
import com.mmt.induction.MMT_Induction.annotations.*;

public class appModel extends AbstractModule {

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(Host.class).toInstance("localhost");
    bind(Integer.class).annotatedWith(Port.class).toInstance(3306);
    bind(String.class).annotatedWith(Database.class).toInstance("todos");
    bind(String.class).annotatedWith(User.class).toInstance("root");
    bind(String.class).annotatedWith(Password.class).toInstance("secret");
  }
}
