Product Tracker: Inventory Management API
1. Project Description
The Product Tracker is a robust, transactional RESTful API built using Spring Boot. It is designed to manage product inventory, ensuring data integrity and preventing common errors associated with stock management, such as processing orders that would result in negative stock.

The application adheres to a standard layered architecture (Controller, Service, Repository) and includes custom exception handling for critical business logic.

Key Features:
CRUD Operations: Full creation, reading, updating, and deletion of products.

Transactional Stock Management: Dedicated endpoints for increasing/decreasing stock with guaranteed data consistency.

Insufficient Stock Exception: Custom error handling that prevents stock from dropping below zero.

Low Stock Reporting: A dedicated endpoint to quickly identify all products whose current quantity is below their predefined threshold.

2. Local Setup and Running Instructions
These instructions assume you have Java 21+ and a modern IDE (like Eclipse, STS, or IntelliJ) with Maven support installed.

Prerequisites
Java Development Kit (JDK) 21 or higher

Maven 3.x

A REST client (e.g., Postman) for testing the API endpoints.

Setup Steps
Clone the Repository:

git clone [Your Repository URL]
cd product-tracker

Verify Configuration:
Ensure your src/main/resources/application.properties file is configured correctly for local development with the H2 in-memory database:

spring.application.name=product-tracker
spring.datasource.url=jdbc:h2:mem:warehouse;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create
spring.h2.console.enabled=true 
spring.h2.console.path=/h2-console

Build the Project:
Open your project in the IDE and force a Maven build to download all dependencies:

mvn clean install

Run the Application:
Locate the main class (ProductTrackerApplication.java) and run it as a Spring Boot App from your IDE.

The application will start on the default port: http://localhost:8080

3. API Test Case Instructions (Using Postman)
Once the application is running, use a REST client to execute the following test cases.

Base URL: http://localhost:8080/api/products

Test 1: Create a Product (Setup)
Endpoint: /api/products

Method: POST

Body (JSON):

{
    "name": "Widget Alpha",
    "description": "High quality widget for general use.",
    "stockQuantity": 50,
    "lowStockThreshold": 10
}

Expected Response: 201 Created. Note the ID returned (e.g., 1), which will be used in subsequent tests.

Test 2: Increase Stock
Endpoint: /api/products/{id}/increase-stock (e.g., /api/products/1/increase-stock)

Method: PATCH

Body (JSON):

{
    "quantity": 10
}

Expected Response: 200 OK. Stock should increase to 60.

Test 3: Successful Decrease Stock
Endpoint: /api/products/{id}/decrease-stock

Method: PATCH

Body (JSON):

{
    "quantity": 5
}

Expected Response: 200 OK. Stock should decrease to 55.

Test 4: Insufficient Stock Error (Critical Test)
Endpoint: /api/products/{id}/decrease-stock

Method: PATCH

Body (JSON):

{
    "quantity": 100 
}

Expected Response: 400 Bad Request. The body will contain a custom error message reflecting the InsufficientStockException.

Test 5: Get Low Stock Products
Endpoint: /api/products/low-stock

Method: GET

Prerequisite: Ensure one product's stock is below its threshold (e.g., if threshold is 10, stock must be 9 or less).

Expected Response: 200 OK. Returns a JSON array containing only products that meet the low stock criteria.

4. Assumptions and Design Choices
Choice/Assumption

Rationale

H2 In-Memory Database

Used for rapid development and testing. Data is transient (cleared on application shutdown). This would be replaced by PostgreSQL or MySQL in production.

Transactional Stock Methods

Dedicated increaseStock and decreaseStock endpoints using the PATCH method (implemented as a POST for simplicity) ensure that stock updates are handled as atomic business operations within the Service layer, not just generic PUT updates.

Custom Exception Handling

The InsufficientStockException is a custom, unchecked exception mapped to a 400 Bad Request status code. This provides a clear error message to the client, enforcing the business rule that stock cannot go negative.

Simple DTO/Request Body

For the stock update endpoints, a simple JSON structure like {"quantity": 10} is assumed to be handled, which is cleaner than passing the quantity as a URL path variable.

