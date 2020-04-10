package com.senzing.neo4j.connector.graphdatabase;

import java.util.Iterator;
import java.util.Map;

import com.senzing.neo4j.connector.graphdatabase.exception.GraphDatabaseConnectionException;

public interface GraphDatabaseConnection {

	public void connect(String url) throws GraphDatabaseConnectionException;
	
	public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException;
}
