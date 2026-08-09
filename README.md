
## Getting Started

This project uses Maven to manage dependencies and Spring Boot for a simple application.

### Prerequisites

* Maven installed on your system
* Java Development Kit (JDK) 1.8 or higher

### Running the Application

To run the application, follow these steps:

1. Run `mvn clean install` in your terminal to compile and package the project.
2. Once the build process is complete, run `mvn spring-boot:run` to start the Spring Boot application.

## Usage

The application will be available at [http://localhost:8080](http://localhost:8080).

### Maven Commands

| Command | Description |
| --- | --- |
| `mvn clean install` | Compiles and packages the project. |
| `mvn spring-boot:run` | Starts the Spring Boot application. |

### Endpoints

* `/api/alerts`: Retrieve list of alerts
* `/api/spottings`: Post a spotting
* `/api/routes/5/{id}`: Get Route details with route number