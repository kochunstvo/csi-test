package internal;

import java.time.LocalDateTime;

public class Duration {

    private final LocalDateTime from;
    private final LocalDateTime to;

    public Duration(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public boolean contains(Duration duration) {
        return this.from.isBefore(duration.from) && this.to.isAfter(duration.to);
    }

    public boolean overlapsAtStart(Duration duration) {
//        return (this.from.isEqual(duration.from) || this.from.isAfter(duration.from)) && this.to.isAfter(duration.to);
        return this.from.isAfter(duration.from) && this.to.isAfter(duration.to);
    }

    public boolean overlapsAtEnd(Duration duration) {
//        return this.from.isBefore(duration.from) && (this.to.isEqual(duration.to) || this.to.isBefore(duration.to));
        return this.from.isBefore(duration.from) && this.to.isBefore(duration.to);
    }

    public boolean containsIn(Duration duration) {
        return duration.contains(this);
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }
}
