package ru.autoservice.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepairOrder(long id, long vehicleId, String description, BigDecimal cost,
                          LocalDate date, RepairStatus status) {
    public RepairOrder changeStatus(RepairStatus next) {
        boolean allowed = switch (status) {
            case CREATED -> next == RepairStatus.IN_PROGRESS || next == RepairStatus.CANCELLED;
            case IN_PROGRESS -> next == RepairStatus.COMPLETED || next == RepairStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new IllegalStateException("Недопустимый переход статуса");
        }
        return new RepairOrder(id, vehicleId, description, cost, date, next);
    }

    public boolean isActive() {
        return status == RepairStatus.CREATED || status == RepairStatus.IN_PROGRESS;
    }
}
