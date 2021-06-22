-- Database Schema for data sheets provided by NEDAP
Article(Article_ID PK, Category, Product, Color, Size, Price);
Alarms(EPC PK ID, Timestamp, Store_ID FK, Article_ID FK, REF Stores(Store_ID), REF Article(Article_ID));
Stores(Store_ID PK, Latitude, Longitude);

-- Database Schema for user management in webapp
Users(email PK, hashed_pass, first_name, last_name, type, salt)