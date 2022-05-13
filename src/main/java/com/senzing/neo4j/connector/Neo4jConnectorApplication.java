package com.senzing.neo4j.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONException;

import com.senzing.listener.communication.ConsumerType;
import com.senzing.listener.communication.rabbitmq.RabbitMQConsumer;
import com.senzing.neo4j.connector.cmdline.CommandOptions;
import com.senzing.neo4j.connector.config.AppConfiguration;
import com.senzing.neo4j.connector.config.ConfigKeys;

/**
 * This application feeds G2 data into a data mart. It reads information via a
 * consumer, processes and feeds to a mart.
 */
public class Neo4jConnectorApplication {

  private static final String RABBITMQ_CONSUMER_TYPE = ConsumerType.RABBIT_MQ.toString();

  private static Map<String, Object> configValues;

  public static void main(String[] args) {
    configValues = new HashMap<>();
    configValues.put(CommandOptions.CONSUMER_TYPE, RABBITMQ_CONSUMER_TYPE);
    try {
      processConfigFileConfiguration();
      // Process the command line arguments after the config file since they override config file values.
      processArguments(args);

      validateCommandLineParams();

      String config = buildConfigJson();
      Neo4jConnector neo4jConnector = new Neo4jConnector();
      neo4jConnector.run(config);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void processConfigFileConfiguration() {
    try {
      AppConfiguration config = new AppConfiguration();
      configValues.put(CommandOptions.CONSUMER_TYPE, RABBITMQ_CONSUMER_TYPE);
      configValues.put(CommandOptions.INI_FILE, config.getConfigValue(ConfigKeys.G2_INI_FILE));
      configValues.put(CommandOptions.NEO4J_CONNECTION, config.getConfigValue(ConfigKeys.NEO4J_URI));
      configValues.put(RabbitMQConsumer.MQ_HOST, config.getConfigValue(ConfigKeys.RABBITMQ_HOST));
      configValues.put(RabbitMQConsumer.MQ_QUEUE, config.getConfigValue(ConfigKeys.RABBITMQ_NAME));
      configValues.put(RabbitMQConsumer.MQ_USER, config.getConfigValue(ConfigKeys.RABBITMQ_USER_NAME));
      configValues.put(RabbitMQConsumer.MQ_PASSWORD, config.getConfigValue(ConfigKeys.RABBITMQ_PASSWORD));
      // This is a future enhancement and enabled when other consumers have been added.
      //configValues.put(CommandOptions.CONSUMER_TYPE, config.getConfigValue(ConfigKeys.CONSUMER_TYPE));
    } catch (IOException e) {
      System.out.println("Configuration file not found. Expecting command line arguments.");
    }
  }

  private static void processArguments(String[] args) throws ParseException, JSONException {
    Options options = new Options();

    // add a option
    options.addOption(CommandOptions.INI_FILE, true, "Path to the G2 ini file");
    options.addOption(CommandOptions.NEO4J_CONNECTION, true, "Connection string for Neo4j");
    options.addOption(CommandOptions.MQ_HOST, true, "Host for RabbitMQ");
    options.addOption(CommandOptions.MQ_USER, true, "User name for RabbitMQ");
    options.addOption(CommandOptions.MQ_PASSWORD, true, "Password for RabbitMQ");
    options.addOption(CommandOptions.MQ_QUEUE, true, "Queue name for the receiving queue");

    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine = parser.parse(options, args);

    addCommandLineValue(commandLine, CommandOptions.INI_FILE);
    addCommandLineValue(commandLine, RabbitMQConsumer.MQ_HOST);
    addCommandLineValue(commandLine, RabbitMQConsumer.MQ_USER);
    addCommandLineValue(commandLine, RabbitMQConsumer.MQ_PASSWORD);
    addCommandLineValue(commandLine, RabbitMQConsumer.MQ_QUEUE);
    addCommandLineValue(commandLine, CommandOptions.CONSUMER_TYPE);
    addCommandLineValue(commandLine, CommandOptions.NEO4J_CONNECTION);
  }

  private static void addCommandLineValue(CommandLine commandLine, String key) {
    String cmdLineValue = commandLine.getOptionValue(key);
    if (cmdLineValue != null && !cmdLineValue.isEmpty()) {configValues.put(key, cmdLineValue);}
  }


  private static void validateCommandLineParams() {
    List<String> unsetParameters = new ArrayList<>();
    checkParameter(unsetParameters, CommandOptions.INI_FILE);
    checkParameter(unsetParameters, RabbitMQConsumer.MQ_HOST);
    checkParameter(unsetParameters, RabbitMQConsumer.MQ_QUEUE);

    if (!unsetParameters.isEmpty()) {
      System.out.println("No configuration found for parameters: " + String.join(", ", unsetParameters));
      helpMessage();
      System.out.println("Failed to start!!!");
      System.exit(-1);
    }
  }

  private static void checkParameter(List<String> parameters, String key) {
    Object value = configValues.get(key);
    if (value == null || value.toString().isEmpty()) {
      parameters.add(key);
    }
  }


  private static String buildConfigJson() {
    JsonObjectBuilder jsonRoot = Json.createObjectBuilder();
    for (String key : configValues.keySet()) {
      Object value = configValues.get(key);
      if (value != null) {
        jsonRoot.add(key, value.toString());
      }
    }
    return jsonRoot.build().toString();
  }

  private static void helpMessage() {
    System.out.println("Set the configuration in the neo4jconnector.properties or add command line parameters.");
    System.out.println("Command line usage: java -jar neo4j-connector.jar -neo4jConnection <Neo4j bolt connection string> \\");
    System.out.println("                                                  -iniFile <path to ini file> \\");
    System.out.println("                                                  -mqQueue <name of the queue read from> \\");
    System.out.println("                                                  -mqHost <host name for queue server> \\");
    System.out.println("                                                  [-mqUser <queue server user name>] \\");
    System.out.println("                                                  [-mqPassword <queue server password>]");
    System.out.println("");
  }

  Neo4jConnectorApplication() {

  }
}
