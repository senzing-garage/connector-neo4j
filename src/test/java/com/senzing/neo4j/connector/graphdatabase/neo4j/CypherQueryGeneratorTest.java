package com.senzing.neo4j.connector.graphdatabase.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CypherQueryGeneratorTest {
  @Test
  public void createNodeQueryWorkOK() {

    Map<String, Object> properties = new HashMap<>();
    properties.put("key1", "value1");
    properties.put("key2", 2);
    CypherQuery result = CypherQueryGenerator.createNodeQuery("Entity",properties);

    assertThat(result.getQuery(), is(equalTo("CREATE (:Entity $createProps)")));
    @SuppressWarnings("unchecked")
    Map<String, Object> params = (Map<String, Object>)result.getParameters().get("createProps");
    assertThat(params, is(notNullValue()));
    assertThat(params.get("key1"), is(equalTo("value1")));
    assertThat(params.get("key2"), is(equalTo(2)));
  }

  @Test
  public void createRelationshipQueryWorkOK() {

    Map<String, Object> fromCriteria = new HashMap<>();
    fromCriteria.put("ENTITY_ID", 7);

    Map<String, Object> toCriteria = new HashMap<>();
    toCriteria.put("ENTITY_ID", 8);

    Map<String, Object> properties = new HashMap<>();
    properties.put("key1", "value1");
    properties.put("key2", 2);
    CypherQuery result = CypherQueryGenerator.createRelationshipQuery(fromCriteria, toCriteria, "KNOWS", properties);

    assertThat(result.getQuery(), is(equalTo("MATCH (fromNode), (toNode) WHERE fromNode.ENTITY_ID=7 AND toNode.ENTITY_ID=8 CREATE (fromNode)-[:KNOWS $relProps]->(toNode)")));
    @SuppressWarnings("unchecked")
    Map<String, Object> params = (Map<String, Object>)result.getParameters().get("relProps");
    assertThat(params, is(notNullValue()));
    assertThat(params.get("key1"), is(equalTo("value1")));
    assertThat(params.get("key2"), is(equalTo(2)));
  }

  @Test
  public void deleteNodeAndDetachQueryWorkOK() {

    Map<String, Object> criteria = new HashMap<>();
    criteria.put("ENTITY_ID", 7);

    CypherQuery result = CypherQueryGenerator.deleteNodeAndDetachQuery(criteria);

    assertThat(result.getQuery(), is(equalTo("MATCH (node) WHERE node.ENTITY_ID=7 DETACH DELETE node")));
    assertThat(result.getParameters().size(), is(0));
  }

  @Test
  public void deleteAndDetachRelatedNodesByRelationshipLabelWorkOK() {

    Map<String, Object> criteria = new HashMap<>();
    criteria.put("ENTITY_ID", 7);

    CypherQuery result = CypherQueryGenerator.deleteAndDetachRelatedNodesByRelationshipLabel(criteria, "BELONGS_TO");

    assertThat(result.getQuery(), is(equalTo("MATCH (a) -[:BELONGS_TO]- (b) WHERE a.ENTITY_ID=7 DETACH DELETE b")));
    assertThat(result.getParameters().size(), is(0));
  }

}
