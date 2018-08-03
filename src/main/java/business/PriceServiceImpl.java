package business;

import internal.Price;
import internal.PriceRepository;
import internal.PriceRepositoryImpl;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PriceServiceImpl implements PriceService {

    private PriceRepository priceRepository;

    public PriceServiceImpl() {
        this.priceRepository = new PriceRepositoryImpl();
    }

    @Override
    public void add(Price price) {
        List<Price> filteredPrices = priceRepository.findByCodeAndNumber(price.getProductCode(), price.getNumber());

        if (filteredPrices.isEmpty()) {
            save(price);
            return;
        }

        Price toSplit = filteredPrices.stream()
                .filter(it -> it.duration().contains(price.duration()))
                .findFirst().orElse(null);
        if (toSplit == null) {
            Price overlapsAtStart = filteredPrices.stream()
                    .filter(it -> it.duration().overlapsAtStartBy(price.duration()))
                    .findFirst().orElse(null);
            Price overlapsAtEnd = filteredPrices.stream()
                    .filter(it -> it.duration().overlapsAtEndBy(price.duration()))
                    .findFirst().orElse(null);
            List<Price> pricesToReplace = filteredPrices.stream()
                    .filter(it -> it.duration().containsIn(price.duration()))
                    .collect(Collectors.toList());
            cleanDurationBetween(overlapsAtStart, overlapsAtEnd, price);
            replace(pricesToReplace);
        } else {
            splitAndSave(toSplit, price);
        }
    }

    private void replace(List<Price> prices) {
        prices.forEach(priceRepository::delete);
    }

    private void cleanDurationBetween(Price overlapsAtStart, Price overlapsAtEnd, Price newPrice) {
        Optional.ofNullable(overlapsAtStart)
                .map(price -> price.withUpdatedDuration(newPrice.getEnd(), price.getEnd()))
                .ifPresent(this::save);
        Optional.ofNullable(overlapsAtEnd)
                .map(price -> price.withUpdatedDuration(price.getBegin(), newPrice.getBegin()))
                .ifPresent(this::save);
        save(newPrice);
    }

    private void splitAndSave(Price toSplit, Price newPrice) {
        toSplit.splitBy(newPrice)
                .forEach(this::save);
        save(newPrice);
    }

    @Override
    public Price find(Long id) {
        return priceRepository.findOne(id);
    }

    @Override
    public Collection<Price> findAll() {
        return priceRepository.findAll();
    }

    @Override
    public List<Price> findByBeginAndEnd(LocalDateTime start, LocalDateTime end) {
        return priceRepository.findByBeginAndEnd(start, end);
    }

    @Override
    public Price save(Price price) {
        return priceRepository.findByCodeAndNumber(price.getProductCode(), price.getNumber()).stream()
                .filter(it -> it.haveSameValueWith(price))
                .filter(it -> it.getEnd().isEqual(price.getBegin()))
                .findFirst()
                .map(it -> it.withUpdatedDuration(it.getBegin(), price.getEnd()))
                .map(priceRepository::save)
                .orElseGet(() -> priceRepository.save(price));
    }
}
