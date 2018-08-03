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
    public void add(Price newPrice) {
        List<Price> filteredPrices = priceRepository.findByCodeAndNumber(newPrice.getProductCode(), newPrice.getNumber());

        if (filteredPrices.isEmpty()) {
            save(newPrice);
            return;
        }

        Price toSplit = filteredPrices.stream()
                .filter(price -> price.duration().contains(newPrice.duration()))
                .findFirst().orElse(null);
        if (toSplit == null) {
            Price overlapsAtStart = filteredPrices.stream()
                    .filter(price -> price.duration().overlapsAtStartBy(newPrice.duration()))
                    .findFirst().orElse(null);
            Price overlapsAtEnd = filteredPrices.stream()
                    .filter(price -> price.duration().overlapsAtEndBy(newPrice.duration()))
                    .findFirst().orElse(null);
            List<Price> pricesToReplace = filteredPrices.stream()
                    .filter(price -> price.duration().containsIn(newPrice.duration()))
                    .collect(Collectors.toList());
            cleanDurationBetween(overlapsAtStart, overlapsAtEnd, newPrice);
            replace(pricesToReplace);
        } else {
            splitAndSave(toSplit, newPrice);
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
    public Price save(Price newPrice) {
        return priceRepository.findByCodeAndNumber(newPrice.getProductCode(), newPrice.getNumber()).stream()
                .filter(price -> price.haveSameValueWith(newPrice))
                .filter(price -> price.getEnd().isEqual(newPrice.getBegin()))
                .findFirst()
                .map(price -> price.withUpdatedDuration(price.getBegin(), newPrice.getEnd()))
                .map(priceRepository::save)
                .orElseGet(() -> priceRepository.save(newPrice));
    }
}
