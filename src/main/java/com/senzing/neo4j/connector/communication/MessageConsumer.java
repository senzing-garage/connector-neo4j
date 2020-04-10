package com.senzing.neo4j.connector.communication;

/**
 * Interface for a queue consumer.
 */
import com.senzing.neo4j.connector.communication.exception.MessageConsumerSetupException;
import com.senzing.neo4j.connector.service.MainService;

public interface MessageConsumer {

  /**
   * Consumer main function.  Receives messages from message source and processes.
   * 
   * @param service Processes messages
   * 
   * @throws MessageConsumerSetupException
   */
	public void consume(MainService service) throws MessageConsumerSetupException;
}
