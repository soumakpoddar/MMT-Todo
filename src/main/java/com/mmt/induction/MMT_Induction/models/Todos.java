package com.mmt.induction.MMT_Induction.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Todos {
  @JsonProperty("todos")
  public Todo[] todos;
}
