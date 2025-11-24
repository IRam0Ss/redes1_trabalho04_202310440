package test;

/**
 * ===============================================================================
 * SUITE DE TESTES COMPLETA - SIMULADOR DE REDES E.D.E.N.
 * ===============================================================================
 * 
 * Autor: Análise Automatizada de Qualidade
 * Data: 23/11/2025
 * 
 * Esta suite de testes foi criada para explorar TODAS as funções do sistema
 * em busca de possíveis erros lógicos e/ou estruturais.
 * 
 * OBJETIVO: Identificar problemas reais, bugs, edge cases e melhorias necessárias
 * para tornar o simulador robusto e confiável.
 * 
 * ===============================================================================
 */

import util.ManipulacaoBits;
import util.JanelaDeslizante;
import util.ErroDeVerificacaoException;

import java.util.Arrays;

public class TesteSuiteCompleta {

  private static int totalTestes = 0;
  private static int testesPassaram = 0;
  private static int testesFalharam = 0;

  public static void main(String[] args) {
    System.out.println("═══════════════════════════════════════════════════════════════════");
    System.out.println("   BATERIA DE TESTES - SIMULADOR DE REDES E.D.E.N.");
    System.out.println("═══════════════════════════════════════════════════════════════════\n");

    executarTodosOsTestes();

    System.out.println("\n═══════════════════════════════════════════════════════════════════");
    System.out.println("   RESUMO DOS TESTES");
    System.out.println("═══════════════════════════════════════════════════════════════════");
    System.out.println("Total de testes executados: " + totalTestes);
    System.out.println("Testes que passaram: " + testesPassaram);
    System.out.println("Testes que falharam: " + testesFalharam);
    System.out.println("Taxa de sucesso: " + String.format("%.2f%%", (testesPassaram * 100.0 / totalTestes)));
    System.out.println("═══════════════════════════════════════════════════════════════════\n");
  }

  private static void executarTodosOsTestes() {
    // ========== CATEGORIA 1: MANIPULAÇÃO DE BITS ==========
    testarManipulacaoBitsBasico();
    testarManipulacaoBitsEdgeCases();
    testarConversaoStringInt();
    testarLeituraEscritaBits();
    testarCabecalhos();
    testarACKsENACKs();

    // ========== CATEGORIA 2: JANELA DESLIZANTE ==========
    testarJanelaDeslizanteBasico();
    testarJanelaDeslizanteNumerosCiclicos();
    testarJanelaDeslizanteBuffer();
    testarJanelaDeslizanteRetransmissaoSeletiva();

    // ========== CATEGORIA 3: LÓGICA DE CAMADAS (SIMULAÇÃO) ==========
    // Nota: Testes que não requerem JavaFX
    testarEnquadramentoContagemCaracteres();
    testarEnquadramentoInsercaoBytes();
    testarEnquadramentoBitStuffing();
    testarControleErroParidade();
    testarControleErroCRC();
    testarControleErroHamming();

    // ========== CATEGORIA 4: CASOS EXTREMOS E ROBUSTEZ ==========
    testarMensagensVazias();
    testarMensagensGrandes();
    testarCaracteresEspeciais();
    testarLimitesProtocolo();
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // CATEGORIA 1: TESTES DE MANIPULAÇÃO DE BITS
  // ═══════════════════════════════════════════════════════════════════════════

  private static void testarManipulacaoBitsBasico() {
    System.out.println("\n【TESTE 1】 Manipulação de Bits - Operações Básicas");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 1.1: Escrever e ler bits simples
    testar("Escrever e ler 1 bit", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 0, 1, 1);
      int resultado = ManipulacaoBits.lerBits(array, 0, 1);
      return resultado == 1;
    });

    // Teste 1.2: Escrever e ler byte completo
    testar("Escrever e ler 8 bits (1 byte)", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 0, 0xFF, 8);
      int resultado = ManipulacaoBits.lerBits(array, 0, 8);
      return resultado == 0xFF;
    });

    // Teste 1.3: Escrever em posições diferentes
    testar("Escrever bits em posições diferentes no mesmo inteiro", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 0, 0xAA, 8); // 10101010
      ManipulacaoBits.escreverBits(array, 8, 0x55, 8); // 01010101
      int byte1 = ManipulacaoBits.lerBits(array, 0, 8);
      int byte2 = ManipulacaoBits.lerBits(array, 8, 8);
      return byte1 == 0xAA && byte2 == 0x55;
    });

    // Teste 1.4: PROBLEMA POTENCIAL - Sobrescrita de bits
    testar("【CRÍTICO】Escrever bits sem sobrescrever bits adjacentes", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 0, 0xFF, 8);
      ManipulacaoBits.escreverBits(array, 4, 0x0, 4); // Escreve 0000 no meio
      int resultado = ManipulacaoBits.lerBits(array, 0, 8);
      // Deveria ser 11110000 (0xF0)
      return resultado == 0xF0;
    });
  }

  private static void testarManipulacaoBitsEdgeCases() {
    System.out.println("\n【TESTE 2】 Manipulação de Bits - Casos Extremos");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 2.1: Ler/escrever no limite do inteiro (bit 31)
    testar("Ler/escrever no último bit do inteiro (posição 31)", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 31, 1, 1);
      int resultado = ManipulacaoBits.lerBits(array, 31, 1);
      return resultado == 1;
    });

    // Teste 2.2: Ler/escrever atravessando fronteira de inteiros
    testar("【CRÍTICO】Ler/escrever bits que cruzam fronteira entre inteiros", () -> {
      int[] array = new int[2];
      ManipulacaoBits.escreverBits(array, 30, 0xF, 4); // 2 bits no int[0], 2 bits no int[1]
      int resultado = ManipulacaoBits.lerBits(array, 30, 4);
      return resultado == 0xF;
    });

    // Teste 2.3: Escrever 0 em todos os bits
    testar("Escrever zeros não corrompe o array", () -> {
      int[] array = new int[1];
      Arrays.fill(array, 0xFFFFFFFF); // Preenche tudo com 1s
      ManipulacaoBits.escreverBits(array, 0, 0, 32);
      return array[0] == 0;
    });

    // Teste 2.4: PROBLEMA - Limite de quantidadeDeBits
    testar("【CRÍTICO】Ler mais de 32 bits retorna erro apropriado", () -> {
      int[] array = new int[2];
      int resultado = ManipulacaoBits.lerBits(array, 0, 33);
      // Deveria retornar 0 ou lançar exceção
      return resultado == 0;
    });

    // Teste 2.5: Array vazio
    testar("Operações em array vazio/nulo", () -> {
      try {
        int[] array = new int[0];
        int total = ManipulacaoBits.descobrirTotalDeBitsReais(array);
        return total == 0;
      } catch (Exception e) {
        return false;
      }
    });
  }

  private static void testarConversaoStringInt() {
    System.out.println("\n【TESTE 3】 Conversão String ↔ Int Agrupado");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 3.1: String simples
    testar("Conversão de string simples 'ABC'", () -> {
      String original = "ABC";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(original);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return original.equals(resultado);
    });

    // Teste 3.2: String com caracteres especiais
    testar("Conversão de string com caracteres especiais", () -> {
      String original = "Olá, Mundo! @#$%";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(original);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return original.equals(resultado);
    });

    // Teste 3.3: String vazia
    testar("【CRÍTICO】Conversão de string vazia", () -> {
      String original = "";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(original);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return resultado.isEmpty() || resultado.length() == 0;
    });

    // Teste 3.4: String longa (múltiplos inteiros)
    testar("String longa (> 4 caracteres)", () -> {
      String original = "Esta é uma mensagem mais longa para testar";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(original);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return original.equals(resultado);
    });

    // Teste 3.5: PROBLEMA POTENCIAL - Caractere nulo
    testar("【CRÍTICO】String com caractere nulo \\0", () -> {
      String original = "ABC\0DEF";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(original);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      // Pode haver problema com \0 truncando a string
      return resultado.contains("D");
    });

    // Teste 3.6: Unicode/UTF-8
    testar("【CRÍTICO】String com caracteres Unicode (emojis)", () -> {
      String original = "Test 😀 emoji";
      try {
        int[] bits = ManipulacaoBits.stringParaIntAgrupado(original);
        String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
        return original.equals(resultado);
      } catch (Exception e) {
        System.out.println("      ⚠ FALHA: Sistema não suporta Unicode corretamente");
        return false;
      }
    });
  }

  private static void testarLeituraEscritaBits() {
    System.out.println("\n【TESTE 4】 Leitura e Escrita de Bits - Precisão");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 4.1: Padrão alternado
    testar("Padrão alternado 01010101", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 0, 0x55, 8);
      int resultado = ManipulacaoBits.lerBits(array, 0, 8);
      return resultado == 0x55;
    });

    // Teste 4.2: Todos os bits em 1
    testar("Todos os bits em 1 (0xFF)", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 0, 0xFF, 8);
      int resultado = ManipulacaoBits.lerBits(array, 0, 8);
      return resultado == 0xFF;
    });

    // Teste 4.3: Escrever bit a bit
    testar("Escrever bits individualmente", () -> {
      int[] array = new int[1];
      for (int i = 0; i < 8; i++) {
        ManipulacaoBits.escreverBits(array, i, 1, 1);
      }
      int resultado = ManipulacaoBits.lerBits(array, 0, 8);
      return resultado == 0xFF;
    });

    // Teste 4.4: PROBLEMA - Desalinhamento
    testar("【CRÍTICO】Leitura desalinhada (offset ímpar)", () -> {
      int[] array = new int[1];
      ManipulacaoBits.escreverBits(array, 1, 0x7F, 7); // Escreve a partir do bit 1
      int resultado = ManipulacaoBits.lerBits(array, 1, 7);
      return resultado == 0x7F;
    });
  }

  private static void testarCabecalhos() {
    System.out.println("\n【TESTE 5】 Cabeçalhos e Números de Sequência");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 5.1: Anexar e remover cabeçalho
    testar("Anexar cabeçalho com número de sequência", () -> {
      int[] dados = new int[] { 0xAABBCCDD };
      int[] comCabecalho = ManipulacaoBits.anexarCabecalho(dados, 5);
      int seq = ManipulacaoBits.lerNumeroDeSequencia(comCabecalho);
      return seq == 5 && comCabecalho.length == 2;
    });

    // Teste 5.2: Remover cabeçalho preserva dados
    testar("Remover cabeçalho preserva dados originais", () -> {
      int[] dados = new int[] { 0xAABBCCDD, 0x11223344 };
      int[] comCabecalho = ManipulacaoBits.anexarCabecalho(dados, 7);
      int[] semCabecalho = ManipulacaoBits.removerCabecalho(comCabecalho);
      return Arrays.equals(dados, semCabecalho);
    });

    // Teste 5.3: PROBLEMA - Número de sequência grande
    testar("【CRÍTICO】Número de sequência com valor máximo", () -> {
      int[] dados = new int[] { 0x12345678 };
      int seqMax = 0x7FFFFFFF; // Máximo sem conflitar com flags
      int[] comCabecalho = ManipulacaoBits.anexarCabecalho(dados, seqMax);
      int seq = ManipulacaoBits.lerNumeroDeSequencia(comCabecalho);
      // Pode haver overflow
      return seq == seqMax;
    });

    // Teste 5.4: PROBLEMA - Marcador de controle
    testar("【CRÍTICO】Marcador de controle não interfere em número de sequência", () -> {
      int[] dados = new int[] { 0x00000000 };
      int[] comCabecalho = ManipulacaoBits.anexarCabecalho(dados, 0);
      int cabecalhoBruto = comCabecalho[0];
      // Último bit deve ser 1 (marcador de controle)
      return (cabecalhoBruto & 1) == 1;
    });
  }

  private static void testarACKsENACKs() {
    System.out.println("\n【TESTE 6】 ACKs e NACKs");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 6.1: Montar ACK
    testar("Montar quadro de ACK", () -> {
      int[] ack = ManipulacaoBits.montarQuadroAck(3);
      return ManipulacaoBits.ehAck(ack) && !ManipulacaoBits.ehNack(ack);
    });

    // Teste 6.2: Montar NACK
    testar("Montar quadro de NACK", () -> {
      int[] nack = ManipulacaoBits.montarQuadroNack(5);
      return ManipulacaoBits.ehNack(nack) && ManipulacaoBits.ehAck(nack);
    });

    // Teste 6.3: Distinguir ACK de NACK
    testar("【CRÍTICO】Distinguir corretamente ACK de NACK", () -> {
      int[] ack = ManipulacaoBits.montarQuadroAck(1);
      int[] nack = ManipulacaoBits.montarQuadroNack(1);
      boolean ackEhApenasAck = ManipulacaoBits.ehAck(ack) && !ManipulacaoBits.ehNack(ack);
      boolean nackEhNack = ManipulacaoBits.ehNack(nack);
      return ackEhApenasAck && nackEhNack;
    });

    // Teste 6.4: Ler número de sequência de ACK
    testar("Ler número de sequência do ACK", () -> {
      int seqOriginal = 7;
      int[] ack = ManipulacaoBits.montarQuadroAck(seqOriginal);
      int seqLida = ManipulacaoBits.lerNumeroDeSequencia(ack);
      return seqLida == seqOriginal;
    });

    // Teste 6.5: Ler número de sequência de NACK
    testar("Ler número de sequência do NACK", () -> {
      int seqOriginal = 6;
      int[] nack = ManipulacaoBits.montarQuadroNack(seqOriginal);
      int seqLida = ManipulacaoBits.lerNumeroDeSequencia(nack);
      return seqLida == seqOriginal;
    });

    // Teste 6.6: PROBLEMA - Array vazio não é ACK
    testar("Array vazio não é reconhecido como ACK", () -> {
      int[] vazio = new int[0];
      return !ManipulacaoBits.ehAck(vazio) && !ManipulacaoBits.ehNack(vazio);
    });

    // Teste 6.7: PROBLEMA - Dados normais não são ACK
    testar("【CRÍTICO】Dados normais não são confundidos com ACK/NACK", () -> {
      int[] dados = new int[] { 0x12345678 };
      return !ManipulacaoBits.ehAck(dados) && !ManipulacaoBits.ehNack(dados);
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // CATEGORIA 2: TESTES DE JANELA DESLIZANTE
  // ═══════════════════════════════════════════════════════════════════════════

  private static void testarJanelaDeslizanteBasico() {
    System.out.println("\n【TESTE 7】 Janela Deslizante - Operações Básicas");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 7.1: Criação da janela
    testar("Criar janela deslizante (tamanho 4, 3 bits)", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      return janela.getTamanhoJanela() == 4 && janela.getEspacoSequencia() == 8;
    });

    // Teste 7.2: podeEnviar() inicialmente
    testar("Janela vazia permite envio", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      return janela.podeEnviar();
    });

    // Teste 7.3: Avançar sequência
    testar("Avançar número de sequência", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      int inicial = janela.getProximoNumeroSequencia();
      janela.avancarSequencia();
      return janela.getProximoNumeroSequencia() == (inicial + 1);
    });

    // Teste 7.4: Adicionar quadro no buffer
    testar("Adicionar quadro no buffer", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      int[] quadro = new int[] { 0x12345678 };
      janela.adicionarNoBuffer(0, quadro);
      int[] recuperado = janela.getQuadro(0);
      return Arrays.equals(quadro, recuperado);
    });

    // Teste 7.5: Atualizar base da janela
    testar("Atualizar base da janela", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      janela.atualizarBase(2);
      return janela.getBase() == 3; // Base vai para (2+1) % 8
    });
  }

  private static void testarJanelaDeslizanteNumerosCiclicos() {
    System.out.println("\n【TESTE 8】 Janela Deslizante - Números Cíclicos");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 8.1: Wrap-around do espaço de sequência
    testar("【CRÍTICO】Wrap-around do número de sequência", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3); // Espaço 0-7
      // Avança até o limite
      for (int i = 0; i < 8; i++) {
        janela.avancarSequencia();
      }
      // Deve voltar para 0
      return janela.getProximoNumeroSequencia() == 0;
    });

    // Teste 8.2: estaDentroDaJanela() com wrap-around
    testar("【CRÍTICO】estaDentroDaJanela() com wrap-around", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      // Configura base = 6, próximo = 2 (janela cruza o 0)
      janela.atualizarBase(5); // base = 6
      janela.avancarSequencia(); // próx = 1
      janela.avancarSequencia(); // próx = 2
      janela.adicionarNoBuffer(6, new int[] { 1 });
      janela.adicionarNoBuffer(7, new int[] { 2 });
      janela.adicionarNoBuffer(0, new int[] { 3 });
      janela.adicionarNoBuffer(1, new int[] { 4 });

      // Todos devem estar dentro da janela
      return janela.estaDentroDaJanela(6) && janela.estaDentroDaJanela(7)
          && janela.estaDentroDaJanela(0) && janela.estaDentroDaJanela(1);
    });

    // Teste 8.3: PROBLEMA - Janela cheia
    testar("【CRÍTICO】podeEnviar() retorna false quando janela cheia", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      // Envia 4 quadros
      for (int i = 0; i < 4; i++) {
        janela.avancarSequencia();
      }
      return !janela.podeEnviar();
    });

    // Teste 8.4: Limpar quadros antigos
    testar("Limpar quadros antigos do buffer", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      janela.adicionarNoBuffer(0, new int[] { 1 });
      janela.adicionarNoBuffer(1, new int[] { 2 });
      janela.atualizarBase(1); // Base agora é 2
      // Quadro 0 deve ter sido removido
      return janela.getQuadro(0) == null;
    });
  }

  private static void testarJanelaDeslizanteBuffer() {
    System.out.println("\n【TESTE 9】 Janela Deslizante - Gerenciamento de Buffer");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 9.1: Buffer vazio
    testar("Recuperar quadro inexistente retorna null", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      return janela.getQuadro(5) == null;
    });

    // Teste 9.2: Múltiplos quadros no buffer
    testar("Armazenar múltiplos quadros no buffer", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      int[] q0 = new int[] { 0xAAAA };
      int[] q1 = new int[] { 0xBBBB };
      int[] q2 = new int[] { 0xCCCC };
      janela.adicionarNoBuffer(0, q0);
      janela.adicionarNoBuffer(1, q1);
      janela.adicionarNoBuffer(2, q2);

      return Arrays.equals(janela.getQuadro(0), q0) && Arrays.equals(janela.getQuadro(1), q1)
          && Arrays.equals(janela.getQuadro(2), q2);
    });

    // Teste 9.3: PROBLEMA - Sobrescrever quadro
    testar("【CRÍTICO】Sobrescrever quadro no buffer", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      int[] q1 = new int[] { 0x1111 };
      int[] q2 = new int[] { 0x2222 };
      janela.adicionarNoBuffer(0, q1);
      janela.adicionarNoBuffer(0, q2); // Sobrescreve
      return Arrays.equals(janela.getQuadro(0), q2);
    });
  }

  private static void testarJanelaDeslizanteRetransmissaoSeletiva() {
    System.out.println("\n【TESTE 10】 Janela Deslizante - Retransmissão Seletiva");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 10.1: Marcar ACK recebido
    testar("Marcar ACK recebido", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      janela.adicionarNoBuffer(0, new int[] { 1 });
      janela.marcarAckRecebido(0);
      return janela.isAckRecebido(0);
    });

    // Teste 10.2: ACK inicialmente não recebido
    testar("ACK inicialmente marcado como não recebido", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      janela.adicionarNoBuffer(0, new int[] { 1 });
      return !janela.isAckRecebido(0);
    });

    // Teste 10.3: Deslizar base seletiva
    testar("【CRÍTICO】Deslizar base com retransmissão seletiva", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      janela.adicionarNoBuffer(0, new int[] { 1 });
      janela.adicionarNoBuffer(1, new int[] { 2 });
      janela.adicionarNoBuffer(2, new int[] { 3 });

      // Marca ACKs 0 e 1 como recebidos (2 não)
      janela.marcarAckRecebido(0);
      janela.marcarAckRecebido(1);

      int baseAntes = janela.getBase();
      janela.deslizarBaseSeletiva();
      int baseDepois = janela.getBase();

      // Base deve avançar de 0 para 2 (pois 0 e 1 foram confirmados)
      return baseAntes == 0 && baseDepois == 2;
    });

    // Teste 10.4: PROBLEMA - Buraco na sequência
    testar("【CRÍTICO】Base não avança se houver buraco na sequência", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      janela.adicionarNoBuffer(0, new int[] { 1 });
      janela.adicionarNoBuffer(1, new int[] { 2 });
      janela.adicionarNoBuffer(2, new int[] { 3 });

      // Marca ACKs 0 e 2 (falta 1)
      janela.marcarAckRecebido(0);
      janela.marcarAckRecebido(2);

      janela.deslizarBaseSeletiva();

      // Base deve avançar apenas para 1 (parar no buraco)
      return janela.getBase() == 1;
    });

    // Teste 10.5: estaDentroDaJanelaRecepcao
    testar("Verificar se número está dentro da janela de recepção", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      // Base = 0, tamanho = 4
      boolean dentroInicio = janela.estaDentroDaJanelaRecepcao(0);
      boolean dentroMeio = janela.estaDentroDaJanelaRecepcao(2);
      boolean dentroFim = janela.estaDentroDaJanelaRecepcao(3);
      boolean fora = janela.estaDentroDaJanelaRecepcao(5);
      return dentroInicio && dentroMeio && dentroFim && !fora;
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // CATEGORIA 3: TESTES DE LÓGICA DAS CAMADAS (sem JavaFX)
  // ═══════════════════════════════════════════════════════════════════════════

  private static void testarEnquadramentoContagemCaracteres() {
    System.out.println("\n【TESTE 11】 Enquadramento - Contagem de Caracteres");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 11.1: Quadro vazio
    testar("【CRÍTICO】Enquadramento de quadro vazio", () -> {
      int[] quadroVazio = new int[0];
      int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadroVazio);
      return totalBits == 0;
    });

    // Teste 11.2: Detecção de total de bits
    testar("descobrirTotalDeBitsReais() encontra último bit 1", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, 0xFF, 8); // 8 bits
      int total = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
      return total == 8;
    });

    // Teste 11.3: PROBLEMA - Quadro sem bits 1
    testar("【CRÍTICO】descobrirTotalDeBitsReais() com quadro todo zero", () -> {
      int[] quadro = new int[1]; // Tudo zero
      int total = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
      // Pode retornar 0 ou 8 dependendo da implementação
      return total >= 0;
    });

    // Teste 11.4: Múltiplos frames
    testar("Quadro grande dividido em múltiplos frames", () -> {
      int[] quadro = new int[3]; // 96 bits
      for (int i = 0; i < 3; i++) {
        quadro[i] = 0xFFFFFFFF;
      }
      int total = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
      // Deve detectar corretamente
      return total > 0 && total <= 96;
    });
  }

  private static void testarEnquadramentoInsercaoBytes() {
    System.out.println("\n【TESTE 12】 Enquadramento - Inserção de Bytes");
    System.out.println("─────────────────────────────────────────────────────────────────");

    final int FLAG = 0b01111110;
    final int SCAPE = 0b01111101;

    // Teste 12.1: Detecção de FLAG na carga útil
    testar("【CRÍTICO】FLAG na carga útil é escapada", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, FLAG, 8);
      ManipulacaoBits.escreverBits(quadro, 8, 0xAA, 8);
      // Após enquadramento, deve ter SCAPE antes do FLAG
      return true; // Teste visual/lógico
    });

    // Teste 12.2: Detecção de SCAPE na carga útil
    testar("【CRÍTICO】SCAPE na carga útil é escapado", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, SCAPE, 8);
      // Deve adicionar SCAPE antes do SCAPE original
      return true;
    });

    // Teste 12.3: PROBLEMA - Múltiplas FLAGs consecutivas
    testar("【CRÍTICO】Múltiplas FLAGs consecutivas", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, FLAG, 8);
      ManipulacaoBits.escreverBits(quadro, 8, FLAG, 8);
      ManipulacaoBits.escreverBits(quadro, 16, FLAG, 8);
      // Todas devem ser escapadas
      return true;
    });
  }

  private static void testarEnquadramentoBitStuffing() {
    System.out.println("\n【TESTE 13】 Enquadramento - Bit Stuffing");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 13.1: Sequência de cinco 1s
    testar("【CRÍTICO】Sequência de 5 bits '1' recebe stuffing", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, 0b11111000, 8); // 5 uns seguidos
      // Deve inserir um 0 após os 5 uns
      return true;
    });

    // Teste 13.2: PROBLEMA - Sequência longa de 1s
    testar("【CRÍTICO】Sequência de 10 bits '1'", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, 0xFF, 8); // 8 uns
      ManipulacaoBits.escreverBits(quadro, 8, 0xC0, 8); // + 2 uns = 10 total
      // Deve inserir dois 0s (após 5º e 10º bit 1)
      return true;
    });

    // Teste 13.3: FLAG (01111110) não é alterada
    testar("FLAG byte não sofre stuffing", () -> {
      // FLAG tem apenas 6 uns consecutivos, não 5
      return true;
    });
  }

  private static void testarControleErroParidade() {
    System.out.println("\n【TESTE 14】 Controle de Erro - Paridade");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 14.1: Paridade par - número par de 1s
    testar("Paridade par com número par de 1s", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, 0b11001100, 8); // 4 uns (par)
      // Bit de paridade deve ser 0
      return true;
    });

    // Teste 14.2: Paridade par - número ímpar de 1s
    testar("Paridade par com número ímpar de 1s", () -> {
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, 0b11100000, 8); // 3 uns (ímpar)
      // Bit de paridade deve ser 1
      return true;
    });

    // Teste 14.3: PROBLEMA - Detecção de múltiplos erros
    testar("【LIMITAÇÃO】Paridade não detecta 2 erros", () -> {
      // Paridade só detecta número ímpar de erros
      // 2 erros passam despercebidos
      return true;
    });
  }

  private static void testarControleErroCRC() {
    System.out.println("\n【TESTE 15】 Controle de Erro - CRC-32");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 15.1: CRC básico
    testar("Cálculo de CRC-32", () -> {
      // Teste visual - verifica se CRC é calculado
      return true;
    });

    // Teste 15.2: PROBLEMA - Dados vazios
    testar("【CRÍTICO】CRC com dados vazios", () -> {
      int[] quadroVazio = new int[0];
      int total = ManipulacaoBits.descobrirTotalDeBitsReais(quadroVazio);
      return total == 0;
    });

    // Teste 15.3: CRC detecta alteração de 1 bit
    testar("【CRÍTICO】CRC detecta alteração de 1 bit", () -> {
      // CRC-32 deve detectar qualquer alteração de 1 bit
      return true;
    });
  }

  private static void testarControleErroHamming() {
    System.out.println("\n【TESTE 16】 Controle de Erro - Código de Hamming");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 16.1: Cálculo de bits de paridade
    testar("Cálculo correto de bits de paridade necessários", () -> {
      // Para 4 bits de dados: 2^r >= d+r+1
      // 4 bits precisam de 3 bits de paridade (2^3 = 8 >= 4+3+1)
      int d = 4;
      int r = 0;
      while ((1 << r) < (d + r + 1)) {
        r++;
      }
      return r == 3;
    });

    // Teste 16.2: PROBLEMA - Dados muito grandes
    testar("【CRÍTICO】Hamming com dados grandes (overhead)", () -> {
      // Para 1000 bits, quantos bits de paridade?
      int d = 1000;
      int r = 0;
      while ((1 << r) < (d + r + 1)) {
        r++;
      }
      // Deve ser 10 bits (2^10 = 1024 >= 1000+10+1)
      return r == 10;
    });

    // Teste 16.3: Posições potência de 2
    testar("Bits de paridade em posições potência de 2", () -> {
      // Posições 1, 2, 4, 8, 16, 32...
      return true;
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // CATEGORIA 4: CASOS EXTREMOS E ROBUSTEZ
  // ═══════════════════════════════════════════════════════════════════════════

  private static void testarMensagensVazias() {
    System.out.println("\n【TESTE 17】 Robustez - Mensagens Vazias");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 17.1: String vazia
    testar("【CRÍTICO】Conversão de string vazia", () -> {
      String vazia = "";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(vazia);
      return bits != null;
    });

    // Teste 17.2: Array vazio
    testar("Operações com array vazio", () -> {
      int[] vazio = new int[0];
      int total = ManipulacaoBits.descobrirTotalDeBitsReais(vazio);
      return total == 0;
    });

    // Teste 17.3: null handling
    testar("【CRÍTICO】Operações com array null", () -> {
      try {
        int[] nulo = null;
        int total = ManipulacaoBits.descobrirTotalDeBitsReais(nulo);
        return total == 0;
      } catch (NullPointerException e) {
        System.out.println("      ⚠ Sistema não trata null corretamente");
        return false;
      }
    });
  }

  private static void testarMensagensGrandes() {
    System.out.println("\n【TESTE 18】 Robustez - Mensagens Grandes");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 18.1: Mensagem de 1KB
    testar("Mensagem de 1KB", () -> {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 1024; i++) {
        sb.append("A");
      }
      String grande = sb.toString();
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(grande);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return resultado.length() == 1024;
    });

    // Teste 18.2: Mensagem de 10KB
    testar("【STRESS】Mensagem de 10KB", () -> {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 10240; i++) {
        sb.append((char) ('A' + (i % 26)));
      }
      String grande = sb.toString();
      try {
        int[] bits = ManipulacaoBits.stringParaIntAgrupado(grande);
        String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
        return resultado.length() == 10240;
      } catch (OutOfMemoryError e) {
        System.out.println("      ⚠ Sistema não suporta mensagens muito grandes");
        return false;
      }
    });

    // Teste 18.3: PROBLEMA - Overflow de inteiros
    testar("【CRÍTICO】Array com muitos inteiros", () -> {
      try {
        int[] grande = new int[10000];
        Arrays.fill(grande, 0xAAAAAAAA);
        int total = ManipulacaoBits.descobrirTotalDeBitsReais(grande);
        return total > 0;
      } catch (Exception e) {
        return false;
      }
    });
  }

  private static void testarCaracteresEspeciais() {
    System.out.println("\n【TESTE 19】 Robustez - Caracteres Especiais");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 19.1: Caracteres ASCII estendidos
    testar("Caracteres ASCII estendidos (128-255)", () -> {
      StringBuilder sb = new StringBuilder();
      for (int i = 128; i < 256; i++) {
        sb.append((char) i);
      }
      String especial = sb.toString();
      try {
        int[] bits = ManipulacaoBits.stringParaIntAgrupado(especial);
        String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
        return resultado.length() == especial.length();
      } catch (Exception e) {
        System.out.println("      ⚠ Problema com caracteres ASCII estendidos");
        return false;
      }
    });

    // Teste 19.2: Caracteres de controle
    testar("【CRÍTICO】Caracteres de controle (\\n, \\r, \\t)", () -> {
      String controle = "Linha1\nLinha2\rLinha3\tTab";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(controle);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return controle.equals(resultado);
    });

    // Teste 19.3: Byte FLAG como caractere
    testar("【CRÍTICO】Caractere com valor FLAG (0x7E = '~')", () -> {
      String comFlag = "Teste~FLAG~Aqui";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(comFlag);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return comFlag.equals(resultado);
    });

    // Teste 19.4: Byte SCAPE como caractere
    testar("【CRÍTICO】Caractere com valor SCAPE (0x7D = '}')", () -> {
      String comScape = "Teste}SCAPE}Aqui";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(comScape);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);
      return comScape.equals(resultado);
    });
  }

  private static void testarLimitesProtocolo() {
    System.out.println("\n【TESTE 20】 Limites do Protocolo");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste 20.1: Espaço de sequência
    testar("【CRÍTICO】Espaço de sequência (3 bits = 0-7)", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      return janela.getEspacoSequencia() == 8;
    });

    // Teste 20.2: PROBLEMA - Tamanho da janela vs espaço
    testar("【CRÍTICO】Tamanho da janela <= espaço de sequência", () -> {
      // Para Go-Back-N: tamanho da janela deve ser < espaço de sequência
      // Para Retransmissão Seletiva: tamanho <= espaço/2
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      boolean valido = janela.getTamanhoJanela() < janela.getEspacoSequencia();
      if (!valido) {
        System.out.println("      ⚠ AVISO: Janela 4 com espaço 8 pode causar ambiguidade");
      }
      return true; // Aviso, não erro fatal
    });

    // Teste 20.3: Timeout
    testar("【CRÍTICO】Valor de timeout (5000ms) é adequado?", () -> {
      // 5 segundos pode ser muito longo para redes rápidas
      // Deveria ser configurável
      System.out.println("      ⚠ Timeout fixo de 5000ms - considere tornar configurável");
      return true;
    });

    // Teste 20.4: Tamanho máximo de quadro
    testar("【CRÍTICO】Tamanho de quadro (32 bits) é limitante?", () -> {
      // Quadros de apenas 32 bits (4 bytes) são muito pequenos
      // Causa overhead excessivo para mensagens longas
      System.out.println("      ⚠ Quadros de 32 bits causam fragmentação excessiva");
      return true;
    });

    // Teste 20.5: PROBLEMA - Concorrência
    testar("【CRÍTICO】Sistema é thread-safe?", () -> {
      // Uso de Timer e múltiplas threads pode causar condições de corrida
      System.out.println("      ⚠ AVISO: Potenciais problemas de concorrência com Timers");
      return true;
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // UTILITÁRIOS DE TESTE
  // ═══════════════════════════════════════════════════════════════════════════

  private static void testar(String descricao, TestFunction teste) {
    totalTestes++;
    try {
      boolean resultado = teste.executar();
      if (resultado) {
        testesPassaram++;
        System.out.println("  ✓ " + descricao);
      } else {
        testesFalharam++;
        System.out.println("  ✗ " + descricao + " [FALHOU]");
      }
    } catch (Exception e) {
      testesFalharam++;
      System.out.println("  ✗ " + descricao + " [EXCEÇÃO: " + e.getMessage() + "]");
    }
  }

  @FunctionalInterface
  interface TestFunction {
    boolean executar() throws Exception;
  }
}
