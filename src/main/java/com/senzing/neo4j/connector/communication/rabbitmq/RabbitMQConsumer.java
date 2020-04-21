package com.senzing.neo4j.connector.communication.rabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.senzing.neo4j.connector.cmdline.CommandOptions;
import com.senzing.neo4j.connector.communication.MessageConsumer;
import com.senzing.neo4j.connector.communication.exception.MessageConsumerSetupException;
import com.senzing.neo4j.connector.config.AppConfiguration;
import com.senzing.neo4j.connector.config.ConfigKeys;
import com.senzing.neo4j.connector.data.g2.G2Entity;
import com.senzing.neo4j.connector.service.MainService;
import com.senzing.neo4j.connector.service.exception.ServiceExecutionException;

/**
 * A consumer for RabbidMQ.
 */
public class RabbitMQConsumer implements MessageConsumer {

  private final String HOST_NAME;
  private final String QUEUE_NAME;
  private final String USER_NAME;
  private final String PASSWORD;

  private MainService service;

  private final String UTF8_ENCODING = "UTF-8";

  /**
   * Generates a Rabbit MQ consumer.
   * 
   * @return
   * 
   * @throws MessageConsumerSetupException
   */
  public static RabbitMQConsumer generateRabbitMQConsumer(String config) throws MessageConsumerSetupException {
    return new RabbitMQConsumer(config);
  }

  /**
   * Constructor receives the 2 needed configuration parameters, host name and
   * queue name.
   */
  private RabbitMQConsumer(String config) throws MessageConsumerSetupException {
    String queueName;
    String queueHost;
    String userName;
    String password;
    try {
      JSONObject configObject = new JSONObject(config);
      AppConfiguration configuration = new AppConfiguration();
      queueName = getConfigValue(configObject, configuration, CommandOptions.MQ_QUEUE, ConfigKeys.RABBITMQ_NAME);
      queueHost = getConfigValue(configObject, configuration, CommandOptions.MQ_HOST, ConfigKeys.RABBITMQ_HOST);
      userName = getConfigValue(configObject, configuration, CommandOptions.MQ_USER, ConfigKeys.RABBITMQ_USER_NAME);
      password = getConfigValue(configObject, configuration, CommandOptions.MQ_PASSWORD, ConfigKeys.RABBITMQ_PASSWORD);
      if (queueName == null || queueHost == null) {
        List<String> configParams = new ArrayList<>();
        if (queueName == null) {
          configParams.add(ConfigKeys.RABBITMQ_NAME);
        }
        if (queueHost == null) {
          configParams.add(ConfigKeys.RABBITMQ_HOST);
        }
        StringBuilder errorMessage = new StringBuilder("Following parameters missing from config file: ");
        errorMessage.append(String.join(", ", configParams));
        throw new MessageConsumerSetupException(errorMessage.toString());
      }
    } catch (IOException | JSONException e) {
      throw new MessageConsumerSetupException(e);
    }
    HOST_NAME = queueHost;
    QUEUE_NAME = queueName;
    USER_NAME = userName;
    PASSWORD = password;
  }

  /**
   * Sets up a RabbitMQ consumer and then receives messages from RabbidMQ and
   * feeds to service.
   * 
   * @param service Processes messages
   * 
   * @throws MessageConsumerSetupException
   */
  @Override
  public void consume(MainService service) throws MessageConsumerSetupException {

    this.service = service;

    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(HOST_NAME);
      if (USER_NAME != null) {
        factory.setUsername(USER_NAME);
        factory.setPassword(PASSWORD);
      }
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();

      boolean durable = false;
      boolean exclusive = false;
      boolean autoDelete = false;
      channel.queueDeclare(QUEUE_NAME, durable, exclusive, autoDelete, null);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), UTF8_ENCODING);
        try {
          processMessage(message);
        } finally {
          boolean ackMultiple = false;
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), ackMultiple);
        }
      };

      boolean autoAck = false;
      channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {
      });

    } catch (IOException | TimeoutException e) {
      throw new MessageConsumerSetupException(e);
    }

  }

  private void processMessage(String message) {
    try {
      // Processes messages of the format:
      // {"DATA_SOURCE":"TEST","RECORD_ID":"RECORD3","AFFECTED_ENTITIES":[{"ENTITY_ID":1,"LENS_CODE":"DEFAULT"}]}
      JSONObject json = new JSONObject(message);
      // We are only interested in the entity ids.
      JSONArray entities = json.getJSONArray("AFFECTED_ENTITIES");
      if (entities != null) {
        for (int i = 0; i < entities.length(); i++) {
          JSONObject entity = entities.getJSONObject(i);
          if (entity != null) {
            Long entityId = entity.getLong(G2Entity.ENTITY_ID_FIELD);
            service.processEntity(entityId);
          }
        }
      }
    } catch (JSONException | ServiceExecutionException e) {
      e.printStackTrace();
    }
  }

  private String getConfigValue(JSONObject configObject, AppConfiguration configuration, String cmdOption, String key)
      throws JSONException {
    String value = configObject.optString(cmdOption);
    if (value == null || value.isEmpty()) {
      value = configuration.getConfigValue(key);
    }
    return value;
  }

}
