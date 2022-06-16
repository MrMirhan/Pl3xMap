package net.pl3x.map.logger;

import io.papermc.paper.console.HexFormattingConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Pl3xLogger extends Logger {
    private static final String RESET = "\u001b[0m";
    private static final String BOLD = "\u001b[1m";
    private static final String CYAN = "\u001b[36m";
    private static final String RED = "\u001b[31m";
    private static final String YELLOW = "\u001b[33m";

    public Pl3xLogger() {
        super("Pl3xMap", null);
        LogManager.getLogManager().addLogger(this);
    }

    @Override
    public String getName() {
        // bright colors (\u001b[#;1m) don't seem to work correctly here. hmm..
        return RESET + CYAN + BOLD +super.getName() + RESET;
    }

    private String parseMiniMessage(String miniMessage) {
        Component component = MiniMessage.miniMessage().deserialize(miniMessage);
        return HexFormattingConverter.SERIALIZER.serialize(component);
    }

    private void doLog(LogRecord lr) {
        lr.setMessage(parseMiniMessage(lr.getMessage()));

        if (lr.getLevel() == Level.SEVERE) {
            lr.setLoggerName(getName() + RED + BOLD);
        } else if (lr.getLevel() == Level.WARNING) {
            lr.setLoggerName(getName() + YELLOW + BOLD);
        } else {
            lr.setLoggerName(getName());
        }

        log(lr);
    }

    /*
     * The rest of this is just copying methods in order to use the modified doLog private method above..
     */

    @Override
    public void log(Level level, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        doLog(lr);
    }

    @Override
    public void log(Level level, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        doLog(lr);
    }

    @Override
    public void log(Level level, String msg, Object param1) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        Object[] params = {param1};
        lr.setParameters(params);
        doLog(lr);
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setParameters(params);
        doLog(lr);
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msg);
        lr.setThrown(thrown);
        doLog(lr);
    }

    @Override
    public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        if (!isLoggable(level)) {
            return;
        }
        LogRecord lr = new LogRecord(level, msgSupplier.get());
        lr.setThrown(thrown);
        doLog(lr);
    }

    @Override
    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        if (!isLoggable(Level.FINER)) {
            return;
        }
        LogRecord lr = new LogRecord(Level.FINER, "THROW");
        lr.setSourceClassName(sourceClass);
        lr.setSourceMethodName(sourceMethod);
        lr.setThrown(thrown);
        doLog(lr);
    }
}