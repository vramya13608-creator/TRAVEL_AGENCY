package travelagency;

/** Thrown when a business rule (capacity, validation, cancellation policy) is violated. */
public class BookingException extends Exception {
    public BookingException(String message) { super(message); }
}
