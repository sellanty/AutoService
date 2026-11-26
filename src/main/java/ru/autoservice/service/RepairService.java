package ru.autoservice.service;

import ru.autoservice.domain.RepairOrder;
import ru.autoservice.domain.RepairStatus;
import ru.autoservice.repository.Repository;
import ru.autoservice.validation.Validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RepairService {
    private static final int MONEY_SCALE = 2;
    private final Repository<RepairOrder> repairs;
    private final VehicleService vehicles;

    public RepairService(Repository<RepairOrder> repairs, VehicleService vehicles) {
        this.repairs = repairs;
        this.vehicles = vehicles;
    }

    public RepairOrder create(long vehicleId, String description, BigDecimal cost) {
        vehicles.get(vehicleId);
        description = Validation.required(description, "Описание работ");
        if (cost == null || cost.signum() < 0 || cost.stripTrailingZeros().scale() > MONEY_SCALE) {
            throw new IllegalArgumentException("Некорректная стоимость (до двух знаков после запятой)");
        }
        if (repairs.findAll().stream().anyMatch(order -> order.vehicleId() == vehicleId && order.isActive())) {
            throw new IllegalStateException("У автомобиля уже есть активный заказ");
        }
        return repairs.save(new RepairOrder(repairs.nextId(), vehicleId, description, cost.setScale(MONEY_SCALE),
                LocalDate.now(), RepairStatus.CREATED));
    }

    public RepairOrder get(long id) {
        return repairs.findById(id).orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
    }

    public RepairOrder changeStatus(long id, RepairStatus status) {
        return repairs.save(get(id).changeStatus(status));
    }

    public List<RepairOrder> list() {
        return repairs.findAll();
    }

    public List<RepairOrder> history(long vehicleId) {
        vehicles.get(vehicleId);
        return repairs.findAll().stream().filter(order -> order.vehicleId() == vehicleId).toList();
    }

    public BigDecimal revenue() {
        return repairs.findAll().stream()
                .filter(order -> order.status() == RepairStatus.COMPLETED)
                .map(RepairOrder::cost)
                .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE), BigDecimal::add);
    }
}
