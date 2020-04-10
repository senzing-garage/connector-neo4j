package com.senzing.neo4j.connector.graphdatabase.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CypherQueryGenerator {

  static public CypherQuery createNodeQuery(final String nodeType, final Map<String, Object> properties) {
    final String baseQuery = "CREATE (:%s %s)";

    Map<String, Object> parameters = new HashMap<>();
    StringBuilder propVariable = new StringBuilder("");
    if (!properties.isEmpty()) {
      String propName = "createProps";
      parameters.put(propName, properties);
      propVariable.append('$').append(propName);
    }

    String query = String.format(baseQuery, nodeType, propVariable.toString());
    
    CypherQuery cypherQuery = new CypherQuery();
    cypherQuery.setQuery(query);
    cypherQuery.setParameters(parameters);

    return cypherQuery;
  }

  static public CypherQuery createRelationshipQuery(final Map<String, Object> fromCriteria, final Map<String, Object> toCriteria,
      final String relationshipType, final Map<String, Object> properties) {

    final String baseQuery = "MATCH (fromNode), (toNode) WHERE %s AND %s CREATE (fromNode)-[:%s %s]->(toNode)";

    String fromCriteriaString = generateExclusiveCriteria(fromCriteria, "fromNode");
    String toCriteriaString = generateExclusiveCriteria(toCriteria, "toNode");

    Map<String, Object> parameters = new HashMap<>();
    StringBuilder propVariable = new StringBuilder("");
    if (!properties.isEmpty()) {
      String propName = "relProps";
      parameters.put(propName, properties);
      propVariable.append('$').append(propName);
    }

    String query = String.format(baseQuery, fromCriteriaString, toCriteriaString, relationshipType, propVariable.toString());
    
    CypherQuery cypherQuery = new CypherQuery();
    cypherQuery.setQuery(query);
    cypherQuery.setParameters(parameters);

    return cypherQuery;
  }

  static public CypherQuery deleteNodeAndDetachQuery(Map<String, Object> criteria) {
    final String baseQuery = "MATCH (node) WHERE %s DETACH DELETE node";

    String criteriaString = generateExclusiveCriteria(criteria, "node");

    String query = String.format(baseQuery, criteriaString);
    Map<String, Object> parameters = new HashMap<>();

    CypherQuery cypherQuery = new CypherQuery();
    cypherQuery.setQuery(query);
    cypherQuery.setParameters(parameters);

    return cypherQuery;
  }

  static public CypherQuery deleteAndDetachRelatedNodesByRelationshipLabel(Map<String, Object> criteria, String relationshipLabel) {
    final String baseQuery = "MATCH (a) -[:%s]- (b) WHERE %s DETACH DELETE b";

    String criteriaString = generateExclusiveCriteria(criteria, "a");

    String query = String.format(baseQuery, relationshipLabel, criteriaString);
    Map<String, Object> parameters = new HashMap<>();

    CypherQuery cypherQuery = new CypherQuery();
    cypherQuery.setQuery(query);
    cypherQuery.setParameters(parameters);

    return cypherQuery;
  }

  static private String generateExclusiveCriteria(Map<String, Object> inCriteria, String prefix) {
    List<String> criteriaElements = inCriteria.keySet().stream().map(k -> prefix + "." + k + "=" + getStringValue(inCriteria.get(k))).collect(Collectors.toList());
    return String.join(" AND ", criteriaElements);
  }

  static private String getStringValue(Object value) {
    return (value instanceof String) ? "\"" + value + "\"" : value.toString();
  }

  private CypherQueryGenerator() {
    // do nothing
  }
}
