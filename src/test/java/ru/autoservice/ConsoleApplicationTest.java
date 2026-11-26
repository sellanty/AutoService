package ru.autoservice;

import org.junit.jupiter.api.Test;
import ru.autoservice.cli.ConsoleApplication;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleApplicationTest {
    @Test
    void helpUsesRegisteredCommands() {
        var buffer = new ByteArrayOutputStream();
        var cli = new ConsoleApplication(new Scanner("help\nunknown\ncustom\nexit\n"),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        cli.register("custom", "Тестовая команда", () -> cli.print("Выполнено"));
        cli.run();
        String result = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("custom — Тестовая команда"));
        assertTrue(result.contains("Неизвестная команда"));
        assertTrue(result.contains("Выполнено"));
    }
}
