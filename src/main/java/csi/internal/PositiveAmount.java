package csi.internal;

import java.math.BigDecimal;

public class PositiveAmount {

    private BigDecimal value;

    public PositiveAmount(BigDecimal value) {
        validate(value);
        this.value = value;
    }

    public PositiveAmount(Integer value) {
        this(new BigDecimal(value));
    }

    private void validate(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Значение цены не может быть отрицательным");
        }
    }

    public BigDecimal getValue() {
        return value;
    }

    public boolean equalTo(PositiveAmount amount) {
        return this.value.compareTo(amount.value) == 0;
    }
}
