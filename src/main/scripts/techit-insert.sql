INSERT INTO `units` (`id`, `email`, `location`, `name`) VALUES
(1, 'email', 'location', 'unit'),
(2, 'new email', 'new location', 'new name');

INSERT INTO `users` (`id`, `department`, `email`, `firstName`, `hash`, `lastName`, `phoneNumber`, `position`, `username`, `unitid`) VALUES
(2, 'ADMIN', 'ADMIN', 'ADMIN', '$2a$10$mIxtfv7UEwuWLGJDozhYpeQxRvlXX1dbq8TRPrG2bKi2rWqJSUPia', 'ADMIN', 'ADMIN', 0, 'ADMIN', 1),
(3, 'SUPERVISOR', 'SUPERVISOR', 'SUPERVISOR', '$2a$10$DtX/MB9Ms.cK5kYBH6AgGOQxZUzTYGplqcjELo29tNi7dac3DHhoS', 'SUPERVISOR', 'SUPERVISOR', 1, 'SUPERVISOR', 2),
(4, 'TECHNICIAN', 'TECHNICIAN', 'TECHNICIAN', '$2a$10$7RZhby3wYnaBlsGP6gs99.C6FjL0RFSDstbAprSkZbmfOimpx4uvW', 'TECHNICIAN', 'TECHNICIAN', 2, 'TECHNICIAN', 1),
(5, 'USER', 'USER', 'USER', '$2a$10$tGjLwXeyyFcvffmnXMp5lui9RXWr7xWAQLHaA0.2u23SCbEAIBHBa', 'USER', 'USER', 3, 'USER', NULL),
(6, 'TECHNICIAN', 'TECHNICIAN1', 'TECHNICIAN', '$2a$10$t9kYLkYVFQrs46q/h483PuliehZsOcXgDxRAi3Sc1IGQeLGXyL7Wy', 'TECHNICIAN', 'TECHNICIAN', 2, 'TECHNICIAN1', 2),
(7, 'SUPERVISOR1', 'SUPERVISOR1', 'SUPERVISOR1', '$2a$10$EgDzfqhaL8ofFJOPhumASeDqVuLOeIoTUzF0a3yOFc0Vd09KhgqnK', 'SUPERVISOR1', 'SUPERVISOR1', 1, 'SUPERVISOR1', 1),
(8, NULL, 'Q', 'Q', 'Q', 'Q', 'Q', 3, 'Q', NULL),
(9, 'TECHNICIAN3', 'TECHNICIAN3', 'TECHNICIAN3', 'TECHNICIAN3', 'TECHNICIAN3', 'TECHNICIAN3', 2, 'TECHNICIAN3', 1),
(22, 'SUPERVISOR1', 'SUPERVISOR2', 'SUPERVISOR1', '$2a$10$zVJUzyCIVF9.thM3rRTSKORw9.PkStOTGDMg0gnUw/lBGWXR62N0O', 'SUPERVISOR1', 'SUPERVISOR1', 1, 'SUPERVISOR2', 1);

INSERT INTO `supervisor_unit` (`unit_id`, `supervisor_id`) VALUES
(1, 22),
(2, 3);

INSERT INTO `tickets` (`id`, `priority`, `progress`, `dateAssigned`, `dateUpdated`, `details`, `endDate`, `startDate`, `subject`, `userid`, `unitid`,`created_for_email`) VALUES
(1, 0, 0, NULL, NULL, 'a', NULL, '2018-04-11 00:00:00', 'a', 5, 1,'tet1@g.com'),
(2, 0, 0, NULL, NULL, 'a', NULL, '2018-04-11 00:00:00', 'a', 5, 1,'tet2@g.com'),
(3, 0, 0, NULL, NULL, 'a', NULL, '2018-04-11 00:00:00', 'a', 5, 1,'tet3@g.com');

INSERT INTO `ticket_technicians` (`ticket_id`, `technician_id`) VALUES
(1, 4);

INSERT INTO `updates` (`id`, `modifiedDate`, `updateDetails`, `modifier`, `ticketid`) VALUES
(1, '2018-04-04 00:00:00', 'A', 22, 1),
(2, '2018-04-05 00:00:00', 'q', 4, 1);





