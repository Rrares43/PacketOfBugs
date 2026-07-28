import bootstrap.AppBootstrap;
import bootstrap.AppContext;
import bootstrap.ApplicationLoop;
import com.github.lalyos.jfiglet.FigletFont;
import persistence.DataBaseConnection;
import io.TextFormatter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(TextFormatter.color(FigletFont.convertOneLine("Buggit"), TextFormatter.GREEN));
        System.out.println(TextFormatter.separator(42));

        // command for database migration, no longer needs to be called
        // DataMigrator.runMigration();

        // Test for the database connection
        System.out.println("Starting Reddit CLI...");
        try (Connection conn = DataBaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful");
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database");
            e.printStackTrace();
        }


        AppContext context = AppBootstrap.wire();
        new ApplicationLoop(context).run();
    }
}
