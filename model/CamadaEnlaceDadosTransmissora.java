package model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import controller.ControlerTelaPrincipal;
import util.ErroDeVerificacaoException;
import util.ManipulacaoBits;

/**
 * classe responsavel por separar o quadro em subquadros aplicando os algoritmos
 * responsaveis pelo enquadramento a escolha do usuario
 */
public class CamadaEnlaceDadosTransmissora {

  private CamadaFisicaTransmissora camadaFisicaTransmissora;
  private ControlerTelaPrincipal controlerTelaPrincipal;

  // constantes do protocolo de ACK e Temporizador, STOP ADN WAIT
  // fiz stop and wasit porque eh oq mais se aproxima do janela dezlizante de 1
  // bit

  // tempo de espera escolhido arbitrariamente em um mundo real esse
  // tempo seria calculado com base na larghura de banda e velocidade de
  // transmissao do fio
  private final int TIMEOUT_MILISEGUNDOS = 3000;

  // maquina de estados
  private volatile String estado = "PRONTO_PARA_ENVIAR"; // estado varia entre PRONTO_PARA_ENVIAR e ESPERANDO_ACK

  // fila de envio e quadro em espera
  private Queue<int[]> filaDeEnvio = new LinkedList<>();
  private int[] quadroEmEspera;

  // timer
  private Timer timer;

  /**
   * construtor da classe
   * 
   * @param camadaFisicaTransmissora camada imediatamente abaixo
   * @param controlerTelaPrincipal   contorle da interface
   */
  public CamadaEnlaceDadosTransmissora(CamadaFisicaTransmissora camadaFisicaTransmissora,
      ControlerTelaPrincipal controlerTelaPrincipal) {
    this.camadaFisicaTransmissora = camadaFisicaTransmissora;
    this.controlerTelaPrincipal = controlerTelaPrincipal;
  } // fim contrutor

  /**
   * metodo que envia o quadro para a proxima camada da rede apos aplicar o
   * enquadramaneto e controle de erro selecionado
   * 
   * @param quadro mensagem em bits recebida pela camada anterior
   */
  public void transmitirQuadro(int[] quadro) throws ErroDeVerificacaoException {

    // debug
    System.out.println("Camada de Enlace TX: Recebi " + quadro.length + " inteiros para transmitir.");

    // limpa a fila de transmissao anteriores e cancela os timers. Se clicar 2 vezes
    // em transmitir pra resetar tudo
    cancelarTimer();
    filaDeEnvio.clear();
    estado = "PRONTO_PARA_ENVIAR";

    // trata cada int ou seja cada 32 bits de carga util como sendo um subquadro
    for (int i = 0; i < quadro.length; i++) {

      // verifica se o 'int' contem dados validos antes de processar
      int totalBitsNoInt = ManipulacaoBits.descobrirTotalDeBitsReais(new int[] {
          quadro[i] });
      if (totalBitsNoInt == 0) {
        continue; // pular 'ints' de padding (vazios)
      } // fim if

      int[] subQuadro = new int[] { quadro[i] }; // o subquadro eh de 1 int ou seja, ate 32 bits de carga util

      // debug
      System.out.println("Enlace TX: Processando sub-quadro " + i);

      // aplica enquadramento no subquadro.
      int[] quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramento(subQuadro);
      // aplica controle de erro
      int[] quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErro(quadroEnquadrado);

      // Aplica controle de fluxo
      filaDeEnvio.add(quadroComControleDeErro);
    } // fim for

    // agora a fila tem todos os quadros que precisam ser enviados
    // inicia o controle de fluxo, usando a logica StopAndWait
    System.out.println("Enlace TX: Fila preenchida. Iniciando motor Stop-and-Wait...");
    CamadaEnlaceDadosTransmissoraControleDeFluxo();

  }// fim e transmitirQuadro

  /**
   * metodo paralelo que envia o ACK sem passar pelo controle de fluxo
   * 
   * @param quadro
   */
  public void transmitirACK(int[] quadro) throws ErroDeVerificacaoException {
    System.out.println("ENLACE DADOS TRANSMISSORA: enviando ACK");

    // trata Ack como um unico subquadro
    if (quadro.length == 0) {
      return; // quadro vazio nao faz nada
    } // fim, if

    int[] quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramento(quadro);
    int[] quadroComControleErro = CamadaEnlaceDadosTransmissoraControleDeErro(quadroEnquadrado);

    // envia diretamente para evitar loop, nao faz sentido ficar esperando um ack
    // para um ack
    this.camadaFisicaTransmissora.transmitirQuadro(quadroComControleErro);
  } // fim do transmitirACK

  /**
   * metodo que escolhe o tipo de enquadramento a ser aplicado na mensagem
   * 
   * @param quadro mensagem na forma binaria, o array de int, ja com os bits
   *               armazenados recebido da camada anterior
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramento(int quadro[]) {

    int tipoDeEnquadramento = this.controlerTelaPrincipal.opcaoEnquadramentoSelecionada();
    int quadroEnquadrado[] = null;
    switch (tipoDeEnquadramento) {
      case 0: // contagem de caracteres
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: // insercao de bytes
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: // insercao de bits
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: // violacao da camada fisica
        quadroEnquadrado = CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(quadro);
        break;
    }// fim do switch/case

    return quadroEnquadrado; // retorna o quadro ja enquadrado

  }// fim do metodo CamadaEnlaceDadosTransmissoraEnquadramentos

  /**
   * metodo que escolhe o tipo de controle de erro a ser aplicado na mensagem
   * 
   * @param quadro mensagem ja com o enquadramento
   * @return mensagem com o controle de erro aplicado
   */
  public int[] CamadaEnlaceDadosTransmissoraControleDeErro(int quadro[]) {

    int tipoDeControleDeErro = this.controlerTelaPrincipal.opcaoControleErroSelecionada();
    int quadroComControleDeErro[] = null;
    switch (tipoDeControleDeErro) {
      case 0: // paridade par
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(quadro);
        break;
      case 1: // paridade impar
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 2: // CRC
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroCRC(quadro);
        break;
      case 3: // Hamming
        quadroComControleDeErro = CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(quadro);
        break;
    }// fim do switch/case

    return quadroComControleDeErro; // retorna o quadro ja com controle de erro aplicado

  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErro

  /**
   * metodo de controle de fluxo, controla a logica de ack e retransmissao
   * 
   * @throws ErroDeVerificacaoException trata os erros 
   */
  public void CamadaEnlaceDadosTransmissoraControleDeFluxo() throws ErroDeVerificacaoException {

    enviarProximoDaFila();

  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeFluxo

  /**
   * metodo que funciona como a logica basica do Stop and Wait, analisa a fila e
   * envia os quadros em ordem
   * Chamado pelo controle de Fluxo
   */
  private synchronized void enviarProximoDaFila() throws ErroDeVerificacaoException {

    if (estado.equals("PRONTO_PARA_ENVIAR") && !filaDeEnvio.isEmpty()) {
      // se tem quadros a serem enviados e a camada do transmissor ta pronta para
      // enviar quadros
      this.quadroEmEspera = filaDeEnvio.poll(); // pega o primeiro da fila
      this.estado = "ESPERANDO_ACK"; // muda o estado do transmissor para esperar o ACK

      System.out.println("CAMADA DE ENLACE TX: Enviando o proximo da fila ... ");
      this.camadaFisicaTransmissora.transmitirQuadro(quadroEmEspera); // envia o quadro
      iniciarTimer();
    } else if (estado.equals("PRONTO_PARA_ENVIAR") && filaDeEnvio.isEmpty()) {
      // se a fila ta vazia e ta pronto pra enviar, a transmissao acabou
      System.out.println("ENLACE TX: Fila de envio vazia. Transmissão concluída.");
    } // fim else/if

  } // fim metodo enviarProximoDaFila

  /**
   * metodo responsavel por inicializar um timer, start numa thread que espera o
   * tempo ate a chegada do ACk de confirmacao e chama tratarTimeOut caso o tempo
   * acabe
   */
  private void iniciarTimer() throws ErroDeVerificacaoException {
    cancelarTimer(); // finaliza qualquer timerr anterior
    timer = new Timer(); // cria um timer
    timer.schedule(new TimerTask() {
      @Override
      public void run() { // cria a thread que vai controlar o tempo de espera para recebimento do ACK
        try {
          tratarTimeOut();
        } catch (ErroDeVerificacaoException e) {
          e.printStackTrace();
        } // trata oque acontece quanmdo o tempo acabar
      }
    }, TIMEOUT_MILISEGUNDOS);
  } // fim do iniciarTimer

  /**
   * metodo que cancela o timer atual e o reseta
   */
  private void cancelarTimer() {
    if (timer != null) { // se existe um timer, cancele ele
      timer.cancel();
      timer = null;
    } // fim if
  }// fim metodo cancelarTimer

  /**
   * quando o tempo do timer acaba, ele reenvia o quadro pois o ack nao chegou e
   * reinicia um novo timer
   */
  private synchronized void tratarTimeOut() throws ErroDeVerificacaoException {
    if (!estado.equals("ESPERANDO_ACK")) {
      return; // se nao esta esperando o ACK entao nao faz nada, seguranca
    } // fim if

    System.out.println("ENLACE TRANSMISSORA: TIMEOUT!! tempo de espera pelo ack acabou, reenviuando quadro ... ");
    this.camadaFisicaTransmissora.transmitirQuadro(this.quadroEmEspera);
    iniciarTimer(); // recomeca o timer
  }// fim metodo

  /**
   * metodo que realiza o enquadramento por contagem de caracteres
   * 
   * @param quadro quadro original a ser enquadrado
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(int quadro[]) {
    // descobre o tamanho real dos dados, ignorando o lixo.
    int totalDeBitsReais = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
    if (totalDeBitsReais == 0)
      return new int[0];

    final int TAMANHO_MAX_CARGA_UTIL_EM_BITS = 32; // A carga util sera de ATE 4 bytes
    final int TAMANHO_CABECALHO_EM_BITS = 8;

    // Para calcular o tamanho final, precisamos simular a criacao dos quadros
    int tamanhoTotalEstimadoEmBits = 0;
    for (int i = 0; i < totalDeBitsReais; i += TAMANHO_MAX_CARGA_UTIL_EM_BITS) {
      int bitsNesteFrame = Math.min(TAMANHO_MAX_CARGA_UTIL_EM_BITS, totalDeBitsReais - i);
      tamanhoTotalEstimadoEmBits += TAMANHO_CABECALHO_EM_BITS + bitsNesteFrame;
    }

    int[] quadroEnquadrado = new int[(tamanhoTotalEstimadoEmBits + 31) / 32];
    int bitEscritaGlobal = 0;
    int bitLeituraGlobal = 0;

    while (bitLeituraGlobal < totalDeBitsReais) {
      // 1. Calcula o tamanho da carga util para ESTE frame
      int bitsParaLer = Math.min(TAMANHO_MAX_CARGA_UTIL_EM_BITS, totalDeBitsReais - bitLeituraGlobal);
      int cargaUtil = ManipulacaoBits.lerBits(quadro, bitLeituraGlobal, bitsParaLer);
      bitLeituraGlobal += bitsParaLer;

      // 2. Calcula o valor do cabecalho para ESTE frame
      // O valor eh o numero de bytes da carga util + 1 (o proprio cabecalho)
      int valorDoCabecalho = (bitsParaLer / 8) + 1;

      // 3. Escreve o cabecalho (8 bits)
      ManipulacaoBits.escreverBits(quadroEnquadrado, bitEscritaGlobal, valorDoCabecalho, TAMANHO_CABECALHO_EM_BITS);
      bitEscritaGlobal += TAMANHO_CABECALHO_EM_BITS;

      // 4. Escreve a carga util (APENAS os bits lidos)
      ManipulacaoBits.escreverBits(quadroEnquadrado, bitEscritaGlobal, cargaUtil, bitsParaLer);
      bitEscritaGlobal += bitsParaLer;
    }

    return quadroEnquadrado;
  } // fim de CamadaEnlaceDadosTransmissoraContagemCaractere

  /**
   * metodo para realizar o enquadramento por insercao de bytes
   * 
   * @param quadro quadro original a ser enquadrado
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(int quadro[]) {

    final int FLAG = 0b01111110; // valor do byte de flag, equivale a 126 em decimal, em ASCII eh o '~'
    final int SCAPE = 0b01111101; // valor do byte de escape, equivale a 125 em decimal, em ASCII eh o '}'
    final int TAMANHO_SUBQUADRO_EM_BYTES = 4; // define que a cada quntos bytes sera adicionado uma flag

    int contadorBytesCargaUtilQuadro = 0; // contador de quantos bytes de carga util tem na mensagem
    int contadorBytesEnquadrados = 0; // contador para enquadrar os bits da mensagem

    boolean fimDados = false; // controle se a carga util acabou
    for (int inteiroAgrupado : quadro) {
      if (fimDados)
        break; // se acabou a carga util sai do loop

      for (int i = 0; i < 4; i++) { // percorre os 4 bytes do inteiro e contabiliza os validos
        int umByte = (inteiroAgrupado >> (24 - i * 8)) & 0xFF; // pega 1 byte do inteiro, comecando do mais a direita
                                                               // ate o mais a esquerda
        if (umByte == 0) { // se um byte for 00000000 significa que a acabou as cargas uteis daquele
                           // inteiro, possivelmente da mensagem
          fimDados = true;
          break;
        } // fim do if
        contadorBytesCargaUtilQuadro++; // se chegou ate aqui, significa que o byte eh uma carga util.
      } // fim do for
    } // fim do for

    if (contadorBytesCargaUtilQuadro > 0) { // tem ao menos 1 carga util na mensagem
      contadorBytesEnquadrados = 1; // comeca com a flag inicial
      int contadorFlagIntermediaria = 0;

      for (int i = 0; i < contadorBytesCargaUtilQuadro; i++) { // percorre todas as cargas uteis do quadro
                                                               // contabilizando quantas flags serao necessarias
        // extrair o byte novamente
        int indiceInteiro = i / 4;
        int posicaoNoInteiro = i % 4;

        int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

        if (umByte == FLAG || umByte == SCAPE) {
          contadorBytesEnquadrados += 2; // Adiciona SCAPE + byte
        } else {
          contadorBytesEnquadrados += 1; // Adiciona o byte
        }

        contadorFlagIntermediaria++; // conta quantos bytes ja foram lidos

        if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) { // quando chega no byte que tem que adicionar a
                                                                       // flag ele adiciona e 0 o contador
          contadorBytesEnquadrados++; // Adiciona a FLAG intermediária
          contadorFlagIntermediaria = 0;
        } // fim do if

        // Se a mensagem não terminou exatamente em um bloco de 4, adiciona a FLAG
        // final.
        if (contadorFlagIntermediaria != 0) {
          contadorBytesEnquadrados++;
        } // fim do if

      } // fim for

    } // fim if

    // Se não havia dados, o quadro terá 0 bytes.
    if (contadorBytesEnquadrados == 0) {
      System.out.println("MENSAGEM VAZIA");
      return new int[0];
    } // fim do if

    // agora sim cria o array final com os tamanhos corretos e preenche com as FLAGS
    // e as Cargas uteis

    int tamanhoArrayFinal = (contadorBytesEnquadrados + 3) / 4; // usa o tamanho encontrado arredondando para cima
    int[] quadroEnquadrado = new int[tamanhoArrayFinal];

    int indiceBitDestino = 0; // controla a posicao inicial do proximoBit a ser escrito

    // escreve a FLAG inicial
    ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
    indiceBitDestino += 8;

    int contadorFlagIntermediaria = 0;
    // percorre todos os bytes de carga util adicionando as FLAGS e SCAPS
    // necessarios
    for (int i = 0; i < contadorBytesCargaUtilQuadro; i++) {
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF; // extrai o byte(8bits) para comparar
                                                                                  // se precisa de Scape

      if (umByte == FLAG || umByte == SCAPE) {
        // Escreve o SCAPE (8 bits).
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, SCAPE, 8);
        indiceBitDestino += 8;

        // Escreve o byte original (8 bits).
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, umByte, 8);
        indiceBitDestino += 8;
      } else {
        // Escreve o byte normal (8 bits).
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, umByte, 8);
        indiceBitDestino += 8;
      } // fim do if/else

      contadorFlagIntermediaria++;
      if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) {
        // escreve a flag intermediaria
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
        indiceBitDestino += 8;

        contadorFlagIntermediaria = 0;
      } // fim if

    } // fim for

    if (contadorFlagIntermediaria != 0) { // caso nao tenha acabado um multiplo de 4
      // Escreve a FLAG final (8 bits).
      ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);

    } // fim if

    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBytes

  /**
   * metodo para realizar o enquadramento por insercao de bit
   * 
   * @param quadro quadro original a ser enquadrado
   * @return o quadro ja enquadrado
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(int quadro[]) {

    final int FLAG = 0b01111110; // valor do byte de flag, equivale a 126 em decimal, em ASCII eh o '~'

    final int TAMANHO_SUBQUADRO_EM_BYTES = 4; // a cada 32 bits lidos, ou seja 4 bytes, adiciona a flag de divisao

    // calcular o tamanho da carga util do quadro
    int contadorBytesCargaUtil = 0;
    boolean fimDados = false;
    for (int inteiroAgrupado : quadro) {
      if (fimDados)
        break;
      for (int i = 0; i < 4; i++) {
        int umByte = (inteiroAgrupado >> (24 - i * 8)) & 0xFF;
        if (umByte == 0) {
          fimDados = true;
          break;
        } // fim if
        contadorBytesCargaUtil++;
      } // fim for
    } // fim for

    if (contadorBytesCargaUtil == 0) {
      System.out.println("MENSAGEM VAZIA");
      return new int[0];
    } // fim if

    // calcula o total de bits que o quadro enquadrado vai ter
    int contadorBitsEnquadrados = 8; // comeca com 8 bits para as FLAG de inicio
    int contadorBitsUm = 0; // contador para a sequencia de bits 1
    int contadorFlagIntermediaria = 0; // conta quando colocar a flag intermediaria no quadro

    // percorre apenas os bytes de carga util, bit a bit, para simular o stuffing
    for (int i = 0; i < contadorBytesCargaUtil; i++) {
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      for (int j = 7; j >= 0; j--) { // percorre os 8 bits do byte
        int bitAtual = (umByte >> j) & 1;
        contadorBitsEnquadrados++; // conta o bit de dado

        if (bitAtual == 1) {
          contadorBitsUm++;
          if (contadorBitsUm == 5) {
            contadorBitsEnquadrados++; // conta o bit '0' de stuffing
            contadorBitsUm = 0;
          }
        } else {
          contadorBitsUm = 0;
        } // fim do if / else
      } // fim do for

      contadorFlagIntermediaria++;

      if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) {
        contadorBitsEnquadrados += 8; // adiciona os 8 bits da flag
        contadorFlagIntermediaria = 0;
      }

    } // fim do for

    if (contadorFlagIntermediaria != 0) { // caso nao tenha finalizado com flag adiciona a final
      contadorBitsEnquadrados += 8;
    }

    // cria o array final com o tamanho exato calculado
    int tamanhoArrayFinal = (contadorBitsEnquadrados + 31) / 32;
    int[] quadroEnquadrado = new int[tamanhoArrayFinal];

    // indice para controlar a posicao do proximo bit a ser escrito
    int indiceBitDestino = 0;
    contadorFlagIntermediaria = 0;

    // escreve a FLAG inicial (8 bits)
    ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
    indiceBitDestino += 8;

    contadorBitsUm = 0; // reseta o contador para a escrita
    // percorre os bytes de carga util novamente para preencher o quadro
    for (int i = 0; i < contadorBytesCargaUtil; i++) {
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int umByte = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      for (int j = 7; j >= 0; j--) {
        int bitAtual = (umByte >> j) & 1;

        // escreve o bit de dado atual
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, bitAtual, 1);
        indiceBitDestino++;

        if (bitAtual == 1) {
          contadorBitsUm++;
          if (contadorBitsUm == 5) {
            // escreve o bit '0' de stuffing
            ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, 0, 1);
            indiceBitDestino++;
            contadorBitsUm = 0;
          } // fim if
        } else {
          contadorBitsUm = 0;
        } // fim if /else
      } // fim for

      contadorFlagIntermediaria++;

      if (contadorFlagIntermediaria == TAMANHO_SUBQUADRO_EM_BYTES) { // escreve a flag intermediaria
        ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
        indiceBitDestino += 8;
        contadorFlagIntermediaria = 0;
      } // fim if

    } // fim for

    // escreve a FLAG final (8 bits) se nao tiver acabado com flag
    if (contadorFlagIntermediaria != 0) {
      ManipulacaoBits.escreverBits(quadroEnquadrado, indiceBitDestino, FLAG, 8);
    } // fim if

    return quadroEnquadrado;
  }// fim do metodo CamadaEnlaceDadosTransmissoraInsercaoDeBits

  /**
   * passa o quadro para a proxima camada, violando a logica de responsabilidades
   * transferindo para a camada fisica a responsabilidade de enquadrar. Por isso
   * Violacao da Camada Fisica
   * 
   * @param quadro quadro a ser enquadrado
   * @return o mesmo quadro
   */
  public int[] CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDaCamadaFisica(int quadro[]) {

    // passa o quadro recebido para a camada fisica e ela se responsabiliza por
    // enquadrar com os sinais de violacao 11, no inicio e no fim
    return quadro;
  }// fim metodo CamadaEnlaceDadosTransmissoraEnquadramentoViolacaoDeCamadaFisica

  /**
   * metodo que aplica o controle de erro por bit de paridade par
   * 
   * @param quadro quadro original a ser aplicado o controle de erro
   * @return quadro com o bit de paridade anexado
   */
  public int[] CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(int quadro[]) {

    int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);

    if (totalBits == 0) {
      return quadro;
    }

    // conta a quantidade de bits 1 no quadro
    int contadorUns = 0;

    for (int i = 0; i < totalBits; i++) {
      if (ManipulacaoBits.lerBits(quadro, i, 1) == 1) {
        contadorUns++;
      }
    } // fim for

    // calcula o bit de paridade necessario
    int bitDeParidade;
    if (contadorUns % 2 == 0) {
      bitDeParidade = 0; // ja eh par
    } else {
      bitDeParidade = 1; // precisa adicionar 1 para ficar par
    }

    int novoTotalBits = totalBits + 1;
    int novoTotalBitsAlinhado = (novoTotalBits + 7) / 8 * 8; // alinha para o proximo byte

    // o + 7 garante padding para alhinhar o bit de paridade mesmo se for 0

    int tamanhoArrayFinal = (novoTotalBitsAlinhado + 31) / 32;
    int[] quadroComParidade = new int[tamanhoArrayFinal];

    // copia a carga do quadro para o quadro verificado
    for (int i = 0; i < totalBits; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      ManipulacaoBits.escreverBits(quadroComParidade, i, bitAtual, 1);
    } // fim for

    // adiciona o bit de paridade no final
    ManipulacaoBits.escreverBits(quadroComParidade, totalBits, bitDeParidade, 1);

    // Adiciona um "bit marcador" 1 no final do quadro arredondado
    // Isso garante que descobrirTotalDeBitsReais() no receptor funcione.
    // O receptor ja ignora esse ultimo 7 bits
    ManipulacaoBits.escreverBits(quadroComParidade, novoTotalBitsAlinhado - 1, 1, 1);

    return quadroComParidade;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroBitParidadePar

  /**
   * metodo que aplica o controle de erro por bit de paridade impar
   * 
   * @param quadro quadro original a ser aplicado o controle de erro
   * @return quadro com o bit de paridade anexado
   */
  public int[] CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(int quadro[]) {

    int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);

    if (totalBits == 0) {
      return quadro;
    }

    // conta a quantidade de bits 1 no quadro
    int contadorUns = 0;

    for (int i = 0; i < totalBits; i++) {
      if (ManipulacaoBits.lerBits(quadro, i, 1) == 1) {
        contadorUns++;
      }
    } // fim for

    // calcula o bit de paridade necessario
    int bitDeParidade;
    if (contadorUns % 2 == 0) {
      bitDeParidade = 1; // precisa adicionar o 1 para ficar impar
    } else {
      bitDeParidade = 0; // ja eh impar
    }

    int novoTotalBits = totalBits + 1;
    int novoTotalBitsAlinhado = (novoTotalBits + 7) / 8 * 8; // alinha para o proximo byte

    // o + 7 garante padding para alhinhar o bit de paridade mesmo se for 0

    int tamanhoArrayFinal = (novoTotalBitsAlinhado + 31) / 32;
    int[] quadroComParidade = new int[tamanhoArrayFinal];

    // copia a carga do quadro para o quadro verificado
    for (int i = 0; i < totalBits; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      ManipulacaoBits.escreverBits(quadroComParidade, i, bitAtual, 1);
    } // fim for

    // adiciona o bit de paridade no final
    ManipulacaoBits.escreverBits(quadroComParidade, totalBits, bitDeParidade, 1);

    // Adiciona um "bit marcador" 1 no final do quadro arredondado
    // Isso garante que descobrirTotalDeBitsReais() no receptor funcione.
    // O receptor ja ignora esse ultimo 7 bits
    ManipulacaoBits.escreverBits(quadroComParidade, novoTotalBitsAlinhado - 1, 1, 1);

    return quadroComParidade;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar

  /**
   * metodo que aplica o controle de erro com o polinomio CRC32
   * 
   * @param quadro quadro original a ser aplicado controle de erro
   * @return quadro com controle de erro aplicado
   */
  public int[] CamadaEnlaceDadosTransmissoraControleDeErroCRC(int quadro[]) {

    int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);

    final int POLINOMIO_GERADOR = 0x04C11DB7; // Polinomio CRC-32
    final int VALOR_INICIAL = 0xFFFFFFFF; // Valor inicial do registrador CRC
    final int VALOR_FINAL_XOR = 0xFFFFFFFF; // Valor final para XOR, adicionar o CRC correto no quadro

    int registradorCRC = VALOR_INICIAL;

    // processamendo dos bits do quadro
    for (int i = 0; i < totalBits; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1); // lê o bit atual
      int bitMaisSignificativo = (registradorCRC >> 31) & 1; // obtém o bit mais significativo do registrador CRC

      int xorBit = bitMaisSignificativo ^ bitAtual; // calcula o bit de XOR

      registradorCRC = registradorCRC << 1; // desloca o registrador para a esquerda

      if (xorBit == 1) {
        registradorCRC = registradorCRC ^ POLINOMIO_GERADOR; // aplica o polinomio gerador
      } // fim do if

    } // fim do for

    // processamento dos 32 bits de 0 adicionais
    for (int i = 0; i < 32; i++) {

      int bitAtual = 0; // bits adicionais sao 0
      int bitMaisSignificativo = (registradorCRC >> 31) & 1; // obtém o bit mais significativo do registrador CRC

      int xorBit = bitMaisSignificativo ^ bitAtual; // calcula o bit de XOR

      registradorCRC = registradorCRC << 1; // desloca o registrador para a esquerda

      if (xorBit == 1) {
        registradorCRC = registradorCRC ^ POLINOMIO_GERADOR; // aplica o polinomio gerador
      } // fim do if

    } // fim do for

    int crcFinal = registradorCRC ^ VALOR_FINAL_XOR; // valor final do CRC apos o XOR final

    // cria o novo quadro com o CRC anexado
    int novoTotalBits = totalBits + 32;
    int tamanhoArrayFinal = (novoTotalBits + 31) / 32;
    int[] quadroComCRC = new int[tamanhoArrayFinal];

    // copia os dados originais para o inicio do novo quadro
    for (int i = 0; i < totalBits; i++) {
      int bitAtual = ManipulacaoBits.lerBits(quadro, i, 1);
      ManipulacaoBits.escreverBits(quadroComCRC, i, bitAtual, 1);
    } // fim for

    // anexa o CRC no final do quadro
    ManipulacaoBits.escreverBits(quadroComCRC, totalBits, crcFinal, 32);

    return quadroComCRC;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroCRC

  /**
   * metodo que aplica o controle de erro com o codigo de Hamming
   * 
   * @param quadro quadro original a ser aplicado o controle
   * @return quadro com o controle de erro aplicado
   */
  public int[] CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(int quadro[]) {

    int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);

    if (totalBits == 0) { // caso nao tenha bits validos, retorna quadro com 0
      return new int[0];
    }

    // descobrir quantos bits de paridade serao necessarios
    // formula 2^r >= (d+r+1); onde r -> quantidade de bits de paridade e d-> total
    // de bits do quadro

    int quantBitsParidade = 0;
    while ((1 << quantBitsParidade) < (totalBits + quantBitsParidade + 1)) {
      quantBitsParidade++;
    } // fim while

    // cria o array do quadro novo
    int totalBitsHammming = totalBits + quantBitsParidade;
    int tamanhoQuadroFinal = (totalBitsHammming + 31) / 32;
    int[] quadroComHamming = new int[tamanhoQuadroFinal];

    // posicionar os bits de paridade
    int indiceBit = 0;

    for (int posicao = 1; posicao <= totalBitsHammming; posicao++) { // posicao indexada no quadro hamming

      // (posicao & (posicao - 1) eh um truque pra saber se a posicao eh par
      if ((posicao & (posicao - 1)) == 0) {
        continue; // pula as posicoes que sao potencia de 2. reservando o espaco
      } else if (indiceBit < totalBits) { // se nao eh potencia de 2 entao eh espaco de dado

        int bitDado = ManipulacaoBits.lerBits(quadro, indiceBit, 1);
        ManipulacaoBits.escreverBits(quadroComHamming, posicao - 1, bitDado, 1);
        indiceBit++;

      }

    } // fim for

    // calcular e posicionar os bits de paridade, paridade PAR

    for (int i = 0; i < quantBitsParidade; i++) {
      int posBitParidade = 1 << i;
      ; // Posicao do bit de paridade (1, 2, 4, 8, ...)

      int contadorUns = 0;
      // verifica os bits cobertos pela paridade
      for (int bit = 1; bit <= totalBitsHammming; bit++) {

        // verifica se o bit tem que ser verificado pelo BitDeParidade daquela posicao
        if ((bit & posBitParidade) != 0) {
          // nao contamos o proprio bit de paridade
          if (bit != posBitParidade) {
            if (ManipulacaoBits.lerBits(quadroComHamming, bit - 1, 1) == 1) {
              contadorUns++;
            } // fim if
          } // fim if
        } // fim if
      } // fim for

      // Define o bit de paridade p (em k-1) para garantir paridade PAR
      if ((contadorUns % 2) != 0) {
        ManipulacaoBits.escreverBits(quadroComHamming, posBitParidade - 1, 1, 1);
      }

    } // fim for

    return quadroComHamming;
  }// fim do metodo CamadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming

  /**
   * metodo que garente que acks recebidos matarao o timer certo para nao entrar
   * em loop de reenvio constante
   */
  public synchronized void receberAck() throws ErroDeVerificacaoException {
    if (!estado.equals("ESPERANDO_ACK")) {
      System.out.println("Enlace TX: ACK inesperado recebido. Ignorando.");
      return;
    } // fim do if

    System.out.println("Enlace TX: ACK Válido recebido!");

    cancelarTimer(); // finaliza o timer
    estado = "PRONTO_PARA_ENVIAR";// muda o estado para liberar envio do proximo
    quadroEmEspera = null; // libera o buffer

    CamadaEnlaceDadosTransmissoraControleDeFluxo(); // chama o controle de fluxo

  }// fim do receberAck

  /**
   * metodo responsavel por abortar uma transmissao invalida
   */
  public synchronized void abortarTranmissao() {
    System.out.println("PROBLEMA DETECTADO, ABORTANDO TRANSMISSAO!");
    cancelarTimer();
    filaDeEnvio.clear();
    quadroEmEspera = null;
    estado = "PRONTO_PARA_ENVIAR";
  }// fim do abortarTransmissao

} // fim da classe