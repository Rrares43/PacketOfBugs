import bootstrap.AppBootstrap;
import bootstrap.AppContext;
import bootstrap.ApplicationLoop;
import com.github.lalyos.jfiglet.FigletFont;
import io.TextFormatter;
import persistence.RedditApiClient;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(TextFormatter.color(FigletFont.convertOneLine("Buggit"), TextFormatter.GREEN));
        System.out.println(TextFormatter.separator(42));

        System.out.println("Starting Reddit CLI...");
        if (RedditApiClient.isReachable()) {
            System.out.println("Spring API connection successful (" + RedditApiClient.getBaseUrl() + ")");
        } else {
            System.err.println("Warning: Spring API not reachable at " + RedditApiClient.getBaseUrl());
            System.err.println("Local JSON persistence will still work; remote dual-write may fail.");
            System.err.println("Account login and related operations require the Spring Boot backend.");
        }

        AppContext context = AppBootstrap.wire();
        new ApplicationLoop(context).run();
    }
}
