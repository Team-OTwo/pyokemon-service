package com.pyokemon.common.exception.code;

//Event error code 예시 - 서비스 맡으신 분이 사용하면서 수정하시면 됩니다 ~!

public final class EventErrorCodes {

    public static final String EVENT_NOT_FOUND = "EVENT_NOT_FOUND";
    public static final String EVENT_ALREADY_EXISTS = "EVENT_ALREADY_EXISTS";
    public static final String EVENT_NOT_ACTIVE = "EVENT_NOT_ACTIVE";
    public static final String EVENT_CANCELLED = "EVENT_CANCELLED";
    public static final String EVENT_SOLD_OUT = "EVENT_SOLD_OUT";
    public static final String EVENT_REGISTRATION_CLOSED = "EVENT_REGISTRATION_CLOSED";
    public static final String EVENT_NOT_STARTED = "EVENT_NOT_STARTED";
    public static final String EVENT_ALREADY_ENDED = "EVENT_ALREADY_ENDED";

    public static final String VENUE_NOT_FOUND = "VENUE_NOT_FOUND";
    public static final String VENUE_NOT_AVAILABLE = "VENUE_NOT_AVAILABLE";
    public static final String VENUE_CAPACITY_EXCEEDED = "VENUE_CAPACITY_EXCEEDED";

    public static final String SEAT_NOT_FOUND = "SEAT_NOT_FOUND";
    public static final String SEAT_ALREADY_RESERVED = "SEAT_ALREADY_RESERVED";
    public static final String SEAT_NOT_AVAILABLE = "SEAT_NOT_AVAILABLE";
    public static final String SEAT_CLASS_NOT_FOUND = "SEAT_CLASS_NOT_FOUND";
    public static final String SEAT_SELECTION_REQUIRED = "SEAT_SELECTION_REQUIRED";

    public static final String BOOKING_NOT_FOUND = "BOOKING_NOT_FOUND";
    public static final String BOOKING_ALREADY_EXISTS = "BOOKING_ALREADY_EXISTS";
    public static final String BOOKING_CANCELLED = "BOOKING_CANCELLED";
    public static final String BOOKING_EXPIRED = "BOOKING_EXPIRED";
    public static final String BOOKING_LIMIT_EXCEEDED = "BOOKING_LIMIT_EXCEEDED";
    public static final String BOOKING_NOT_CANCELLABLE = "BOOKING_NOT_CANCELLABLE";
    public static final String BOOKING_ALREADY_CONFIRMED = "BOOKING_ALREADY_CONFIRMED";

    public static final String SCHEDULE_NOT_FOUND = "SCHEDULE_NOT_FOUND";
    public static final String SCHEDULE_CONFLICT = "SCHEDULE_CONFLICT";
    public static final String SCHEDULE_TIME_INVALID = "SCHEDULE_TIME_INVALID";

    public static final String PRICE_NOT_FOUND = "PRICE_NOT_FOUND";
    public static final String PRICE_INVALID = "PRICE_INVALID";
    public static final String DISCOUNT_NOT_APPLICABLE = "DISCOUNT_NOT_APPLICABLE";
    public static final String DISCOUNT_EXPIRED = "DISCOUNT_EXPIRED";

    public static final String EVENT_ID_REQUIRED = "EVENT_ID_REQUIRED";
    public static final String EVENT_TITLE_REQUIRED = "EVENT_TITLE_REQUIRED";
    public static final String EVENT_DATE_REQUIRED = "EVENT_DATE_REQUIRED";
    public static final String VENUE_ID_REQUIRED = "VENUE_ID_REQUIRED";
    public static final String SEAT_ID_REQUIRED = "SEAT_ID_REQUIRED";
    public static final String BOOKING_ID_REQUIRED = "BOOKING_ID_REQUIRED";

    public static final String EVENT_ACCESS_DENIED = "EVENT_ACCESS_DENIED";
    public static final String BOOKING_ACCESS_DENIED = "BOOKING_ACCESS_DENIED";
    public static final String EVENT_MODIFICATION_DENIED = "EVENT_MODIFICATION_DENIED";

    public static final String EVENT_INTERNAL_ERROR = "EVENT_INTERNAL_ERROR";
    public static final String EVENT_SERVICE_UNAVAILABLE = "EVENT_SERVICE_UNAVAILABLE";
    public static final String EVENT_DATABASE_ERROR = "EVENT_DATABASE_ERROR";
    public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";

    private EventErrorCodes() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스를 생성할 수 없습니다");
    }
} 