package ru.autoservice.service;

import ru.autoservice.domain.Vehicle;
import ru.autoservice.domain.RepairOrder;
import ru.autoservice.repository.Repository;
import ru.autoservice.validation.Validation;

import java.time.Year;
import java.util.List;
import java.util.Locale;

public class VehicleService {
    private static final int FIRST_CAR_YEAR = 1886;
    private final Repository<Vehicle> vehicles;
    private final Repository<RepairOrder> repairs;
    private final CustomerService customers;

    public VehicleService(Repository<Vehicle> vehicles, Repository<RepairOrder> repairs, CustomerService customers) {
        this.vehicles = vehicles;
        this.repairs = repairs;
        this.customers = customers;
    }

    public Vehicle add(long customerId, String brand, String model, int year, String plate) {
        customers.get(customerId);
        brand = Validation.required(brand, "Марка");
        model = Validation.required(model, "Модель");
        String normalizedPlate = Validation.required(plate, "Госномер").toUpperCase(Locale.ROOT);
        if (year < FIRST_CAR_YEAR || year > Year.now().getValue()) {
            throw new IllegalArgumentException("Некорректный год выпуска");
        }
        if (vehicles.findAll().stream().anyMatch(v -> v.plate().equals(normalizedPlate))) {
            throw new IllegalArgumentException("Госномер уже зарегистрирован");
        }
        return vehicles.save(new Vehicle(vehicles.nextId(), customerId, brand, model, year, normalizedPlate));
    }

    public Vehicle get(long id) {
        return vehicles.findById(id).orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
    }

    public List<Vehicle> list() {
        return vehicles.findAll();
    }

    public void delete(long id) {
        get(id);
        if (repairs.findAll().stream().anyMatch(order -> order.vehicleId() == id)) {
            throw new IllegalStateException("У автомобиля есть история ремонта");
        }
        vehicles.deleteById(id);
    }
}
