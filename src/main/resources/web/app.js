const estado = {
    dados: null,
    timerToast: null
};

const $ = (seletor) => document.querySelector(seletor);

async function carregarStatus() {
    const resposta = await fetch('/api/status');
    const dados = await resposta.json();
    estado.dados = dados;
    renderizar(dados);
}

async function postBackend(url) {
    const resposta = await fetch(url, { method: 'POST' });
    const retorno = await resposta.json();

    if (!resposta.ok) {
        throw new Error(retorno.erro || 'Erro ao comunicar com o backend.');
    }

    estado.dados = retorno;
    renderizar(retorno);
    return retorno;
}

function renderizar(dados) {
    const totalPedidos = dados.artesaos.reduce((soma, artesao) => soma + artesao.quantidadePedidos, 0);
    const finalizadas = dados.threads.filter(thread => thread.status === 'FINALIZADA').length;

    $('#totalArtesaos').textContent = dados.artesaos.length;
    $('#totalThreads').textContent = dados.threads.length;
    $('#totalPedidos').textContent = totalPedidos;
    $('#statusSimulacao').textContent = dados.rodando ? 'Rodando' : 'Parada';
    $('#threadsFinalizadas').textContent = `${finalizadas} finalizadas`;

    $('#btnSimular').disabled = dados.rodando || totalPedidos === 0;
    $('#btnReset').disabled = dados.rodando;
    $('#avisoRodando').classList.toggle('escondido', !dados.rodando);

    renderizarThreads(dados.threads);
    renderizarArtesaos(dados.artesaos);
    renderizarRecursos(dados.recursos);
    renderizarLogs(dados.logs);
}

function renderizarThreads(threads) {
    const lista = $('#listaThreads');
    lista.innerHTML = '';

    if (!threads || threads.length === 0) {
        lista.innerHTML = '<div class="vazio">Nenhuma thread criada ainda.</div>';
        return;
    }

    threads.forEach((thread) => {
        const item = document.createElement('article');
        item.className = `item thread-card status-${thread.status.toLowerCase()}`;

        item.innerHTML = `
            <div class="item-topo">
                <h3>${escaparHtml(thread.nome)}</h3>
                <span class="pill">${formatarTipo(thread.status)}</span>
            </div>
            <div class="meta">Artesão: <strong>${escaparHtml(thread.artesao)}</strong></div>
            <div class="meta">Tipo: ${formatarTipo(thread.tipo)}</div>
            <div class="barra-status"><span></span></div>
        `;

        lista.appendChild(item);
    });
}

function renderizarArtesaos(artesaos) {
    const lista = $('#listaArtesaos');
    lista.innerHTML = '';

    if (artesaos.length === 0) {
        lista.innerHTML = '<div class="vazio">Nenhum artesão cadastrado.</div>';
        return;
    }

    artesaos.forEach((artesao) => {
        const item = document.createElement('article');
        item.className = 'item';

        const pedidosHtml = artesao.pedidos.length === 0
            ? '<div class="vazio">Esse artesão terminou os pedidos.</div>'
            : artesao.pedidos.map((pedido) => `
                <div class="pedido">
                    <strong>${escaparHtml(pedido.nome)}</strong><br>
                    Prioridade: ${formatarTipo(pedido.prioridade)} | Tempo: ${pedido.tempo} ms | Espera: ${pedido.tempoEspera}<br>
                    Recursos: ${pedido.recursos.map(escaparHtml).join(', ')}
                </div>
            `).join('');

        item.innerHTML = `
            <div class="item-topo">
                <h3>${escaparHtml(artesao.nome)}</h3>
                <span class="pill">${formatarTipo(artesao.tipo)}</span>
            </div>
            <div class="meta">Pedidos pendentes: ${artesao.quantidadePedidos}</div>
            <div class="pedidos">${pedidosHtml}</div>
        `;

        lista.appendChild(item);
    });
}

function renderizarRecursos(recursos) {
    const lista = $('#listaRecursos');
    lista.innerHTML = '';

    if (recursos.length === 0) {
        lista.innerHTML = '<div class="vazio">Nenhum recurso cadastrado.</div>';
        return;
    }

    recursos.forEach((recurso) => {
        const item = document.createElement('article');
        item.className = `item recurso-card ${recurso.disponivel ? 'livre' : 'ocupado'}`;
        item.innerHTML = `
            <div class="item-topo">
                <h3>${escaparHtml(recurso.nome)}</h3>
                <span class="pill">${formatarTipo(recurso.tipo)}</span>
            </div>
            <div class="meta">Usos registrados: <strong>${recurso.usos}</strong></div>
            <div class="meta">Manutenção: ${recurso.manutencao ? 'Sim' : 'Não'}</div>
            <div class="meta">Disponível: ${recurso.disponivel ? 'Sim' : 'Não'}</div>
        `;
        lista.appendChild(item);
    });
}

function renderizarLogs(logs) {
    const area = $('#logs');
    area.innerHTML = '';

    if (!logs || logs.length === 0) {
        area.innerHTML = '<div>Nenhum log ainda.</div>';
        return;
    }

    logs.forEach((linha) => {
        const div = document.createElement('div');
        div.textContent = linha;
        area.appendChild(div);
    });

    area.scrollTop = area.scrollHeight;
}

function mostrarToast(texto) {
    const toast = $('#toast');
    toast.textContent = texto;
    toast.classList.remove('escondido');

    clearTimeout(estado.timerToast);
    estado.timerToast = setTimeout(() => {
        toast.classList.add('escondido');
    }, 2800);
}

function formatarTipo(valor) {
    return String(valor)
        .replaceAll('_', ' ')
        .toLowerCase()
        .replace(/(^|\s)\S/g, letra => letra.toUpperCase());
}

function escaparHtml(texto) {
    return String(texto)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

$('#btnSimular').addEventListener('click', async () => {
    try {
        await postBackend('/api/simular');
        mostrarToast('Threads iniciadas. Acompanhe os cards e o console.');
    } catch (erro) {
        mostrarToast(erro.message);
    }
});

$('#btnReset').addEventListener('click', async () => {
    try {
        await postBackend('/api/demo');
        mostrarToast('Demonstração reiniciada.');
    } catch (erro) {
        mostrarToast(erro.message);
    }
});

carregarStatus().catch(() => mostrarToast('Não consegui carregar o backend.'));
setInterval(carregarStatus, 700);
