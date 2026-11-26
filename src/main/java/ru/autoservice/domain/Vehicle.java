package ru.autoservice.domain;

public record Vehicle(long id, long customerId, String brand, String model, int year, String plate) {}
