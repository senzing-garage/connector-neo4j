package com.senzing.neo4j.connector.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import com.senzing.util.JsonUtilities;
import com.senzing.listener.service.ListenerService;
import com.senzing.listener.service.exception.ServiceExecutionException;
import com.senzing.listener.service.exception.ServiceSetupException;
import com.senzing.listener.service.g2.G2Service;
import com.senzing.neo4j.connector.cmdline.CommandOptions;
import com.senzing.neo4j.connector.config.AppConfiguration;
import com.senzing.neo4j.connector.config.ConfigKeys;
import com.senzing.neo4j.connector.config.EnvVariables;
import com.senzing.neo4j.connector.data.g2.G2Entity;
import com.senzing.neo4j.connector.service.graph.GraphService;
import com.senzing.neo4j.connector.service.graph.GraphServiceFactory;
import com.senzing.neo4j.connector.service.graph.GraphType;

import static com.senzing.listener.service.ListenerService.*;
import static com.senzing.listener.service.ListenerService.State.*;
import static com.senzing.util.JsonUtilities.*;

/**
 * This class handles interactions between G2 and graph database.
 */
public class Neo4jConnectorService implements ListenerService {

  // Tags for incoming message.
  private static final String AFFECTED_ENTITIES_TAG = "AFFECTED_ENTITIES";
  // Tags for statistics message.
  private static final String TOTAL_COUNT = "TotalCount";
  private static final String FAILED_COUNT = "FailedCount";

  private GraphService graphService;
  private G2Service g2Service;
  private boolean serviceUp;

  private State state = UNINITIALIZED;

  private long totalCount;
  private long failedCount;

  /**
   * Sets up the service. It creates a G2 service and Neo4j
   * service and sets configuration parameters.
   * 
   * @throws ServiceSetupException
   */
  @Override
  public void init(JsonObject config) throws ServiceSetupException {
    this.setState(INITIALIZING);
    // Get configuration
    String g2IniFile = null;
    String graphDbType = null;
    String senzingConfig = System.getenv(EnvVariables.SENZING_ENGINE_CONFIGURATION_JSON);
    try {
      g2IniFile = JsonUtilities.getString(config, CommandOptions.INI_FILE);
      graphDbType = JsonUtilities.getString(config, CommandOptions.GRAPH_TYPE);
      AppConfiguration appConfig = new AppConfiguration();
      graphDbType = appConfig.getConfigValue(ConfigKeys.GRAPH_DATABASE_TYPE);
      if (g2IniFile == null || g2IniFile.isEmpty()) {
        g2IniFile = appConfig.getConfigValue(ConfigKeys.G2_INI_FILE);
      }
    } catch (IOException | JSONException e) {
      throw new ServiceSetupException(e);
    }

    // Verify that required values are set.
    List<String> missingConfigParams = new ArrayList<>();
    if ((g2IniFile == null || g2IniFile.isEmpty()) && (senzingConfig == null || senzingConfig.isEmpty())) {
      missingConfigParams.add(ConfigKeys.G2_INI_FILE);
    }
    if (graphDbType == null || graphDbType.isEmpty()) {
      missingConfigParams.add(ConfigKeys.GRAPH_DATABASE_TYPE);
    }
    if (missingConfigParams.size() > 0) {
      StringBuilder errorMessage = new StringBuilder("Following parameters missing from config file: ");
      errorMessage.append(String.join(", ", missingConfigParams));
      throw new ServiceSetupException(errorMessage.toString());
    }

    JsonObjectBuilder job = Json.createObjectBuilder();
    if (g2IniFile != null && !g2IniFile.isEmpty()) {
      job.add(G2Service.G2_INIT_CONFIG_KEY, g2IniFile);
    } else {
      job.add(G2Service.G2_INIT_CONFIG_KEY, senzingConfig);
    }
    job.add(G2Service.G2_MODULE_NAME_KEY, "Neo4jConnector");

    JsonObject g2Config = job.build();

    g2Service = new G2Service();
    g2Service.init(g2Config);

    GraphType graphType;
    try {
      graphType = GraphType.valueOf(graphDbType);
    } catch (IllegalArgumentException | NullPointerException e) {
      StringBuilder errorMessage = new StringBuilder("Invalid graph database type specified: ").append(graphDbType);
      throw new ServiceSetupException(errorMessage.toString(), e);
    }
    graphService = GraphServiceFactory.generateGraphService(graphType, config);

    serviceUp = true;

    totalCount = 0;
    failedCount = 0;
    this.setState(AVAILABLE);
  }

  /**
   * Implemented to return the statistics associated with this instance.
   * 
   * {@inheritDoc}
   */
  @Override
  public synchronized Map<Statistic, Number> getStatistics() {
      return Collections.emptyMap();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized State getState() {
    return this.state;
  }

    /**
   * Provides a means to set the {@link State} for this instance as a
   * synchronized method that will notify all upon changing the state.
   *
   * @param state The {@link State} for this instance.
   */
  protected synchronized void setState(State state) {
    Objects.requireNonNull(state,"State cannot be null");
    this.state = state;
    this.notifyAll();
  }

  /**
   * Process G2 records. It retrieves an entity from G2 and loads it into Neo4j
   * database.
   * 
   * @param entityID The id of the entity in the G2 data store.
   * 
   * @throws ServiceExecutionException
   */
  public void processEntity(Long entityID) throws ServiceExecutionException {
    totalCount++;
    String message = g2Service.getEntity(entityID, false, false);

    // Start by removing the entity. The "message" contains the current state of the
    // entity so it is easier to remove the existing one and then re-add it rather
    // than update. If the message is empty then it needs to be removed anyway.
    graphService.removeEntity(entityID);

    if (message != null) {
      G2Entity entity = null;

      try {
        entity = new G2Entity(message);
      } catch (JSONException e) {
        failedCount++;
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

  /**
   * Processes messages containing a list of entity IDs.  The IDs are used to get the entity
   * information from G2 and add that data to a graph database.
   * 
   * @param message Json message containing list of entity IDs.
   * 
   * @throws ServiceExecutionException
   */
  @Override
  public void process(JsonObject message) throws ServiceExecutionException {
    // The message should be of format:
    // {
    //   "DATA_SOURCE":"TEST",
    //   "RECORD_ID":"RECORD3",
    //   "AFFECTED_ENTITIES":[
    //     {"ENTITY_ID":1,"LENS_CODE":"DEFAULT"}
    //   ]
    // }
    try {
      // We are only interested in the entity ids from the AFFECTED_ENTITIES section.
      JsonArray entities = message.getJsonArray(AFFECTED_ENTITIES_TAG);
      if (entities != null) {
        for (int i = 0; i < entities.size(); i++) {
          JsonObject entity = entities.getJsonObject(i);
          if (entity != null) {
            //Long entityID = entity.getJsonNumber(Definitions.ENTITY_ID_FIELD).longValue();
            Long entityID = entity.getJsonNumber(G2Entity.ENTITY_ID_FIELD).longValue();
            processEntity(entityID);
          }
        }
      }
    } catch (RuntimeException e) {
      throw new ServiceExecutionException(e);
    }
  }

  /**
   * Returns statistics for messages processed so far for this run.
   * 
   * @return Statistics in json format: {"totalCount": 10023, "failedCount": 3}
   */
  public String getStats() {
    StringBuilder statJson = new StringBuilder();
    statJson.append('{');
    statJson.append('"').append(TOTAL_COUNT).append('"').append(": ").append(Long.valueOf(totalCount));
    statJson.append(", ");
    statJson.append('"').append(FAILED_COUNT).append('"').append(": ").append(Long.valueOf(failedCount));
    statJson.append('}');
    return statJson.toString();
  }

  /**
   * Cleans up the service.  Closes G2 and graph database connections and frees up resources.
   */
  @Override
  public void destroy() {
    this.setState(DESTROYING);
    g2Service.destroy();
    graphService.cleanUp();
    this.setState(DESTROYED);
  }

  /**
   * Returns whether the service is up or not.
   * 
   * @return true for up, false for down
   */
  public boolean isServiceUp() {
    return serviceUp;
  }

}
