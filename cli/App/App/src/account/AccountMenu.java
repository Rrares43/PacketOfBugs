package account;

import account.command.AccountCommand;
import io.StringReader;
import io.OutputWriter;
import io.TextFormatter;

import java.util.HashMap;
import java.util.Map;

public class AccountMenu {
  private final Map<String, AccountCommand> commands;
  private final StringReader stringReader;
  private final OutputWriter output;
  private final SessionService sessionService;
  private boolean running;

  public AccountMenu(StringReader stringReader, OutputWriter output, SessionService sessionService) {
    this.sessionService = sessionService;
    this.commands = new HashMap<>();
    this.stringReader = stringReader;
    this.output = output;

    this.commands.put("0", () -> this.running = false);
  }

  public void registerCommand(String key, AccountCommand command) {
    commands.put(key, command);
  }

  public void startAccountMenu() {
    running = true;

    while (running) {
      String choice;
      if(!sessionService.isLoggedIn()){
        output.write(TextFormatter.header("\n--- ACCOUNT MENU ---"));
        output.write("0. End Application");
        output.write("1. Create Account");
        output.write("2. Login");
        output.write("");
        output.write(TextFormatter.separator(42));

        choice = stringReader.readString("Select an option (0/1/2): ");
        if(choice.equals("1") || choice.equals("2")) {
          AccountCommand command = commands.get(choice);
          if (command != null) {
            try {
              command.execute();
              if(choice.equals("2")){
                break;
              }
            } catch (Exception e) {
              output.write("Error: " + e.getMessage());
            }
          }
          else {
            output.write("Invalid option! Please try again.");
          }
        }
        else if(choice.equals("0")){
          output.write("App closed");
          System.exit(0);
        }
        else{
          output.write("Choice invalid!");
        }
      }
      else {
        output.write(TextFormatter.header("\n--- ACCOUNT MENU ---"));
        output.write("0. Back to Main Menu");
        output.write("1. Add account");
        output.write("2. Login into another account");
        output.write("3. Change Password");
        output.write("4. Check Current User");
        output.write("5. Logout");
        output.write("6. Delete Account");

        System.out.println(TextFormatter.separator(42));

        choice = stringReader.readString("Select an option (0/1/2/3/4/5/6): ");

        AccountCommand command = commands.get(choice);
        if (command != null) {
          try {
            command.execute();
          } catch (Exception e) {
            output.write("Error: " + e.getMessage());
          }
        }
        else {
          output.write("Invalid option! Please try again.");
        }
      }
    }
  }
}