-- Предзаполнение отелей
INSERT INTO hotels (name, address, city, created_at) VALUES
                                                         ('Grand Hotel', '123 Main St', 'Moscow', CURRENT_TIMESTAMP),
                                                         ('City Inn', '456 Lenin Ave', 'Moscow', CURRENT_TIMESTAMP),
                                                         ('Park Resort', '789 Park Rd', 'Saint Petersburg', CURRENT_TIMESTAMP),
                                                         ('Business Center Hotel', '321 Office Blvd', 'Moscow', CURRENT_TIMESTAMP),
                                                         ('Seaside Hotel', '654 Beach Way', 'Sochi', CURRENT_TIMESTAMP);

-- Предзаполнение номеров
INSERT INTO rooms (hotel_id, room_number, room_type, price, available, times_booked, version) VALUES
-- Grand Hotel
(1, '101', 'SINGLE', 3000.00, true, 5, 0),
(1, '102', 'DOUBLE', 5000.00, true, 8, 0),
(1, '103', 'SUITE', 10000.00, true, 3, 0),
(1, '104', 'DOUBLE', 5000.00, true, 2, 0),

-- City Inn
(2, '201', 'SINGLE', 2500.00, true, 10, 0),
(2, '202', 'DOUBLE', 4500.00, true, 7, 0),
(2, '203', 'TWIN', 4500.00, true, 4, 0),
(2, '204', 'FAMILY', 8000.00, true, 1, 0),

-- Park Resort
(3, '301', 'DELUXE', 7000.00, true, 6, 0),
(3, '302', 'SUITE', 12000.00, true, 9, 0),
(3, '303', 'DOUBLE', 5500.00, true, 0, 0),
(3, '304', 'PRESIDENTIAL', 20000.00, true, 2, 0),

-- Business Center Hotel
(4, '401', 'SINGLE', 3500.00, true, 11, 0),
(4, '402', 'DOUBLE', 6000.00, true, 5, 0),
(4, '403', 'SUITE', 11000.00, true, 3, 0),

-- Seaside Hotel
(5, '501', 'DOUBLE', 8000.00, true, 4, 0),
(5, '502', 'SUITE', 15000.00, true, 7, 0),
(5, '503', 'FAMILY', 12000.00, true, 2, 0),
(5, '504', 'PRESIDENTIAL', 25000.00, true, 1, 0);
