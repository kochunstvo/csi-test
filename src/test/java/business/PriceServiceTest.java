package business;

import internal.Price;
import internal.PriceRepository;
import internal.PriceRepositoryImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class PriceServiceTest {

    private PriceService    service    = new PriceServiceImpl();
    private PriceRepository repository = new PriceRepositoryImpl();

    private static final Price price       = new Price(
            "code",
            1,
            2,
            LocalDateTime.now().minus(Period.ofMonths(1)),
            LocalDateTime.now().plus(Period.ofWeeks(1)),
            new BigDecimal(200)
    );
    private static final Price firstPrice  = new Price(
            "code",
            1,
            2,
            LocalDateTime.now().minus(Period.ofMonths(1)),
            LocalDateTime.now(),
            new BigDecimal(200)
    );
    private static final Price secondPrice = new Price(
            "code",
            1,
            2,
            LocalDateTime.now(),
            LocalDateTime.now().plus(Period.ofWeeks(1)),
            new BigDecimal(200)
    );

    @After
    public void tearDown() throws Exception {
        repository.truncate();
    }

    /*
    Before:
    nothing
    After:
    |---new---|
    */
    @Test
    public void willAddPriceToEmptyDb() {
        service.add(price);

        assertNotNull("Цена не была добавлена в БД", service.find(price.getId()));
    }


    /*
    Before:
    |----1st----|
    After:
    |----1st----|---new---|
    */
    @Test
    public void willAddNotOverlappingPrice() {
        LocalDateTime firstStart = price.getBegin();
        LocalDateTime firstEnd   = price.getEnd();
        LocalDateTime secondEnd  = price.getEnd().plusWeeks(1);
        Price anotherPrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                secondEnd,
                new BigDecimal(150)
        );
        service.add(price);
        service.add(anotherPrice);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 2, prices.size());
        assertFalse(service.findByBeginAndEnd(firstStart, firstEnd).isEmpty());
        assertFalse(service.findByBeginAndEnd(firstEnd, secondEnd).isEmpty());
    }


    /*
    Before:
    |-------1st-------|
    After:
    |-1st-|-new-|-1st-|
    */
    @Test
    public void willInsertPriceInTheMiddleOfDuration() {
        LocalDateTime firstStart = price.getBegin();
        LocalDateTime firstEnd   = LocalDateTime.now().minusDays(2);
        LocalDateTime thirdStart = LocalDateTime.now().plusDays(2);
        LocalDateTime thirdEnd   = price.getEnd();
        Price newPrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                thirdStart,
                new BigDecimal(150)
        );
        service.add(price);
        service.add(newPrice);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 3, prices.size());
        assertFalse(service.findByBeginAndEnd(firstStart, firstEnd).isEmpty());
        assertFalse(service.findByBeginAndEnd(firstEnd, thirdStart).isEmpty());
        assertFalse(service.findByBeginAndEnd(thirdStart, thirdEnd).isEmpty());
    }

    /*
    Before:
    |----1st----|---2nd----|
    After:
    |-1st-|--new---|--2rd--|
    */
    @Test
    public void willStandBetweenTwoPrices() {
        LocalDateTime firstStart = firstPrice.getBegin();
        LocalDateTime firstEnd   = LocalDateTime.now().minusDays(2);
        LocalDateTime thirdStart = LocalDateTime.now().plusDays(2);
        LocalDateTime thirdEnd   = secondPrice.getEnd();
        Price newPrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                thirdStart,
                new BigDecimal(150)
        );
        service.add(firstPrice);
        service.add(secondPrice);
        service.add(newPrice);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 3, prices.size());
        assertFalse(service.findByBeginAndEnd(firstStart, firstEnd).isEmpty());
        assertFalse(service.findByBeginAndEnd(firstEnd, thirdStart).isEmpty());
        assertFalse(service.findByBeginAndEnd(thirdStart, thirdEnd).isEmpty());
    }

    /*
    Before:
    |--1st--|-2nd-|---3rd---|
    After:
    |-1st-|---new---|--3rd--|
     */
    @Test
    public void willReplaceMiddlePrice() {
        LocalDateTime firstStart    = firstPrice.getBegin();
        LocalDateTime firstEnd      = LocalDateTime.now().minusDays(2);
        LocalDateTime newFirstEnd   = firstEnd.minusDays(1);
        LocalDateTime thirdStart    = LocalDateTime.now().plusDays(2);
        LocalDateTime newThirdStart = thirdStart.plusDays(1);
        LocalDateTime thirdEnd      = secondPrice.getEnd();
        Price middlePrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                thirdStart,
                new BigDecimal(150)
        );
        Price newPrice = new Price(
                "code",
                1,
                2,
                newFirstEnd,
                newThirdStart,
                new BigDecimal(150)
        );
        service.add(firstPrice);
        service.add(secondPrice);
        service.add(middlePrice);
        service.add(newPrice);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 3, prices.size());
        assertFalse(service.findByBeginAndEnd(firstStart, newFirstEnd).isEmpty());
        assertFalse(service.findByBeginAndEnd(newFirstEnd, newThirdStart).isEmpty());
        assertFalse(service.findByBeginAndEnd(newThirdStart, thirdEnd).isEmpty());
        assertTrue(service.findByBeginAndEnd(firstStart, thirdStart).isEmpty());
    }
}