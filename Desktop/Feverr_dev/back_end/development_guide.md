

# Real Estate Listing and Management System - API Development GuideLines

### This implementation provides:

- Complete project structure with Maven
- All required endpoints as specified in the requiment doc part 1
- Standard response format
- Jakarta Filter for authentication (without Spring Security)
- Model classes matching the class diagram in the requiment doc part 1
-  Minimal Spring dependencies for easier framework migration
-  DO NOT USE jakarta.validation we will do our validation
-  Avoid spring specific annotation like   @Bean as much as possible

- Professional code organization and comments

- The implementation leaves service and repository layers empty for your implementation.

- The authentication filter is implemented but currently just passes through requests - you would need to add your actual authentication logic there.
  
-----

## Project Structure

- GroupId: `com.imovel.api`

- ArtfactId: `imovel-api`

 - Application Properties
  
``` properties

# Server configuration
server.port=8080
server.servlet.context-path=/imovel
# Application properties
app.name=Imovel API
app.version=1.0.0
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/imovel_db

<!-- lets all use these credentials -->
spring.datasource.username=postgres
spring.datasource.password=postgres  
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate/JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

# Connection pool settings (HikariCP is the default with Spring Boot)
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.max-lifetime=200000
spring.datasource.hikari.auto-commit=true
```

- Project structure

``` xml

imovel-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── imovel/
│   │   │           └── api/
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── response/
|   |   |               ├── request/
│   │   │               ├── filter/
│   │   │               ├── model/
|   |   |               |──repository/
│   │   │               ├── service/
│   │   │               └── ImovelApiApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml

```

- Base pom.xml
- We will remove lombok dependency in the future

``` xml

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath/>
    </parent>

    <groupId>com.imovel.api</groupId>
    <artifactId>imovel-api</artifactId>
    <version>1.0.0</version>
    <name>imovel-api</name>
    <description>Real Estate Listing and Management System</description>

    <properties>
        <java.version>17</java.version>
        <jakarta.servlet-api.version>5.0.0</jakarta.servlet-api.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>${jakarta.servlet-api.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

```
- Application entry point
  
``` java
package com.imovel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImovelApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImovelApiApplication.class, args);
    }
}


``` 

- StandardResponse.java
``` java

package com.imovel.api.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response structure for all endpoints
 * @param <T> Type of the data payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardResponse<T> {
    private String errorText;
    private String errorCode;
    private T data;
}

```

- AuthenticationFilter.java
  
``` java

package com.imovel.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter for authenticating requests using JWT or other authentication mechanisms
 */
@Component
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        
        chain.doFilter(request, response);
    }
}

```
- Model classes (under com.imovel.api.model)
  
``` java

package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a user in the system
 */
@Data
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String avatar;
    private UserRole role;
    private List<String> socialLinks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum UserRole {
    ADMIN, AGENT, CLIENT
}

package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a property listing
 */
@Data
public class Property {
    private String id;
    private String title;
    private String description;
    private Double price;
    private PropertyType type;
    private PropertyCategory category;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;
    private Location location;
    private List<String> amenities;
    private List<String> images;
    private PropertyStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum PropertyType {
    HOUSE, APARTMENT, LAND, COMMERCIAL
}

public enum PropertyCategory {
    SALE, RENT, LEASE
}

public enum PropertyStatus {
    AVAILABLE, PENDING, SOLD, RENTED
}

@Data
class Location {
    private String address;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;
}

package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a user's wishlist of properties
 */
@Data
public class Wishlist {
    private String id;
    private String userId;
    private List<String> propertyIds;
    private LocalDateTime createdAt;
}

package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a review/rating for a property
 */
@Data
public class Review {
    private String id;
    private String propertyId;
    private String userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents media files associated with properties
 */
@Data
public class Media {
    private String id;
    private String url;
    private MediaType type;
    private String format;
    private Integer width;
    private Integer height;
    private Long size;
    private Integer duration;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum MediaType {
    IMAGE, VIDEO, DOCUMENT, TOUR_3D
}

```

- Controller classes (under com.imovel.api.controller)

``` java
package com.imovel.api.controller;

import com.imovel.api.dto.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public StandardResponse<Void> register() {
        // TODO: Implement registration logic
        return new StandardResponse<>();
    }

    @PostMapping("/login")
    public StandardResponse<String> login() {
        // TODO: Implement login logic
        return new StandardResponse<>();
    }

    @PostMapping("/logout")
    public StandardResponse<Void> logout() {
        // TODO: Implement logout logic
        return new StandardResponse<>();
    }

    @PostMapping("/forgot-password")
    public StandardResponse<Void> forgotPassword() {
        // TODO: Implement forgot password logic
        return new StandardResponse<>();
    }

    @PostMapping("/reset-password")
    public StandardResponse<Void> resetPassword() {
        // TODO: Implement reset password logic
        return new StandardResponse<>();
    }
}

package com.imovel.api.controller;

import com.imovel.api.dto.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * User profile management endpoints
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public StandardResponse<Void> getCurrentUser() {
        // TODO: Implement get current user logic
        return new StandardResponse<>();
    }

    @PutMapping("/me")
    public StandardResponse<Void> updateProfile() {
        // TODO: Implement update profile logic
        return new StandardResponse<>();
    }

    @PutMapping("/me/password")
    public StandardResponse<Void> changePassword() {
        // TODO: Implement change password logic
        return new StandardResponse<>();
    }

    @PostMapping("/me/avatar")
    public StandardResponse<Void> uploadAvatar() {
        // TODO: Implement upload avatar logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/me/avatar")
    public StandardResponse<Void> removeAvatar() {
        // TODO: Implement remove avatar logic
        return new StandardResponse<>();
    }
}

package com.imovel.api.controller;

import com.imovel.api.dto.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Property management endpoints
 */
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @GetMapping
    public StandardResponse<Void> getAllProperties() {
        // TODO: Implement get all properties logic
        return new StandardResponse<>();
    }

    @PostMapping
    public StandardResponse<Void> createProperty() {
        // TODO: Implement create property logic
        return new StandardResponse<>();
    }

    @GetMapping("/{id}")
    public StandardResponse<Void> getProperty(@PathVariable String id) {
        // TODO: Implement get property logic
        return new StandardResponse<>();
    }

    @PutMapping("/{id}")
    public StandardResponse<Void> updateProperty(@PathVariable String id) {
        // TODO: Implement update property logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{id}")
    public StandardResponse<Void> deleteProperty(@PathVariable String id) {
        // TODO: Implement delete property logic
        return new StandardResponse<>();
    }

    @GetMapping("/me")
    public StandardResponse<Void> getUserProperties() {
        // TODO: Implement get user properties logic
        return new StandardResponse<>();
    }

    @PostMapping("/{id}/media")
    public StandardResponse<Void> uploadPropertyMedia(@PathVariable String id) {
        // TODO: Implement upload property media logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    public StandardResponse<Void> deletePropertyMedia(
            @PathVariable String id, 
            @PathVariable String mediaId) {
        // TODO: Implement delete property media logic
        return new StandardResponse<>();
    }
}

package com.imovel.api.controller;

import com.imovel.api.dto.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Wishlist management endpoints
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @GetMapping
    public StandardResponse<Void> getWishlist() {
        // TODO: Implement get wishlist logic
        return new StandardResponse<>();
    }

    @PostMapping("/{propertyId}")
    public StandardResponse<Void> addToWishlist(@PathVariable String propertyId) {
        // TODO: Implement add to wishlist logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{propertyId}")
    public StandardResponse<Void> removeFromWishlist(@PathVariable String propertyId) {
        // TODO: Implement remove from wishlist logic
        return new StandardResponse<>();
    }

    @DeleteMapping
    public StandardResponse<Void> clearWishlist() {
        // TODO: Implement clear wishlist logic
        return new StandardResponse<>();
    }
}

package com.imovel.api.controller;

import com.imovel.api.dto.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Search endpoints
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @GetMapping
    public StandardResponse<Void> searchProperties() {
        // TODO: Implement search properties logic
        return new StandardResponse<>();
    }

    @GetMapping("/suggestions")
    public StandardResponse<Void> getSearchSuggestions() {
        // TODO: Implement search suggestions logic
        return new StandardResponse<>();
    }
}

package com.imovel.api.controller;

import com.imovel.api.dto.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Review management endpoints
 */
@RestController
@RequestMapping("/api/properties/{propertyId}/reviews")
public class ReviewController {

    @GetMapping
    public StandardResponse<Void> getPropertyReviews(@PathVariable String propertyId) {
        // TODO: Implement get property reviews logic
        return new StandardResponse<>();
    }

    @PostMapping
    public StandardResponse<Void> addReview(@PathVariable String propertyId) {
        // TODO: Implement add review logic
        return new StandardResponse<>();
    }
}

@RestController
@RequestMapping("/api/reviews")
class ReviewDeleteController {
    
    @DeleteMapping("/{id}")
    public StandardResponse<Void> deleteReview(@PathVariable String id) {
        // TODO: Implement delete review logic
        return new StandardResponse<>();
    }
}


package com.imovel.api.controller;

import com.imovel.api.dto.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Media management endpoints
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    @PostMapping("/upload")
    public StandardResponse<Void> uploadMedia() {
        // TODO: Implement upload media logic
        return new StandardResponse<>();
    }

    @GetMapping("/{mediaId}")
    public StandardResponse<Void> getMedia(@PathVariable String mediaId) {
        // TODO: Implement get media logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{mediaId}")
    public StandardResponse<Void> deleteMedia(@PathVariable String mediaId) {
        // TODO: Implement delete media logic
        return new StandardResponse<>();
    }
}

```
- Main application class
  
``` java
package com.imovel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImovelApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImovelApiApplication.class, args);
    }
}

```