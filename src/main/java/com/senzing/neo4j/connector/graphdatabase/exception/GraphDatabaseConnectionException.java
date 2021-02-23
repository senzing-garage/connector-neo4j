package com.senzing.neo4j.connector.graphdatabase.exception;

public class GraphDatabaseConnectionException extends Exception {

  private static final long serialVersionUID = 1L;

  public GraphDatabaseConnectionException(String message) {
    super(message);
  }

  public GraphDatabaseConnectionException(Exception e) {
    super(e);
  }
}
