# **Food Microservice API Documentation**

This document outlines the API endpoints for the **Food Microservice**, designed to manage food tracking in the **Gym Progress App (GPA)**. It includes all required HTTP requests, expected responses, and details about the `food_entry` model.

---

## **Food Model**

The `food_entry` table defines the following properties:

| **Property**   | **Type**          | **Description**                                                                 |
|-----------------|-------------------|---------------------------------------------------------------------------------|
| `id`           | `string` (UUID)   | Unique identifier for the food entry.                                          |
| `createdAt`    | `timestamp`       | Timestamp when the food entry was created (ISO 8601 format).                   |
| `updatedAt`    | `timestamp`       | Timestamp when the food entry was last updated (ISO 8601 format).              |
| `name`         | `string`          | Name of the meal (e.g., "Chicken Salad").                                      |
| `protein`      | `double`          | Protein content in grams.                                                      |
| `carbs`        | `double`          | Carbohydrates content in grams.                                                |
| `fat`          | `double`          | Fat content in grams.                                                          |
| `date`         | `date`            | The date the meal is associated with (`YYYY-MM-DD` format).                    |
| `userId`       | `string`          | Unique identifier of the user who owns this food entry.                        |

---

## **API Overview**

| **Method** | **Endpoint**       | **Description**                     |
|------------|--------------------|-------------------------------------|
| `POST`     | `/api/food`        | Add a new meal                      |
| `GET`      | `/api/food`        | Retrieve all meals for the user     |
| `GET`      | `/api/food/{id}`   | Fetch details of a specific meal    |
| `PUT`      | `/api/food/{id}`   | Update an existing meal             |
| `DELETE`   | `/api/food/{id}`   | Delete a specific meal              |

---

## **1. Add a New Meal**

### **POST** `/api/food`

This endpoint allows users to create a new meal entry.

---

### **Request Headers**

| **Key**         | **Value**               |
|------------------|-------------------------|
| `Content-Type`   | `application/json`     |
| `Authorization`  | `Bearer {token}`       |



### **Request Body**

```json
{
  "name": "Chicken Salad",
  "protein": 45.5,
  "carbs": 12.3,
  "fat": 15.0,
  "date": "2024-12-17"
}
```

| **Field**  | **Type** | **Description**                                   |
|------------|----------|---------------------------------------------------|
| `name`     | `string` | Name of the meal.                                 |
| `protein`  | `number` | Protein content in grams.                         |
| `carbs`    | `number` | Carbohydrates content in grams.                   |
| `fat`      | `number` | Fat content in grams.                             |
| `date`     | `string` | Date associated with the meal (`YYYY-MM-DD`).     |



### **Response**

#### **201 Created**

```json
{
  "id": "12345",
  "name": "Chicken Salad",
  "protein": 45.5,
  "carbs": 12.3,
  "fat": 15.0,
  "date": "2024-12-17",
  "userId": "abc123",
  "createdAt": "2024-12-17T08:00:00Z",
  "updatedAt": "2024-12-17T08:00:00Z"
}
```

#### **400 Bad Request**

| **Error** | **Description**         |
|-----------|-------------------------|
| `error`   | "Invalid input data."   |

```json
{
  "error": "Invalid input data."
}
```

#### **401 Unauthorized**

| **Field**   | **Description**                |
|-------------|--------------------------------|
| `error`     | "Authentication is required." |

```json
{
  "error": "Authentication is required."
}
```
2. Retrieve All Meals
GET /api/food
Retrieve all meals associated with the authenticated user.

Request Headers
Key	Value
Authorization	Bearer {token}
Query Parameters (Optional)
Key	Type	Description
date	string	Filter meals by a specific date (format: YYYY-MM-DD).
Response
200 OK
[
  {
    "id": "12345",
    "name": "Chicken Salad",
    "protein": 45.5,
    "carbs": 12.3,
    "fat": 15.0,
    "date": "2024-12-17",
    "userId": "abc123",
    "createdAt": "2024-12-17T08:00:00Z",
    "updatedAt": "2024-12-17T08:00:00Z"
  },
  {
    "id": "12346",
    "name": "Beef Stir Fry",
    "protein": 40.0,
    "carbs": 20.0,
    "fat": 10.0,
    "date": "2024-12-17",
    "userId": "abc123",
    "createdAt": "2024-12-17T08:00:00Z",
    "updatedAt": "2024-12-17T08:00:00Z"
  }
]

401 Unauthorized
{
  "error": "Authentication is required."
}

3. Fetch Meal by ID
GET /api/food/{id}
Fetch details of a specific meal by its unique identifier.

Request Headers
Key	Value
Authorization	Bearer {token}
Path Parameters
Key	Type	Description
id	string	The unique identifier of the meal.
Response
200 OK
{
  "id": "12345",
  "name": "Chicken Salad",
  "protein": 45.5,
  "carbs": 12.3,
  "fat": 15.0,
  "date": "2024-12-17",
  "userId": "abc123",
  "createdAt": "2024-12-17T08:00:00Z",
  "updatedAt": "2024-12-17T08:00:00Z"
}

404 Not Found
{
  "error": "Meal not found."
}

401 Unauthorized
{
  "error": "Authentication is required."
}

4. Update a Meal
PUT /api/food/{id}
Update the details of an existing meal.

Request Headers
Key	Value
Content-Type	application/json
Authorization	Bearer {token}
Path Parameters
Key	Type	Description
id	string	The unique identifier of the meal.
Request Body
{
  "name": "Updated Meal Name",
  "protein": 50.0,
  "carbs": 20.0,
  "fat": 10.0,
  "date": "2024-12-18"
}

Field	Type	Description
name	string	Updated name of the meal.
protein	number	Updated protein content in grams.
carbs	number	Updated carbohydrates in grams.
fat	number	Updated fat content in grams.
date	string	Updated date associated with the meal.
Response
200 OK
{
  "id": "12345",
  "name": "Updated Meal Name",
  "protein": 50.0,
  "carbs": 20.0,
  "fat": 10.0,
  "date": "2024-12-18",
  "userId": "abc123",
  "createdAt": "2024-12-17T08:00:00Z",
  "updatedAt": "2024-12-18T08:00:00Z"
}

400 Bad Request
{
  "error": "Invalid input data."
}

404 Not Found
{
  "error": "Meal not found."
}

401 Unauthorized
{
  "error": "Authentication is required."
}

5. Delete a Meal
DELETE /api/food/{id}
Delete an existing meal by its unique identifier.

Request Headers
Key	Value
Authorization	Bearer {token}
Path Parameters
Key	Type	Description
id	string	The unique identifier of the meal.
Response
204 No Content
(No body returned)

404 Not Found
{
  "error": "Meal not found."
}

401 Unauthorized
{
  "error": "Authentication is required."
}

Status Code Summary
Status Code	Description
200 OK	Request succeeded. Returns the requested data.
201 Created	Resource successfully created.
204 No Content	Resource successfully deleted.
400 Bad Request	Invalid request data.
401 Unauthorized	Authentication is required.
404 Not Found	Resource could not be found.
🎉 Ready to integrate your Food Microservice? Let’s track your meals with ease! 🚀