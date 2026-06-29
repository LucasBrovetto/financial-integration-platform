# Contexto del Desarrollador y Objetivo del Proyecto

Este proyecto se está construyendo para el portafolio y CV de Lucas Brovetto, con el objetivo de demostrar habilidades avanzadas y facilitar la reinserción en el mercado laboral de Java y Spring Boot 3.

El repositorio simulará una solución distribuida y robusta basada en las mejores prácticas de la industria de software financiero, microservicios y sistemas de pagos.

## Perfil Técnico Ampliado del Desarrollador (Basado en CV y Objetivos)
- **Rol principal:** Desarrollador Backend Java Senior con fuerte enfoque en integraciones, ecosistemas de microservicios y procesamiento de transacciones.
- **Ecosistema Java:** Java 17/21, Spring Boot 3.x, Spring Cloud, Spring Security, Apache Camel, Hibernate/JPA Avanzado, Lombok, MapStruct (Mappers).
- **Mensajería y Streaming:** Apache Kafka.
- **Arquitectura y Calidad:** Arquitectura Hexagonal, Clean Code, principios SOLID, SonarQube (métricas de calidad y código limpio).
- **Bases de Datos:** PostgreSQL and MongoDB.
- **DevOps y Despliegue:** Docker, Kubernetes, Maven, Jenkins (CI/CD).
- **Testing:** Unit Testing (JUnit 5, Mockito) e Integration Testing.

## Reglas Estrictas para Copilot

1. **Spring Boot 3, Java Moderno y Lombok:**
   - Utilizar características modernas de Java (Java 17+).
   - Combinar el uso de **Lombok** (`@Value`, `@Builder`, `@Data` de manera consciente) para reducir código repetitivo, pero priorizar **Records** nativos para payloads de mensería (DTOs de entrada/salida).
   - Inyección de dependencias **siempre por constructor** (puedes usar `@RequiredArgsConstructor` de Lombok para mantener el código limpio). Evitar `@Autowired` en atributos.
   - Manejo global de excepciones centralizado con `@RestControllerAdvice`.

2. **Arquitectura Hexagonal y Microservicios (Spring Cloud & Kafka):**
   - El core de **Dominio** debe estar 100% aislado. No debe contener anotaciones de Spring, JPA/Hibernate, Lombok de persistencia, ni anotaciones de Jackson/JSON.
   - La capa de **Infraestructura** debe contener las implementaciones de los adaptadores:
      - *Persistencia:* Repositorios avanzados de Hibernate/JPA utilizando buenas prácticas (evitar el problema de consultas N+1, uso correcto de cargas perezosas/Eager).
      - *Mapeo de Datos:* Utilizar **MapStruct** de forma estricta para realizar las conversiones de datos entre las Entidades de Persistencia (JPA/Mongo), los Objetos de Dominio y los DTOs de Infraestructura, manteniendo las capas totalmente desacopladas.
      - *Comunicación/Streaming:* Productores y consumidores de **Kafka** completamente desacoplados del dominio a través de puertos.
      - *Seguridad:* Configuraciones de **Spring Security** (filtros JWT, seguridad a nivel de método si aplica).
   - Si se diseñan múltiples componentes, utilizar patrones de **Spring Cloud** (Gateway, Config) preparados para un entorno orquestado con **Kubernetes**.

3. **Estrategia Rigurosa de Testing e Integración:**
   - El código debe diseñarse siguiendo principios de testabilidad.
   - Cada funcionalidad debe incluir su suite de **Unit Testing** (JUnit 5 + Mockito) y **Integration Testing** (utilizando `@SpringBootTest`, `Testcontainers` si aplica para simular PostgreSQL/Kafka, o perfiles de prueba separados).

4. **Calidad de Código (SonarQube Ready):**
   - Evitar "code smells", duplicación de código, métodos excesivamente largos o falta de manejo adecuado de logs/excepciones que degraden la calificación en **SonarQube**.

5. **Estructura de Construcción:**
   - La gestión de dependencias y el ciclo de vida del proyecto deben basarse estrictamente en **Maven** (uso correcto de plugins, perfiles y gestión de propiedades en el `pom.xml`).

6. **Estrategia de Control de Versiones (Git Commit Assistant):**
   - Al finalizar la generación o refactorización de código en cualquier respuesta, debes indicarme explícitamente si es un momento recomendado para hacer un **Git Commit**.
   - Debes listar específicamente qué clases/archivos deberían incluirse en ese commit para mantener la atomicidad.
   - Debes sugerir el mensaje de commit exacto siguiendo el estándar **Conventional Commits** (ej: `feat(domain): add transaction aggregate core logic` o `test(infra): implement integration tests for postgres adapter`).