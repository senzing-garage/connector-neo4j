package com.senzing.neo4j.connector.graphdatabase;

import java.util.Iterator;
import java.util.Map;

import com.senzing.neo4j.connector.graphdatabase.exception.GraphDatabaseConnectionException;

/**
 * Interface for graph database connection.
 */
public interface GraphDatabaseConnection {

  /**
   * Establishes a connection to the graph database.
   * 
   * @param url URL connection string
   * 
   * @throws GraphDatabaseConnectionException
   */
  public void connect(String url) throws GraphDatabaseConnectionException;

  /**
   * Runs a query against the graph database.
   * 
   * @param query Graph database query
   * @param params Parameters for the query
   * 
   * @return Results from the query (i.e. edges and vertices)
   * 
   * @throws GraphDatabaseConnectionException
   */
  public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException;

  /**
   * Closes a connection to the graph database.
   */
  public void close();
}
