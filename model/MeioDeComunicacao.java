package model;

import controller.ControlerTelaPrincipal;
import javafx.application.Platform;
import util.ErroDeVerificacaoException;
import util.ManipulacaoBits;
import java.util.Random;

/**
 * Simula a transmissao de forma otimizada em uma unica passada, aplicando o
 * erro de um bit por quadro e exibindo o resultado em um Alert.
 */
public class MeioDeComunicacao {

  private ControlerTelaPrincipal controlerTelaPrincipal;
  private Random random;

  // referencia para todas as "4 camadas fisicas", obs: cada 2 camdas fisicas eh
  // usada para simular uma camdaFisica completa, em outras palavras ele tem a
  // referencia para 2 camadas fisicas a do hostA e do HostB

  private CamadaFisicaReceptora fisicaReceptoraHostA;
  private CamadaFisicaTransmissora fisicaTransmissoraHostA;

  private CamadaFisicaReceptora fisicaReceptoraHostB;
  private CamadaFisicaTransmissora fisicaTransmissoraHostB;

  /**
   * construtor da classe, sabe de onde pra onde a comunicacao flui
   * 
   * @param fisicaTransmissoraHostA referencia a camada fisica do hostA
   * @param fisicaReceptoraHostA    referencia a camada fisica do hostA
   * @param fisicaTransmissoraHostB referencia a camada fisica do hostB
   * @param fisicaReceptoraHostB    referencia a camada fisica do hostB
   * @param controlerTelaPrincipal  referencia ao controle da UI
   */
  public MeioDeComunicacao(CamadaFisicaTransmissora fisicaTransmissoraHostA, CamadaFisicaReceptora fisicaReceptoraHostA,
      CamadaFisicaTransmissora fisicaTransmissoraHostB, CamadaFisicaReceptora fisicaReceptoraHostB,
      ControlerTelaPrincipal controlerTelaPrincipal) {

    // cria as instancias
    this.fisicaTransmissoraHostA = fisicaTransmissoraHostA;
    this.fisicaReceptoraHostA = fisicaReceptoraHostA;
    this.fisicaTransmissoraHostB = fisicaTransmissoraHostB;
    this.fisicaReceptoraHostB = fisicaReceptoraHostB;

    this.controlerTelaPrincipal = controlerTelaPrincipal;
    this.random = new Random();

    // seguranca de que as camdas fisicas saberao o meio de comunicacao usado
    this.fisicaTransmissoraHostA.setMeioDeComunicacao(this);
    this.fisicaReceptoraHostA.setMeioDeComunicacao(this);
    this.fisicaTransmissoraHostB.setMeioDeComunicacao(this);
    this.fisicaReceptoraHostB.setMeioDeComunicacao(this);

  } // fim construtor

  /**
   * metodo principal que simula a transmissao da mensagem
   * 
   * @param fluxoBrutoDeBits fluxoBruto de bits que represneta o sinal codificado
   *                         pela camada anterior
   * @param remetente        que mandou a mensagem
   */
  public void transmitirMensagem(int fluxoBrutoDeBits[], CamadaFisicaTransmissora remetente)
      throws ErroDeVerificacaoException {

    // transferir bits e aplicar erro

    double taxaErro = this.controlerTelaPrincipal.getValorTaxaErro();

    int[] fluxoBrutoDeBitsPontoInicial = fluxoBrutoDeBits;
    int[] fluxoBrutoDeBitsPontoFinal = new int[fluxoBrutoDeBitsPontoInicial.length]; // array de destino tem o mesmo
                                                                                     // tamanho do
    // array partida

    int totalDeBits = ManipulacaoBits.descobrirTotalDeBitsReais(fluxoBrutoDeBitsPontoInicial);
    int tipoDeCodificacao = this.controlerTelaPrincipal.opcaoSelecionada();

    // um strigBuider pra construir o relatorio de erro (debug)
    StringBuilder relatorio = new StringBuilder();
    relatorio.append("Taxa de Erro configurada: ").append(String.format("%.1f%%", taxaErro * 100))
        .append(" por quadro.\n");
    relatorio.append("Iniciando transferência otimizada de ").append(totalDeBits).append(" bits...\n\n");

    // Isso garante que aplica-se APENAS 1 ERRO POR QUADRO
    int tamanhoFisicoDoQuadroEmBits = totalDeBits;

    // Se houver codificacao Manchester ou Diferencial, o tamanho dobra
    if (tipoDeCodificacao == 1 || tipoDeCodificacao == 2) { // Manchester/Diferencial
      tamanhoFisicoDoQuadroEmBits *= 2;
    } // fim do if

    // simulacao da transferencia bit a bit
    int posicaoDoErroNesteQuadro = -1; // -1 significa que nao ha erro planejado
    int contadorDeErros = 0;

    for (int i = 0; i < totalDeBits; i++) {

      // no inicio de cada quadro, decide se havera um erro
      if (tamanhoFisicoDoQuadroEmBits > 0 && i % tamanhoFisicoDoQuadroEmBits == 0) {
        posicaoDoErroNesteQuadro = -1; // reseta o erro do quadro anterior

        // sorteia se o quadro ATUAL tera um erro
        if (random.nextDouble() < taxaErro) {
          // sorteia a POSICAO do erro dentro do quadro
          int bitAleatorioNoQuadro = random.nextInt(tamanhoFisicoDoQuadroEmBits);
          // calcula a posicao global onde o erro acontecera
          posicaoDoErroNesteQuadro = i + bitAleatorioNoQuadro;
          // Garante que o erro nao caia fora do total de bits
          if (posicaoDoErroNesteQuadro >= totalDeBits) {
            posicaoDoErroNesteQuadro = totalDeBits - 1;
          } // fim if
        } // fim if
      } // fim if

      // le o bit do A
      int bit = ManipulacaoBits.lerBits(fluxoBrutoDeBitsPontoInicial, i, 1);

      // verifica se este bit deve ser corrompido
      if (i == posicaoDoErroNesteQuadro) {
        // se for, inverte o bit
        bit = 1 - bit;

        contadorDeErros++;
        relatorio.append("-> Erro inserido no bit de índice: ").append(i).append("\n");
      } // fim if

      // escreve o bit no B
      ManipulacaoBits.escreverBits(fluxoBrutoDeBitsPontoFinal, i, bit, 1);

    } // fim for

    relatorio.append("\nTransferência concluída.");
    relatorio.append("\nTotal de bits corrompidos = " + contadorDeErros);

    System.out.println("--- RELATORIO DO MEIO DE COMUNICACAO (DEBUG) ---");
    System.out.println(relatorio.toString());

    // fim da transferencia com aplicacao de erro, se tiver erro o
    // fluxoBrutoBitsPontoB ja tem eles

    // define quem mandou a mensagem pra saber se foi o hostA e a mensagem tem que
    // ir para B ou se foi o HostB mandando um ACK que tem que ser recebido por A
    if (remetente == this.fisicaTransmissoraHostA) {
      // se foi o hostA que enviou os dados:
      System.out.println("MEIO: Enviando A -> B");

      // faz a animacao
      int totalBitsEnviados = ManipulacaoBits.descobrirTotalDeBitsReais(fluxoBrutoDeBitsPontoFinal);
      // gera o array simplificado para a animacao
      final int[] bitsAnimacao = ManipulacaoBits.desempacotarBits(fluxoBrutoDeBitsPontoFinal, totalBitsEnviados);
      // garante que a animacao seja sempre chamada pela thread de javaFX
      Platform.runLater(() -> {
        this.controlerTelaPrincipal.desenharSinalTransmissao(bitsAnimacao);
      });

      // entrega a mensagem de A para B
      this.fisicaReceptoraHostB.receberQuadro(fluxoBrutoDeBitsPontoFinal);

    } // fim if
    else if (remetente == this.fisicaTransmissoraHostB) {
      // se foi op hostB que enviou algo, entao eh o ACK na nossa simulacao, entrega o
      // ACK para o HostA

      System.out.println("MEIO: Enviando (ACK) B -> A ");

      // nao realiza animacao do quadro de ack (talvez implementar isso no proximo)

      // entrega o ack de B para A
      this.fisicaReceptoraHostA.receberQuadro(fluxoBrutoDeBitsPontoFinal);
    } // fim else/if

  } // fim do MeioComunicacao

} // fim da classe