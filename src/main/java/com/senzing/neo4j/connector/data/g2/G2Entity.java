package com.senzing.neo4j.connector.data.g2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is for holding information about G2 entities. The information can
 * be set by using "setters" or pass in a JSON entity information received from
 * G2, either using a constructor or "extractData" method.
 */
public class G2Entity {

  // JSON tags
  private static final String RESOLVED_ENTITY_TAG = "RESOLVED_ENTITY";
  private static final String RELATED_ENTITIES_TAG = "RELATED_ENTITIES";
  private static final String RECORDS_TAG = "RECORDS";
  private static final String FEATURES_TAG = "FEATURES";

  // Entity fields
  public static final String ENTITY_ID_FIELD = "ENTITY_ID";
  private final static String ENTITY_NAME = "ENTITY_NAME";
  private static final String ENTITY_LENS_CODE = "LENS_CODE";
  private static final String ENTITY_TYPE = "ENTITY_TYPE";
  private static final String ENTITY_FEAT_DESC = "FEAT_DESC";
  private static final String[] ENTITY_FIELDS = { ENTITY_ID_FIELD, ENTITY_NAME, ENTITY_LENS_CODE };

  // Relationship fields
  private static final String RELATIONSHIP_MATCH_KEY = "MATCH_KEY";
  private static final String RELATIONSHIP_MATCH_LEVEL = "MATCH_LEVEL";
  private static final String RELATIONSHIP_MATCH_LEVEL_CODE = "MATCH_LEVEL_CODE";
  private static final String RELATIONSHIP_MATCH_SCORE = "MATCH_SCORE";
  private static final String RELATIONSHIP_ERRULE_CODE = "ERRULE_CODE";
  private static final String RELATIONSHIP_REF_SCORE = "REF_SCORE";
  private static final String RELATIONSHIP_IS_DISCLOSED = "IS_DISCLOSED";
  private static final String RELATIONSHIP_IS_AMBIGUOUS = "IS_AMBIGUOUS";
  private static final String[] RELATIONSHIP_FIELDS = { RELATIONSHIP_MATCH_KEY, RELATIONSHIP_MATCH_LEVEL,
      RELATIONSHIP_MATCH_LEVEL_CODE, RELATIONSHIP_MATCH_SCORE, RELATIONSHIP_ERRULE_CODE, RELATIONSHIP_REF_SCORE,
      RELATIONSHIP_IS_DISCLOSED, RELATIONSHIP_IS_AMBIGUOUS };

  // Record fields
  public static final String RECORD_ID_FIELD = "RECORD_ID";
  public static final String RECORD_DATA_SOURCE = "DATA_SOURCE";
  private static final String RECORD_MATCH_KEY = "MATCH_KEY";
  private static final String RECORD_MATCH_SCORE = "MATCH_SCORE";
  private static final String RECORD_ERRULE_CODE = "ERRULE_CODE";
  private static final String RECORD_REF_SCORE = "REF_SCORE";
  private static final String RECORD_MATCH_LEVEL = "MATCH_LEVEL";
  private static final String RECORD_MATCH_LEVEL_CODE = "MATCH_LEVEL_CODE";
  private static final String G2_RECORD_ID_FIELD = "INTERNAL_ID";
  private static final String[] RECORD_FIELDS = { RECORD_ID_FIELD, RECORD_DATA_SOURCE, RECORD_MATCH_KEY,
      RECORD_MATCH_SCORE, RECORD_ERRULE_CODE, RECORD_REF_SCORE, RECORD_MATCH_LEVEL, RECORD_MATCH_LEVEL_CODE };

  private Long entityId;
  private String entityType;
  private Map<String, Object> features;
  private Map<Long, Map<String, Object>> relationships;
  private Map<Long, Map<String, Object>> records;

  /**
   * Default constructor.
   */
  public G2Entity() {
    init();
  }

  /**
   * This constructor receives G2 entity information in JSON format. It parses the
   * message and uses its information to populate the member variables.
   * 
   * @param message G2 entity message in JSON format
   * 
   * @throws JSONException
   */
  public G2Entity(String message) throws JSONException {
    init();
    extractData(message);
  }

  private void init() {
    features = new HashMap<>();
    relationships = new HashMap<>();
    records = new HashMap<>();
  }

  /**
   * Takes the input and populates the member variables.
   * 
   * @param message In JSON format
   * 
   * @throws JSONException
   */
  public void extractData(String message) throws JSONException {
    if (message == null || message.isEmpty()) {
      return;
    }
    JSONObject json = new JSONObject(message);

    JSONObject resolvedEntity = json.getJSONObject(RESOLVED_ENTITY_TAG);

    entityId = resolvedEntity.getLong(ENTITY_ID_FIELD);

    // Gather entity features.
    addEntityFeatures(resolvedEntity, ENTITY_FIELDS, features);

    // Gather relationship features.
    JSONArray relatedEntities = json.getJSONArray(RELATED_ENTITIES_TAG);
    populateFeatureMap(relatedEntities, RELATIONSHIP_FIELDS, ENTITY_ID_FIELD, relationships);

    // Gather record information.
    JSONArray jsonRecords = resolvedEntity.getJSONArray(RECORDS_TAG);
    populateFeatureMap(jsonRecords, RECORD_FIELDS, G2_RECORD_ID_FIELD, records);
    // All the records should have same entity type - grab the first one for the
    // entity type.
    entityType = ((JSONObject) jsonRecords.get(0)).getString(ENTITY_TYPE);
  }

  private void addEntityFeatures(final JSONObject resolvedEntity, final String[] fieldList,
      Map<String, Object> features) throws org.json.JSONException {
    // Add main features.
    populateFeatures(resolvedEntity, ENTITY_FIELDS, features);
    // Add any additional feature data.  They are in the "FEATURES" section.
    JSONObject detailFeatures = resolvedEntity.getJSONObject(FEATURES_TAG);
    if (detailFeatures != null) {
      Iterator<String> keys = detailFeatures.keys();
      while(keys.hasNext()) {
          String key = keys.next();
          if (detailFeatures.get(key) instanceof JSONArray) {
            JSONArray featureArray = detailFeatures.getJSONArray(key);
            for (int i = 0; i < featureArray.length(); i++) {
              String featureDescription = featureArray.getJSONObject(i).getString(ENTITY_FEAT_DESC);
              features.put(key + "-" + String.valueOf(i+1), featureDescription);
            }
         }
      }
    }
  }

  private static void populateFeatureMap(final JSONArray jsonArray, final String[] fieldList, String idField,
      Map<Long, Map<String, Object>> featureMap) throws org.json.JSONException {
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonObject = jsonArray.getJSONObject(i);
      Long id = jsonObject.getLong(idField);
      Map<String, Object> features = featureMap.get(id);
      if (features == null) {
        features = new HashMap<>();
        featureMap.put(id, features);
      }
      populateFeatures(jsonObject, fieldList, features);
    }
  }

  private static void populateFeatures(final JSONObject jsonObject, final String[] fieldList,
      Map<String, Object> features) throws org.json.JSONException {
    for (String fieldName : fieldList) {
      Object value = jsonObject.get(fieldName);
      if (value != null && !value.toString().isEmpty()) {
        features.put(fieldName, value);
      }
    }
  }

  public Long getEntityId() {
    return entityId;
  }

  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public Map<String, Object> getFeatures() {
    return features;
  }

  public void setFeatures(Map<String, Object> features) {
    this.features = features;
  }

  public Map<Long, Map<String, Object>> getRelationships() {
    return relationships;
  }

  public void setRelationships(Map<Long, Map<String, Object>> relationships) {
    this.relationships = relationships;
  }

  public Map<Long, Map<String, Object>> getRecords() {
    return records;
  }

  public void setRecords(Map<Long, Map<String, Object>> records) {
    this.records = records;
  }

}
