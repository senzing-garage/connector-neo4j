package com.senzing.neo4j.connector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import com.senzing.neo4j.connector.cmdline.CommandOptions;
import com.senzing.neo4j.connector.communication.ConsumerType;
import com.senzing.neo4j.connector.service.graph.GraphType;

/**
 * This application feeds G2 data into a data mart. It reads information via a
 * consumer, processes and feeds to a mart.
 */
public class Neo4jConnectorApplication {

  public static void main(String[] args) {
    try {
      String config = processArguments(args);
      Neo4jConnector neo4jConnector = new Neo4jConnector(config);
      neo4jConnector.run(config);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String processArguments(String[] args) throws ParseException, JSONException {
    Options options = new Options();

    // add a option
    options.addOption(CommandOptions.INI_FILE, false, "Path to the G2 ini file");
    options.addOption(CommandOptions.NEO4J_CONNECTION, false, "Connection string for Neo4j");
    options.addOption(CommandOptions.MQ_HOST, false, "Host for RabbitMQ");
    options.addOption(CommandOptions.MQ_USER, false, "User name for RabbitMQ");
    options.addOption(CommandOptions.MQ_PASSWORD, false, "Password for RabbitMQ");
    options.addOption(CommandOptions.MQ_QUEUE, false, "Queue name for the receiving queue");

    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine = parser.parse(options, args);

    JSONObject jsonRoot = new JSONObject();
    if (commandLine.hasOption(CommandOptions.INI_FILE)) {
      jsonRoot.put(CommandOptions.INI_FILE, commandLine.getOptionValue(CommandOptions.INI_FILE));
    }
    if (commandLine.hasOption(CommandOptions.NEO4J_CONNECTION)) {
      jsonRoot.put(CommandOptions.NEO4J_CONNECTION, commandLine.getOptionValue(CommandOptions.NEO4J_CONNECTION));
    }
    if (commandLine.hasOption(CommandOptions.MQ_HOST)) {
      jsonRoot.put(CommandOptions.MQ_HOST, commandLine.getOptionValue(CommandOptions.MQ_HOST));
    }
    if (commandLine.hasOption(CommandOptions.MQ_USER)) {
      jsonRoot.put(CommandOptions.MQ_USER, commandLine.getOptionValue(CommandOptions.MQ_USER));
    }
    if (commandLine.hasOption(CommandOptions.MQ_PASSWORD)) {
      jsonRoot.put(CommandOptions.MQ_PASSWORD, commandLine.getOptionValue(CommandOptions.MQ_PASSWORD));
    }
    if (commandLine.hasOption(CommandOptions.MQ_QUEUE)) {
      jsonRoot.put(CommandOptions.MQ_QUEUE, commandLine.getOptionValue(CommandOptions.MQ_QUEUE));
    }

    String consumer_type = commandLine.getOptionValue(CommandOptions.MQ_QUEUE, ConsumerType.rabbitmq.toString());
    String graph_type = commandLine.getOptionValue(CommandOptions.GRAPH_TYPE, GraphType.neo4j.toString());
    jsonRoot.put(CommandOptions.MQ_QUEUE, consumer_type);
    jsonRoot.put(CommandOptions.GRAPH_TYPE, graph_type);

    return jsonRoot.toString();
  }

  Neo4jConnectorApplication() {

  }
}
