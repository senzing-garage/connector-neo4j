package com.senzing.neo4j.connector.service.graph;

import com.senzing.listener.service.exception.ServiceExecutionException;
import com.senzing.neo4j.connector.data.g2.G2Entity;

/**
 * Interface for graph services for G2 applications.
 */
public interface GraphService {

  /**
   * Removes an entity from the graph, along with all its relationships.
   * 
   * @param entityID Id specifying the targeted entity
   * @throws ServiceExecutionException
   */
  public void removeEntity(Long entityID) throws ServiceExecutionException;

  /**
   * Adds a G2 entity to the graph.
   * 
   * @param entity contains the g2 entity data
   * @throws ServiceExecutionException
   */
  public void addEntity(G2Entity entity) throws ServiceExecutionException;

  /**
   * Adds relationships between G2 entities
   * 
   * @param entity contains the relationship data
   * @throws ServiceExecutionException
   */
  public void addEntityRelationships(G2Entity entity) throws ServiceExecutionException;

  /**
   * Adds relationships between a G2 entity a data source records.
   * 
   * @param entity contains the relationship data
   * @throws ServiceExecutionException
   */
  public void addRecordRelationsips(G2Entity entity) throws ServiceExecutionException;

  /**
   * Cleans up the service after usage.
   */
  public void cleanUp();
}
