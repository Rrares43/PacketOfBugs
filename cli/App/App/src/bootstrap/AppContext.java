package bootstrap;

import account.SessionService;
import menu.MenuDispatcher;
import io.OutputWriter;
import io.StringReader;

public class AppContext {
    private final SessionService sessionService;
    private final StringReader stringReader;
    private final OutputWriter output;
    private final MenuDispatcher dispatcher;

    public AppContext(SessionService sessionService,
                      StringReader stringReader,
                      OutputWriter output,
                      MenuDispatcher dispatcher) {
        this.sessionService = sessionService;
        this.stringReader = stringReader;
        this.output = output;
        this.dispatcher = dispatcher;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    public StringReader getStringReader() {
        return stringReader;
    }

    public OutputWriter getOutput() {
        return output;
    }

    public MenuDispatcher getDispatcher() {
        return dispatcher;
    }
}
