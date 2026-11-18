-- Hand-written sample records (bulk data is added by python/generate_data.py)
INSERT INTO customer VALUES (1,'Arun Kumar','9876543210','arun@example.com');
INSERT INTO customer VALUES (2,'Meena Devi','9123456780','meena@example.com');
INSERT INTO customer VALUES (3,'Karthik Raj','9988776655','karthik@example.com');
INSERT INTO customer VALUES (4,'Divya S','9090909090','divya@example.com');
INSERT INTO customer VALUES (5,'Suresh Babu','9345612780','suresh@example.com');

INSERT INTO destination(destination_name,country) VALUES ('Goa','India');
INSERT INTO destination(destination_name,country) VALUES ('Ooty','India');
INSERT INTO destination(destination_name,country) VALUES ('Munnar','India');
INSERT INTO destination(destination_name,country) VALUES ('Manali','India');
INSERT INTO destination(destination_name,country) VALUES ('Jaipur','India');

INSERT INTO travel_package VALUES (1,'Goa Beach Escape',1,4,12000,10);
INSERT INTO travel_package VALUES (2,'Ooty Hill Retreat',2,3,8000,12);
INSERT INTO travel_package VALUES (3,'Munnar Tea Trails',3,4,9500,8);
INSERT INTO travel_package VALUES (4,'Manali Snow Adventure',4,6,18000,10);
INSERT INTO travel_package VALUES (5,'Jaipur Royal Heritage',5,5,15000,10);

INSERT INTO booking VALUES (1,1,1,2,'2026-01-10','2026-02-14','CONFIRMED',24000,NULL,NULL,0);
INSERT INTO booking VALUES (2,2,2,4,'2026-01-15','2026-03-05','CONFIRMED',32000,NULL,NULL,0);
INSERT INTO booking VALUES (3,3,3,3,'2026-02-01','2026-04-10','CANCELLED',28500,'2026-02-20','Health issue',25650);
INSERT INTO booking VALUES (4,4,4,2,'2026-03-12','2026-05-20','CONFIRMED',36000,NULL,NULL,0);
INSERT INTO booking VALUES (5,5,5,5,'2026-04-02','2026-06-15','CONFIRMED',75000,NULL,NULL,0);

INSERT INTO payment VALUES (1,1,24000,'UPI','PAID','2026-01-10',0);
INSERT INTO payment VALUES (2,2,32000,'CARD','PAID','2026-01-16',0);
INSERT INTO payment VALUES (3,3,28500,'NET_BANKING','REFUNDED','2026-02-01',25650);
INSERT INTO payment VALUES (4,4,36000,'UPI','PAID','2026-03-12',0);
INSERT INTO payment VALUES (5,5,75000,NULL,'PENDING',NULL,0);
