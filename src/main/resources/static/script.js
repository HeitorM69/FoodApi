import { comidasApi } from './api.js';

const $ = (id) => document.getElementById(id);
const nomesCategoria = {
  ENTRADA: 'Entrada',
  PRATO_PRINCIPAL: 'Prato principal',
  SOBREMESA: 'Sobremesa',
  BEBIDA: 'Bebida',
  LANCHE: 'Lanche',
};
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

function mostrarMensagem(texto, erro = false) {
  const el = $('mensagem');
  el.textContent = texto;
  el.className = erro ? 'erro' : 'ok';
}

function limparFormulario() {
  $('form-comida').reset();
  $('id').value = '';
  $('titulo-form').textContent = 'Nova comida';
  $('btn-cancelar').hidden = true;
}

function el(tag, classe, texto) {
  const e = document.createElement(tag);
  if (classe) e.className = classe;
  if (texto !== undefined) e.textContent = texto;
  return e;
}

function botao(texto, aoClicar, classe) {
  const b = el('button', classe, texto);
  b.type = 'button';
  b.addEventListener('click', aoClicar);
  return b;
}

function desenharItem(c) {
  const li = el('li', c.disponivel ? 'item' : 'item indisponivel');
  const linha = el('div', 'linha');
  linha.append(el('span', 'item-nome', c.nome), el('span', 'pontos'), el('span', 'item-preco', moeda.format(c.preco)));
  li.appendChild(linha);
  if (c.descricao) li.appendChild(el('p', 'item-descricao', c.descricao));
  const meta = el('div', 'item-meta');
  if (!c.disponivel) meta.appendChild(el('span', 'esgotado', 'Indisponível hoje'));
  if (c.tempoPreparoMinutos) meta.appendChild(el('span', 'tempo', `Preparo: ${c.tempoPreparoMinutos} min`));
  const acoes = el('div', 'acoes');
  acoes.append(botao('Editar', () => editar(c.id)), botao('Excluir', () => excluir(c), 'excluir'));
  meta.appendChild(acoes);
  li.appendChild(meta);
  return li;
}

function desenharCardapio(comidas) {
  const raiz = $('cardapio');
  raiz.replaceChildren();
  if (comidas.length === 0) {
    raiz.appendChild(el('p', 'vazio', 'Nenhuma comida encontrada.'));
    return;
  }
  // Ordem fixa das seções; categorias desconhecidas vão para o fim.
  const ordem = new Set([...Object.keys(nomesCategoria), ...comidas.map((c) => c.categoria)]);
  for (const cat of ordem) {
    const doGrupo = comidas.filter((c) => c.categoria === cat);
    if (doGrupo.length === 0) continue;
    const secao = el('section', 'categoria');
    const titulo = el('h2', 'categoria-titulo', nomesCategoria[cat] ?? cat);
    titulo.appendChild(el('span', 'categoria-qtd', `${doGrupo.length} ${doGrupo.length === 1 ? 'item' : 'itens'}`));
    const lista = el('ul', 'itens');
    for (const c of doGrupo) lista.appendChild(desenharItem(c));
    secao.append(titulo, lista);
    raiz.appendChild(secao);
  }
}

async function carregar() {
  try {
    const categoria = $('filtro-categoria').value;
    desenharCardapio(categoria ? await comidasApi.listarPorCategoria(categoria) : await comidasApi.listar());
  } catch (e) {
    mostrarMensagem(e.message, true);
  }
}

async function buscarPorNome() {
  const nome = $('busca-nome').value.trim();
  if (!nome) return carregar();
  try {
    desenharCardapio([await comidasApi.buscarPorNome(nome)]);
    mostrarMensagem('');
  } catch (e) {
    desenharCardapio([]);
    mostrarMensagem(e.message, true);
  }
}

async function editar(id) {
  try {
    const c = await comidasApi.buscarPorId(id);
    $('id').value = c.id;
    $('nome').value = c.nome;
    $('descricao').value = c.descricao ?? '';
    $('preco').value = c.preco;
    $('categoria').value = c.categoria;
    $('tempo').value = c.tempoPreparoMinutos ?? '';
    $('disponivel').checked = !!c.disponivel;
    $('titulo-form').textContent = `Editando: ${c.nome}`;
    $('btn-cancelar').hidden = false;
    $('nome').focus();
  } catch (e) {
    mostrarMensagem(e.message, true);
  }
}

async function excluir(c) {
  if (!confirm(`Excluir "${c.nome}"?`)) return;
  try {
    await comidasApi.excluir(c.id);
    mostrarMensagem('Comida excluída.');
    await carregar();
  } catch (e) {
    mostrarMensagem(e.message, true);
  }
}

$('form-comida').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const tempo = $('tempo').value;
  const comida = {
    nome: $('nome').value.trim(),
    descricao: $('descricao').value,
    preco: Number($('preco').value),
    categoria: $('categoria').value,
    disponivel: $('disponivel').checked,
    tempoPreparoMinutos: tempo === '' ? null : Number(tempo),
  };
  const id = $('id').value;
  try {
    if (id) await comidasApi.atualizar(id, comida);
    else await comidasApi.criar(comida);
    mostrarMensagem(id ? 'Comida atualizada.' : 'Comida cadastrada.');
    limparFormulario();
    await carregar();
  } catch (e) {
    mostrarMensagem(e.message, true);
  }
});

$('btn-cancelar').addEventListener('click', limparFormulario);
$('filtro-categoria').addEventListener('change', () => { $('busca-nome').value = ''; carregar(); });
$('btn-buscar').addEventListener('click', buscarPorNome);
$('btn-limpar').addEventListener('click', () => {
  $('busca-nome').value = '';
  $('filtro-categoria').value = '';
  carregar();
});

carregar();
