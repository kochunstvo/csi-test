package csi.business;

import csi.internal.PositiveAmount;
import csi.internal.Price;
import csi.internal.PriceRepository;
import csi.internal.PriceRepositoryImpl;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class PriceServiceTest {

    private PriceService service = new PriceServiceImpl();
    private PriceRepository repository = new PriceRepositoryImpl();

    private static final Price price = new Price(
            "code",
            1,
            2,
            LocalDateTime.now().minus(Period.ofMonths(1)),
            LocalDateTime.now().plus(Period.ofWeeks(1)),
            new PositiveAmount(200)
    );
    private static final Price firstPrice = new Price(
            "code",
            1,
            2,
            LocalDateTime.now().minus(Period.ofMonths(1)),
            LocalDateTime.now(),
            new PositiveAmount(210)
    );
    private static final Price secondPrice = new Price(
            "code",
            1,
            2,
            LocalDateTime.now(),
            LocalDateTime.now().plus(Period.ofWeeks(1)),
            new PositiveAmount(190)
    );

    @After
    public void tearDown() {
        repository.truncate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddPriceWithNegativeAmount() {
        Price withNegativeAmount = new Price(
                "code",
                1,
                2,
                LocalDateTime.now(),
                LocalDateTime.now().plus(Period.ofWeeks(1)),
                new PositiveAmount(-5)
        );

        service.add(withNegativeAmount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddPriceWithZeroAmount() {
        Price withZeroAmount = new Price(
                "code",
                1,
                2,
                LocalDateTime.now(),
                LocalDateTime.now().plus(Period.ofWeeks(1)),
                new PositiveAmount(0)
        );

        service.add(withZeroAmount);
    }

    /*
    Before:
    nothing
    After:
    |---200---|
    */
    @Test
    public void willAddPriceToEmptyDb() {
        service.add(price);

        assertNotNull("Цена не была добавлена в БД", service.find(price.getId()));
    }

    /*
    Before:
    |----200----|
    After:
    |---------200---------|
    */
    @Test
    public void willNotInsertNewPriceIfItNotOverlappingAndHasSameValue() {
        LocalDateTime firstStart = price.getBegin();
        LocalDateTime firstEnd = price.getEnd();
        LocalDateTime secondEnd = price.getEnd().plusWeeks(1);
        Price anotherPrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                secondEnd,
                price.getAmount()
        );
        service.add(price);
        service.add(anotherPrice);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 1, prices.size());
        assertFalse(service.findByBeginAndEnd(firstStart, secondEnd).isEmpty());
    }

    /*
    Before:
    |---------|----200----|
    After:
    |---------200---------|
    */
    @Test
    public void willMergeIfNewIsBefore() throws Exception {
        Price anotherPrice = new Price(
                "code",
                1,
                2,
                price.getEnd().minusDays(1),
                price.getEnd().plusWeeks(1),
                price.getAmount()
        );
        service.add(anotherPrice);
        service.add(price);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 1, prices.size());
        assertFalse(service.findByBeginAndEnd(price.getBegin(), price.getEnd().plusWeeks(1)).isEmpty());
    }

    /*
    Before:
    |----200----|
    After:
    |----200----|---150---|
    */
    @Test
    public void willAddNotOverlappingPrice() {
        LocalDateTime firstStart = price.getBegin();
        LocalDateTime firstEnd = price.getEnd();
        LocalDateTime secondEnd = price.getEnd().plusWeeks(1);
        Price anotherPrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                secondEnd,
                new PositiveAmount(150)
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
    |----200----|
    After:
    |----200----| |---200---|
    */
    @Test
    public void willAddNotOverlappingPrice_2() {
        LocalDateTime firstStart = price.getBegin();
        LocalDateTime firstEnd = price.getEnd();
        LocalDateTime secondStart = firstEnd.plusDays(1);
        LocalDateTime secondEnd = price.getEnd().plusWeeks(1);
        Price anotherPrice = new Price(
                "code",
                1,
                2,
                secondStart,
                secondEnd,
                price.getAmount()
        );
        service.add(price);
        service.add(anotherPrice);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 2, prices.size());
        assertFalse(service.findByBeginAndEnd(firstStart, firstEnd).isEmpty());
        assertFalse(service.findByBeginAndEnd(secondStart, secondEnd).isEmpty());
    }


    /*
    Before:
    |-------200-------|
    After:
    |-200-|-150-|-200-|
    */
    @Test
    public void willInsertPriceInTheMiddleOfDuration() {
        LocalDateTime firstStart = price.getBegin();
        LocalDateTime firstEnd = LocalDateTime.now().minusDays(2);
        LocalDateTime thirdStart = LocalDateTime.now().plusDays(2);
        LocalDateTime thirdEnd = price.getEnd();
        Price newPrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                thirdStart,
                new PositiveAmount(150)
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
    |----210----|---190----|
    After:
    |-210-|--150---|--190--|
    */
    @Test
    public void willStandBetweenTwoPrices() {
        LocalDateTime firstStart = firstPrice.getBegin();
        LocalDateTime firstEnd = LocalDateTime.now().minusDays(2);
        LocalDateTime thirdStart = LocalDateTime.now().plusDays(2);
        LocalDateTime thirdEnd = secondPrice.getEnd();
        Price newPrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                thirdStart,
                new PositiveAmount(150)
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
    |--210--|-180-|---190---|
    After:
    |-210-|---140---|--190--|
     */
    @Test
    public void willReplaceMiddlePrice() {
        LocalDateTime firstStart = firstPrice.getBegin();
        LocalDateTime firstEnd = LocalDateTime.now().minusDays(2);
        LocalDateTime newFirstEnd = firstEnd.minusDays(1);
        LocalDateTime thirdStart = LocalDateTime.now().plusDays(2);
        LocalDateTime newThirdStart = thirdStart.plusDays(1);
        LocalDateTime thirdEnd = secondPrice.getEnd();
        Price middlePrice = new Price(
                "code",
                1,
                2,
                firstEnd,
                thirdStart,
                new PositiveAmount(180)
        );
        Price newPrice = new Price(
                "code",
                1,
                2,
                newFirstEnd,
                newThirdStart,
                new PositiveAmount(140)
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

    /*
        Before:
        |-180-|-190-|-180-|
        After:
        |-----------180-----------|
         */
    @Test
    public void willMergeIfHaveSameValue() {
        LocalDateTime dateTime = LocalDateTime.now();

        Price first = new Price(
                "code",
                1,
                1,
                dateTime.minusDays(30),
                dateTime.minusDays(25),
                new PositiveAmount(180)
        );

        Price second = new Price(
                "code",
                1,
                1,
                dateTime.minusDays(25),
                dateTime.minusDays(20),
                new PositiveAmount(190)
        );

        Price third = new Price(
                "code",
                1,
                1,
                dateTime.minusDays(20),
                dateTime.minusDays(15),
                new PositiveAmount(180)
        );

        Price forth = new Price(
                "code",
                1,
                1,
                dateTime.minusDays(26),
                dateTime.minusDays(21),
                new PositiveAmount(180)
        );

        Price fifth = new Price(
                "code",
                1,
                1,
                dateTime.minusDays(22),
                dateTime.minusDays(13),
                new PositiveAmount(180)
        );

        service.add(first);
        service.add(second);
        service.add(third);

        service.add(forth);
        service.add(fifth);

        Collection<Price> prices = service.findAll();

        assertEquals("Количество цен не сходится", 1, prices.size());
        assertFalse(service.findByBeginAndEnd(first.getBegin(), fifth.getEnd()).isEmpty());
    }
}