package ru.autoservice.cli;

public record Command(String name, String description, Runnable action) {
    public void execute() {
        action.run();
    }
}
