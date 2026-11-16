# Food API - Spring Boot 3

API RESTful para gerenciamento de cardápio de restaurante, com uma interface web simples (HTML, CSS e JavaScript puros) servida pelo próprio Spring Boot. Os dados ficam em um banco PostgreSQL.

## Como executar

1. Tenha o JDK 17 ou superior instalado.
2. Tenha o PostgreSQL rodando em `localhost:5432` (usuário `postgres`, senha `postgres`).
3. Crie o banco (as tabelas são criadas pelo JPA ao iniciar):
   ```bash
   createdb -h localhost -U postgres food_api
   ```
4. Execute a classe `FoodApiApplication.java` ou rode:
   ```bash
   ./mvnw spring-boot:run      # Windows: .\mvnw.cmd spring-boot:run
   ```
5. Abra http://localhost:8080 para usar a interface. A API fica em `http://localhost:8080/api/comidas`.

## Endpoints

| Verbo | Rota | Corpo | Resposta |
|-------|------|-------|----------|
| POST | `/api/comidas` | JSON da comida | 201 + comida criada; 400 se inválido |
| GET | `/api/comidas` | - | 200 + lista de comidas |
| GET | `/api/comidas?categoria=BEBIDA` | - | 200 + lista filtrada; 400 se categoria inválida |
| GET | `/api/comidas/{id}` | - | 200 + comida; 404 se não existe |
| GET | `/api/comidas/nome/{nome}` | - | 200 + comida; 404 se não existe |
| PUT | `/api/comidas/{id}` | JSON da comida | 200 + comida atualizada; 400 / 404 |
| DELETE | `/api/comidas/{id}` | - | 204 sem corpo; 404 se não existe |
| DELETE | `/api/comidas/nome/{nome}` | - | 204 sem corpo; 404 se não existe |

## Formato do JSON

Envio (POST e PUT):

```json
{
  "nome": "Pizza Margherita",
  "descricao": "Molho de tomate, mussarela e manjericão",
  "preco": 39.90,
  "categoria": "PRATO_PRINCIPAL",
  "disponivel": true,
  "tempoPreparoMinutos": 25
}
```

Categorias aceitas: `ENTRADA`, `PRATO_PRINCIPAL`, `SOBREMESA`, `BEBIDA`, `LANCHE`.

A resposta traz os mesmos campos mais `id`, `dataCriacao` e `dataAtualizacao`.

Erro 404: `{ "timestamp": ..., "status": 404, "error": "Recurso não encontrado", "message": "..." }`.
Erro 400 de validação: `{ "timestamp": ..., "status": 400, "errors": { "campo": "mensagem" } }`.

## Estrutura do projeto

```
food-api/
├── pom.xml
├── docs/                      PROMPT.md e APRESENTACAO.md
└── src/main/
    ├── java/com/exemplo/foodapi/
    │   ├── controller/        ComidaController
    │   ├── dto/               ComidaRequestDTO, ComidaResponseDTO
    │   ├── entity/            Comida
    │   ├── enums/             CategoriaComida
    │   ├── repository/        ComidaRepository
    │   ├── service/           ComidaService
    │   └── exception/         ResourceNotFoundException, GlobalExceptionHandler
    └── resources/
        ├── application.properties
        └── static/            index.html, api.js, script.js, style.css
```
