document.addEventListener('DOMContentLoaded', () => {
    const user = requireAuth();
    if (!user) return;

    // Display user name and company
    const nome = user.nome || (user.email ? user.email.split('@')[0] : 'Usuário');
    const empresa = user.empresa || nome;

    const dashGreeting = document.getElementById('dashGreeting');
    if (dashGreeting) {
        dashGreeting.textContent = `Olá, ${nome}! 👋`;
    }

    const dropdownName = document.getElementById('dashUserDropdownName');
    if (dropdownName) {
        dropdownName.textContent = empresa;
    }

    document.querySelectorAll('.user-name, .sidebar-user-name').forEach(el => {
        el.textContent = nome;
    });

    document.querySelectorAll('.user-avatar').forEach(img => {
        img.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=2A0539&color=fff`;
    });

    // Populate client select for modal
    carregarClientesSelect();

    // Attach submit listener to charge creation button
    const btnCriar = document.getElementById('btnDashCriarCobranca');
    if (btnCriar) {
        btnCriar.addEventListener('click', criarCobrancaDashboard);
    }

    // Attach logout to any "Sair" links
    document.querySelectorAll('a[href="index.html"]').forEach(link => {
        if (link.textContent.toLowerCase().includes('sair')) {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                logout();
            });
        }
    });
});

async function carregarClientesSelect() {
    const select = document.getElementById('dashCobrancaCliente');
    if (!select) return;

    try {
        const clientes = await apiRequest('/clientes');
        if (Array.isArray(clientes)) {
            select.innerHTML = '<option value="">Selecione ou deixe em branco</option>';
            clientes.forEach(cliente => {
                const opt = document.createElement('option');
                opt.value = cliente.id;
                opt.textContent = cliente.nome || `Cliente #${cliente.id}`;
                select.appendChild(opt);
            });
        }
    } catch (error) {
        console.error('Erro ao carregar clientes no dashboard:', error);
    }
}

async function criarCobrancaDashboard() {
    const valorInput = document.getElementById('dashCobrancaValor');
    const descricaoInput = document.getElementById('dashCobrancaDescricao');
    const clienteSelect = document.getElementById('dashCobrancaCliente');
    const vencimentoInput = document.getElementById('dashCobrancaVencimento');
    const btnCriar = document.getElementById('btnDashCriarCobranca');

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

        const form = document.getElementById('dashFormCriarCobranca');
        if (form) form.reset();
    } catch (error) {
        showToast(error.message || 'Erro ao criar cobrança.', 'error');
    } finally {
        if (btnCriar) {
            btnCriar.disabled = false;
            btnCriar.innerHTML = originalBtnText;
        }
    }
}
