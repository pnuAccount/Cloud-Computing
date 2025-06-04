package cloud.term.redisqueue.model;

public enum BookingRequestResult {
    ALREADY_BOOKED,
    ALREADY_QUEUED,
    CAPACITY_FULL,
    QUEUED_SUCCESS
}
