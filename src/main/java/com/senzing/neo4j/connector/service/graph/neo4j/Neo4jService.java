package com.senzing.neo4j.connector.service.graph.neo4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.senzing.listener.senzing.service.exception.ServiceExecutionException;
import com.senzing.listener.senzing.service.exception.ServiceSetupException;
import com.senzing.neo4j.connector.cmdline.CommandOptions;
import com.senzing.neo4j.connector.config.AppConfiguration;
import com.senzing.neo4j.connector.config.ConfigKeys;
import com.senzing.neo4j.connector.data.g2.G2Entity;
import com.senzing.neo4j.connector.graphdatabase.exception.GraphDatabaseConnectionException;
import com.senzing.neo4j.connector.graphdatabase.neo4j.CypherQuery;
import com.senzing.neo4j.connector.graphdatabase.neo4j.CypherQueryGenerator;
import com.senzing.neo4j.connector.graphdatabase.neo4j.Neo4jConnection;
import com.senzing.neo4j.connector.service.graph.GraphService;

/**
 * This class services a Neo4j database.
 */
public class Neo4jService implements GraphService {

  Neo4jConnection connection;

  private String relationshipG2Tag;

  private static final String DEFAULT_EDGE_TYPE = "KNOWS";
  private static final String DEFAULT_RECORD_EDGE_TYPE = "BELONGS_TO";
  private static final String DEFAULT_EDGE_G2_TAG = "MATCH_KEY";
  private static final String EMPTY_STRING = "";

  public static Neo4jService generateNeo4jService(String config) throws ServiceSetupException {
    return new Neo4jService(config);
  }

  /**
   * The constructor gets the configuration and connects to the Neo4j database.
   * 
   * @throws ServiceSetupException
   */
  private Neo4jService(String config) throws ServiceSetupException {
    // Get configuration
    String uri = null;
    try {
      JSONObject configObject = new JSONObject(config);
      uri = configObject.optString(CommandOptions.NEO4J_CONNECTION);
      AppConfiguration javaConfiguration = new AppConfiguration();
      if (uri == null || uri.isEmpty()) {
        uri = javaConfiguration.getConfigValue(ConfigKeys.NEO4J_URI);
      }
      relationshipG2Tag = javaConfiguration.getConfigValue(ConfigKeys.RELATIONSHIP_G2_TAG);
      if (relationshipG2Tag == null) {
        relationshipG2Tag = DEFAULT_EDGE_G2_TAG;
      }
    } catch (IOException | JSONException e) {
      throw new ServiceSetupException(e);
    }

    // Verify that required values are set.
    if (uri == null || uri.isEmpty()) {
      List<String> configParams = new ArrayList<>();
      if (uri == null || uri.isEmpty()) {
        configParams.add(ConfigKeys.NEO4J_URI);
      }
      StringBuilder errorMessage = new StringBuilder("Following parameters missing from config file: ");
      errorMessage.append(String.join(", ", configParams));
      throw new ServiceSetupException(errorMessage.toString());
    }

    init(uri);
  }

  /**
   * Initializes and connect to the database.
   * 
   * @param uri The format is bolt://<user>:<password>@<host>:7687
   * @throws ServiceSetupException
   */
  private void init(String uri) throws ServiceSetupException {
    connection = new Neo4jConnection();
    try {
      connection.connect(uri);
    } catch (GraphDatabaseConnectionException e) {
      throw new ServiceSetupException(e);
    }
  }

  /**
   * Runs a query against Neo4j database.
   * 
   * @param cypherQuery Combined Cypher query string and parameters
   * @throws ServiceExecutionException
   */
  private void runQuery(CypherQuery cypherQuery) throws ServiceExecutionException {
    try {
      connection.runQuery(cypherQuery.getQuery(), cypherQuery.getParameters());
    } catch (GraphDatabaseConnectionException e) {
      throw new ServiceExecutionException(e);
    }
  }

  /**
   * Removes an entity from the graph, along with all its relationships.
   * 
   * @param entityID
   * @throws ServiceExecutionException
   */
  @Override
  public void removeEntity(Long entityID) throws ServiceExecutionException {
    Map<String, Object> criteria = new HashMap<>();
    criteria.put(G2Entity.ENTITY_ID_FIELD, entityID);
    CypherQuery deleteQuery = CypherQueryGenerator.deleteNodeAndDetachQuery(criteria);
    runQuery(deleteQuery);
  }

  /**
   * Adds a G2 entity to the graph.
   * 
   * @param entity
   * @throws ServiceExecutionException
   */
  @Override
  public void addEntity(G2Entity entity) throws ServiceExecutionException {
    Map<String, Object> features = new HashMap<>();
    features.putAll(entity.getFeatures());

    CypherQuery cypherQuery = CypherQueryGenerator.createNodeQuery(entity.getEntityType(), features);
    runQuery(cypherQuery);
  }

  /**
   * Adds relationships between G2 entities
   * 
   * @param entity contains the relationship data
   * @throws ServiceExecutionException
   */
  @Override
  public void addEntityRelationships(G2Entity entity) throws ServiceExecutionException {

    List<CypherQuery> relationshipQueries = new ArrayList<>();
    for (Long relEntityId : entity.getRelationships().keySet()) {
      Map<String, Object> relationship = entity.getRelationships().get(relEntityId);

      // Create relationship type. It is required since it labels the relationship.
      String edgeString = createRelationshipType(relationship, relationshipG2Tag);
      String relationshipType = edgeString.isEmpty() ? DEFAULT_EDGE_TYPE : edgeString;

      // Set up the query with each node as the criteria for end points of the
      // relationship.
      Map<String, Object> fromCriteria = new HashMap<>();
      fromCriteria.put(G2Entity.ENTITY_ID_FIELD, entity.getEntityId());
      Map<String, Object> toCriteria = new HashMap<>();
      toCriteria.put(G2Entity.ENTITY_ID_FIELD, relEntityId);

      // Build the relationship query.
      CypherQuery relQuery = CypherQueryGenerator.createRelationshipQuery(fromCriteria, toCriteria, relationshipType,
          relationship);
      relationshipQueries.add(relQuery);
    }

    // Execute the queries.
    for (CypherQuery relQuery : relationshipQueries) {
      runQuery(relQuery);
    }
  }

  /**
   * Adds relationships between a G2 entity data source records.
   * 
   * @param entity contains the relationship data
   * @throws ServiceExecutionException
   */
  @Override
  public void addRecordRelationsips(G2Entity entity) throws ServiceExecutionException {

    Map<Long, Map<String, Object>> records = entity.getRecords();

    List<CypherQuery> relationshipQueries = new ArrayList<>();
    for (Long recordID : records.keySet()) {
      Map<String, Object> g2Features = records.get(recordID);

      // Build the relationship query and execute.
      Map<String, Object> fromCriteria = new HashMap<>();
      fromCriteria.put(G2Entity.RECORD_ID_FIELD, g2Features.get(G2Entity.RECORD_ID_FIELD));
      fromCriteria.put(G2Entity.RECORD_DATA_SOURCE, g2Features.get(G2Entity.RECORD_DATA_SOURCE));
      Map<String, Object> toCriteria = new HashMap<>();
      // Get the tag that defines the relationship.
      toCriteria.put(G2Entity.ENTITY_ID_FIELD, entity.getEntityId());
      String relationshipTag = createRelationshipType(g2Features, relationshipG2Tag);
      if (relationshipTag.isEmpty()) {
        relationshipTag = createRelationshipType(g2Features, DEFAULT_EDGE_G2_TAG);
      }
      if (relationshipTag.isEmpty()) {
        relationshipTag = DEFAULT_RECORD_EDGE_TYPE;
      }

      CypherQuery recordRelQuery = CypherQueryGenerator.createRelationshipQuery(fromCriteria, toCriteria,
          relationshipTag.toString(), g2Features);
      relationshipQueries.add(recordRelQuery);
    }

    // Execute the queries.
    for (CypherQuery relQuery : relationshipQueries) {
      runQuery(relQuery);
    }
  }

  @Override
  public void cleanUp() {
    connection.close();
  }

  private String createRelationshipType(Map<String, Object> relationship, String tag) {
    Object tagValue = relationship.get(tag);
    if (tagValue == null || tagValue.toString().isEmpty()) {
      return EMPTY_STRING;
    }
    return wrapForSpecialCharacters(removeLeadingSymbols(tagValue.toString()));
  }

  // Adds back ticks around a string. They are needed for special characters like
  // '+', '-'. Neo4j rejects
  // the string otherwise.
  String wrapForSpecialCharacters(String value) {
    return "`" + value + "`";
  }

  // Removes any leading non-alphanumeric characters.
  static private String removeLeadingSymbols(String value) {
    return value.replaceFirst("[^A-Za-z0-9]*", "");
  }

}
