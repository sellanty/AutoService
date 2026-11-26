package ru.autoservice;

import org.junit.jupiter.api.Test;
import ru.autoservice.domain.Customer;
import ru.autoservice.inmemory.InMemoryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {
    @Test
    void savesUpdatesAndDeletes() {
        var repo = new InMemoryRepository<Customer>(Customer::id);
        var customer = new Customer(repo.nextId(), "Иван", "1234567");
        repo.save(customer);
        assertEquals(customer, repo.findById(customer.id()).orElseThrow());
        repo.save(new Customer(customer.id(), "Пётр", "1234567"));
        assertEquals(1, repo.findAll().size());
        assertEquals("Пётр", repo.findById(customer.id()).orElseThrow().name());
        assertEquals(2L, repo.nextId());
        repo.deleteById(customer.id());
        assertTrue(repo.findAll().isEmpty());
    }
}
