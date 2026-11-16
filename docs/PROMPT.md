# Prompt utilizado para gerar o frontend

## Como este prompt foi usado

O frontend foi gerado pela IA Claude em conversa, a partir de pedidos curtos do aluno. O texto abaixo é um prompt refinado e consolidado: ele reúne, de forma detalhada, os requisitos que resultaram dessa conversa. Não é o texto que o aluno digitou palavra por palavra. Os pedidos originais estão no final deste documento, em "Pedidos feitos em sequência".

## Prompt detalhado

### Papel

Você é um desenvolvedor frontend sênior. Gere o frontend de uma aplicação de cardápio de restaurante e faça as pequenas adaptações necessárias no backend Spring Boot já existente. O backend já foi testado no Postman. Não troque o tema do projeto: a aplicação é um cardápio de comidas.

### Contexto

- Backend: Spring Boot 3.2, Java 17, Spring Data JPA e PostgreSQL (banco `food_api`, tabela `tb_comidas`).
- Frontend: arquivos estáticos em `src/main/resources/static`, servidos pelo próprio Spring em `http://localhost:8080`. Frontend e API ficam na mesma origem. Use URLs relativas (`/api/comidas`) e não configure CORS.
- Tecnologia: apenas HTML, CSS e JavaScript puros, com módulos ES. Sem frameworks, sem bibliotecas JavaScript e sem etapa de build. A única dependência externa permitida são as fontes do Google Fonts.
- Todo texto da interface, comentários e nomes devem estar em português do Brasil.
- Nome fictício do restaurante: **Casa Jabuti**. Não use marca real.

### Contrato da API

Base: `/api/comidas`. Categorias válidas (enum `CategoriaComida`): `ENTRADA`, `PRATO_PRINCIPAL`, `SOBREMESA`, `BEBIDA`, `LANCHE`.

| Verbo | Rota | Corpo | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/comidas` | comida | 201 e a comida criada | 400 |
| GET | `/api/comidas` | nenhum | 200 e lista | nenhum |
| GET | `/api/comidas?categoria=BEBIDA` | nenhum | 200 e lista filtrada | 400 se a categoria não existe |
| GET | `/api/comidas/{id}` | nenhum | 200 e a comida | 404 |
| GET | `/api/comidas/nome/{nome}` | nenhum | 200 e a comida (ignora maiúsculas) | 404 |
| PUT | `/api/comidas/{id}` | comida | 200 e a comida atualizada | 400, 404 |
| DELETE | `/api/comidas/{id}` | nenhum | 204 sem corpo | 404 |
| DELETE | `/api/comidas/nome/{nome}` | nenhum | 204 sem corpo | 404 (a tela não usa) |

Corpo enviado no POST e no PUT:

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

Resposta (acrescenta `id`, `dataCriacao` e `dataAtualizacao`):

```json
{
  "id": 1,
  "nome": "Pizza Margherita",
  "descricao": "Molho de tomate, mussarela e manjericão",
  "preco": 39.90,
  "categoria": "PRATO_PRINCIPAL",
  "disponivel": true,
  "tempoPreparoMinutos": 25,
  "dataCriacao": "2026-11-16T20:00:00",
  "dataAtualizacao": "2026-11-16T20:00:00"
}
```

Erro 404:

```json
{ "timestamp": "...", "status": 404, "error": "Recurso não encontrado", "message": "Item de comida não encontrado com ID: 99" }
```

Erro 400 de validação (mapa de campo para mensagem):

```json
{ "timestamp": "...", "status": 400, "errors": { "nome": "O nome é obrigatório", "preco": "O preço é obrigatório" } }
```

Regras de validação: `nome` obrigatório, até 100 caracteres; `preco` obrigatório e maior que zero; `categoria` obrigatória; `tempoPreparoMinutos`, se enviado, no mínimo 1; `descricao` e `disponivel` opcionais. Se `disponivel` não for enviado, vale `true`. No PUT, `disponivel` e `tempoPreparoMinutos` enviados como `null` mantêm o valor já gravado.

### Funcionalidades (ação da tela para endpoint)

| Ação na tela | Chamada |
|---|---|
| Abrir a página | `GET /api/comidas` |
| Escolher uma categoria no filtro | `GET /api/comidas?categoria=X`; "Todas" volta para `GET /api/comidas` |
| Buscar por nome (botão Buscar) | `GET /api/comidas/nome/{nome}`; mostra um item ou "Nenhuma comida encontrada." (404) |
| Salvar um formulário vazio | `POST /api/comidas` |
| Botão Editar de um item | `GET /api/comidas/{id}` e preenche o formulário; Salvar chama `PUT /api/comidas/{id}`; há "Cancelar edição" |
| Botão Excluir de um item | `confirm()` e depois `DELETE /api/comidas/{id}` |
| Depois de criar, editar ou excluir | recarrega o cardápio com `GET` |

Mensagens de sucesso ("Comida cadastrada.", "Comida atualizada.", "Comida excluída.") e de erro aparecem em uma área própria.

### Componentes da interface

- **Cabeçalho da casa:** faixa escura com o nome "Casa Jabuti" em fonte serifada grande e uma frase curta de boas-vindas.
- **Área de mensagens:** `<p id="mensagem" role="status" aria-live="polite">`, com borda lateral azul para sucesso e rosa para erro. Fica oculta quando vazia.
- **Cardápio:** itens agrupados por categoria, na ordem Entrada, Prato principal, Sobremesa, Bebida, Lanche. Cada seção tem título grande e a quantidade de itens. Cada item mostra o nome e o preço (R$, com `Intl.NumberFormat('pt-BR')`) ligados por uma linha pontilhada, a descrição, o tempo de preparo e os botões Editar e Excluir.
- **Selo de indisponível:** item com `disponivel` falso mostra o selo "Indisponível hoje", com nome riscado e preço em cinza.
- **Filtro e busca:** `<select>` de categoria, campo "Buscar por nome" e botões Buscar e Limpar.
- **Área "Cozinha":** painel ao lado do cardápio (fixo no desktop, abaixo do cardápio no celular) com o formulário. Campos com `<label>` visível: Nome, Descrição, Preço (R$), Categoria, Tempo de preparo (minutos) e a caixa "Disponível hoje". O título muda entre "Nova comida" e "Editando: nome".
- **Estados:** lista vazia mostra "Nenhuma comida encontrada."; servidor parado mostra "Não foi possível conectar ao servidor."

### Organização dos arquivos

- `index.html`: estrutura da página; carrega `script.js` com `type="module"`.
- `api.js` (módulo ES): **único arquivo que usa `fetch`**. Exporta `comidasApi` com `listar()`, `listarPorCategoria(categoria)`, `buscarPorNome(nome)`, `buscarPorId(id)`, `criar(comida)`, `atualizar(id, comida)` e `excluir(id)`. A função interna `requisitar(url, opcoes)` envia `Content-Type: application/json` quando há corpo, devolve `null` no 204 e, em erro, lança `Error` com a mensagem do backend (404: campo `message`; 400: junta as mensagens do mapa `errors`). Falha de rede: "Não foi possível conectar ao servidor." Valores de URL passam por `encodeURIComponent`.
- `script.js` (módulo ES): eventos, formulário e desenho do cardápio (`carregar`, `buscarPorNome`, `editar`, `excluir`, `desenharCardapio`, `desenharItem`).
- `style.css`: visual, com as cores em variáveis CSS.

### Identidade visual

Use sites de restaurantes como referência de organização: cabeçalho forte, categorias em destaque, nome ligado ao preço por pontilhado, tom acolhedor. O visual deve ser original, sem copiar nenhuma marca real.

- Tinta marinha `#12303A`: texto e cabeçalho.
- Névoa `#EDF2F1`: fundo da página.
- Papel `#FBFDFC`: superfícies (formulário, campos, mensagens).
- Pimenta rosa `#A8324F`: preço e ação principal. Versão escura `#7F2339` no hover e `#7D1F37` em textos de erro.
- Azulejo `#1F6F8B`: foco e detalhes.
- Pedra `#4A5F67`: texto secundário.
- Fontes: Young Serif nos títulos e no preço; Figtree no restante. Com fontes de reserva (Georgia e fonte do sistema).

### Requisitos de qualidade

- Layout responsivo a partir de 375 px, sem rolagem horizontal.
- Foco visível no teclado (`:focus-visible`), contraste de texto de pelo menos 4,5:1 e respeito a `prefers-reduced-motion`.
- Montar a tela com `document.createElement` e `textContent`. Nunca usar `innerHTML` com dados do backend.
- Nenhum `fetch` fora de `api.js`.
- Tratar erros com `try/catch`, sem quebrar a página.
- Cada campo com `<label>` associado.

### Critérios de aceite

1. O cardápio aparece ao abrir a página e atualiza após cada cadastro, edição ou exclusão.
2. Cadastrar com dados válidos mostra "Comida cadastrada." e o item na categoria certa.
3. Cadastrar com preço vazio mostra a mensagem de validação do backend.
4. Editar carrega os dados no formulário e o PUT atualiza o item.
5. Excluir pede confirmação; cancelar o `confirm()` não exclui.
6. O filtro por categoria e a busca por nome funcionam; nome inexistente mostra erro e cardápio vazio.
7. Um item com `disponivel` falso mostra o selo "Indisponível hoje".
8. Com o servidor parado, aparece "Não foi possível conectar ao servidor."
9. Um nome como `<b>x</b>` aparece como texto, sem ser interpretado como HTML.
10. Em 375 px não há rolagem horizontal.

## Pedidos feitos em sequência

Estes foram os pedidos curtos feitos à IA nesta conversa, em ordem:

1. "agora faça um para esse outro projeto aqui, mantenha o tema dele, só adicione as funcionalidades da atividade lá, nao se preocupe com o front end"
2. "faz um css inspirado em sites de restaurantes famosos"

O pedido 1 gerou as funcionalidades e as adaptações do backend. O pedido 2 gerou o visual "Casa Jabuti". O prompt detalhado acima descreve o resultado final dessas duas etapas.
