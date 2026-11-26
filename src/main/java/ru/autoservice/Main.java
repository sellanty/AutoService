package ru.autoservice;

import ru.autoservice.cli.ConsoleApplication;
import ru.autoservice.cli.ServiceCommands;
import ru.autoservice.domain.Customer;
import ru.autoservice.domain.Vehicle;
import ru.autoservice.domain.RepairOrder;
import ru.autoservice.inmemory.InMemoryRepository;
import ru.autoservice.service.CustomerService;
import ru.autoservice.service.VehicleService;
import ru.autoservice.service.RepairService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var customers = new InMemoryRepository<Customer>(Customer::id);
        var vehicles = new InMemoryRepository<Vehicle>(Vehicle::id);
        var repairs = new InMemoryRepository<RepairOrder>(RepairOrder::id);
        var customerService = new CustomerService(customers);
        var vehicleService = new VehicleService(vehicles, repairs, customerService);
        var repairService = new RepairService(repairs, vehicleService);
        try (Scanner scanner = new Scanner(System.in)) {
            var cli = new ConsoleApplication(scanner, System.out);
            new ServiceCommands(cli, customerService, vehicleService, repairService).register();
            cli.run();
        }
    }
}
