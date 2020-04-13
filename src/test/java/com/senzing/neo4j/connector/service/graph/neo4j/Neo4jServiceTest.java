package com.senzing.neo4j.connector.service.graph.neo4j;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.senzing.neo4j.connector.config.AppConfiguration;
import com.senzing.neo4j.connector.data.g2.G2Entity;
import com.senzing.neo4j.connector.graphdatabase.exception.GraphDatabaseConnectionException;
import com.senzing.neo4j.connector.graphdatabase.neo4j.Neo4jConnection;
import com.senzing.neo4j.connector.service.exception.ServiceExecutionException;
import com.senzing.neo4j.connector.service.exception.ServiceSetupException;
import com.senzing.neo4j.connector.service.graph.neo4j.Neo4jService;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;


@RunWith(JMockit.class)
public class Neo4jServiceTest {

  static Neo4jService service = null;

  static final String entityMessage = "{\"RESOLVED_ENTITY\":{\"ENTITY_ID\":2,\"LENS_CODE\":\"DEFAULT\",\"ENTITY_NAME\":\"JENNY SMITH\",\"FEATURES\":{\"ADDRESS\":[{\"FEAT_DESC\":\"808 STAR COURT LAS VEGAS NV 89222\",\"LIB_FEAT_ID\":12,\"UTYPE_CODE\":\"PRIMARY\",\"FEAT_DESC_VALUES\":[{\"FEAT_DESC\":\"808 STAR COURT LAS VEGAS NV 89222\",\"LIB_FEAT_ID\":12}]}],\"DOB\":[{\"FEAT_DESC\":\"1982-02-02\",\"LIB_FEAT_ID\":11,\"FEAT_DESC_VALUES\":[{\"FEAT_DESC\":\"1982-02-02\",\"LIB_FEAT_ID\":11}]}],\"NAME\":[{\"FEAT_DESC\":\"JENNY SMITH\",\"LIB_FEAT_ID\":10,\"UTYPE_CODE\":\"PRIMARY\",\"FEAT_DESC_VALUES\":[{\"FEAT_DESC\":\"JENNY SMITH\",\"LIB_FEAT_ID\":10}]}],\"REL_LINK\":[{\"FEAT_DESC\":\"OWNERSHIP 1001-2\",\"LIB_FEAT_ID\":21,\"UTYPE_CODE\":\"OWNER-OF\",\"FEAT_DESC_VALUES\":[{\"FEAT_DESC\":\"OWNERSHIP 1001-2\",\"LIB_FEAT_ID\":21}]},{\"FEAT_DESC\":\"OWNERSHIP 1002-2\",\"LIB_FEAT_ID\":25,\"UTYPE_CODE\":\"OWNER-OF\",\"FEAT_DESC_VALUES\":[{\"FEAT_DESC\":\"OWNERSHIP 1002-2\",\"LIB_FEAT_ID\":25}]},{\"FEAT_DESC\":\"OWNERSHIP 1003-2\",\"LIB_FEAT_ID\":28,\"UTYPE_CODE\":\"OWNER-OF\",\"FEAT_DESC_VALUES\":[{\"FEAT_DESC\":\"OWNERSHIP 1003-2\",\"LIB_FEAT_ID\":28}]}]},\"RECORD_SUMMARY\":[{\"DATA_SOURCE\":\"PEOPLE\",\"RECORD_COUNT\":3,\"FIRST_SEEN_DT\":\"2020-04-05 01:59:27.438\",\"LAST_SEEN_DT\":\"2020-04-05 01:59:28.643\"}],\"LAST_SEEN_DT\":\"2020-04-05 01:59:28.643\",\"RECORDS\":[{\"JSON_DATA\":{\"RECORD_ID\":\"1001-2\",\"PRIMARY_NAME_LAST\":\"Smith\",\"PRIMARY_NAME_FIRST\":\"Jenny\",\"DATE_OF_BIRTH\":\"2/2/82\",\"PRIMARY_ADDR_LINE1\":\"808 Star Court\",\"PRIMARY_ADDR_CITY\":\"Las Vegas\",\"PRIMARY_ADDR_STATE\":\"NV\",\"PRIMARY_ADDR_POSTAL_CODE\":\"89222\",\"OWNER-OF_RELATIONSHIP_TYPE\":\"OWNERSHIP\",\"OWNER-OF_RELATIONSHIP_KEY\":\"1001-2\",\"OWNER PERCENT\":\"Owns 12 % of ABC Corp\",\"OWNER_TYPE\":\"Individual\",\"DATA_SOURCE\":\"PEOPLE\",\"ENTITY_TYPE\":\"PEOPLE\",\"DSRC_ACTION\":\"A\",\"LENS_CODE\":\"DEFAULT\"},\"DATA_SOURCE\":\"PEOPLE\",\"ENTITY_TYPE\":\"PEOPLE\",\"ENTITY_KEY\":\"17DC8FB37EFD8C7323B1AFE7E2894580B2890763\",\"ENTITY_NAME\":\"Jenny Smith\",\"RECORD_ID\":\"1001-2\",\"MATCH_KEY\":\"\",\"MATCH_SCORE\":\"\",\"ERRULE_CODE\":\"\",\"REF_SCORE\":0,\"MATCH_LEVEL\":0,\"MATCH_LEVEL_CODE\":\"\",\"LAST_SEEN_DT\":\"2020-04-05 01:59:27.438\",\"NAME_DATA\":[\"PRIMARY: Smith Jenny\"],\"ATTRIBUTE_DATA\":[\"DOB: 2/2/82\"],\"IDENTIFIER_DATA\":[],\"ADDRESS_DATA\":[\"PRIMARY: 808 Star Court Las Vegas NV 89222\"],\"PHONE_DATA\":[],\"RELATIONSHIP_DATA\":[\"REL_LINK: OWNER-OF: OWNERSHIP 1001-2\"],\"ENTITY_DATA\":[],\"OTHER_DATA\":[\"OWNER_TYPE: Individual\",\"OWNER PERCENT: Owns 12 % of ABC Corp\"],\"INTERNAL_ID\":2,\"FEATURES\":[{\"LIB_FEAT_ID\":10,\"UTYPE_CODE\":\"PRIMARY\"},{\"LIB_FEAT_ID\":11},{\"LIB_FEAT_ID\":12,\"UTYPE_CODE\":\"PRIMARY\"},{\"LIB_FEAT_ID\":13},{\"LIB_FEAT_ID\":14},{\"LIB_FEAT_ID\":15},{\"LIB_FEAT_ID\":16},{\"LIB_FEAT_ID\":17},{\"LIB_FEAT_ID\":18},{\"LIB_FEAT_ID\":19},{\"LIB_FEAT_ID\":20},{\"LIB_FEAT_ID\":21,\"UTYPE_CODE\":\"OWNER-OF\"}]},{\"JSON_DATA\":{\"RECORD_ID\":\"1002-2\",\"PRIMARY_NAME_LAST\":\"Smith\",\"PRIMARY_NAME_FIRST\":\"Jenny\",\"DATE_OF_BIRTH\":\"2/2/82\",\"PRIMARY_ADDR_LINE1\":\"808 Star Court\",\"PRIMARY_ADDR_CITY\":\"Las Vegas\",\"PRIMARY_ADDR_STATE\":\"NV\",\"PRIMARY_ADDR_POSTAL_CODE\":\"89222\",\"OWNER-OF_RELATIONSHIP_TYPE\":\"OWNERSHIP\",\"OWNER-OF_RELATIONSHIP_KEY\":\"1002-2\",\"OWNER PERCENT\":\"Owns 22 % of BBB Business Brokers\",\"OWNER_TYPE\":\"Individual\",\"DATA_SOURCE\":\"PEOPLE\",\"ENTITY_TYPE\":\"PEOPLE\",\"DSRC_ACTION\":\"A\",\"LENS_CODE\":\"DEFAULT\"},\"DATA_SOURCE\":\"PEOPLE\",\"ENTITY_TYPE\":\"PEOPLE\",\"ENTITY_KEY\":\"A5A3EFA46629C5DD5B4C2D90406D6CE9704E2178\",\"ENTITY_NAME\":\"Jenny Smith\",\"RECORD_ID\":\"1002-2\",\"MATCH_KEY\":\"+NAME+DOB+ADDRESS\",\"MATCH_SCORE\":\"13\",\"ERRULE_CODE\":\"CNAME_CFF_CEXCL\",\"REF_SCORE\":8,\"MATCH_LEVEL\":1,\"MATCH_LEVEL_CODE\":\"RESOLVED\",\"LAST_SEEN_DT\":\"2020-04-05 01:59:28.547\",\"NAME_DATA\":[\"PRIMARY: Smith Jenny\"],\"ATTRIBUTE_DATA\":[\"DOB: 2/2/82\"],\"IDENTIFIER_DATA\":[],\"ADDRESS_DATA\":[\"PRIMARY: 808 Star Court Las Vegas NV 89222\"],\"PHONE_DATA\":[],\"RELATIONSHIP_DATA\":[\"REL_LINK: OWNER-OF: OWNERSHIP 1002-2\"],\"ENTITY_DATA\":[],\"OTHER_DATA\":[\"OWNER_TYPE: Individual\",\"OWNER PERCENT: Owns 22 % of BBB Business Brokers\"],\"INTERNAL_ID\":4,\"FEATURES\":[{\"LIB_FEAT_ID\":10,\"UTYPE_CODE\":\"PRIMARY\"},{\"LIB_FEAT_ID\":11},{\"LIB_FEAT_ID\":12,\"UTYPE_CODE\":\"PRIMARY\"},{\"LIB_FEAT_ID\":13},{\"LIB_FEAT_ID\":14},{\"LIB_FEAT_ID\":15},{\"LIB_FEAT_ID\":16},{\"LIB_FEAT_ID\":17},{\"LIB_FEAT_ID\":18},{\"LIB_FEAT_ID\":19},{\"LIB_FEAT_ID\":20},{\"LIB_FEAT_ID\":25,\"UTYPE_CODE\":\"OWNER-OF\"}]},{\"JSON_DATA\":{\"RECORD_ID\":\"1003-2\",\"PRIMARY_NAME_LAST\":\"Smith\",\"PRIMARY_NAME_FIRST\":\"Jenny\",\"DATE_OF_BIRTH\":\"\",\"PRIMARY_ADDR_LINE1\":\"808 Star Court\",\"PRIMARY_ADDR_CITY\":\"Las Vegas\",\"PRIMARY_ADDR_STATE\":\"NV\",\"PRIMARY_ADDR_POSTAL_CODE\":\"89222\",\"OWNER-OF_RELATIONSHIP_TYPE\":\"OWNERSHIP\",\"OWNER-OF_RELATIONSHIP_KEY\":\"1003-2\",\"OWNER PERCENT\":\"Owns 32 % of BNC Connections\",\"OWNER_TYPE\":\"Individual\",\"DATA_SOURCE\":\"PEOPLE\",\"ENTITY_TYPE\":\"PEOPLE\",\"DSRC_ACTION\":\"A\",\"LENS_CODE\":\"DEFAULT\"},\"DATA_SOURCE\":\"PEOPLE\",\"ENTITY_TYPE\":\"PEOPLE\",\"ENTITY_KEY\":\"BACD68990D9811B7F39DB2AE2CE1AE055FAB0E92\",\"ENTITY_NAME\":\"Jenny Smith\",\"RECORD_ID\":\"1003-2\",\"MATCH_KEY\":\"+NAME+ADDRESS\",\"MATCH_SCORE\":\"12\",\"ERRULE_CODE\":\"CNAME_CFF\",\"REF_SCORE\":6,\"MATCH_LEVEL\":1,\"MATCH_LEVEL_CODE\":\"RESOLVED\",\"LAST_SEEN_DT\":\"2020-04-05 01:59:28.643\",\"NAME_DATA\":[\"PRIMARY: Smith Jenny\"],\"ATTRIBUTE_DATA\":[],\"IDENTIFIER_DATA\":[],\"ADDRESS_DATA\":[\"PRIMARY: 808 Star Court Las Vegas NV 89222\"],\"PHONE_DATA\":[],\"RELATIONSHIP_DATA\":[\"REL_LINK: OWNER-OF: OWNERSHIP 1003-2\"],\"ENTITY_DATA\":[],\"OTHER_DATA\":[\"OWNER_TYPE: Individual\",\"OWNER PERCENT: Owns 32 % of BNC Connections\"],\"INTERNAL_ID\":6,\"FEATURES\":[{\"LIB_FEAT_ID\":10,\"UTYPE_CODE\":\"PRIMARY\"},{\"LIB_FEAT_ID\":12,\"UTYPE_CODE\":\"PRIMARY\"},{\"LIB_FEAT_ID\":13},{\"LIB_FEAT_ID\":14},{\"LIB_FEAT_ID\":15},{\"LIB_FEAT_ID\":18},{\"LIB_FEAT_ID\":19},{\"LIB_FEAT_ID\":20},{\"LIB_FEAT_ID\":28,\"UTYPE_CODE\":\"OWNER-OF\"}]}]},\"RELATED_ENTITIES\":[{\"ENTITY_ID\":1001,\"LENS_CODE\":\"DEFAULT\",\"MATCH_LEVEL\":11,\"MATCH_LEVEL_CODE\":\"DISCLOSED\",\"MATCH_KEY\":\"+OWNERSHIP\",\"MATCH_SCORE\":\"10\",\"ERRULE_CODE\":\"DISCLOSED\",\"REF_SCORE\":0,\"IS_DISCLOSED\":1,\"IS_AMBIGUOUS\":0,\"ENTITY_NAME\":\"ABC COMPANY\",\"DISCLOSED_FROM_DATE\":\"\",\"DISCLOSED_THRU_DATE\":\"\",\"RECORD_SUMMARY\":[{\"DATA_SOURCE\":\"COMPANIES\",\"RECORD_COUNT\":1,\"FIRST_SEEN_DT\":\"2020-04-05 01:59:48.441\",\"LAST_SEEN_DT\":\"2020-04-05 01:59:48.441\"}],\"LAST_SEEN_DT\":\"2020-04-05 01:59:48.441\"},{\"ENTITY_ID\":1002,\"LENS_CODE\":\"DEFAULT\",\"MATCH_LEVEL\":11,\"MATCH_LEVEL_CODE\":\"DISCLOSED\",\"MATCH_KEY\":\"+OWNERSHIP\",\"MATCH_SCORE\":\"10\",\"ERRULE_CODE\":\"DISCLOSED\",\"REF_SCORE\":0,\"IS_DISCLOSED\":1,\"IS_AMBIGUOUS\":0,\"ENTITY_NAME\":\"BBB BUSINESS BROKERS\",\"DISCLOSED_FROM_DATE\":\"\",\"DISCLOSED_THRU_DATE\":\"\",\"RECORD_SUMMARY\":[{\"DATA_SOURCE\":\"COMPANIES\",\"RECORD_COUNT\":1,\"FIRST_SEEN_DT\":\"2020-04-05 01:59:56.600\",\"LAST_SEEN_DT\":\"2020-04-05 01:59:56.600\"}],\"LAST_SEEN_DT\":\"2020-04-05 01:59:56.600\"},{\"ENTITY_ID\":1003,\"LENS_CODE\":\"DEFAULT\",\"MATCH_LEVEL\":11,\"MATCH_LEVEL_CODE\":\"DISCLOSED\",\"MATCH_KEY\":\"+OWNERSHIP\",\"MATCH_SCORE\":\"10\",\"ERRULE_CODE\":\"DISCLOSED\",\"REF_SCORE\":0,\"IS_DISCLOSED\":1,\"IS_AMBIGUOUS\":0,\"ENTITY_NAME\":\"BNC CONNECTIONS\",\"DISCLOSED_FROM_DATE\":\"\",\"DISCLOSED_THRU_DATE\":\"\",\"RECORD_SUMMARY\":[{\"DATA_SOURCE\":\"COMPANIES\",\"RECORD_COUNT\":1,\"FIRST_SEEN_DT\":\"2020-04-05 01:59:56.611\",\"LAST_SEEN_DT\":\"2020-04-05 01:59:56.611\"}],\"LAST_SEEN_DT\":\"2020-04-05 01:59:56.611\"}]}";

  static G2Entity g2Entity = null;
 
  @BeforeClass
  public static void init() throws ServiceSetupException, JSONException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public void connect(String uri) throws GraphDatabaseConnectionException {
      }
    };
    service  = Neo4jService.generateNeo4jService();
    g2Entity = new G2Entity(entityMessage);
  }

  @Before
  public void setup() {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        return null;
      }
    };
  }

  @Test
  public void removeEntityWorksOK() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        assertThat(query, is(equalTo("MATCH (node) WHERE node.ENTITY_ID=2 DETACH DELETE node")));
        assertThat(params.size(), is(equalTo(0)));
        return null;
      }
    };
    service.removeEntity(2L);
  }
  
  @Test(expected=ServiceExecutionException.class)
  public void removeEntityHandlesDatabaseException() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        throw new GraphDatabaseConnectionException("Failure");
      }
    };
    service.removeEntity(2L);
  }
  
  @Test
  public void addEntityWorksOK() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        assertThat(query, is(equalTo("CREATE (:PEOPLE $createProps)")));
        assertThat(params.size(), is(equalTo(1)));
        return null;
      }
    };
    service.addEntity(g2Entity);
  }
  
  @Test(expected=ServiceExecutionException.class)
  public void addEntityHandlesDatabaseException() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        throw new GraphDatabaseConnectionException("Failure");
      }
    };
    service.addEntity(g2Entity);
  }
  
  @Test
  public void addEntityRelationshipsWorksOK() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        assertThat(query, containsString("ENTITY_ID="));
        assertThat(params.size(), is(equalTo(1)));
        return null;
      }
    };
    service.addEntityRelationships(g2Entity);
  }

  @Test(expected=ServiceExecutionException.class)
  public void addEntityRelationshipsHandlesDatabaseException() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        throw new GraphDatabaseConnectionException("Failure");
      }
    };
    service.addEntityRelationships(g2Entity);
  }

  @Test
  public void addRecordRelationsipsWorksOK() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        assertThat(query, containsString("RECORD_ID="));
        assertThat(params.size(), is(equalTo(1)));
        return null;
      }
    };
    service.addRecordRelationsips(g2Entity);
  }

  @Test(expected=ServiceExecutionException.class)
  public void addRecordRelationsipsHandlesDatabaseException() throws ServiceExecutionException {
    new MockUp<Neo4jConnection>() {
      @Mock
      public Iterator<Map<String, Object>> runQuery(String query, Map<String, Object> params) throws GraphDatabaseConnectionException {
        throw new GraphDatabaseConnectionException("Failure");
      }
    };
    service.addRecordRelationsips(g2Entity);
  }

  @Test(expected=ServiceSetupException.class)
  public void generateNeo4jServiceHandlesMissingUri() throws ServiceExecutionException, ServiceSetupException {
    new MockUp<AppConfiguration>() {
      @Mock
      public String getConfigValue(String configParameter) {
        return null;
      }
    };
    Neo4jService.generateNeo4jService();
  }

  @Test(expected=ServiceSetupException.class)
  public void generateNeo4jServiceHandlesDataabaseException() throws ServiceExecutionException, ServiceSetupException {
    new MockUp<AppConfiguration>() {
      @Mock
      public String getConfigValue(String configParameter) {
        return "bolt://user:password@host:1234";
      }
    };
    new MockUp<Neo4jConnection>() {
      @Mock
      public void connect(String uri) throws GraphDatabaseConnectionException {
        throw new GraphDatabaseConnectionException("Failure");
      }
    };
    Neo4jService.generateNeo4jService();
  }

}
