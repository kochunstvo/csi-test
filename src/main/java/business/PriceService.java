package business;

import internal.Price;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PriceService {

    void add(Price price);

    Price find(Long id);

    Collection<Price> findAll();

    List<Price> findByBeginAndEnd(LocalDateTime start, LocalDateTime end);

    Price save(Price price);
}
