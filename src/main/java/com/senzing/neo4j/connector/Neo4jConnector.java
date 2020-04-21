package com.senzing.neo4j.connector;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.senzing.neo4j.connector.cmdline.CommandOptions;
import com.senzing.neo4j.connector.communication.ConsumerType;
import com.senzing.neo4j.connector.communication.MessageConsumer;
import com.senzing.neo4j.connector.communication.MessageConsumerFactory;
import com.senzing.neo4j.connector.communication.exception.MessageConsumerSetupException;
import com.senzing.neo4j.connector.config.AppConfiguration;
import com.senzing.neo4j.connector.config.ConfigKeys;
import com.senzing.neo4j.connector.service.MainService;
import com.senzing.neo4j.connector.service.exception.ServiceSetupException;

/**
 * This class feeds G2 data into a Neo4j data mart. It reads information from a
 * queue (RabbitMQ). The received information is in JSON format: {"ENTITY_ID":
 * <id>}. It uses the id to get the entity information from G2 and enters that
 * data into a Neo4j graph database. It gets its configuration information from
 * Java config file: neo4jconnector.configuration.
 */
public class Neo4jConnector {

  MainService service;

  /**
   * The constructor gets starts the G2 and Neo4j services.
   * 
   * @throws ServiceSetupException
   */
  Neo4jConnector(String config) throws ServiceSetupException {
    service = new MainService(config);
  }

  /**
   * Executes the tasks. It reads messages from a message source, through a
   * consumer, which then feeds it to a service, where it is processed.
   * 
   * @throws ServiceSetupException
   * @throws MessageConsumerSetupException
   */
  public void run(String commandConfig) throws ServiceSetupException, MessageConsumerSetupException {

    // Set up a consumer, which receives messages from some source.

    String consumerTypeString = null;
    try {
      JSONObject configObject = new JSONObject(commandConfig);
      consumerTypeString = configObject.optString(CommandOptions.CONSUMER_TYPE);
      if (consumerTypeString == null || consumerTypeString.isEmpty()) {
        AppConfiguration config = new AppConfiguration();
        consumerTypeString = config.getConfigValue(ConfigKeys.CONSUMER_TYPE);
      }
    } catch (IOException | JSONException e) {
      throw new ServiceSetupException(e);
    }
    // Verify that required values are set.
    if (consumerTypeString == null || consumerTypeString.isEmpty()) {
      StringBuilder errorMessage = new StringBuilder("Following parameters missing from config file: ");
      errorMessage.append(ConfigKeys.CONSUMER_TYPE);
      throw new ServiceSetupException(errorMessage.toString());
    }

    ConsumerType consumerType;
    try {
      consumerType = ConsumerType.valueOf(consumerTypeString);
    } catch (IllegalArgumentException | NullPointerException e) {
      StringBuilder errorMessage = new StringBuilder("Invalid consumer type specified: ").append(consumerTypeString);
      throw new ServiceSetupException(errorMessage.toString(), e);
    }

    MessageConsumer consumer = MessageConsumerFactory.generateMessageConsumer(consumerType, commandConfig);
    // Start message processing through consumer.
    try {
      consumer.consume(service);
    } catch (MessageConsumerSetupException e) {
      throw new ServiceSetupException(e);
    }
  }
}
