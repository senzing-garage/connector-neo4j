package com.senzing.neo4j.connector.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class handles configuration for Neo4j service.
 */
public class AppConfiguration {

  Properties prop;

  static private final String CONFIG_FILE_NAME = "neo4jconnector.properties";

  /**
   * Reads configuration from config file into memory.
   * 
   * @throws IOException
   */
  public AppConfiguration() throws IOException {

    prop = new Properties();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
    
    if (inputStream != null) {
      prop.load(inputStream);
    } else {
      throw new FileNotFoundException("property file '" + CONFIG_FILE_NAME + "' not found in the classpath");
    }
    inputStream.close();
  }

  /**
   * Gets a parameter from configuration.
   * 
   * @param configParameter Name of the parameter
   * @return Value of the parameter
   */
  public String getConfigValue(String configParameter) {
    return prop.getProperty(configParameter);
  }

}
