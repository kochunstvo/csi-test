package internal;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Price {

    private static AtomicLong counter = new AtomicLong();

    private Long           id;
    private String         productCode;
    private Integer        number;
    private Integer        depart;
    private LocalDateTime  begin;
    private LocalDateTime  end;
    private PositiveAmount amount;

    public Price(String productCode, Integer number, Integer depart, LocalDateTime begin, LocalDateTime end, PositiveAmount amount) {
        this.id = counter.incrementAndGet();
        this.productCode = productCode;
        this.number = number;
        this.depart = depart;
        this.begin = begin;
        this.end = end;
        this.amount = amount;
    }

    private Price(Price price, LocalDateTime newBegin, LocalDateTime newEnd) {
        this.id = price.id;
        this.productCode = price.productCode;
        this.number = price.number;
        this.depart = price.depart;
        this.amount = price.amount;
        this.begin = newBegin;
        this.end = newEnd;
    }

    public List<Price> splitBy(Price splitter) {
        return Arrays.asList(
                this.withUpdatedDuration(this.begin, splitter.begin),
                new Price(this.productCode, this.number, this.depart, splitter.end, this.end, this.amount)
        );
    }

    public Duration duration() {
        return new Duration(begin, end);
    }

    public Price withUpdatedDuration(LocalDateTime newBegin, LocalDateTime newEnd) {
        return new Price(this, newBegin, newEnd);
    }

    public Long getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public Integer getNumber() {
        return number;
    }

    public Integer getDepart() {
        return depart;
    }

    public LocalDateTime getBegin() {
        return begin;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public PositiveAmount getAmount() {
        return amount;
    }

    public boolean haveSameAmountWith(Price price) {
        return this.amount.equalTo(price.amount);
    }
}
