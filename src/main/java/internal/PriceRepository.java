package internal;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PriceRepository {

    Collection<Price> findAll();

    Price findOne(Long id);

    Price save(Price price);

    void delete(Price price);

    void truncate();

    List<Price> findByBeginAndEnd(LocalDateTime start, LocalDateTime end);

    List<Price> findByCodeAndNumber(String code, Integer number);
}
