# Food API - Spring Boot 3

API RESTful completa para gerenciamento de cardápio de restaurante com tema de Comida.

## 📁 Estrutura do Projeto

- `controller`: `ComidaController.java` (Endpoints REST)
- `dto`: `ComidaRequestDTO.java`, `ComidaResponseDTO.java`
- `entity`: `Comida.java`
- `enums`: `CategoriaComida.java`
- `repository`: `ComidaRepository.java`
- `service`: `ComidaService.java`
- `exception`: `ResourceNotFoundException.java`, `GlobalExceptionHandler.java`

## 🚀 Como Executar

1. Importe o projeto em sua IDE favorita (IntelliJ IDEA, Eclipse, VS Code).
2. Execute a classe principal `FoodApiApplication.java` ou rode via terminal:
   ```bash
   mvn spring-boot:run
   ```
3. A API estará disponível em `http://localhost:8080/api/comidas`.
4. O console do banco H2 fica em `http://localhost:8080/h2-console`.
