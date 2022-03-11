package edu.brown.cs.student.main;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.json.JSONException;
import org.json.JSONObject;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * The Main class of our project. This is where execution begins.
 *
 */
public final class Main {

  private static final int DEFAULT_PORT = 4567;

  /**
   * The initial method called when execution begins.
   *
   * @param args
   *             An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);

    OptionSet options = parser.parse(args);
    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }
  }

  private void runSparkServer(int port) {
    Spark.port(port);
    Spark.exception(Exception.class, new ExceptionPrinter());

    // Setup Spark Routes
    Spark.post("/match", new ResultsHandler());
    // TODO: create a call to Spark.post to make a POST request to a URL which
    // will handle getting matchmaking results for the input
    // It should only take in the route and a new ResultsHandler
    Spark.options("/*", (request, response) -> {
      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");

      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    // Allows requests from any domain (i.e., any URL). This makes development
    // easier, but it’s not a good idea for deployment.
    Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
    // For each route handler, run and replace with the correct names
//    Spark.post("/<endpoint>", new ResultsHandler());
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * Handles requests for horoscope matching on an input
   * 
   * @return GSON which contains the result of MatchMaker.makeMatches
   */
  private static class ResultsHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      // TODO: Get JSONObject from req and use it to get the value of the sun, moon,
      // and rising
      // for generating matches
      // TODO: use the MatchMaker.makeMatches method to get matches

      // TODO: create an immutable map using the matches

      // TODO: return a json of the suggestions (HINT: use GSON.toJson())
      JSONObject reqJson;
      try {
        reqJson = new JSONObject(req.body());
        String sun = reqJson.getString("sun");
        String moon = reqJson.getString("moon");
        String rising = reqJson.getString("rising");
        List<String> matches = MatchMaker.makeMatches(sun,moon,rising);
        Gson GSON = new Gson();
        Map<String, List<String>> matchMap = ImmutableMap.of("matches", matches);
        return GSON.toJson(matchMap);
      } catch(JSONException e){
        e.printStackTrace();
      }
      return null;
    }
  }
}
