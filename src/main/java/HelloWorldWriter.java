import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HelloWorldWriter implements RequestHandler<Map<String, String>, String> {

    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        var message = event.get("message");

        Region region = Region.EU_WEST_1;
        CloudWatchClient client = CloudWatchClient.builder()
                .region(region)
                .build();
        publishDataPoint(client);
        return String.format("Recieved message: %s \n hello world! (this line is not part of the message)",
                message);
    }

    private static void publishDataPoint(CloudWatchClient client){
       try {
        // Set an Instant object.
        String time = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
        Instant instant = Instant.parse(time);

        var customMetricName = "lambdaTestMetric";

        MetricDatum datum = MetricDatum.builder()
                .metricName(customMetricName)
                .unit("requests handled")
                .value(1.0)
                .timestamp(instant)
                .build();

        PutMetricDataRequest request = PutMetricDataRequest.builder()
                .metricData(datum)
                .namespace("lambdaTestJava")
                .build();

        client.putMetricData(request);
        System.out.println("Added metric values for for metric " + customMetricName);

    } catch (CloudWatchException e) {
        System.err.println(e.getMessage());
        System.exit(1);
    }
    }
}
