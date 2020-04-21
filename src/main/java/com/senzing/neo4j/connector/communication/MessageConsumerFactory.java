package com.senzing.neo4j.connector.communication;

import com.senzing.neo4j.connector.communication.exception.MessageConsumerSetupException;
import com.senzing.neo4j.connector.communication.rabbitmq.RabbitMQConsumer;



public class MessageConsumerFactory {

  /**
   * Generates a message consumer based on consumer type.
   * 
   * @param consumerType
   * 
   * @return
   * 
   * @throws MessageConsumerSetupException
   */
  public static MessageConsumer generateMessageConsumer(ConsumerType consumerType, String config) throws MessageConsumerSetupException {
    if (consumerType == ConsumerType.rabbitmq) {
      return RabbitMQConsumer.generateRabbitMQConsumer(config);
    }

    StringBuilder errorMessage = new StringBuilder("Invalid message consumer specified: ").append(consumerType.toString());
    throw new MessageConsumerSetupException(errorMessage.toString());
  }
}
