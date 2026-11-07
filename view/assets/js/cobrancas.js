let cobrancasData = [];

document.addEventListener('DOMContentLoaded', () => {
    const user = requireAuth();
    if (!user) return;

    // Display user info in navbar/sidebar if elements exist
    document.querySelectorAll('.user-name, .sidebar-user-name').forEach(el => {
        el.textContent = user.nome || user.email || 'Usuário';
    });
    document.querySelectorAll('.user-avatar').forEach(img => {
        img.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.nome || 'User')}&background=2A0539&color=fff`;
    });

    carregarCobrancas();
    carregarClientesSelect();

    const btnCriar = document.getElementById('btnCriarCobranca');
    if (btnCriar) {
        btnCriar.addEventListener('click', criarCobranca);
    }
});

function formatarData(dataStr) {
    if (!dataStr) return '-';
    if (/^\d{4}-\d{2}-\d{2}/.test(dataStr)) {
        const parts = dataStr.substring(0, 10).split('-');
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    const date = new Date(dataStr);
    if (isNaN(date.getTime())) return dataStr;
    return date.toLocaleDateString('pt-BR');
}

function formatarMoeda(valor) {
    const num = Number(valor);
    if (isNaN(num)) return 'R$ 0,00';
    return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function getStatusBadge(status) {
    const s = (status || '').toUpperCase();
    switch (s) {
        case 'PAGO':
            return '<span class="badge bg-success">Pago</span>';
        case 'ESTORNADO':
            return '<span class="badge bg-secondary">Estornado</span>';
        case 'VENCIDO':
            return '<span class="badge bg-danger">Vencido</span>';
        case 'CANCELADO':
            return '<span class="badge bg-secondary">Cancelado</span>';
        case 'PENDENTE':
        default:
            return '<span class="badge bg-warning text-dark">Pendente</span>';
    }
}

async function carregarClientesSelect() {
    const select = document.getElementById('cobrancaCliente');
    if (!select) return;

    try {
        const clientes = await apiRequest('/clientes');
        if (Array.isArray(clientes)) {
            // Keep default option
            select.innerHTML = '<option value="">Selecione ou deixe em branco</option>';
            clientes.forEach(cliente => {
                const opt = document.createElement('option');
                opt.value = cliente.id;
                opt.textContent = cliente.nome || `Cliente #${cliente.id}`;
                select.appendChild(opt);
            });
        }
    } catch (error) {
        console.error('Erro ao carregar clientes para o select:', error);
    }
}

async function carregarCobrancas() {
    const tbody = document.getElementById('cobrancasTableBody');
    if (!tbody) return;

    tbody.innerHTML = `
        <tr>
            <td colspan="6" class="text-center py-4 text-secondary">
                <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
                Carregando cobranças...
            </td>
        </tr>
    `;

    try {
        const data = await apiRequest('/cobrancas');
        cobrancasData = Array.isArray(data) ? data : [];
        renderCobrancas(cobrancasData);
    } catch (error) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4 text-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Erro ao carregar cobranças: ${error.message}
                </td>
            </tr>
        `;
        showToast('Erro ao carregar cobranças.', 'error');
    }
}

function renderCobrancas(cobrancas) {
    const tbody = document.getElementById('cobrancasTableBody');
    if (!tbody) return;

    if (!cobrancas || cobrancas.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4 text-secondary">
                    Nenhuma cobrança encontrada.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = cobrancas.map(cobranca => {
        const id = cobranca.id;
        const descricao = cobranca.descricao || 'Cobrança sem descrição';
        const clienteNome = cobranca.clienteNome || (cobranca.cliente ? cobranca.cliente.nome : 'Cliente não informado');
        const dataVencimento = formatarData(cobranca.dataVencimento);
        const valorFormatado = formatarMoeda(cobranca.valor);
        const badge = getStatusBadge(cobranca.status);

        return `
            <tr data-cobranca-id="${id}">
                <td class="ps-4 fw-medium text-dark align-middle">${descricao}</td>
                <td class="align-middle text-secondary">${clienteNome}</td>
                <td class="align-middle text-secondary">${dataVencimento}</td>
                <td class="align-middle">${badge}</td>
                <td class="text-end fw-bold text-dark align-middle">${valorFormatado}</td>
                <td class="text-center pe-4 align-middle">
                    <div class="d-inline-flex gap-1">
                        <button class="btn btn-sm btn-outline-primary rounded-pill btn-copy-link" data-id="${id}" title="Copiar Link"><i class="bi bi-clipboard me-1"></i> Copiar Link</button>
                        <button class="btn btn-sm btn-light text-danger rounded-pill btn-delete-cobranca" data-id="${id}" title="Excluir"><i class="bi bi-trash"></i></button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    // Attach row events
    tbody.querySelectorAll('.btn-delete-cobranca').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            excluirCobranca(id);
        });
    });

    tbody.querySelectorAll('.btn-copy-link').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            const link = `${window.location.origin}/pagamentos.html?id=${id}`;
            navigator.clipboard.writeText(link).then(() => {
                showToast('Link de pagamento copiado para a área de transferência!', 'success');
            }).catch(() => {
                prompt('Copie o link de pagamento:', link);
            });
        });
    });
}

async function criarCobranca() {
    const valorInput = document.getElementById('cobrancaValor');
    const descricaoInput = document.getElementById('cobrancaDescricao');
    const clienteSelect = document.getElementById('cobrancaCliente');
    const vencimentoInput = document.getElementById('cobrancaVencimento');
    const btnCriar = document.getElementById('btnCriarCobranca');

    const valorRaw = valorInput ? valorInput.value : '';
    const descricao = descricaoInput ? descricaoInput.value.trim() : '';
    const clienteId = clienteSelect ? clienteSelect.value : '';
    const dataVencimento = vencimentoInput ? vencimentoInput.value : '';

    if (!valorRaw || parseFloat(valorRaw) <= 0) {
        showToast('Por favor, informe um valor válido para a cobrança.', 'error');
        if (valorInput) valorInput.focus();
        return;
    }

    if (!descricao) {
        showToast('Por favor, informe a descrição da cobrança.', 'error');
        if (descricaoInput) descricaoInput.focus();
        return;
    }

    const payload = {
        clienteId: clienteId ? Number(clienteId) : null,
        descricao,
        valor: parseFloat(valorRaw),
        dataVencimento: dataVencimento || null,
        metodoPagamento: 'PIX'
    };

    const originalBtnText = btnCriar ? btnCriar.innerHTML : 'Gerar Link de Pagamento';
    if (btnCriar) {
        btnCriar.disabled = true;
        btnCriar.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Gerando...';
    }

    try {
        await apiRequest('/cobrancas', 'POST', payload);
        showToast('Cobrança criada com sucesso!', 'success');

        const modalEl = document.getElementById('createChargeModal');
        if (modalEl) {
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            if (modalInstance) modalInstance.hide();
        }

        // Reset form
        const form = document.getElementById('formCriarCobranca');
        if (form) form.reset();

        await carregarCobrancas();
    } catch (error) {
        showToast(error.message || 'Erro ao criar cobrança.', 'error');
    } finally {
        if (btnCriar) {
            btnCriar.disabled = false;
            btnCriar.innerHTML = originalBtnText;
        }
    }
}

async function excluirCobranca(id) {
    if (!confirm('Tem certeza que deseja excluir esta cobrança?')) {
        return;
    }

    try {
        await apiRequest(`/cobrancas/${id}`, 'DELETE');
        showToast('Cobrança excluída com sucesso!', 'success');
        await carregarCobrancas();
    } catch (error) {
        showToast(error.message || 'Erro ao excluir cobrança.', 'error');
    }
}
