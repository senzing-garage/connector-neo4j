package com.senzing.neo4j.connector.service.graph;

import com.senzing.listener.service.exception.ServiceSetupException;
import com.senzing.neo4j.connector.service.graph.neo4j.Neo4jService;

public class GraphServiceFactory {

  /**
   * Generator for graph interface, based on graph type.
   * 
   * @param graphType
   * 
   * @return
   * 
   * @throws ServiceSetupException
   */
  static public GraphService generateGraphService(GraphType graphType, String config) throws ServiceSetupException {

    if (graphType == GraphType.neo4j) {
      return Neo4jService.generateNeo4jService(config);
    }

    StringBuilder errorMessage = new StringBuilder("Invalid graph type specified: ").append(graphType.toString());
    throw new ServiceSetupException(errorMessage.toString());
  }

}
