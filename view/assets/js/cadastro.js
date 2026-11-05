document.addEventListener('DOMContentLoaded', () => {
    const cadastroForm = document.getElementById('cadastroForm');
    const cadNome = document.getElementById('cadNome');
    const cadEmpresa = document.getElementById('cadEmpresa');
    const cadEmail = document.getElementById('cadEmail');
    const cadTelefone = document.getElementById('cadTelefone');
    const cadSenha = document.getElementById('cadSenha');
    const cadConfirmarSenha = document.getElementById('cadConfirmarSenha');
    const cadastroBtn = document.getElementById('cadastroBtn');
    const cadastroError = document.getElementById('cadastroError');

    if (cadastroForm) {
        cadastroForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            // Clear errors
            if (cadastroError) {
                cadastroError.classList.add('d-none');
                cadastroError.textContent = '';
            }

            const nome = cadNome ? cadNome.value.trim() : '';
            const empresa = cadEmpresa ? cadEmpresa.value.trim() : '';
            const email = cadEmail ? cadEmail.value.trim() : '';
            const telefone = cadTelefone ? cadTelefone.value.trim() : '';
            const senha = cadSenha ? cadSenha.value : '';
            const confirmarSenha = cadConfirmarSenha ? cadConfirmarSenha.value : '';

            if (!nome || !email || !senha || !confirmarSenha) {
                if (cadastroError) {
                    cadastroError.textContent = 'Por favor, preencha todos os campos obrigatórios.';
                    cadastroError.classList.remove('d-none');
                }
                return;
            }

            if (senha !== confirmarSenha) {
                if (cadastroError) {
                    cadastroError.textContent = 'As senhas não coincidem. Verifique e tente novamente.';
                    cadastroError.classList.remove('d-none');
                }
                return;
            }

            if (senha.length < 6) {
                if (cadastroError) {
                    cadastroError.textContent = 'A senha deve conter pelo menos 6 caracteres.';
                    cadastroError.classList.remove('d-none');
                }
                return;
            }

            const originalBtnText = cadastroBtn ? cadastroBtn.innerHTML : 'Criar Minha Conta';
            if (cadastroBtn) {
                cadastroBtn.disabled = true;
                cadastroBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Criando conta...';
            }

            try {
                await apiRequest('/usuarios', 'POST', {
                    nome,
                    empresa,
                    email,
                    telefone,
                    senha
                });

                showToast('Conta criada com sucesso! Redirecionando...', 'success');
                setTimeout(() => {
                    window.location.href = 'login.html?cadastro=sucesso';
                }, 1000);
            } catch (error) {
                if (cadastroError) {
                    cadastroError.textContent = error.message || 'Erro ao realizar cadastro. Tente novamente.';
                    cadastroError.classList.remove('d-none');
                }
            } finally {
                if (cadastroBtn) {
                    cadastroBtn.disabled = false;
                    cadastroBtn.innerHTML = originalBtnText;
                }
            }
        });
    }
});
