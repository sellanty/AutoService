package ru.autoservice.cli;

import ru.autoservice.domain.RepairStatus;
import ru.autoservice.service.CustomerService;
import ru.autoservice.service.VehicleService;
import ru.autoservice.service.RepairService;

import java.math.BigDecimal;
import java.util.List;

public class ServiceCommands {
    private final ConsoleApplication cli;
    private final CustomerService customers;
    private final VehicleService vehicles;
    private final RepairService repairs;

    public ServiceCommands(ConsoleApplication cli, CustomerService customers, VehicleService vehicles, RepairService repairs) {
        this.cli = cli;
        this.customers = customers;
        this.vehicles = vehicles;
        this.repairs = repairs;
    }

    public void register() {
        cli.register("customer-add", "Добавить клиента", () -> cli.print(customers.add(cli.read("ФИО"), cli.read("Телефон"))));
        cli.register("customer-list", "Список клиентов", () -> printList(customers.list()));
        cli.register("vehicle-add", "Добавить автомобиль", this::addVehicle);
        cli.register("vehicle-list", "Список автомобилей", () -> printList(vehicles.list()));
        cli.register("vehicle-get", "Найти автомобиль", () -> cli.print(vehicles.get(readId("ID автомобиля"))));
        cli.register("vehicle-delete", "Удалить автомобиль", () -> {
            vehicles.delete(readId("ID автомобиля"));
            cli.print("Автомобиль удалён");
        });
        cli.register("repair-create", "Создать заказ", this::createRepair);
        cli.register("repair-list", "Список заказов", () -> printList(repairs.list()));
        cli.register("repair-start", "Начать ремонт", () -> changeStatus(RepairStatus.IN_PROGRESS));
        cli.register("repair-complete", "Завершить ремонт", () -> changeStatus(RepairStatus.COMPLETED));
        cli.register("repair-cancel", "Отменить заказ", () -> changeStatus(RepairStatus.CANCELLED));
        cli.register("history", "История ремонта автомобиля", () -> printList(repairs.history(readId("ID автомобиля"))));
        cli.register("revenue", "Выручка по завершённым заказам", () -> cli.print(repairs.revenue() + " руб."));
    }

    private void addVehicle() {
        cli.print(vehicles.add(readId("ID клиента"), cli.read("Марка"), cli.read("Модель"),
                Integer.parseInt(cli.read("Год выпуска")), cli.read("Госномер")));
    }

    private void createRepair() {
        cli.print(repairs.create(readId("ID автомобиля"), cli.read("Описание работ"),
                new BigDecimal(cli.read("Стоимость").replace(',', '.'))));
    }

    private void changeStatus(RepairStatus status) {
        cli.print(repairs.changeStatus(readId("ID заказа"), status));
    }

    private long readId(String prompt) {
        return Long.parseLong(cli.read(prompt));
    }

    private void printList(List<?> values) {
        if (values.isEmpty()) {
            cli.print("Нет записей");
        } else {
            values.forEach(cli::print);
        }
    }
}
