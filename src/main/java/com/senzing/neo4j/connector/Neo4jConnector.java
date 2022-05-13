package com.senzing.neo4j.connector;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.senzing.listener.communication.ConsumerType;
import com.senzing.listener.communication.MessageConsumer;
import com.senzing.listener.communication.MessageConsumerFactory;
import com.senzing.listener.service.exception.ServiceSetupException;
import com.senzing.neo4j.connector.cmdline.CommandOptions;
import com.senzing.neo4j.connector.service.Neo4jConnectorService;

/**
 * This class feeds G2 data into a Neo4j data mart. It reads information from a
 * queue (RabbitMQ). The received information is in JSON format: {"ENTITY_ID":
 * <id>}. It uses the id to get the entity information from G2 and enters that
 * data into a Neo4j graph database. It gets its configuration information from
 * Java config file: neo4jconnector.configuration.
 */
public class Neo4jConnector {


  /**
   * Executes the tasks. It reads messages from a message source, through a
   * consumer, which then feeds it to a service, where it is processed.
   * 
   * @throws ServiceSetupException
   * @throws MessageConsumerSetupException
   */
  public void run(String config) throws ServiceSetupException, Exception {
    String consumerType = getConfigValue(config, CommandOptions.CONSUMER_TYPE);
    if (consumerType == null || consumerType.isEmpty()) {
      consumerType = "rabbitmq";
    }

    Neo4jConnectorService service = new Neo4jConnectorService();
    service.init(config);

    MessageConsumer consumer = MessageConsumerFactory.generateMessageConsumer(ConsumerType.valueOf(consumerType), config);
    consumer.consume(service);

    int cnt = 0;
    while (service.isServiceUp()) {
      Thread.sleep(30000);
      cnt++;
      // Roughly every 10 minutes report the statistics.
      if (cnt == 20)
      {
        LocalDate curDate = LocalDate.now();
        LocalTime curTime = LocalTime.now();
        String stats = service.getStats();
        System.out.println(curDate + " " + curTime + " - " + stats);
        cnt = 0;
      }
    }
    service.destroy();
  }

  private String getConfigValue(String config, String key) {
    JsonReader reader = Json.createReader(new StringReader(config));
    JsonObject jsonConfig = reader.readObject();
    return jsonConfig.getString(key, null);
  }
}

