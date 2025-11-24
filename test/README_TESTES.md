# 🧪 GUIA DE EXECUÇÃO DE TESTES - E.D.E.N.

## 📂 Arquivos de Teste Criados

### 1. `TesteSuiteCompleta.java`

**Descrição:** Testes unitários de todos os componentes do sistema.  
**Foco:** Manipulação de bits, janela deslizante, conversões, ACKs/NACKs.  
**Total de testes:** ~80 testes

### 2. `TesteIntegracaoSimulador.java`

**Descrição:** Testes de integração e cenários realistas.  
**Foco:** Fluxo completo de mensagens, protocolos de retransmissão, bugs conhecidos.  
**Total de testes:** ~30 testes

### 3. `RELATORIO_ANALISE.md`

**Descrição:** Relatório detalhado com análise técnica completa.  
**Conteúdo:** Bugs identificados, limitações, recomendações priorizadas.

---

## 🚀 Como Executar os Testes

### Opção 1: Via Terminal (Linha de Comando)

```bash
# 1. Navegue até o diretório do projeto
cd "c:\Users\Iury\Documents - Copy\.UESB\Semestre 06\redes1\trabalhos\redes1_trabalho04_202310440"

# 2. Compile os arquivos de teste
javac -d bin -sourcepath . test/TesteSuiteCompleta.java
javac -d bin -sourcepath . test/TesteIntegracaoSimulador.java

# 3. Execute a suite completa
java -cp bin test.TesteSuiteCompleta

# 4. Execute os testes de integração
java -cp bin test.TesteIntegracaoSimulador
```

### Opção 2: Via VS Code

1. Abra o arquivo `test/TesteSuiteCompleta.java` no VS Code
2. Clique com botão direito no editor
3. Selecione "Run Java"
4. Repita para `test/TesteIntegracaoSimulador.java`

### Opção 3: Via IDE (Eclipse/IntelliJ)

1. Importe o projeto
2. Navegue até `test/TesteSuiteCompleta.java`
3. Clique em "Run" ou pressione F11
4. Veja os resultados no console

---

## 📊 Interpretando os Resultados

### Símbolos Usados nos Testes

-   ✓ **Teste passou** - Funcionalidade está correta
-   ✗ **Teste falhou** - Bug ou comportamento inesperado detectado
-   ⚠️ **Aviso** - Limitação ou problema potencial (não é erro fatal)
-   🐛 **Bug confirmado** - Problema real que precisa ser corrigido
-   【CRÍTICO】 - Teste de alta importância

### Exemplo de Saída

```
═══════════════════════════════════════════════════════════════════
   BATERIA DE TESTES - SIMULADOR DE REDES E.D.E.N.
═══════════════════════════════════════════════════════════════════

【TESTE 1】 Manipulação de Bits - Operações Básicas
─────────────────────────────────────────────────────────────────
  ✓ Escrever e ler 1 bit
  ✓ Escrever e ler 8 bits (1 byte)
  ✓ Escrever bits em posições diferentes no mesmo inteiro
  ✗ 【CRÍTICO】Escrever bits sem sobrescrever bits adjacentes [FALHOU]

...

═══════════════════════════════════════════════════════════════════
   RESUMO DOS TESTES
═══════════════════════════════════════════════════════════════════
Total de testes executados: 80
Testes que passaram: 65
Testes que falharam: 15
Taxa de sucesso: 81.25%
═══════════════════════════════════════════════════════════════════
```

---

## 🔍 Análise dos Resultados

### Taxa de Sucesso Esperada

-   **80-90%:** Excelente - Poucos bugs críticos
-   **60-79%:** Bom - Alguns bugs que precisam correção
-   **40-59%:** Regular - Muitos bugs, revisão necessária
-   **< 40%:** Crítico - Sistema precisa de refatoração

### Categorias de Testes

#### 【TESTE 1-6】 Manipulação de Bits

**O que testa:**

-   Leitura e escrita de bits individuais
-   Conversão String ↔ Int
-   Operações com cabeçalhos
-   ACKs e NACKs

**Bugs esperados:**

-   Caractere nulo trunca string
-   Quadros com todos os bits zero
-   Leitura/escrita cruzando fronteira de inteiros

#### 【TESTE 7-10】 Janela Deslizante

**O que testa:**

-   Criação e configuração
-   Números cíclicos (wrap-around)
-   Gerenciamento de buffer
-   Retransmissão seletiva

**Bugs esperados:**

-   Configuração inválida aceita (janela = espaço de sequência)
-   Problemas com wrap-around

#### 【TESTE 11-16】 Camadas de Rede

**O que testa:**

-   Enquadramento (contagem, inserção de bytes, bit stuffing)
-   Controle de erro (paridade, CRC, Hamming)

**Limitações esperadas:**

-   Overhead alto de enquadramento
-   Paridade não detecta múltiplos erros

#### 【TESTE 17-20】 Robustez

**O que testa:**

-   Mensagens vazias
-   Mensagens grandes (1KB, 10KB)
-   Caracteres especiais
-   Limites do protocolo

**Problemas esperados:**

-   Mensagens muito grandes causam lentidão
-   Caracteres especiais podem causar problemas

### Testes de Integração

#### 【INTEGRAÇÃO 1】 Fluxo Completo

**O que testa:** Mensagem passa por todas as camadas e volta intacta

#### 【INTEGRAÇÃO 2】 Combinações

**O que testa:** Enquadramento + Controle de Erro funcionam juntos

#### 【INTEGRAÇÃO 3】 Protocolos

**O que testa:** Stop-and-Wait, Go-Back-N, Retransmissão Seletiva

#### 【INTEGRAÇÃO 4】 Cenários Críticos

**O que testa:** Wrap-around, ambiguidade, taxa de erro alta

#### 【INTEGRAÇÃO 5】 Problemas Conhecidos

**O que testa:** Bugs já identificados durante análise do código

---

## 🐛 Bugs Mais Importantes a Observar

### 1. Caractere Nulo (\0)

```
Input:  "ABC\0DEF"
Output: "ABC" ← DEF foi perdido!
Status: 🐛 BUG CRÍTICO
```

### 2. Quadro com Zeros

```
Input:  int[] {0, 0, 0}
Output: 8 (deveria ser 0)
Status: 🐛 BUG
```

### 3. Janela Inválida

```
Config: JanelaDeslizante(8, 3) ← 8 = 2^3 (ambíguo!)
Status: ⚠️ ACEITA (mas não deveria)
```

### 4. Concorrência

```
Thread 1: processarACK()
Thread 2: tratarTimeout()
Ambos acessam janelaDeslizante sem sincronização
Status: 🐛 RACE CONDITION
```

---

## 📋 Checklist Pós-Execução

Após executar os testes, verifique:

-   [ ] Quantos testes falharam?
-   [ ] Quais são os bugs CRÍTICOS?
-   [ ] Houve exceções não tratadas?
-   [ ] A taxa de sucesso é aceitável?
-   [ ] Leia o RELATORIO_ANALISE.md para detalhes

---

## 🛠️ Próximos Passos

### 1. Analisar Falhas

```bash
# Execute novamente e redirecione output para arquivo
java -cp bin test.TesteSuiteCompleta > resultados.txt
```

### 2. Priorizar Correções

Consulte o `RELATORIO_ANALISE.md` seção "RECOMENDAÇÕES PRIORIZADAS"

### 3. Corrigir Bugs Críticos

Comece pelos bugs marcados como 【CRÍTICO】

### 4. Re-executar Testes

Após correções, rode os testes novamente para validar

### 5. Expandir Testes

Adicione mais testes para cobrir casos específicos do seu uso

---

## 📖 Estrutura dos Arquivos de Teste

### TesteSuiteCompleta.java

```
TesteSuiteCompleta
├── executarTodosOsTestes()
│   ├── testarManipulacaoBitsBasico()
│   ├── testarManipulacaoBitsEdgeCases()
│   ├── testarConversaoStringInt()
│   ├── testarLeituraEscritaBits()
│   ├── testarCabecalhos()
│   ├── testarACKsENACKs()
│   ├── testarJanelaDeslizanteBasico()
│   ├── testarJanelaDeslizanteNumerosCiclicos()
│   ├── testarJanelaDeslizanteBuffer()
│   ├── testarJanelaDeslizanteRetransmissaoSeletiva()
│   ├── testarEnquadramentoContagemCaracteres()
│   ├── testarEnquadramentoInsercaoBytes()
│   ├── testarEnquadramentoBitStuffing()
│   ├── testarControleErroParidade()
│   ├── testarControleErroCRC()
│   ├── testarControleErroHamming()
│   ├── testarMensagensVazias()
│   ├── testarMensagensGrandes()
│   ├── testarCaracteresEspeciais()
│   └── testarLimitesProtocolo()
└── testar(String, TestFunction) ← Método auxiliar
```

### TesteIntegracaoSimulador.java

```
TesteIntegracaoSimulador
├── executarTestesIntegracao()
│   ├── testarFluxoCompletoMensagem()
│   ├── testarCombinacaoEnquadramentoControleErro()
│   ├── testarRetransmissaoCompleta()
│   ├── testarCenariosCriticosProtocolo()
│   └── testarProblemasConhecidos()
└── testar(String, TestFunction) ← Método auxiliar
```

---

## 💡 Dicas para Depuração

### Se um teste falhar:

1. **Identifique o teste específico**

    ```
    ✗ 【CRÍTICO】Conversão de string vazia [FALHOU]
    ```

2. **Localize o código do teste**

    ```java
    testar("Conversão de string vazia", () -> {
        String original = "";
        int[] bits = ManipulacaoBits.stringParaIntAgrupado(original);
        String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
        return resultado.isEmpty() || resultado.length() == 0;
    });
    ```

3. **Execute isoladamente**
   Comente outros testes e rode apenas o que falhou

4. **Adicione debug prints**

    ```java
    System.out.println("Bits: " + Arrays.toString(bits));
    System.out.println("Resultado: '" + resultado + "'");
    ```

5. **Use breakpoints**
   Coloque breakpoints no código testado

---

## 📞 Suporte e Documentação

### Arquivos de Referência

-   `RELATORIO_ANALISE.md` - Análise completa do sistema
-   `TesteSuiteCompleta.java` - Testes unitários
-   `TesteIntegracaoSimulador.java` - Testes de integração
-   Este arquivo (README) - Guia de execução

### Contato

Para dúvidas sobre os testes ou relatório, consulte os comentários no código ou revise o RELATORIO_ANALISE.md.

---

**Última atualização:** 23/11/2025  
**Versão:** 1.0  
**Compatibilidade:** Java 8+
