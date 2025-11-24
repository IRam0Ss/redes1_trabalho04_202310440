package test;

import util.ManipulacaoBits;

/**
 * TESTE - Verificar extracao de numero de sequencia apos correcao Bug 7
 */
public class TesteNumeroSequencia {

  public static void main(String[] args) {
    System.out.println("===============================================================");
    System.out.println("  TESTE - Extracao de Numero de Sequencia");
    System.out.println("  Apos correcao do Bug 7 (adicao do bit 31)");
    System.out.println("===============================================================\n");

    boolean todosPassaram = true;

    // TESTE 1: ACK com seq=0
    System.out.println("Teste 1: ACK com numero de sequencia 0");
    int[] ack0 = ManipulacaoBits.montarQuadroAck(0);
    int seqLida0 = ManipulacaoBits.lerNumeroDeSequencia(ack0);

    System.out.println("  ACK[0] em binario: " + Integer.toBinaryString(ack0[0]));
    System.out.println("  ACK[0] em decimal: " + ack0[0]);
    System.out.println("  Seq esperada: 0");
    System.out.println("  Seq lida: " + seqLida0);

    if (seqLida0 == 0) {
      System.out.println("  Status: OK PASSOU\n");
    } else {
      System.out.println("  Status: X FALHOU\n");
      todosPassaram = false;
    }

    // TESTE 2: ACK com seq=5
    System.out.println("Teste 2: ACK com numero de sequencia 5");
    int[] ack5 = ManipulacaoBits.montarQuadroAck(5);
    int seqLida5 = ManipulacaoBits.lerNumeroDeSequencia(ack5);

    System.out.println("  ACK[0] em binario: " + Integer.toBinaryString(ack5[0]));
    System.out.println("  ACK[0] em decimal: " + ack5[0]);
    System.out.println("  Seq esperada: 5");
    System.out.println("  Seq lida: " + seqLida5);

    if (seqLida5 == 5) {
      System.out.println("  Status: OK PASSOU\n");
    } else {
      System.out.println("  Status: X FALHOU\n");
      todosPassaram = false;
    }

    // TESTE 3: NACK com seq=3
    System.out.println("Teste 3: NACK com numero de sequencia 3");
    int[] nack3 = ManipulacaoBits.montarQuadroNack(3);
    int seqLida3 = ManipulacaoBits.lerNumeroDeSequencia(nack3);

    System.out.println("  NACK[0] em binario: " + Integer.toBinaryString(nack3[0]));
    System.out.println("  NACK[0] em decimal: " + nack3[0]);
    System.out.println("  Seq esperada: 3");
    System.out.println("  Seq lida: " + seqLida3);

    if (seqLida3 == 3) {
      System.out.println("  Status: OK PASSOU\n");
    } else {
      System.out.println("  Status: X FALHOU\n");
      todosPassaram = false;
    }

    // TESTE 4: Todos os numeros de sequencia 0-7
    System.out.println("Teste 4: Todos os numeros de sequencia (0-7)");
    boolean todosOk = true;
    for (int seq = 0; seq <= 7; seq++) {
      int[] ack = ManipulacaoBits.montarQuadroAck(seq);
      int seqLida = ManipulacaoBits.lerNumeroDeSequencia(ack);

      if (seqLida != seq) {
        System.out.println("  FALHOU: seq=" + seq + " lida como " + seqLida);
        todosOk = false;
      }
    }

    if (todosOk) {
      System.out.println("  Todas as sequencias (0-7) foram lidas corretamente");
      System.out.println("  Status: OK PASSOU\n");
    } else {
      System.out.println("  Status: X FALHOU\n");
      todosPassaram = false;
    }

    // TESTE 5: Verificar ehAck() com cada sequencia
    System.out.println("Teste 5: Verificar ehAck() para todas sequencias");
    boolean todosAck = true;
    for (int seq = 0; seq <= 7; seq++) {
      int[] ack = ManipulacaoBits.montarQuadroAck(seq);
      if (!ManipulacaoBits.ehAck(ack)) {
        System.out.println("  FALHOU: ACK seq=" + seq + " nao foi reconhecido como ACK");
        todosAck = false;
      }
    }

    if (todosAck) {
      System.out.println("  Todos os ACKs (0-7) foram reconhecidos corretamente");
      System.out.println("  Status: OK PASSOU\n");
    } else {
      System.out.println("  Status: X FALHOU\n");
      todosPassaram = false;
    }

    // Resultado final
    System.out.println("===============================================================");
    if (todosPassaram) {
      System.out.println("  RESULTADO: Todos os 5 testes PASSARAM!");
      System.out.println("  Extracao de numero de sequencia funciona corretamente!");
      System.out.println("  Bug 7 CORRIGIDO completamente!");
    } else {
      System.out.println("  RESULTADO: Alguns testes FALHARAM!");
      System.out.println("  Problema na extracao de numero de sequencia!");
    }
    System.out.println("===============================================================");
  }
}
