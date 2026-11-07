package com.batpay.batpay;

import com.batpay.batpay.model.Cobranca;
import com.batpay.batpay.model.StatusCobranca;
import com.batpay.batpay.repository.ClienteRepository;
import com.batpay.batpay.repository.CobrancaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("demo")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:batpay-test",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BatpayApplicationTests {
    private static final String SENHA = "senha-de-teste";
    private static final String ORIGEM = "http://127.0.0.1:5500";

    @LocalServerPort
    private int porta;
    @Autowired
    private CobrancaRepository cobrancas;
    @Autowired
    private ClienteRepository clientes;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void controlaSessaoCorsEAlteracaoDaPropriaSenha() throws Exception {
        chamar(401, "GET", "/clientes", null, null, null);
        var preflight = http.send(HttpRequest.newBuilder(url("/pagamentos"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", ORIGEM)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type, Idempotency-Key")
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(preflight.statusCode()).isEqualTo(200);
        assertThat(preflight.headers().firstValue("Access-Control-Allow-Origin")).contains(ORIGEM);

        String email = novoEmail();
        long id = cadastrar(email);
        long outraConta = cadastrar(novoEmail());
        chamar(401, "POST", "/usuarios/login", credenciais(email, "incorreta"), null, null);
        String sessao = autenticar(email, SENHA);
        chamar(403, "GET", "/usuarios/" + outraConta, null, sessao, null);
        chamar(400, "PUT", "/usuarios/" + id + "/senha",
                """
                {"senhaAtual":"incorreta","novaSenha":"nova-senha"}
                """, sessao, null);
        chamar(204, "PUT", "/usuarios/" + id + "/senha",
                """
                {"senhaAtual":"senha-de-teste","novaSenha":"nova-senha"}
                """, sessao, null);
        chamar(401, "GET", "/clientes", null, sessao, null);
        chamar(401, "POST", "/usuarios/login", credenciais(email, SENHA), null, null);
        String novaSessao = autenticar(email, "nova-senha");
        chamar(204, "POST", "/usuarios/logout", null, novaSessao, null);
        chamar(401, "GET", "/clientes", null, novaSessao, null);
    }

    @Test
    void preservaHistoricoDaRecusaAteOEstorno() throws Exception {
        String sessao = novaSessao();
        long cliente = criarCliente(sessao);
        long cobranca = criarCobranca(sessao, cliente, "PIX");
        String aprovacao = pagamento(cobranca, "APROVADO");
        String recusa = pagamento(cobranca, "RECUSADO");

        chamar(400, "POST", "/pagamentos", aprovacao, sessao, null);
        JsonNode recusado = chamar(201, "POST", "/pagamentos", recusa, sessao, "recusa");
        assertThat(recusado.path("status").asString()).isEqualTo("RECUSADO");
        assertThat(statusCobranca(sessao, cobranca)).isEqualTo("PENDENTE");
        chamar(409, "POST", "/pagamentos/" + recusado.path("id").asString() + "/estorno", null, sessao, null);

        JsonNode aprovado = chamar(201, "POST", "/pagamentos", aprovacao, sessao, "aprovacao");
        String id = aprovado.path("id").asString();
        assertThat(aprovado.path("status").asString()).isEqualTo("APROVADO");
        assertThat(aprovado.path("valor").decimalValue()).isEqualByComparingTo("29.90");
        assertThat(aprovado.path("simulado").asBoolean()).isTrue();
        assertThat(statusCobranca(sessao, cobranca)).isEqualTo("PAGO");
        assertThat(chamar(200, "POST", "/pagamentos", aprovacao, sessao, "aprovacao")).isEqualTo(aprovado);
        chamar(409, "POST", "/pagamentos", recusa, sessao, "aprovacao");
        chamar(409, "POST", "/pagamentos", aprovacao, sessao, "segunda-aprovacao");
        chamar(409, "DELETE", "/cobrancas/" + cobranca, null, sessao, null);
        chamar(409, "DELETE", "/clientes/" + cliente, null, sessao, null);

        String estorno = "/pagamentos/" + id + "/estorno";
        JsonNode estornado = chamar(200, "POST", estorno, null, sessao, null);
        assertThat(estornado.path("status").asString()).isEqualTo("ESTORNADO");
        assertThat(estornado.path("estornadoEm").isString()).isTrue();
        assertThat(chamar(200, "POST", estorno, null, sessao, null)).isEqualTo(estornado);
        assertThat(chamar(200, "GET", "/pagamentos/" + id, null, sessao, null)).isEqualTo(estornado);
        assertThat(statusCobranca(sessao, cobranca)).isEqualTo("ESTORNADO");
        assertThat(chamar(200, "GET", "/pagamentos?cobrancaId=" + cobranca + "&metodo=PIX&status=ESTORNADO",
                null, sessao, null).size()).isEqualTo(1);
    }

    @Test
    void validaCobrancasEImpedePagamentoDeVencidasOuCanceladas() throws Exception {
        String sessao = novaSessao();
        long cliente = criarCliente(sessao);
        chamar(400, "POST", "/cobrancas", cobranca(cliente, "PIX", "-2"), sessao, null);
        chamar(400, "POST", "/cobrancas", cobranca(cliente, "PIX", "2.345"), sessao, null);
        chamar(400, "POST", "/cobrancas", cobranca(cliente, "DINHEIRO", "2"), sessao, null);
        chamar(404, "POST", "/cobrancas", cobranca(Long.MAX_VALUE, "PIX", "2"), sessao, null);
        chamar(400, "GET", "/cobrancas?status=INVALIDO", null, sessao, null);

        long cancelada = criarCobranca(sessao, cliente, "BOLETO");
        var cancelamento = chamar(200, "POST", "/cobrancas/" + cancelada + "/cancelamento", null, sessao, null);
        assertThat(cancelamento.path("status").asString()).isEqualTo("CANCELADO");
        chamar(409, "POST", "/pagamentos", pagamento(cancelada, "APROVADO"), sessao, "cancelada");
        chamar(204, "DELETE", "/cobrancas/" + cancelada, null, sessao, null);

        var antiga = cobrancas.save(new Cobranca(clientes.findById(cliente).orElseThrow(),
                "Cobrança antiga", BigDecimal.TEN, Cobranca.hoje().minusDays(1), "BOLETO", StatusCobranca.PENDENTE));
        assertThat(statusCobranca(sessao, antiga.getId())).isEqualTo("VENCIDO");
        var vencidas = chamar(200, "GET", "/cobrancas?status=vencido", null, sessao, null);
        assertThat(vencidas.valueStream().map(item -> item.path("id").asLong())).contains(antiga.getId());
        chamar(409, "POST", "/pagamentos", pagamento(antiga.getId(), "APROVADO"), sessao, "vencida");
    }

    @Test
    void aprovaUmaUnicaTentativaComRequisicoesConcorrentes() throws Exception {
        String sessao = novaSessao();
        long cobranca = criarCobranca(sessao, criarCliente(sessao), "CARTAO");
        String corpo = pagamento(cobranca, "APROVADO");
        var primeira = http.sendAsync(requisicao("POST", "/pagamentos", corpo, sessao, "cartao-1"),
                HttpResponse.BodyHandlers.ofString());
        var segunda = http.sendAsync(requisicao("POST", "/pagamentos", corpo, sessao, "cartao-2"),
                HttpResponse.BodyHandlers.ofString());
        var codigos = Stream.of(primeira.get(), segunda.get()).map(HttpResponse::statusCode).toList();
        assertThat(codigos).containsExactlyInAnyOrder(201, 409);
        assertThat(chamar(200, "GET", "/pagamentos?cobrancaId=" + cobranca, null, sessao, null).size()).isEqualTo(1);
        assertThat(statusCobranca(sessao, cobranca)).isEqualTo("PAGO");
    }

    private String novoEmail() {
        return UUID.randomUUID() + "@batpay.example";
    }

    private long cadastrar(String email) throws Exception {
        var usuario = chamar(201, "POST", "/usuarios", """
                {"nome":"Operador","email":"%s","senha":"%s"}
                """.formatted(email, SENHA), null, null);
        assertThat(usuario.has("senha")).isFalse();
        return usuario.path("id").asLong();
    }

    private String credenciais(String email, String senha) {
        return """
                {"email":"%s","senha":"%s"}
                """.formatted(email, senha);
    }

    private String autenticar(String email, String senha) throws Exception {
        var resposta = enviar(200, "POST", "/usuarios/login", credenciais(email, senha), null, null);
        String cookie = resposta.headers().firstValue("Set-Cookie").orElseThrow();
        assertThat(cookie).contains("HttpOnly", "SameSite=Strict");
        assertThat(resposta.headers().firstValue("Access-Control-Allow-Credentials")).contains("true");
        return cookie.split(";", 2)[0];
    }

    private String novaSessao() throws Exception {
        String email = novoEmail();
        cadastrar(email);
        return autenticar(email, SENHA);
    }

    private long criarCliente(String sessao) throws Exception {
        return chamar(201, "POST", "/clientes", """
                {"nome":"Cliente de demonstração","email":"cliente@batpay.example"}
                """, sessao, null).path("id").asLong();
    }

    private String cobranca(long cliente, String metodo, String valor) {
        return """
                {"clienteId":%d,"descricao":"Pedido de demonstração","valor":%s,"dataVencimento":"%s","metodoPagamento":"%s"}
                """.formatted(cliente, valor, Cobranca.hoje().plusDays(3), metodo);
    }

    private long criarCobranca(String sessao, long cliente, String metodo) throws Exception {
        return chamar(201, "POST", "/cobrancas", cobranca(cliente, metodo, "29.90"), sessao, null)
                .path("id").asLong();
    }

    private String pagamento(long cobranca, String resultado) {
        return """
                {"cobrancaId":%d,"resultado":"%s"}
                """.formatted(cobranca, resultado);
    }

    private String statusCobranca(String sessao, long id) throws Exception {
        return chamar(200, "GET", "/cobrancas/" + id, null, sessao, null).path("status").asString();
    }

    private JsonNode chamar(int status, String metodo, String rota, String corpo, String sessao, String chave)
            throws Exception {
        var resposta = enviar(status, metodo, rota, corpo, sessao, chave);
        return status == 200 || status == 201 ? json.readTree(resposta.body()) : null;
    }

    private HttpResponse<String> enviar(int status, String metodo, String rota, String corpo, String sessao, String chave)
            throws Exception {
        var resposta = http.send(requisicao(metodo, rota, corpo, sessao, chave), HttpResponse.BodyHandlers.ofString());
        assertThat(resposta.statusCode()).as("%s %s → %s", metodo, rota, resposta.body()).isEqualTo(status);
        return resposta;
    }

    private HttpRequest requisicao(String metodo, String rota, String corpo, String sessao, String chave) {
        var builder = HttpRequest.newBuilder(url(rota)).timeout(Duration.ofSeconds(10)).header("Origin", ORIGEM)
                .method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo));
        if (corpo != null) {
            builder.header("Content-Type", "application/json");
        }
        if (sessao != null) {
            builder.header("Cookie", sessao);
        }
        if (chave != null) {
            builder.header("Idempotency-Key", chave);
        }
        return builder.build();
    }

    private URI url(String rota) {
        return URI.create("http://127.0.0.1:" + porta + "/api" + rota);
    }
}
