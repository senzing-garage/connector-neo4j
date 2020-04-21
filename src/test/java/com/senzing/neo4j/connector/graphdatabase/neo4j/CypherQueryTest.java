package com.senzing.neo4j.connector.graphdatabase.neo4j;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import org.junit.Test;

import com.senzing.neo4j.connector.graphdatabase.neo4j.CypherQuery;

public class CypherQueryTest {

  private static String queryString = "CREATE (:PEOPLE $params)";
  @Test
  public void settersAndGettersWorkOK() {
    CypherQuery query = new CypherQuery();
    query.setQuery(queryString);
    query.setParameters(new HashMap<String, Object>());

    assertThat(query.getQuery(), is(equalTo(queryString)));
    assertThat(query.getParameters(), is(notNullValue()));
  }
}
