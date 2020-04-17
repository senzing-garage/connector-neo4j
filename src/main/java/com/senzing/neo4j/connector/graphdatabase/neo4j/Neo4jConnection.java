package com.senzing.neo4j.connector.graphdatabase.neo4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Config;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import com.senzing.neo4j.connector.graphdatabase.GraphDatabaseConnection;
import com.senzing.neo4j.connector.graphdatabase.exception.GraphDatabaseConnectionException;

/**
 * This class handles communication with Neo4j graph.  It opens a connection and runs queries against it.
 */
public class Neo4jConnection implements GraphDatabaseConnection {

  private Driver driver;

  @Override
  public void connect(String uri) throws GraphDatabaseConnectionException {
    // OT-TODO: handle user info so password not in clear text
    String auth = null;
    try {
      auth = new URL(uri.replace("bolt", "http")).getUserInfo();
    } catch (MalformedURLException e) {
      throw new GraphDatabaseConnectionException(e);
    }
    if (auth == null) {
      throw new GraphDatabaseConnectionException("Userinfo missing from Neo4j URI");
    }
    String[] parts = auth.split(":");
    String user = parts[0];
    String password = parts[1];
    boolean hasPassword = password != null && !password.isEmpty();
    AuthToken token = hasPassword ? AuthTokens.basic(user, password) : AuthTokens.none();

    // OT-TODO: add encryption option
    driver = GraphDatabase.driver(uri, token);
  }

  @Override
  public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
    try (Session session = driver.session()) {
      List<Map<String, Object>> list = session.run(query, params).list(r -> r.asMap(Neo4jConnection::convert));
      return list.iterator();
    } catch (Exception e) {
      throw new GraphDatabaseConnectionException(e);
    }
  }

  static Object convert(Value value) {
    switch (value.type().name()) {
    case "PATH":
      return value.asList(Neo4jConnection::convert);
    case "NODE":
    case "RELATIONSHIP":
      return value.asMap();
    }
    return value.asObject();
  }

}
