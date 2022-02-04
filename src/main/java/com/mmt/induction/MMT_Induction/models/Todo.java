package com.mmt.induction.MMT_Induction.models;

import lombok.*;
import javax.persistence.*;
import generator.Required;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "Todo")
@NamedQuery(
  name = "getAllTodos",
  query = "from Todo"
)
public class Todo {

  @Required
  @JsonProperty(value = "_id", required = true)
  @Id
  @Column(name = "id", nullable = false)
  public Integer id;

  @JsonProperty("name")
  @Column(name = "name", nullable = false)
  public String name;

  @JsonProperty("checked")
  @Column(name = "checked", nullable = false)
  public Integer checked;

  @JsonProperty("checked_date")
  @Column(name = "checked_date")
  public String checked_date;

  @JsonProperty("checked_time")
  @Column(name = "checked_time")
  public String checked_time;

  @JsonProperty("parent_id")
  @Column(name = "parent_id")
  public Integer parent_id;

  public Todo(Integer id,String name,Integer checked,Integer parent_id) {
    this.id = id;
    this.name = name;
    this.checked = checked;
    this.parent_id = parent_id;
  }
}
