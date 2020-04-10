package com.senzing.neo4j.connector.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.senzing.neo4j.connector.config.AppConfiguration;
import com.senzing.neo4j.connector.config.ConfigKeys;
import com.senzing.neo4j.connector.data.g2.G2Entity;
import com.senzing.neo4j.connector.service.exception.ServiceExecutionException;
import com.senzing.neo4j.connector.service.exception.ServiceSetupException;
import com.senzing.neo4j.connector.service.g2.G2Service;
import com.senzing.neo4j.connector.service.graph.GraphService;
import com.senzing.neo4j.connector.service.graph.GraphServiceFactory;
import com.senzing.neo4j.connector.service.graph.GraphType;

/**
 * This class handles interactions between G2 and graph database.
 */
public class MainService {

  private GraphService graphService;
  private G2Service g2Service;

  /**
   * The constructor sets up the service.  It creates a G2 service and Neo4j service and sets 
   * configuration parameters.
   * 
   * @throws IOException
   * @throws ServiceSetupException 
   */
  public MainService() throws ServiceSetupException {

    // Get configuration
    String g2IniFile = null;
    String graphDbType = null;
    try {
      AppConfiguration config = new AppConfiguration();
      g2IniFile = config.getConfigValue(ConfigKeys.G2_INI_FILE);
      graphDbType = config.getConfigValue(ConfigKeys.GRAPH_DATABASE_TYPE);
    } catch (IOException e) {
      throw new ServiceSetupException(e);
    }

    // Verify that required values are set.
    List<String> configParams = new ArrayList<>();
    if (g2IniFile == null || g2IniFile.isEmpty() ) {
      configParams.add(ConfigKeys.G2_INI_FILE);
    }
    if (graphDbType == null || graphDbType.isEmpty() ) {
      configParams.add(ConfigKeys.GRAPH_DATABASE_TYPE);
    }
    if (configParams.size() > 0) {
      StringBuilder errorMessage = new StringBuilder("Following parameters missing from config file: ");
      errorMessage.append(String.join(", ", configParams));
      throw new ServiceSetupException(errorMessage.toString());
    }

    g2Service = new G2Service();
    g2Service.init(g2IniFile);

    GraphType graphType;
    try {
      graphType = GraphType.valueOf(graphDbType);
    } catch (IllegalArgumentException | NullPointerException e) {
      StringBuilder errorMessage = new StringBuilder("Invalid graph database type specified: ").append(graphDbType);
      throw new ServiceSetupException(errorMessage.toString(), e);
    }
    graphService = GraphServiceFactory.generateGraphService(graphType);
  }

  /**
   * Process G2 records.  It retrieves an entity from G2 and loads it into Neo4j database.
   * 
   * @param entityID The id of the entity in the G2 data store.
   * @throws ServiceExecutionException 
   */
  public void processEntity(Long entityID) throws ServiceExecutionException {
    String message = g2Service.getEntity(entityID);

    // Start by removing the entity. The "message" contains the current state of the entity so it is 
    // easier to remove the existing one and then re-add it rather than update. If the message is empty
    // then it needs to be removed anyway.
    graphService.removeEntity(entityID);

    if (message != null) {
      G2Entity entity = null;

      try {
        entity = new G2Entity(message);
      } catch (JSONException e) {
        throw new ServiceExecutionException(e);
      }

      graphService.addEntity(entity);

      if (!entity.getRelationships().isEmpty()) {
        graphService.addEntityRelationships(entity);
      }

      if (!entity.getRecords().isEmpty()) {
        graphService.addRecordRelationsips(entity);
      }
    }
  }


}
