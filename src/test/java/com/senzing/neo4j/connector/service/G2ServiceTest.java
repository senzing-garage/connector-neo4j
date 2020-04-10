package com.senzing.neo4j.connector.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import com.senzing.g2.engine.G2JNI;
import com.senzing.neo4j.connector.data.g2.G2Definitions;
import com.senzing.neo4j.connector.service.exception.ServiceExecutionException;
import com.senzing.neo4j.connector.service.exception.ServiceSetupException;
import com.senzing.neo4j.connector.service.g2.G2Service;

@RunWith(JMockit.class)
public class G2ServiceTest {

  @Tested
  G2Service g2Service = null;
 
//  @Injectable
//  G2JNI g2JNIMock;
 

  @Before
  public void init() throws ServiceSetupException {
    new MockUp<G2JNI>() {
      @Mock
      public void $init(){
         
      }
    };
      g2Service = new G2Service();
//      g2Service.init("somefile");
  }

//  @Mock
//  G2JNI g2JnaMock;

  @Test
  public void getEntityWorksOK() throws ServiceExecutionException {
  
    new MockUp<G2JNI>() {
//      @Mock
//      public void $init(){
//         
//      }
      @Mock
      public int getEntityByEntityIDV2(long entityID, int flags, StringBuffer response) {
        response.append("{\"RESOLVED_ENTITY\":{\"ENTITY_ID\":2}}");
        
        return G2Definitions.G2_VALID_RESULT;
      }
  };
//  g2Service.getEntity(3L);

//    g2Service.getEntity(2L);
//    when(g2JnaMock.init("G2JNI", "/path/to/ini/file", false)).thenReturn(G2Definitions.G2_VALID_RESULT);
//    G2Service g2Service = new G2Service("/path/to/ini/file");

  }
}
