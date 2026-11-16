# Guia de estudo da apresentação: Casa Jabuti (food-api)

## Resumo

A avaliação é "Integração de Frontend com Backend utilizando Inteligência Artificial". Entregue: o prompt (`docs/PROMPT.md`) e o link do Git. Na apresentação, explique o funcionamento pessoalmente ao professor.

| Critério | O que mostrar | Pontos |
|---|---|---|
| Prompt usado para gerar o frontend | `docs/PROMPT.md`: qualidade e detalhamento | 2,0 |
| Link do Git | repositório com o código | entrega |
| Explicação pessoal | as seis perguntas deste guia | apresentação |

O projeto é um cardápio de restaurante, a "Casa Jabuti". O Spring Boot serve a página e a API na mesma origem (`http://localhost:8080`).

## Arquitetura

```text
Navegador (index.html + script.js)
   -> api.js               único arquivo com fetch
   -> ComidaController     recebe o HTTP, valida (@Valid), devolve ResponseEntity
   -> ComidaService        regras de negócio, monta a Comida, lança 404
   -> ComidaRepository     JpaRepository (Spring Data)
   -> Hibernate (JPA)      gera o SQL
   -> PostgreSQL           banco food_api, tabela tb_comidas

Ao lado:
   ComidaRequestDTO        o que a API recebe (com as validações)
   ComidaResponseDTO       o que a API devolve (fromEntity)
   GlobalExceptionHandler  transforma exceções em JSON de erro (400 e 404)
```

A resposta volta pelo caminho inverso: entidade, `ComidaResponseDTO`, JSON, `api.js`, `script.js` desenha o cardápio.

## Cada chamada da tela

| Ação na tela | Verbo e rota | Função em `api.js` | Método do controller | Status |
|---|---|---|---|---|
| Abrir a página, recarregar após mudanças | GET `/api/comidas` | `comidasApi.listar()` | `buscarTodas` | 200 |
| Escolher categoria no filtro | GET `/api/comidas?categoria=X` | `comidasApi.listarPorCategoria(c)` | `buscarTodas` | 200 |
| Buscar por nome | GET `/api/comidas/nome/{nome}` | `comidasApi.buscarPorNome(n)` | `buscarPorNome` | 200 ou 404 |
| Clicar em Editar | GET `/api/comidas/{id}` | `comidasApi.buscarPorId(id)` | `buscarPorId` | 200 ou 404 |
| Salvar nova comida | POST `/api/comidas` | `comidasApi.criar(c)` | `criar` | 201 ou 400 |
| Salvar edição | PUT `/api/comidas/{id}` | `comidasApi.atualizar(id, c)` | `atualizar` | 200, 400 ou 404 |
| Excluir | DELETE `/api/comidas/{id}` | `comidasApi.excluir(id)` | `deletarPorId` | 204 ou 404 |

O endpoint `DELETE /api/comidas/nome/{nome}` (`deletarPorNome`) também existe, mas a tela usa o por ID.

## Exemplo completo: cadastrar uma comida (POST)

1. **Clique.** O usuário preenche a "Cozinha" e clica em Salvar. O evento `submit` do `form-comida`, em `script.js`, chama `ev.preventDefault()` (a página não recarrega) e monta o objeto:

```js
const comida = {
  nome: $('nome').value.trim(),
  descricao: $('descricao').value,
  preco: Number($('preco').value),
  categoria: $('categoria').value,
  disponivel: $('disponivel').checked,
  tempoPreparoMinutos: tempo === '' ? null : Number(tempo),
};
```

2. **Chamada.** Como o campo oculto `id` está vazio, `script.js` chama `comidasApi.criar(comida)`. Em `api.js`:

```js
criar: (comida) =>
  requisitar(BASE, { method: 'POST', body: JSON.stringify(comida) }),
```

3. **Requisição HTTP.** `requisitar` vê que há `body` e acrescenta `Content-Type: application/json`. Depois executa `fetch('/api/comidas', config)`. Vai pela rede: `POST /api/comidas` com o JSON no corpo.
4. **Controller.** O Spring encontra o método do `@PostMapping`:

```java
@PostMapping
public ResponseEntity<ComidaResponseDTO> criar(@Valid @RequestBody ComidaRequestDTO dto) {
    ComidaResponseDTO criada = comidaService.criar(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(criada);
}
```

`@RequestBody` converte o JSON no record `ComidaRequestDTO`. `@Valid` confere `@NotBlank`, `@Size`, `@NotNull`, `@Positive` e `@Min`.

5. **Service.** `ComidaService.criar` monta a entidade com `Comida.builder()` e chama `comidaRepository.save(comida)`. Se `disponivel` não veio, usa `true`.
6. **Banco.** O Hibernate roda o `INSERT INTO tb_comidas ...` (aparece no console por causa de `show-sql=true`). Antes de gravar, `@PrePersist` preenche `dataCriacao` e `dataAtualizacao`. O PostgreSQL gera o `id` (`GenerationType.IDENTITY`).
7. **Resposta.** O service converte a entidade em `ComidaResponseDTO.fromEntity(salva)`. O controller devolve status **201** com o JSON da comida, já com `id`.
8. **Volta ao navegador.** `requisitar` converte o JSON com `resposta.json()` e confere `resposta.ok`. O `script.js` mostra "Comida cadastrada.", limpa o formulário e chama `carregar()`, que faz um `GET /api/comidas` e redesenha o cardápio.

## O que muda nos outros casos

- **GET:** não tem corpo. Os parâmetros vão na URL (`?categoria=BEBIDA` ou `/5`). O controller devolve 200 com a lista ou com um item. O `script.js` desenha o cardápio com `desenharCardapio`.
- **PUT:** igual ao POST, mas com o `id` na URL (`@PathVariable`) e `method: 'PUT'`. O service busca a comida, troca os campos e salva. Se o id não existe: 404. Antes de editar, o botão Editar fez um `GET /api/comidas/{id}` para preencher o formulário.
- **DELETE:** sem corpo, o `id` na URL. O service busca e apaga. O controller devolve `ResponseEntity.noContent().build()`: status **204** sem corpo. Por isso `requisitar` tem `if (resposta.status === 204) return null;`. Antes, a tela pede `confirm()`.
- **Erro 400 (validação):** o `@Valid` falha e o Spring lança `MethodArgumentNotValidException`. O `GlobalExceptionHandler` devolve `{ status: 400, errors: { campo: mensagem } }`. O `api.js` junta as mensagens com `Object.values(corpo.errors).join(' ')` e lança um `Error`. O `script.js` mostra na área de mensagens.
- **Erro 404:** o service lança `ResourceNotFoundException`. O `GlobalExceptionHandler` devolve `{ status: 404, error: "Recurso não encontrado", message: "..." }`. O `api.js` usa o campo `message`.
- **Servidor parado:** o `fetch` falha e o `api.js` lança "Não foi possível conectar ao servidor."

## As seis perguntas do professor

**1. Como o frontend faz uma requisição HTTP?**
"Todas as requisições saem do arquivo `api.js`, pela função `requisitar`, que usa o `fetch`. Eu passo a URL, o método e, quando precisa, o corpo em JSON. O `fetch` é assíncrono: com `await` o código espera a resposta sem travar a página. O `script.js` não conhece URL nenhuma, ele só chama funções como `comidasApi.criar`."

**2. Como os verbos GET, POST, PUT e DELETE são usados?**
"GET lê: lista, filtra por categoria, busca por nome ou por id. POST cria uma comida nova, em `/api/comidas`. PUT atualiza uma comida que já existe, e o id vai na URL. DELETE apaga pelo id. Cada verbo tem um método no controller com a anotação certa: `@GetMapping`, `@PostMapping`, `@PutMapping` e `@DeleteMapping`."

**3. Como os dados vão do frontend ao backend?**
"No POST e no PUT, o `script.js` monta um objeto com os campos do formulário. O `api.js` transforma esse objeto em texto com `JSON.stringify` e coloca no corpo da requisição. O cabeçalho `Content-Type: application/json` avisa o Spring que o corpo é JSON. O id não vai no corpo, vai na URL, tipo `/api/comidas/5`."

**4. Como o backend processa e responde?**
"O Spring direciona a rota para o método do controller. O `@RequestBody` converte o JSON em `ComidaRequestDTO` e o `@Valid` confere as regras. Se estiver certo, o `ComidaService` monta a entidade e o `ComidaRepository` salva no PostgreSQL pelo Hibernate. O controller devolve um `ResponseEntity` com o status: 201 no POST, 200 no GET e no PUT, 204 no DELETE. Se der erro, o `GlobalExceptionHandler` devolve 400 ou 404 em JSON."

**5. Como o frontend recebe e mostra os dados?**
"O `api.js` converte a resposta em objeto com `resposta.json()`. Se o status for de erro, ele lança um `Error` com a mensagem do backend. O `script.js` pega a lista e a função `desenharCardapio` agrupa por categoria e cria os elementos com `document.createElement` e `textContent`. Assim, mesmo que o nome tenha HTML, ele aparece como texto. O preço sai formatado em reais com `Intl.NumberFormat`."

**6. Como o prompt orientou a IA?**
"Eu não escrevi um prompt gigante de uma vez. Eu fiz pedidos curtos para o Claude: primeiro pedi para ele pegar este projeto, manter o tema e adicionar as funcionalidades da atividade. Depois pedi um CSS inspirado em sites de restaurantes. O documento `PROMPT.md` é a versão refinada e detalhada desses pedidos. Nele eu coloquei o contrato da API, que definiu rotas, verbos e status, a ligação de cada ação da tela com um endpoint, a regra de usar `fetch` só no `api.js` e os critérios de aceite. Foi isso que guiou o resultado."

## Roteiro de demonstração

Antes de começar:

- [ ] PostgreSQL rodando e banco `food_api` criado.
- [ ] Aplicação iniciada (`FoodApiApplication` ou `.\mvnw.cmd spring-boot:run`) e página aberta em http://localhost:8080.
- [ ] pgAdmin aberto, conectado no banco `food_api`, com uma janela de consulta pronta.
- [ ] Navegador com F12 aberto na aba Rede (Network). Este projeto não tem monitor de requisições na tela, então a aba Rede mostra cada requisição (verbo, URL, status, corpo enviado e recebido).
- [ ] Console do Spring visível, para mostrar o SQL.
- [ ] `PROMPT.md` aberto em outra aba.

Passos:

1. Recarregue a página. Na aba Rede, mostre `GET /api/comidas` com 200 e a resposta em JSON.
2. Cadastre uma comida (por exemplo "Pizza Margherita", R$ 39,90, Prato principal). Na aba Rede, mostre `POST /api/comidas` com 201: o corpo enviado (Payload) e o recebido (Resposta, com `id`). Depois aparece um novo `GET`.
3. No pgAdmin, rode `select * from tb_comidas;` e mostre a linha criada.
4. Clique em Editar (`GET /api/comidas/{id}`), mude o preço e salve (`PUT`, 200). Rode o `select` de novo e mostre `preco` e `data_atualizacao` alterados.
5. Cadastre com o preço vazio e mostre o 400 na aba Rede com o mapa `errors`, e a mensagem na tela.
6. Marque uma comida como indisponível (desmarque "Disponível hoje") e mostre o selo na tela.
7. Exclua uma comida (confirme no `confirm()`). Na aba Rede, mostre `DELETE` com 204. Rode o `select` e mostre que a linha sumiu.
8. Mostre no console do Spring os comandos SQL gerados.

## O prompt

O que postar: o conteúdo do `docs/PROMPT.md` (ou o link dele no Git).

Como apresentar, com honestidade:

- Diga que o frontend foi gerado pela IA Claude a partir de pedidos curtos seus, em conversa.
- Diga que o `PROMPT.md` é uma versão refinada e consolidada desses pedidos, escrita depois, com mais detalhe. Não diga que digitou aquele texto palavra por palavra.
- Mostre a lista "Pedidos feitos em sequência" no final do documento: são os pedidos de verdade.
- Aponte as partes que guiaram a IA: contrato da API, funcionalidades ligadas a endpoints, organização dos arquivos (`fetch` só em `api.js`) e critérios de aceite.
- Mostre que você entende o código gerado: abra `api.js` e o controller e explique o fluxo.

## Perguntas difíceis

| Pergunta | Resposta |
|---|---|
| Por que POST devolve 201 e não 200? | 201 Created diz que um recurso novo foi criado. 200 só diz que deu certo. |
| Por que DELETE devolve 204? | 204 No Content: deu certo e não há corpo para devolver. O `api.js` trata esse caso sem tentar ler JSON. |
| Diferença entre PUT e POST? | POST cria um recurso novo e o banco gera o id. PUT atualiza o recurso existente, identificado pelo id na URL. |
| Para que serve o DTO? | Separa o que a API recebe e devolve da entidade do banco. Permite validar a entrada e evita aceitar ou expor campos indevidos (por exemplo `id` e datas no envio). |
| O que faz o `@Valid` e de onde vem o 400? | Aplica as regras do DTO (`@NotBlank`, `@Positive` etc.). Se falhar, o Spring lança `MethodArgumentNotValidException`, e o `GlobalExceptionHandler` devolve 400 com o mapa campo e mensagem. |
| O que é o `@RestControllerAdvice`? | Uma classe que trata exceções de todos os controllers em um só lugar. Aqui, o `GlobalExceptionHandler` transforma `ResourceNotFoundException` em 404 e erro de validação em 400. |
| Como `dataCriacao` e `dataAtualizacao` são preenchidas? | Pelos callbacks da entidade: `@PrePersist` roda antes do INSERT e preenche as duas datas (e `disponivel` como `true` se vier vazio). `@PreUpdate` roda antes de um UPDATE e atualiza `dataAtualizacao`. |
| Por que o enum é salvo como STRING? | `@Enumerated(EnumType.STRING)` grava o nome (`BEBIDA`) em vez da posição. Se alguém mudar a ordem do enum, os dados antigos não ficam errados, e o banco fica legível. |
| Por que não há problema de CORS? | A página é servida pelo próprio Spring, na mesma origem (`localhost:8080`) da API. CORS só importa entre origens diferentes. |
| Por que `textContent` e não `innerHTML`? | `textContent` trata o dado como texto. Com `innerHTML`, um nome como `<b>x</b>` ou um script viraria HTML do navegador (risco de XSS). |
| Quem cria a tabela? | O Hibernate, por causa de `spring.jpa.hibernate.ddl-auto=update`. |
| E se a categoria do filtro for inválida? | Em `?categoria=XYZ` o Spring não consegue converter para o enum e responde 400. O `GlobalExceptionHandler` não trata esse caso, então a resposta é a padrão do Spring. A tela só envia valores do `<select>`, então não acontece pelo uso normal. |

## Checklist final

- [ ] Prompt (`docs/PROMPT.md`) postado, com a seção de honestidade no topo.
- [ ] Link do Git entregue e repositório atualizado.
- [ ] PostgreSQL e aplicação funcionando antes de apresentar.
- [ ] pgAdmin com `select * from tb_comidas;` pronto.
- [ ] F12 na aba Rede aberto.
- [ ] Consigo explicar as seis perguntas sem ler.
- [ ] Sei traçar um POST do clique até o banco e de volta.
- [ ] Sei dizer o que é 201, 204, 400 e 404.
- [ ] Sei contar como o prompt foi usado, sem exagerar.
