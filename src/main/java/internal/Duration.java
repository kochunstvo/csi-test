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

    public boolean overlapsAtStartBy(Duration duration) {
        return this.from.isBefore(duration.to) && duration.from.isBefore(this.from);
    }

    public boolean overlapsAtEndBy(Duration duration) {
        return this.to.isAfter(duration.from) && duration.to.isAfter(this.to);
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
