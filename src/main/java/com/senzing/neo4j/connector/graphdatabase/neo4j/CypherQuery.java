package com.senzing.neo4j.connector.graphdatabase.neo4j;

import java.util.Map;

/**
 * Holds information for a Cypher query and its parameters.
 * Cypher is a language used by Neo4j.
 */
public class CypherQuery {

  private String query;
  private Map<String, Object> parameters;

  public String getQuery() {
    return query;
  }
  public void setQuery(String query) {
    this.query = query;
  }
  public Map<String, Object> getParameters() {
    return parameters;
  }
  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

}
