// Único arquivo que usa fetch: toda comunicação com o backend passa por aqui.
const BASE = '/api/comidas';

async function requisitar(url, opcoes = {}) {
  const config = { ...opcoes };
  if (config.body !== undefined) {
    config.headers = { 'Content-Type': 'application/json', ...config.headers };
  }

  let resposta;
  try {
    resposta = await fetch(url, config);
  } catch {
    throw new Error('Não foi possível conectar ao servidor.');
  }

  if (resposta.status === 204) return null;

  let corpo = null;
  try {
    corpo = await resposta.json();
  } catch {
    // resposta sem JSON
  }

  if (!resposta.ok) {
    let mensagem = `Erro ${resposta.status} ao processar a requisição.`;
    if (corpo?.errors) mensagem = Object.values(corpo.errors).join(' ');
    else if (corpo?.message) mensagem = corpo.message;
    throw new Error(mensagem);
  }
  return corpo;
}

export const comidasApi = {
  listar: () => requisitar(BASE),
  listarPorCategoria: (categoria) =>
    requisitar(`${BASE}?categoria=${encodeURIComponent(categoria)}`),
  buscarPorNome: (nome) => requisitar(`${BASE}/nome/${encodeURIComponent(nome)}`),
  buscarPorId: (id) => requisitar(`${BASE}/${id}`),
  criar: (comida) =>
    requisitar(BASE, { method: 'POST', body: JSON.stringify(comida) }),
  atualizar: (id, comida) =>
    requisitar(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(comida) }),
  excluir: (id) => requisitar(`${BASE}/${id}`, { method: 'DELETE' }),
};
