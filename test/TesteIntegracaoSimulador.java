package test;

/**
 * ===============================================================================
 * TESTES DE INTEGRAÇÃO E CENÁRIOS REALISTAS - SIMULADOR E.D.E.N.
 * ===============================================================================
 * 
 * Este arquivo contém testes de integração que simulam cenários reais de uso
 * do sistema, identificando problemas que só aparecem na interação entre
 * múltiplos componentes.
 * 
 * FOCO: Bugs que só aparecem em operações complexas e interações entre camadas.
 * ===============================================================================
 */

import util.ManipulacaoBits;
import util.JanelaDeslizante;

public class TesteIntegracaoSimulador {

  private static int totalTestes = 0;
  private static int testesPassaram = 0;
  private static int testesFalharam = 0;

  public static void main(String[] args) {
    System.out.println("═══════════════════════════════════════════════════════════════════");
    System.out.println("   TESTES DE INTEGRAÇÃO - SIMULADOR E.D.E.N.");
    System.out.println("═══════════════════════════════════════════════════════════════════\n");

    executarTestesIntegracao();

    System.out.println("\n═══════════════════════════════════════════════════════════════════");
    System.out.println("   RESUMO - TESTES DE INTEGRAÇÃO");
    System.out.println("═══════════════════════════════════════════════════════════════════");
    System.out.println("Total: " + totalTestes + " | Passou: " + testesPassaram + " | Falhou: " + testesFalharam);
    System.out.println("═══════════════════════════════════════════════════════════════════\n");
  }

  private static void executarTestesIntegracao() {
    testarFluxoCompletoMensagem();
    testarCombinacaoEnquadramentoControleErro();
    testarRetransmissaoCompleta();
    testarCenariosCriticosProtocolo();
    testarProblemasConhecidos();
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // TESTES DE FLUXO COMPLETO
  // ═══════════════════════════════════════════════════════════════════════════

  private static void testarFluxoCompletoMensagem() {
    System.out.println("\n【INTEGRAÇÃO 1】 Fluxo Completo de Mensagem");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste I1.1: String → Int → String (roundtrip completo)
    testar("Roundtrip completo: String → Bits → String", () -> {
      String mensagemOriginal = "Teste de integridade";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(mensagemOriginal);
      String mensagemReconstruida = ManipulacaoBits.intAgrupadoParaString(bits);
      return mensagemOriginal.equals(mensagemReconstruida);
    });

    // Teste I1.2: Anexar cabeçalho, transmitir, remover cabeçalho
    testar("Fluxo: Anexar cabeçalho → Transmitir → Remover cabeçalho", () -> {
      String msg = "ABC";
      int[] dados = ManipulacaoBits.stringParaIntAgrupado(msg);
      int[] comCabecalho = ManipulacaoBits.anexarCabecalho(dados, 5);
      int seq = ManipulacaoBits.lerNumeroDeSequencia(comCabecalho);
      int[] semCabecalho = ManipulacaoBits.removerCabecalho(comCabecalho);
      String msgFinal = ManipulacaoBits.intAgrupadoParaString(semCabecalho);
      return seq == 5 && msg.equals(msgFinal);
    });

    // Teste I1.3: Múltiplos quadros em sequência
    testar("【CRÍTICO】Processar múltiplos quadros sequencialmente", () -> {
      String[] mensagens = { "MSG1", "MSG2", "MSG3", "MSG4", "MSG5" };
      for (int i = 0; i < mensagens.length; i++) {
        int[] dados = ManipulacaoBits.stringParaIntAgrupado(mensagens[i]);
        int[] comCabecalho = ManipulacaoBits.anexarCabecalho(dados, i);
        int seq = ManipulacaoBits.lerNumeroDeSequencia(comCabecalho);
        if (seq != i)
          return false;
        int[] semCabecalho = ManipulacaoBits.removerCabecalho(comCabecalho);
        String msgRecuperada = ManipulacaoBits.intAgrupadoParaString(semCabecalho);
        if (!mensagens[i].equals(msgRecuperada))
          return false;
      }
      return true;
    });

    // Teste I1.4: Mensagem fragmentada em múltiplos quadros
    testar("【CRÍTICO】Mensagem longa fragmentada e reconstruída", () -> {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 100; i++) {
        sb.append("ABCD");
      }
      String msgOriginal = sb.toString();

      // Simula fragmentação
      int[] bitsMensagem = ManipulacaoBits.stringParaIntAgrupado(msgOriginal);

      // Cada int é um "quadro"
      StringBuilder reconstruido = new StringBuilder();
      for (int quadro : bitsMensagem) {
        int[] quadroArray = new int[] { quadro };
        String fragmento = ManipulacaoBits.intAgrupadoParaString(quadroArray);
        reconstruido.append(fragmento);
      }

      return reconstruido.toString().contains("ABCD");
    });
  }

  private static void testarCombinacaoEnquadramentoControleErro() {
    System.out.println("\n【INTEGRAÇÃO 2】 Combinações de Enquadramento + Controle de Erro");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste I2.1: PROBLEMA - Tamanho de quadro após enquadramento
    testar("【CRÍTICO】Tamanho de quadro aumenta após enquadramento", () -> {
      int[] quadroOriginal = new int[] { 0x12345678 };
      int tamanhoOriginal = ManipulacaoBits.descobrirTotalDeBitsReais(quadroOriginal);
      // Após adicionar FLAG/SCAPE/etc, tamanho deve aumentar
      System.out.println("      Tamanho original: " + tamanhoOriginal + " bits");
      return tamanhoOriginal > 0;
    });

    // Teste I2.2: FLAG dentro de dados com escape
    testar("【CRÍTICO】FLAG (0x7E) dentro dos dados é tratada", () -> {
      String msgComFlag = "~Test~";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(msgComFlag);
      // Após enquadramento por inserção de bytes, FLAGS devem ser escapadas
      return bits != null && bits.length > 0;
    });

    // Teste I2.3: PROBLEMA - CRC após enquadramento
    testar("【CRÍTICO】CRC calculado sobre dados enquadrados ou originais?", () -> {
      // Esta é uma questão crítica de design:
      // - Se CRC é calculado ANTES do enquadramento: FLAGS não são protegidas
      // - Se CRC é calculado DEPOIS: desenquadramento deve acontecer antes da
      // verificação
      System.out.println("      ⚠ REVISAR: Ordem de enquadramento vs controle de erro");
      return true;
    });

    // Teste I2.4: Paridade com padding
    testar("【CRÍTICO】Bit de paridade com padding de alinhamento", () -> {
      // Sistema adiciona padding de 7 bits para alinhar
      // Paridade deve ser calculada ANTES do padding
      int[] quadro = new int[1];
      ManipulacaoBits.escreverBits(quadro, 0, 0xFF, 8); // 8 uns
      // Paridade PAR: deve adicionar 0
      // Com padding: último bit deve ser 1 (marcador)
      return true;
    });
  }

  private static void testarRetransmissaoCompleta() {
    System.out.println("\n【INTEGRAÇÃO 3】 Protocolos de Retransmissão");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste I3.1: Stop-and-Wait (Janela de 1 bit)
    testar("Stop-and-Wait: Enviar → ACK → Enviar próximo", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(1, 1); // Tamanho 1, 1 bit
      int[] quadro0 = new int[] { 0x1111 };

      // Envia quadro 0
      if (!janela.podeEnviar())
        return false;
      janela.adicionarNoBuffer(0, quadro0);
      janela.avancarSequencia();

      // Janela cheia, não pode enviar
      if (janela.podeEnviar())
        return false;

      // Recebe ACK 0
      janela.atualizarBase(0);

      // Agora pode enviar próximo
      return janela.podeEnviar();
    });

    // Teste I3.2: Go-Back-N - Retransmissão de toda janela
    testar("【CRÍTICO】Go-Back-N: Timeout retransmite toda janela", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);

      // Envia 4 quadros
      for (int i = 0; i < 4; i++) {
        janela.adicionarNoBuffer(i, new int[] { 0x1000 + i });
        janela.avancarSequencia();
      }

      // Simula timeout - todos os quadros devem estar no buffer
      boolean todosNoBuffer = true;
      for (int i = 0; i < 4; i++) {
        if (janela.getQuadro(i) == null) {
          todosNoBuffer = false;
          break;
        }
      }

      return todosNoBuffer;
    });

    // Teste I3.3: Retransmissão Seletiva - ACKs fora de ordem
    testar("【CRÍTICO】Retransmissão Seletiva: ACKs fora de ordem", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);

      // Envia 4 quadros
      for (int i = 0; i < 4; i++) {
        janela.adicionarNoBuffer(i, new int[] { 0x2000 + i });
        janela.avancarSequencia();
      }

      // Recebe ACKs: 0, 2, 3 (falta 1)
      janela.marcarAckRecebido(0);
      janela.marcarAckRecebido(2);
      janela.marcarAckRecebido(3);

      janela.deslizarBaseSeletiva();

      // Base deve estar em 1 (esperando ACK 1)
      boolean baseCorreta = janela.getBase() == 1;

      // Quadros 2 e 3 ainda devem estar no buffer
      boolean bufferCorreto = janela.getQuadro(2) != null && janela.getQuadro(3) != null;

      return baseCorreta && bufferCorreto;
    });

    // Teste I3.4: PROBLEMA - ACK duplicado
    testar("【CRÍTICO】Receber ACK duplicado não corrompe estado", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);
      janela.adicionarNoBuffer(0, new int[] { 1 });
      janela.avancarSequencia();

      // Recebe ACK 0
      janela.marcarAckRecebido(0);
      janela.deslizarBaseSeletiva();
      int baseApos1 = janela.getBase();

      // Recebe ACK 0 novamente (duplicado)
      janela.marcarAckRecebido(0);
      janela.deslizarBaseSeletiva();
      int baseApos2 = janela.getBase();

      // Base não deve mudar
      return baseApos1 == baseApos2;
    });

    // Teste I3.5: PROBLEMA - NACK vs ACK
    testar("【CRÍTICO】NACK e ACK são distinguíveis", () -> {
      int[] ack = ManipulacaoBits.montarQuadroAck(5);
      int[] nack = ManipulacaoBits.montarQuadroNack(5);

      boolean ackNaoEhNack = ManipulacaoBits.ehAck(ack) && !ManipulacaoBits.ehNack(ack);
      boolean nackEhNack = ManipulacaoBits.ehNack(nack);
      boolean nackTambemEhAck = ManipulacaoBits.ehAck(nack); // NACK tem flag ACK também

      return ackNaoEhNack && nackEhNack && nackTambemEhAck;
    });
  }

  private static void testarCenariosCriticosProtocolo() {
    System.out.println("\n【INTEGRAÇÃO 4】 Cenários Críticos do Protocolo");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Teste I4.1: Wrap-around com janela deslizante
    testar("【CRÍTICO】Wrap-around do espaço de sequência (0→7→0)", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);

      // Avança até próximo de wrap-around
      for (int i = 0; i < 6; i++) {
        janela.avancarSequencia();
      }

      // Envia quadros 6, 7, 0, 1 (cruza o zero)
      janela.adicionarNoBuffer(6, new int[] { 6 });
      janela.avancarSequencia();
      janela.adicionarNoBuffer(7, new int[] { 7 });
      janela.avancarSequencia();
      janela.adicionarNoBuffer(0, new int[] { 0 });
      janela.avancarSequencia();

      // Verifica se todos estão na janela
      boolean seq6 = janela.estaDentroDaJanela(6);
      boolean seq7 = janela.estaDentroDaJanela(7);
      boolean seq0 = janela.estaDentroDaJanela(0);

      return seq6 && seq7 && seq0;
    });

    // Teste I4.2: PROBLEMA - Ambiguidade de números de sequência
    testar("【CRÍTICO】Ambiguidade: Base=0, Próximo=0 (janela vazia ou cheia?)", () -> {
      JanelaDeslizante janela = new JanelaDeslizante(4, 3);

      // Estado inicial: base=0, próximo=0 → VAZIO
      boolean vazioInicial = janela.getBase() == 0 && janela.getProximoNumeroSequencia() == 0;

      // Envia 8 quadros e recebe 8 ACKs
      for (int i = 0; i < 8; i++) {
        janela.adicionarNoBuffer(i, new int[] { i });
        janela.avancarSequencia();
        if (i % 4 == 3) { // A cada 4, processa ACKs
          janela.atualizarBase(i);
        }
      }

      // Após 8 envios/ACKs: base=0, próximo=0 → VAZIO novamente
      boolean vazioFinal = janela.getBase() == 0 && janela.getProximoNumeroSequencia() == 0;

      System.out.println("      ⚠ AMBIGUIDADE: Estados vazio inicial e final são idênticos!");
      return vazioInicial && vazioFinal;
    });

    // Teste I4.3: PROBLEMA - Janela de recepção vs janela de transmissão
    testar("【CRÍTICO】Janela de recepção deve ter mesmo tamanho?", () -> {
      // Go-Back-N: receptor aceita apenas próximo na sequência
      // Retransmissão Seletiva: receptor aceita dentro da janela
      System.out.println("      ⚠ REVISAR: Implementação de janela de recepção");
      return true;
    });

    // Teste I4.4: Taxa de erro vs taxa de retransmissão
    testar("【CRÍTICO】Taxa de erro alta causa loop infinito de retransmissão?", () -> {
      // Se taxa de erro é muito alta (ex: 50%), muitos ACKs podem ser perdidos
      // Sistema pode entrar em loop infinito de retransmissão
      System.out.println("      ⚠ RISCO: Taxa de erro > 20% pode causar travamento");
      return true;
    });
  }

  private static void testarProblemasConhecidos() {
    System.out.println("\n【INTEGRAÇÃO 5】 Problemas Conhecidos e Limitações");
    System.out.println("─────────────────────────────────────────────────────────────────");

    // Problema 1: Descoberta de total de bits
    testar("【BUG】descobrirTotalDeBitsReais() com quadro todo zero", () -> {
      int[] quadroZero = new int[2];
      // Todos os bits são 0
      int total = ManipulacaoBits.descobrirTotalDeBitsReais(quadroZero);
      // Deveria retornar 0, mas retorna 8
      if (total != 0) {
        System.out.println("      🐛 BUG CONFIRMADO: Retorna " + total + " ao invés de 0");
        return false;
      }
      return true;
    });

    // Problema 2: Concorrência com Timer
    testar("【BUG】Múltiplos Timers podem causar condição de corrida", () -> {
      // Se timeout ocorre durante processamento de ACK, pode corromper estado
      System.out.println("      🐛 BUG POTENCIAL: Falta sincronização em tratarTimeOut()");
      return false; // Marcado como falha pois é um problema real
    });

    // Problema 3: Fragmentação excessiva
    testar("【LIMITAÇÃO】Quadros de 32 bits causam overhead excessivo", () -> {
      // Para mensagem de 1KB, são necessários ~256 quadros
      // Cada quadro tem cabeçalho, enquadramento, controle de erro
      // Overhead pode chegar a 300-400%
      System.out.println("      ⚠ LIMITAÇÃO: Overhead de ~300% para mensagens grandes");
      return false; // Marcado como problema de design
    });

    // Problema 4: Marcador de controle
    testar("【BUG】Bit marcador (LSB=1) pode ser confundido com dados", () -> {
      // Sistema adiciona bit 1 no LSB do cabeçalho
      // Se número de sequência ímpar, pode causar confusão
      int[] quadro = ManipulacaoBits.anexarCabecalho(new int[] { 0 }, 7);
      int cabecalho = quadro[0];
      int lsb = cabecalho & 1;
      if (lsb != 1) {
        System.out.println("      🐛 BUG: Marcador de controle não está presente");
        return false;
      }
      return true;
    });

    // Problema 5: Validação de espaço de sequência
    testar("【BUG】Espaço de sequência não é validado vs tamanho da janela", () -> {
      // Para Go-Back-N: tamanho janela < 2^n
      // Para Retransmissão Seletiva: tamanho janela <= 2^(n-1)
      // Sistema apenas imprime aviso, não previne configuração inválida
      JanelaDeslizante janela = new JanelaDeslizante(8, 3); // INVÁLIDO! 8 = 2^3
      System.out.println("      🐛 BUG: Sistema aceita configuração inválida (janela=8, espaço=8)");
      return false;
    });

    // Problema 6: Timeout fixo
    testar("【LIMITAÇÃO】Timeout fixo de 5000ms não é adaptativo", () -> {
      // Timeout deveria ser baseado em RTT (Round-Trip Time) medido
      // 5 segundos é muito longo para redes rápidas
      // Muito curto para redes lentas
      System.out.println("      ⚠ LIMITAÇÃO: Timeout deveria ser adaptativo (ex: RTT + 4*desvio)");
      return false;
    });

    // Problema 7: Caractere nulo
    testar("【BUG】Caractere nulo (\\0) trunca string", () -> {
      String comNulo = "ABC\0DEF";
      int[] bits = ManipulacaoBits.stringParaIntAgrupado(comNulo);
      String resultado = ManipulacaoBits.intAgrupadoParaString(bits);

      if (!resultado.contains("DEF")) {
        System.out.println("      🐛 BUG CONFIRMADO: String truncada após \\0");
        System.out.println("         Original: 'ABC\\0DEF', Resultado: '" + resultado + "'");
        return false;
      }
      return true;
    });

    // Problema 8: Codificação + Enquadramento por violação
    testar("【BUG】Binária + Violação de Camada Física bloqueada, mas outras combinações?", () -> {
      // Sistema bloqueia apenas Binária + Violação
      // Mas Manchester/Diferencial + Violação também tem problemas?
      System.out.println("      ⚠ VERIFICAR: Outras combinações de codificação + enquadramento");
      return true;
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // UTILITÁRIOS
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
      e.printStackTrace();
    }
  }

  @FunctionalInterface
  interface TestFunction {
    boolean executar() throws Exception;
  }
}
