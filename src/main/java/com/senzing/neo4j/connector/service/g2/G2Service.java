package com.senzing.neo4j.connector.service.g2;

import com.senzing.g2.engine.G2Engine;
import com.senzing.g2.engine.G2JNI;
import com.senzing.neo4j.connector.data.g2.G2Definitions;
import com.senzing.neo4j.connector.service.exception.ServiceExecutionException;
import com.senzing.neo4j.connector.service.exception.ServiceSetupException;

/**
 * This class handles communication with G2.  It sets up an instance of G2 and interacts with it (get entities etc.).
 */
public class G2Service {
  private G2Engine g2Engine;
  private String iniFile;
  static final String moduleName = "G2JNI";

  /**
   * Default constructor.
   */
  public G2Service() {
  }

  public void init(String iniFile) throws ServiceSetupException {
    this.iniFile = iniFile;
    boolean verboseLogging = false;

    g2Engine = new G2JNI();
    int result = g2Engine.init(moduleName, this.iniFile, verboseLogging);
    if (result != G2Definitions.G2_VALID_RESULT) {
      StringBuilder errorMessage = new StringBuilder("G2 engine failed to initalize with error: ");
      errorMessage.append(g2ErrorMessage(g2Engine));
      throw new ServiceSetupException(errorMessage.toString());
    }
  }

  /** 
   * Gets an entity for an entity id.
   * 
   * @param g2EntiyId
   * @return
   * @throws ServiceExecutionException
   */
  public String getEntity(long g2EntiyId) throws ServiceExecutionException {
    StringBuffer response = new StringBuffer();
    int result = g2Engine.getEntityByEntityIDV2(g2EntiyId, G2Engine.G2_ENTITY_DEFAULT_FLAGS, response);
    if (result != G2Definitions.G2_VALID_RESULT) {
      StringBuilder errorMessage = new StringBuilder("G2 engine failed to retrieve an entity with error: ");
      errorMessage.append(g2ErrorMessage(g2Engine));
      throw new ServiceExecutionException(errorMessage.toString());
    }
    return response.toString();

  }

  /**
   * Gets and entity for a data source and record id.
   * 
   * @param dataSource
   * @param recordId
   * @return
   * @throws ServiceExecutionException
   */
  public String getEntity(String dataSource, String recordId) throws ServiceExecutionException {
    StringBuffer response = new StringBuffer();
    int result = g2Engine.getEntityByRecordIDV2(dataSource, recordId, G2Engine.G2_ENTITY_DEFAULT_FLAGS, response);
    if (result != G2Definitions.G2_VALID_RESULT) {
      StringBuilder errorMessage = new StringBuilder("G2 engine failed to retrieve an entity with error: ");
      errorMessage.append(g2ErrorMessage(g2Engine));
      throw new ServiceExecutionException(errorMessage.toString());
    }
    return response.toString();
  }

  static private String g2ErrorMessage(G2Engine g2Engine) {
    return g2Engine.getLastExceptionCode() + ", " + g2Engine.getLastException();
  }
}
