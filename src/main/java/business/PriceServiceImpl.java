package business;

import internal.Price;
import internal.PriceRepository;
import internal.PriceRepositoryImpl;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class PriceServiceImpl implements PriceService {

    private PriceRepository priceRepository;

    public PriceServiceImpl() {
        this.priceRepository = new PriceRepositoryImpl();
    }

    @Override
    public void add(Price newPrice) {
        List<Price> filteredPrices = priceRepository.findByCodeAndNumber(newPrice.getProductCode(), newPrice.getNumber());
        save(newPrice);

        if (filteredPrices.isEmpty()) {
            return;
        }

        splitPriceWhichContainsNewPrice(newPrice, filteredPrices);
        spreadOverlappedPrices(newPrice, filteredPrices);
        removeContainedPrices(newPrice, filteredPrices);
    }

    private void spreadOverlappedPrices(Price newPrice, List<Price> filteredPrices) {
        filteredPrices.stream()
                .filter(price -> price.duration().overlapsAtStartBy(newPrice.duration()))
                .findFirst()
                .ifPresent(price -> save(price.withUpdatedDuration(newPrice.getEnd(), price.getEnd())));

        filteredPrices.stream()
                .filter(price -> price.duration().overlapsAtEndBy(newPrice.duration()))
                .findFirst()
                .ifPresent(price -> save(price.withUpdatedDuration(price.getBegin(), newPrice.getBegin())));
    }

    private void removeContainedPrices(Price newPrice, List<Price> filteredPrices) {
        filteredPrices.stream()
                .filter(price -> price.duration().containsIn(newPrice.duration()))
                .forEach(priceRepository::delete);
    }

    private void splitPriceWhichContainsNewPrice(Price newPrice, List<Price> filteredPrices) {
        filteredPrices.stream()
                .filter(price -> price.duration().contains(newPrice.duration()))
                .findFirst()
                .ifPresent(price -> price.splitBy(newPrice).forEach(this::save));
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
                .filter(price -> price.haveSameAmountWith(newPrice) && price.getEnd().isEqual(newPrice.getBegin()))
                .findFirst()
                .map(price -> priceRepository.save(price.withUpdatedDuration(price.getBegin(), newPrice.getEnd())))
                .orElseGet(() -> priceRepository.save(newPrice));
    }
}
