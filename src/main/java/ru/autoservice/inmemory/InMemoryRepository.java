package ru.autoservice.inmemory;

import ru.autoservice.repository.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToLongFunction;

public class InMemoryRepository<T> implements Repository<T> {
    private final Map<Long, T> storage = new LinkedHashMap<>();
    private final ToLongFunction<T> idGetter;
    private long nextId = 1;

    public InMemoryRepository(ToLongFunction<T> idGetter) {
        this.idGetter = idGetter;
    }

    @Override
    public T save(T entity) {
        storage.put(idGetter.applyAsLong(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public long nextId() {
        return nextId++;
    }
}
