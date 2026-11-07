# BatPay · API de demonstração

O BatPay simula pagamentos em reais por Pix, cartão e boleto. O operador escolhe se a tentativa será aprovada ou recusada. A API registra o resultado e o estorno no banco, sem coletar números de cartão, gerar boletos ou transferir dinheiro.

## Rodar no Windows

Abra um terminal em `controller/batpay` e execute:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=demo"
```

A API fica em `http://127.0.0.1:8080/api`. O perfil `demo` usa H2 e salva os dados em `.batpay/` dentro do backend. Use dados fictícios. A demonstração tem um espaço compartilhado entre operadores autenticados. Cada operador pode consultar e editar apenas o próprio perfil de usuário.

Para usar as telas existentes, sirva a pasta `view` por HTTP local, por exemplo com o Live Server. Abra pelo endereço `http://127.0.0.1` ou `http://localhost` com a porta do servidor estático. Faça login novamente para iniciar uma sessão. A tela de pagamentos ainda usa dados estáticos. As operações simuladas podem ser demonstradas diretamente pela API.

## Demonstrar o fluxo pela API

Cole no PowerShell enquanto o servidor estiver rodando:

```powershell
$api = 'http://127.0.0.1:8080/api'
$sessao = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$credenciais = @{ email = "demo-$([guid]::NewGuid())@batpay.example"; senha = 'senha-demo-123' }

function Enviar-BatPay($rota, $dados, $cabecalhos = @{}) {
    Invoke-RestMethod -Uri "$api$rota" -Method Post -ContentType 'application/json' `
        -WebSession $sessao -Headers $cabecalhos -Body ($dados | ConvertTo-Json -Compress)
}

Enviar-BatPay '/usuarios' ($credenciais + @{ nome = 'Operador Demo' })
Enviar-BatPay '/usuarios/login' $credenciais
$cliente = Enviar-BatPay '/clientes' @{ nome = 'Cliente Demo' }
$cobranca = Enviar-BatPay '/cobrancas' @{
    clienteId = $cliente.id
    descricao = 'Pedido de demonstração'
    valor = 29.90
    dataVencimento = (Get-Date).AddDays(7).ToString('yyyy-MM-dd')
    metodoPagamento = 'PIX'
}

$recusa = Enviar-BatPay '/pagamentos' @{ cobrancaId = $cobranca.id; resultado = 'RECUSADO' } @{
    'Idempotency-Key' = 'tentativa-1'
}
$dados = @{ cobrancaId = $cobranca.id; resultado = 'APROVADO' }
$chave = @{ 'Idempotency-Key' = 'tentativa-2' }
$pagamento = Enviar-BatPay '/pagamentos' $dados $chave
$reenvio = Enviar-BatPay '/pagamentos' $dados $chave
$pagamento.id -eq $reenvio.id

Invoke-RestMethod "$api/pagamentos?cobrancaId=$($cobranca.id)" -WebSession $sessao
Invoke-RestMethod "$api/pagamentos/$($pagamento.id)/estorno" -Method Post -WebSession $sessao
Invoke-RestMethod "$api/cobrancas/$($cobranca.id)" -WebSession $sessao
```

| Rota | Comportamento |
| --- | --- |
| `POST /api/usuarios`, `POST /api/usuarios/login` | Cadastro e autenticação. Guarde o cookie de sessão retornado pelo login para as demais operações. |
| `POST /api/usuarios/logout`, `PUT /api/usuarios/{id}/senha` | Encerra a sessão atual. A troca de senha exige `senhaAtual` e `novaSenha`. |
| `GET/POST /api/clientes`, `GET/PUT/DELETE /api/clientes/{id}` | Gerencia clientes. A exclusão é bloqueada se o cliente tiver cobranças. |
| `GET/POST /api/cobrancas`, `GET/DELETE /api/cobrancas/{id}` | Cria e consulta cobranças. O filtro `status` inclui `PENDENTE`, `PAGO`, `VENCIDO`, `CANCELADO` e `ESTORNADO`. Só cobranças sem histórico de pagamento podem ser excluídas. |
| `POST /api/cobrancas/{id}/cancelamento` | Cancela cobranças pendentes ou vencidas. |
| `POST /api/pagamentos` | Registra uma tentativa de pagamento para uma cobrança pendente. Aceita `APROVADO` ou `RECUSADO`. |
| `GET /api/pagamentos`, `GET /api/pagamentos/{id}` | Consulta o histórico. A lista aceita os filtros `cobrancaId`, `status` e `metodo`. |
| `POST /api/pagamentos/{id}/estorno` | Estorna integralmente um pagamento aprovado e preserva o histórico. Repetir o estorno devolve o mesmo pagamento. |

A chave de idempotência vale por cobrança e aceita de 1 a 100 letras ASCII, dígitos, pontos, hífens ou sublinhados. Maiúsculas e minúsculas são equivalentes. A primeira tentativa retorna `201`. Repetir a chave com o mesmo resultado retorna `200` e o registro atual. Usar a mesma chave com outro resultado ou tentar pagar uma cobrança já liquidada retorna `409`. Uma recusa mantém a cobrança pendente, permitindo nova tentativa com outra chave. O vencimento segue a data de São Paulo.

## Testes e MySQL

Execute `.\gradlew.bat test` no backend. Os testes sobem a API em uma porta aleatória com H2 em memória.

Para rodar com MySQL, inicie sem o perfil `demo` e defina `DB_username` e `DB_password`. A URL padrão é `jdbc:mysql://localhost:3306/batpay` e pode ser ajustada em `DB_URL`. Se a coluna `cobrancas.status` de um banco antigo ainda for um `ENUM` nativo, converta-a para `VARCHAR(255) NOT NULL` antes de usar o novo estado `ESTORNADO`:

```sql
ALTER TABLE cobrancas MODIFY COLUMN status VARCHAR(255) NOT NULL;
```
