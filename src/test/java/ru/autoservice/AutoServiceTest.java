package ru.autoservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.autoservice.domain.Customer;
import ru.autoservice.domain.Vehicle;
import ru.autoservice.domain.RepairOrder;
import ru.autoservice.domain.RepairStatus;
import ru.autoservice.inmemory.InMemoryRepository;
import ru.autoservice.service.CustomerService;
import ru.autoservice.service.VehicleService;
import ru.autoservice.service.RepairService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoServiceTest {
    private CustomerService customers;
    private VehicleService vehicles;
    private RepairService repairs;

    @BeforeEach
    void setUp() {
        var orders = new InMemoryRepository<RepairOrder>(RepairOrder::id);
        customers = new CustomerService(new InMemoryRepository<>(Customer::id));
        vehicles = new VehicleService(new InMemoryRepository<>(Vehicle::id), orders, customers);
        repairs = new RepairService(orders, vehicles);
    }

    private Vehicle vehicle() {
        var customer = customers.add("Иван Петров", "+79991234567");
        return vehicles.add(customer.id(), "BMW", "E46", 2003, "А123АА136");
    }

    private RepairOrder order() {
        return repairs.create(vehicle().id(), "Замена масла", new BigDecimal("8500"));
    }

    @Test
    void registersCustomer() {
        assertEquals(1L, customers.add("Иван Петров", "+79991234567").id());
        assertEquals(1, customers.list().size());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> customers.add(" ", "+79991234567"));
        assertTrue(customers.list().isEmpty());
    }

    @Test
    void rejectsInvalidPhone() {
        assertThrows(IllegalArgumentException.class, () -> customers.add("Иван", "abc"));
    }

    @Test
    void rejectsUnknownCustomer() {
        assertThrows(IllegalArgumentException.class, () -> vehicles.add(99, "BMW", "E46", 2003, "A123"));
    }

    @Test
    void rejectsDuplicatePlate() {
        var car = vehicle();
        assertThrows(IllegalArgumentException.class, () -> vehicles.add(car.customerId(), "BMW", "E39", 2002, "а123аа136"));
    }

    @Test
    void rejectsInvalidYear() {
        var car = vehicle();
        assertThrows(IllegalArgumentException.class, () -> vehicles.add(car.customerId(), "BMW", "E39", 1000, "A123"));
    }

    @Test
    void deletesVehicleWithoutHistory() {
        vehicles.delete(vehicle().id());
        assertTrue(vehicles.list().isEmpty());
    }

    @Test
    void rejectsDeletionWithHistory() {
        var order = order();
        assertThrows(IllegalStateException.class, () -> vehicles.delete(order.vehicleId()));
    }

    @Test
    void rejectsNegativeCost() {
        var car = vehicle();
        assertThrows(IllegalArgumentException.class, () -> repairs.create(car.id(), "Ремонт", BigDecimal.valueOf(-1)));
    }

    @Test
    void rejectsUnknownVehicle() {
        assertThrows(IllegalArgumentException.class, () -> repairs.create(99, "Ремонт", BigDecimal.ONE));
    }

    @Test
    void rejectsSecondActiveOrder() {
        var order = order();
        assertThrows(IllegalStateException.class, () -> repairs.create(order.vehicleId(), "Другой ремонт", BigDecimal.ONE));
    }

    @Test
    void completesRepairAndCalculatesRevenue() {
        var order = order();
        repairs.changeStatus(order.id(), RepairStatus.IN_PROGRESS);
        assertEquals(RepairStatus.COMPLETED, repairs.changeStatus(order.id(), RepairStatus.COMPLETED).status());
        assertEquals(new BigDecimal("8500.00"), repairs.revenue());
        assertEquals(1, repairs.history(order.vehicleId()).size());
    }

    @Test
    void cannotCompleteBeforeStart() {
        var order = order();
        assertThrows(IllegalStateException.class, () -> repairs.changeStatus(order.id(), RepairStatus.COMPLETED));
    }

    @Test
    void cancelledOrderDoesNotCountAsRevenue() {
        var order = order();
        repairs.changeStatus(order.id(), RepairStatus.CANCELLED);
        assertEquals(new BigDecimal("0.00"), repairs.revenue());
        assertEquals(RepairStatus.CREATED, repairs.create(order.vehicleId(), "Новый ремонт", BigDecimal.ONE).status());
    }

    @Test
    void cannotReopenCompletedOrder() {
        var order = order();
        repairs.changeStatus(order.id(), RepairStatus.IN_PROGRESS);
        repairs.changeStatus(order.id(), RepairStatus.COMPLETED);
        assertThrows(IllegalStateException.class, () -> repairs.changeStatus(order.id(), RepairStatus.IN_PROGRESS));
    }
}
