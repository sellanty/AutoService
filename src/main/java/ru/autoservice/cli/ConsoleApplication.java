package ru.autoservice.cli;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class ConsoleApplication {
    private final Scanner input;
    private final PrintStream output;
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private boolean running = true;

    public ConsoleApplication(Scanner input, PrintStream output) {
        this.input = input;
        this.output = output;
        register("help", "Список команд", this::help);
        register("exit", "Выход", () -> running = false);
    }

    public void register(String name, String description, Runnable action) {
        commands.put(name, new Command(name, description, action));
    }

    private void help() {
        commands.values().forEach(command -> output.println(command.name() + " — " + command.description()));
    }

    public void run() {
        output.println("AutoService | help — список команд");
        while (running) {
            output.print("> ");
            if (!input.hasNextLine()) {
                break;
            }
            String name = input.nextLine().trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                continue;
            }
            Command command = commands.get(name);
            if (command == null) {
                output.println("Неизвестная команда. Введите help");
                continue;
            }
            try {
                command.execute();
            } catch (NumberFormatException e) {
                output.println("Ошибка: некорректное число");
            } catch (IllegalArgumentException | IllegalStateException e) {
                output.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public String read(String prompt) {
        output.print(prompt + ": ");
        if (!input.hasNextLine()) {
            running = false;
            throw new IllegalStateException("Ввод завершён");
        }
        return input.nextLine().trim();
    }

    public void print(Object value) {
        output.println(value);
    }
}
