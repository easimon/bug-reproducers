import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {

    private static String get(String url) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(url))
            .GET()
            .build();

        HttpResponse<String> response = HttpClient.newBuilder()
            .build()
            .send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("HTTP:  " + get("http://worldtimeapi.org/api/timezone/Europe/Berlin"));
        System.out.println("HTTPs: " + get("https://worldtimeapi.org/api/timezone/Europe/Berlin"));
    }
}
