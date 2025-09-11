import org.json.JSONObject;
public class StatusService {
    private final long startTime;

    public StatusService() {
        this.startTime=System.currentTimeMillis();
    }

    public JSONObject getStatusData(String clientIP) {
        JSONObject data = new JSONObject();
        data.put("status", "OK");
        data.put("uptime_ms", System.currentTimeMillis()-this.startTime);
        data.put("client_ip", clientIP);
        return data;
    }
}