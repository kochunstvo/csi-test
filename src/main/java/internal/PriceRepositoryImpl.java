package internal;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PriceRepositoryImpl implements PriceRepository {
    private static final ConcurrentHashMap<Long, Price> db = new ConcurrentHashMap<>();

    @Override
    public Collection<Price> findAll() {
        return db.values();
    }

    @Override
    public Price findOne(Long id) {
        return db.get(id);
    }

    @Override
    public Price save(Price price) {
        return db.put(price.getId(), price);
    }

    @Override
    public void delete(Price price) {
        db.remove(price.getId());
    }

    @Override
    public void truncate() {
        db.clear();
    }

    @Override
    public List<Price> findByBeginAndEnd(LocalDateTime start, LocalDateTime end) {
        return findAll().stream()
                .filter(price -> price.getBegin().isEqual(start) && price.getEnd().isEqual(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<Price> findByCodeAndNumber(String code, Integer number) {
        return findAll().stream()
                .filter(price -> price.getProductCode().equals(code))
                .filter(price -> price.getNumber().equals(number))
                .collect(Collectors.toList());
    }
}
