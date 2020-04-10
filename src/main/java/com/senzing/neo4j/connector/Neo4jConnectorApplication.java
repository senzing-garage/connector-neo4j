package com.senzing.neo4j.connector;

/**
 * This application feeds G2 data into a data mart. It reads information via a
 * consumer, processes and feeds to a mart.
 */
public class Neo4jConnectorApplication {

  public static void main(String[] args) {
    try {
      Neo4jConnector neo4jConnector = new Neo4jConnector();
      neo4jConnector.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
