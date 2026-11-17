-- 1. Σβήνουμε τον παλιό περιορισμό (constraint)
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_status_check;

-- 2. Προσθέτουμε τον νέο περιορισμό με ΟΛΕΣ τις νέες τιμές
ALTER TABLE bookings ADD CONSTRAINT bookings_status_check
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED'));

