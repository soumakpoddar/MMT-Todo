package com.mmt.induction.MMT_Induction.DAO;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.documentapi.AerospikeDocumentClient;
import com.aerospike.documentapi.JsonConverters;
import com.fasterxml.jackson.databind.JsonNode;
import com.mmt.induction.MMT_Induction.APIS.DataConstants;
import io.vertx.core.json.JsonArray;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class AerospikeDAO{

  //required variables...
  static Host aerospikeHost;
  static ClientPolicy clientPolicy;
  static WritePolicy writepolicy;
  static AerospikeClient client;
  static Key key;

  //get connection to Aerospike Cache DB...
  public static AerospikeClient getConnection() {
    aerospikeHost = new Host(DataConstants.HOST,DataConstants.AerospikePort);
    clientPolicy = new ClientPolicy();
    writepolicy = new WritePolicy();

    client = new AerospikeClient(clientPolicy,aerospikeHost);
    log.info("Successfully Connected to Aerospike...");

    key = new Key("test", "todo", "mykey");
    log.info("Using key {}",key);

    return client;
  }

  //store all the todos...
  public static void writeTodos(JsonArray data) throws IOException {
    //expiring the cache after 30 minutes...
    int n=DataConstants.expireTime;
    writepolicy.expiration = n;

    //calculate times...
    int day = n / (24 * 3600);
    n = n % (24 * 3600);
    int hour = n / 3600;
    n %= 3600;
    int minutes = n / 60 ;
    n %= 60;
    int seconds = n;
    log.warn("Cache Data will expire after {} days, {} hours, {} minutes and {} seconds",day,hour,minutes,seconds);

    AerospikeDocumentClient documentClient = new AerospikeDocumentClient(client);
    String jsonString = data.toString();
    JsonNode jsonNode = JsonConverters.convertStringToJsonNode(jsonString);

    documentClient.put(writepolicy,key,"todo",jsonNode);
    log.info("Todos inserted into cache {}",jsonNode);
  }

  //get the todos from the cache...
  public static JsonArray getTodoFromCache() {

    //get data from cache...
    Record record = client.get(writepolicy, key);
//    client.close();

    if(record == null || record.bins == null) {
      return null;
    }

    ArrayList<Object> list = (ArrayList<Object>) record.getValue("todo");
    JsonArray data = new JsonArray(list);
    return data;
  }

  //remove all the data from the cache when database is updated...
  public static void clearCache() {
    client.delete(writepolicy, key);
  }
}
