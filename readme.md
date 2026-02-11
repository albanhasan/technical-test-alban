# Technical Test Alban - API Documentation

## Installation

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd technical-test-alban

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on **http://localhost:8081**

### H2 Database Console
Access the H2 console at: **http://localhost:8081/h2-console**

**Connection Details:**
- **JDBC URL:** `jdbc:h2:mem:inventorydb`
- **Username:** `sa`
- **Password:** *(empty)*

---

## API Endpoints

### Items API

Manage product items in the system.

#### 1. Get Item by ID
```http
GET /items/{id}
```

**Response:**
```json
{
  "success": true,
  "message": "Items found",
  "data": {
    "id": 1,
    "name": "Laptop",
    "price": 1500.00,
    "remainingStock": 45
  }
}
```

#### 2. List All Items (Paginated)
```http
GET /items?page=0&size=10&sortBy=name&sortDirection=ASC&includeStock=true
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 10 | Number of items per page |
| `sortBy` | string | id | Field to sort by (id, name, price) |
| `sortDirection` | string | ASC | Sort direction (ASC, DESC) |
| `includeStock` | boolean | false | Include remaining stock calculation |

**Response:**
```json
{
  "success": true,
  "message": "Items retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Laptop",
        "price": 1500.00,
        "remainingStock": 45
      },
      {
        "id": 2,
        "name": "Mouse",
        "price": 25.00,
        "remainingStock": 150
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 2,
    "totalPages": 1
  }
}
```

#### 3. Create Item
```http
POST /items
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Cap",
  "price": 25
}
```

**Validations:**
- `name`: Required, must not be blank, must be unique
- `price`: Required, must be positive

**Response:**
```json
{
  "success": true,
  "message": "Items Created successfully",
  "data": {
    "id": 3,
    "name": "Cap",
    "price": 25.00,
    "remainingStock": null
  }
}
```

**Error Cases:**
- **409 Conflict**: Item with the same name already exists

#### 4. Update Item
```http
PUT /items/{id}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Keyboard",
  "price": 50000
}
```

**Response:**
```json
{
  "success": true,
  "message": "Items Updated successfully",
  "data": {
    "id": 3,
    "name": "Keyboard",
    "price": 50000.00,
    "remainingStock": null
  }
}
```

**Error Cases:**
- **404 Not Found**: Item does not exist
- **409 Conflict**: New name conflicts with existing item

#### 5. Delete Item
```http
DELETE /items/{id}
```

**Response:**
```json
{
  "success": true,
  "message": "Item deleted successfully",
  "data": null
}
```

**Error Cases:**
- **404 Not Found**: Item does not exist

---

### Orders API

Manage customer orders with automatic stock validation.

#### 1. Get Order by ID
```http
GET /orders/{orderId}
```

**Response:**
```json
{
  "success": true,
  "message": "Order found",
  "data": {
    "orderNo": "1",
    "itemId": 1,
    "itemName": "Laptop",
    "qty": 2,
    "price": 5
  }
}
```

#### 2. List All Orders (Paginated)
```http
GET /orders?page=0&size=10&sortBy=qty&sortDirection=DESC
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 10 | Number of orders per page |
| `sortBy` | string | orderNo | Field to sort by (orderNo, qty, price) |
| `sortDirection` | string | DESC | Sort direction (ASC, DESC) |

**Response:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "content": [
      {
        "orderNo": "1",
        "itemId": 1,
        "itemName": "Laptop",
        "qty": 5,
        "price": 7500.00
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

#### 3. Create Order
```http
POST /orders
Content-Type: application/json
```

**Request Body:**
```json
{
  "itemId": 1,
  "qty": 2,
  "price": 5
}
```

**Validations:**
- `itemId`: Required, item must exist
- `qty`: Required, must be positive
- `price`: Required, must be positive
- **Stock Check**: Requested quantity must not exceed available stock

**Response:**
```json
{
  "success": true,
  "message": "Order Created successfully",
  "data": {
    "orderNo": "2",
    "itemId": 1,
    "itemName": "Laptop",
    "qty": 2,
    "price": 5.00
  }
}
```

**Error Cases:**
- **404 Not Found**: Item does not exist
- **400 Bad Request**: Insufficient stock available

#### 4. Update Order
```http
PUT /orders/{orderId}
Content-Type: application/json
```

**Request Body:**
```json
{
  "itemId": 1,
  "qty": 11,
  "price": 5
}
```

**Business Logic:**
- If **increasing** quantity: validates additional stock availability
- If **decreasing** quantity: no stock validation needed

**Response:**
```json
{
  "success": true,
  "message": "Order Updated successfully",
  "data": {
    "orderNo": "1",
    "itemId": 1,
    "itemName": "Laptop",
    "qty": 11,
    "price": 5.00
  }
}
```

**Error Cases:**
- **404 Not Found**: Order or item does not exist
- **400 Bad Request**: Insufficient stock for increased quantity

#### 5. Delete Order
```http
DELETE /orders/{orderNo}
```

**Response:**
```json
{
  "success": true,
  "message": "Order Deleted successfully",
  "data": null
}
```

**Note:** Deleting an order releases the reserved stock back to available inventory.

---

### Inventories API

Manage stock with Top-ups (T) and Withdrawals (W).

#### 1. Get Inventories by ID
```http
GET /inventories/{id}
```

**Response:**
```json
{
  "success": true,
  "message": "Inventory found",
  "data": {
    "id": 1,
    "itemId": 1,
    "itemName": "Laptop",
    "qty": 50,
    "type": "T"
  }
}
```

#### 2. List All Inventories (Paginated)
```http
GET /inventories?page=0&size=10&sortBy=qty&sortDirection=DESC
```

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 10 | Number of records per page |
| `sortBy` | string | id | Field to sort by (id, qty, type) |
| `sortDirection` | string | DESC | Sort direction (ASC, DESC) |

**Response:**
```json
{
  "success": true,
  "message": "Inventory found",
  "data": {
    "content": [
      {
        "id": 1,
        "itemId": 1,
        "itemName": "Laptop",
        "qty": 100,
        "type": "T"
      },
      {
        "id": 2,
        "itemId": 1,
        "itemName": "Laptop",
        "qty": 10,
        "type": "W"
      }
    ],
    "totalElements": 2
  }
}
```

#### 3. Create Inventories
```http
POST /inventories
Content-Type: application/json
```

**Request Body:**
```json
{
  "itemId": 1,
  "qty": 6,
  "type": "T"
}
```

**Inventory Types:**
- **`T` (Top-up)**: Adds stock to inventory
- **`W` (Withdrawal)**: Removes stock from inventory

**Validations:**
- `itemId`: Required, item must exist
- `qty`: Required, must be positive
- `type`: Required, must be "T" or "W"
- **For Withdrawal (W)**: Available stock must be sufficient

**Response:**
```json
{
  "success": true,
  "message": "Inventory Created successfully",
  "data": {
    "id": 3,
    "itemId": 1,
    "itemName": "Laptop",
    "qty": 6,
    "type": "T"
  }
}
```

**Error Cases:**
- **404 Not Found**: Item does not exist
- **400 Bad Request**: Insufficient stock for withdrawal

#### 4. Update Inventories
```http
PUT /inventories/{id}
Content-Type: application/json
```

**Request Body:**
```json
{
  "itemId": 1,
  "qty": 10,
  "type": "T"
}
```

**Business Logic:**
- Calculates impact: `newImpact - oldImpact`
- Top-up (T): Positive impact on stock
- Withdrawal (W): Negative impact on stock
- Validates that update won't cause negative stock

**Response:**
```json
{
  "success": true,
  "message": "Inventory Created successfully",
  "data": {
    "id": 1,
    "itemId": 1,
    "itemName": "Laptop",
    "qty": 10,
    "type": "T"
  }
}
```

**Error Cases:**
- **404 Not Found**: Inventory or item does not exist
- **400 Bad Request**: Update would result in negative stock

#### 5. Delete Inventories
```http
DELETE /inventories/{id}
```

**Business Logic:**
- Validates that deletion won't cause negative stock
- If deleting a top-up (T): Removes stock (must have enough available)
- If deleting a withdrawal (W): Adds stock back

**Response:**
```json
{
  "success": true,
  "message": "Inventory deleted successfully",
  "data": null
}
```

**Error Cases:**
- **404 Not Found**: Inventory does not exist
- **400 Bad Request**: Deletion would result in negative stock

---

## 📊 Data Models

### ItemDTO
```json
{
  "id": 1,                      // Long (auto-generated)
  "name": "Laptop",             // String (required, unique)
  "price": 1500.00,             // BigDecimal (required, positive)
  "remainingStock": 45          // Integer (calculated: inventory - orders)
}
```

### OrderDTO
```json
{
  "orderNo": "1",               // String (auto-generated)
  "itemId": 1,                  // Long (required)
  "itemName": "Laptop",         // String (read-only)
  "qty": 2,                     // Integer (required, positive)
  "price": 5.00                 // BigDecimal (required, positive)
}
```

### InventoryDTO
```json
{
  "id": 1,                      // Long (auto-generated)
  "itemId": 1,                  // Long (required)
  "itemName": "Laptop",         // String (read-only)
  "qty": 50,                    // Integer (required, positive)
  "type": "T"                   // String (required: "T" or "W")
}
```

### ApiResponse (Wrapper)
```json
{
  "success": true,              // Boolean (operation status)
  "message": "Success message", // String (descriptive message)
  "data": {}                    // T (generic data payload)
}
```

---

## Response Format

All API responses follow a consistent format:

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### HTTP Status Codes

| Code | Description | Use Case |
|------|-------------|----------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST (not currently used) |
| 400 | Bad Request | Validation errors, insufficient stock |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Duplicate resource (e.g., item name) |
| 500 | Internal Server Error | Unexpected server errors |

---

## Error Handling

### Common Error Scenarios

#### 1. Resource Not Found (404)
```json
{
  "success": false,
  "message": "Item not found with id: 999",
  "data": null
}
```

#### 2. Duplicate Resource (409)
```json
{
  "success": false,
  "message": "Item with name 'Laptop' already exists",
  "data": null
}
```

#### 3. Insufficient Stock (400)
```json
{
  "success": false,
  "message": "Insufficient stock for item 'Laptop'. Requested: 100, Available: 45",
  "data": null
}
```

#### 4. Validation Error (400)
```json
{
  "success": false,
  "message": "Quantity must be positive",
  "data": null
}
```

#### 5. Type Validation (400)
```json
{
  "success": false,
  "message": "Type must be either 'T' (Top Up) or 'W' (Withdrawal)",
  "data": null
}
```

---

## Postman Collections

Three Postman collections are included in the project root:

### 1. Items.postman_collection.json
Contains all item-related API requests:
- Get Item
- List Items
- Create Item
- Update Item
- Delete Item

### 2. Orders.postman_collection.json
Contains all order-related API requests:
- Get Order
- List Orders
- Create Order
- Update Order
- Delete Order

### 3. Inventory.postman_collection.json
Contains all inventory-related API requests:
- Get Inventory
- List Inventory
- Create Inventory (Top-up/Withdrawal)
- Update Inventory
- Delete Inventory

### Importing Collections

1. Open Postman
2. Click **Import** button
3. Select the `.postman_collection.json` files
4. Collections will be imported with all configured requests

### Environment Variables

Set up a Postman environment with:
```json
{
  "LOCAL_HOST": "http://localhost:8081"
}
```

---

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```
### Test Files Location
```
src/test/java/com/alban/technical_test_alban/service/impl/
├── ItemServiceImplTest.java          (17 tests)
├── OrderServiceImplTest.java         (14 tests)
└── InventoryServiceImplTest.java     (16 tests)
```

**Total: 47 comprehensive unit tests**
