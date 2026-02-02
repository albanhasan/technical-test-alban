INSERT INTO items (id, name, price) VALUES
                                       (1, 'Pen', 5),
                                       (2, 'Book', 10),
                                       (3, 'Bag', 30),
                                       (4, 'Pencil', 3),
                                       (5, 'Shoe', 45),
                                       (6, 'Box', 5),
                                       (7, 'Cap', 25);


INSERT INTO orders (order_no, item_id, qty, price) VALUES
                                                       ('O1', 1, 2, 5),
                                                       ('O2', 2, 3, 10),
                                                       ('O3', 5, 4, 45),
                                                       ('O4', 4, 1, 2),
                                                       ('O5', 5, 2, 45),
                                                       ('O6', 6, 3, 5),
                                                       ('O7', 1, 5, 5),
                                                       ('O8', 2, 4, 10),
                                                       ('O9', 3, 2, 30),
                                                       ('O10', 4, 3, 3);

INSERT INTO inventory (id, item_id, qty, type) VALUES
                                                   (1, 1, 5, 'T'),
                                                   (2, 2, 10, 'T'),
                                                   (3, 3, 30, 'T'),
                                                   (4, 4, 3, 'T'),
                                                   (5, 5, 45, 'T'),
                                                   (6, 6, 5, 'T'),
                                                   (7, 7, 25, 'T'),
                                                   (8, 4, 7, 'T'),
                                                   (9, 5, 10, 'W');
