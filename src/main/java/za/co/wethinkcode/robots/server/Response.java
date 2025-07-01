package za.co.wethinkcode.robots.server;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a standardized response object used for communication between the client and server.
 * Encapsulates a JSON structure with result, message, and optional data for consistency.
 */
public class Response {
    public final JSONObject object;

    public static Response responseFromJSONString(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);

            return new Response(jsonObject.getString("result"), jsonObject.getString("message"));
        } catch (JSONException e) {
            return new Response("Invalid JSON", "");
        }
    }

    public Response(String result, String message) {
        this.object = new JSONObject();
        this.object.put("result", result);
        this.object.put("message", message);
    }

    public static Response ok(JSONObject data, String message) {
        Response response = new Response("OK", message != null ? message : "");
        response.object.put("data", data);
        return response;
    }

    public String getMessage() {
        return object.getString("message");
    }

    public String toJSONString() {
        return this.object.toString();
    }

    public boolean isOKResponse() {
        return this.object.getString("result").equalsIgnoreCase("OK");
    }
}
