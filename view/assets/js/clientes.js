let clientesData = [];

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

    carregarClientes();

    // Search filter
    const searchInput = document.getElementById('clienteSearchInput');
    if (searchInput) {
        searchInput.addEventListener('keyup', (e) => {
            const query = e.target.value.toLowerCase().trim();
            filtrarClientes(query);
        });
    }

    // Modal submit button
    const btnSalvar = document.getElementById('btnSalvarCliente');
    if (btnSalvar) {
        btnSalvar.addEventListener('click', salvarCliente);
    }

    // Reset modal when opening for new client
    const btnNovoCliente = document.getElementById('btnNovoCliente');
    if (btnNovoCliente) {
        btnNovoCliente.addEventListener('click', () => {
            resetModalCliente();
        });
    }

    const modalEl = document.getElementById('clientModal');
    if (modalEl) {
        modalEl.addEventListener('show.bs.modal', (e) => {
            // If opened by button other than edit, reset
            if (e.relatedTarget && e.relatedTarget.id === 'btnNovoCliente') {
                resetModalCliente();
            }
        });
    }
});

function resetModalCliente() {
    const editIdInput = document.getElementById('clienteEditId');
    const modalTitle = document.getElementById('clienteModalTitle');
    const form = document.getElementById('clienteForm');

    if (editIdInput) editIdInput.value = '';
    if (modalTitle) modalTitle.textContent = 'Cadastrar Cliente';
    if (form) form.reset();
}

async function carregarClientes() {
    const tbody = document.getElementById('clientesTableBody');
    if (!tbody) return;

    tbody.innerHTML = `
        <tr>
            <td colspan="5" class="text-center py-4 text-secondary">
                <div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
                Carregando clientes...
            </td>
        </tr>
    `;

    try {
        const data = await apiRequest('/clientes');
        clientesData = Array.isArray(data) ? data : [];
        renderClientes(clientesData);
    } catch (error) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-4 text-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>Erro ao carregar clientes: ${error.message}
                </td>
            </tr>
        `;
        showToast('Erro ao carregar clientes.', 'error');
    }
}

function renderClientes(clientes) {
    const tbody = document.getElementById('clientesTableBody');
    if (!tbody) return;

    if (!clientes || clientes.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-4 text-secondary">
                    Nenhum cliente cadastrado.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = clientes.map(cliente => {
        const id = cliente.id;
        const nome = cliente.nome || 'Sem nome';
        const email = cliente.email || 'Sem e-mail';
        const telefone = cliente.telefone || '-';
        const cpf = cliente.cpf ? `CPF: ${cliente.cpf}` : '-';

        return `
            <tr data-cliente-id="${id}">
                <td class="ps-4 align-middle">
                    <div class="d-flex align-items-center">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(nome)}&background=random" class="rounded-circle me-3" width="36" height="36" alt="">
                        <div>
                            <h6 class="mb-0 fw-bold text-dark fs-sm">${nome}</h6>
                            <small class="text-secondary text-xs">${email}</small>
                        </div>
                    </div>
                </td>
                <td class="align-middle">
                    <div class="text-dark fs-sm">${telefone}</div>
                    <small class="text-secondary font-monospace text-xs">${cpf}</small>
                </td>
                <td class="text-center align-middle">
                    <span class="badge bg-success">Ativo</span>
                </td>
                <td class="text-end fw-bold text-dark align-middle">-</td>
                <td class="text-center pe-4 align-middle">
                    <div class="d-inline-flex gap-1">
                        <button class="btn btn-sm btn-light btn-edit-cliente" data-id="${id}" title="Editar"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-light text-danger btn-delete-cliente" data-id="${id}" title="Excluir"><i class="bi bi-trash"></i></button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    // Attach row events
    tbody.querySelectorAll('.btn-edit-cliente').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            abrirEditarCliente(id);
        });
    });

    tbody.querySelectorAll('.btn-delete-cliente').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            excluirCliente(id);
        });
    });
}

function filtrarClientes(query) {
    if (!query) {
        renderClientes(clientesData);
        return;
    }

    const filtrados = clientesData.filter(cliente => {
        const nome = (cliente.nome || '').toLowerCase();
        const email = (cliente.email || '').toLowerCase();
        const cpf = (cliente.cpf || '').toLowerCase();
        const telefone = (cliente.telefone || '').toLowerCase();
        return nome.includes(query) || email.includes(query) || cpf.includes(query) || telefone.includes(query);
    });

    renderClientes(filtrados);
}

function abrirEditarCliente(id) {
    const cliente = clientesData.find(c => String(c.id) === String(id));
    if (!cliente) {
        showToast('Cliente não encontrado.', 'error');
        return;
    }

    const editIdInput = document.getElementById('clienteEditId');
    const modalTitle = document.getElementById('clienteModalTitle');
    const nomeInput = document.getElementById('clienteNome');
    const cpfInput = document.getElementById('clienteCpf');
    const telInput = document.getElementById('clienteTelefone');
    const emailInput = document.getElementById('clienteEmail');

    if (editIdInput) editIdInput.value = cliente.id;
    if (modalTitle) modalTitle.textContent = 'Editar Cliente';
    if (nomeInput) nomeInput.value = cliente.nome || '';
    if (cpfInput) cpfInput.value = cliente.cpf || '';
    if (telInput) telInput.value = cliente.telefone || '';
    if (emailInput) emailInput.value = cliente.email || '';

    const modalEl = document.getElementById('clientModal');
    if (modalEl) {
        const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
        modalInstance.show();
    }
}

async function salvarCliente() {
    const editIdInput = document.getElementById('clienteEditId');
    const nomeInput = document.getElementById('clienteNome');
    const cpfInput = document.getElementById('clienteCpf');
    const telInput = document.getElementById('clienteTelefone');
    const emailInput = document.getElementById('clienteEmail');
    const btnSalvar = document.getElementById('btnSalvarCliente');

    const nome = nomeInput ? nomeInput.value.trim() : '';
    const cpf = cpfInput ? cpfInput.value.trim() : '';
    const telefone = telInput ? telInput.value.trim() : '';
    const email = emailInput ? emailInput.value.trim() : '';
    const editId = editIdInput ? editIdInput.value : '';

    if (!nome) {
        showToast('Por favor, informe o nome do cliente.', 'error');
        if (nomeInput) nomeInput.focus();
        return;
    }

    const payload = { nome, cpf, telefone, email };

    const originalBtnText = btnSalvar ? btnSalvar.innerHTML : 'Salvar Cliente';
    if (btnSalvar) {
        btnSalvar.disabled = true;
        btnSalvar.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Salvando...';
    }

    try {
        if (editId) {
            await apiRequest(`/clientes/${editId}`, 'PUT', payload);
            showToast('Cliente atualizado com sucesso!', 'success');
        } else {
            await apiRequest('/clientes', 'POST', payload);
            showToast('Cliente cadastrado com sucesso!', 'success');
        }

        const modalEl = document.getElementById('clientModal');
        if (modalEl) {
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            if (modalInstance) modalInstance.hide();
        }

        resetModalCliente();
        await carregarClientes();
    } catch (error) {
        showToast(error.message || 'Erro ao salvar cliente.', 'error');
    } finally {
        if (btnSalvar) {
            btnSalvar.disabled = false;
            btnSalvar.innerHTML = originalBtnText;
        }
    }
}

async function excluirCliente(id) {
    if (!confirm('Tem certeza que deseja excluir este cliente?')) {
        return;
    }

    try {
        await apiRequest(`/clientes/${id}`, 'DELETE');
        showToast('Cliente excluído com sucesso!', 'success');
        await carregarClientes();
    } catch (error) {
        showToast(error.message || 'Erro ao excluir cliente.', 'error');
    }
}
