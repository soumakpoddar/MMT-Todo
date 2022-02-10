package com.mmt.induction.MMT_Induction.DAO;

import com.aerospike.client.*;
import com.aerospike.client.Record;
import com.aerospike.client.async.EventLoop;
import com.aerospike.client.async.EventLoops;
import com.aerospike.client.async.EventPolicy;
import com.aerospike.client.async.NettyEventLoops;
import com.aerospike.client.listener.*;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.mmt.induction.MMT_Induction.APIS.DataConstants;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AerospikeDemo {

  public static void connect() {

    //connect to Aerospike...
    EventPolicy eventPolicy = new EventPolicy();
    EventLoopGroup group = new NioEventLoopGroup(4);
    EventLoops eventLoops = new NettyEventLoops(eventPolicy, group);

    ClientPolicy clientPolicy = new ClientPolicy();
    clientPolicy.eventLoops = eventLoops;

    Host[] hosts = new Host[] {new Host(DataConstants.HOST,DataConstants.AerospikePort)};

    AerospikeClient client = new AerospikeClient(clientPolicy,hosts);
    log.info("Connected Successfully...");

    //writing data to Aerospike...
    EventLoop eventLoop = eventLoops.next();
    WritePolicy policy = new WritePolicy();
    Key key = new Key("test", "myset", "mykey");
    Bin bin = new Bin("mybin", "myvalue");
    client.put(eventLoop, new WriteHandler(client,eventLoop,policy), policy, key, bin);

    //closing the connections...
    client.close();
    eventLoops.close();
  }

  public static void main(String[] args) {
    connect();
  }

  public static class WriteHandler implements WriteListener {

    AerospikeClient client;
    EventLoop eventLoop;
    WritePolicy policy;

    public WriteHandler(AerospikeClient client, EventLoop eventLoop, WritePolicy policy) {
      this.client = client;
      this.eventLoop = eventLoop;
      this.policy = policy;
    }

    @Override
    public void onSuccess(Key key) {
      try {
        client.get(eventLoop, new ReadHandler(), policy, key);
      } catch (Exception e) {
        log.error("Error ",e);
      }
    }

    @Override
    public void onFailure(AerospikeException e) {
      log.error("Error ",e);
    }
  }

  public static class ReadHandler implements RecordListener {

    @Override
    public void onSuccess(Key key, Record record) {
      Object received = (record == null)? null: record.getValue("soumakbin");
      log.info(String.format("Received: "+received));
    }

    @Override
    public void onFailure(AerospikeException e) {
      log.error("Error ",e);
    }
  }
}
