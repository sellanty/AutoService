package ru.autoservice.service;

import ru.autoservice.domain.Customer;
import ru.autoservice.repository.Repository;
import ru.autoservice.validation.Validation;

import java.util.List;

public class CustomerService {
    private final Repository<Customer> customers;

    public CustomerService(Repository<Customer> customers) {
        this.customers = customers;
    }

    public Customer add(String name, String phone) {
        name = Validation.required(name, "ФИО");
        phone = Validation.required(phone, "Телефон");
        if (!phone.matches("\\+?[0-9]{7,15}")) {
            throw new IllegalArgumentException("Некорректный телефон");
        }
        return customers.save(new Customer(customers.nextId(), name, phone));
    }

    public Customer get(long id) {
        return customers.findById(id).orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));
    }

    public List<Customer> list() {
        return customers.findAll();
    }
}
