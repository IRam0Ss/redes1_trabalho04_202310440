package util;

import java.util.Map;

/**
 * classe responsavel por gerenciar os estados da janela de transmissa, armazena
 * em buffer os quadros que foram enviados e aguardam confirmacao de recebimento
 * e controla os limites da janela
 */
public class JanelaDeslizante {

  private int tamanhoJanela; // tamanho da janela de transmissao
  private int base; // numero de sequencia mais antigo enviado e nao confirmado
  private int proximoNumeroDeSequencia; // proximo numero de sequencia a ser enviado

  private int espacoSequencia; // espaco total de numeros de sequencia disponiveis

  // buffer que armazena os quadros enviados e aguardando ACK
  private Map<Integer, int[]> bufferQuadros;

  // suporte para retransmissao seletiva, marca individualmente os ACKs recebidos
  private Map<Integer, Boolean> ackRecebidos;

  /**
   * construtor da classe
   * 
   * @param tamanhoJanela tamanho da janela de transmissao
   * @param bitsSequencia numero de bits usados para representar o numero de
   *                      sequencia
   */
  public JanelaDeslizante(int tamanhoJanela, int bitsSequencia) {
    this.tamanhoJanela = tamanhoJanela;
    this.espacoSequencia = 1 << bitsSequencia; // 2^bitsSequencia

    // validacao do protocolo
    if (this.espacoSequencia <= this.tamanhoJanela) {
      System.out.println("AVISO: Espaco de sequencia pequeno demais para o tamanho da janela!");
    }

    this.base = 0;
    this.proximoNumeroDeSequencia = 0;
    this.bufferQuadros = new java.util.HashMap<>();
    this.ackRecebidos = new java.util.HashMap<>();
  } // fim do construtor

  /**
   * verifica se a janela tem espaco considerando o calculo de numeros ciclicos
   * 
   * @return true se puder enviar, false se a janela tiver cheia
   */
  public boolean podeEnviar() {
    int quadrosEmTransito = (proximoNumeroDeSequencia - base + espacoSequencia) % espacoSequencia;
    return quadrosEmTransito < tamanhoJanela;
  } // fim do metodo podeEnviar

  /**
   * adiciona os quadros no buffer de espera de ACKs, ordenando-os em um hashmap
   * baseado no numero de sequencia
   * 
   * @param numeroDeSequencia o numero de sequencia do quadro
   * @param quadro            o quadro a ser armazenado
   */
  public void adicionarNoBuffer(int numeroDeSequencia, int[] quadro) {
    bufferQuadros.put(numeroDeSequencia, quadro);
    ackRecebidos.put(numeroDeSequencia, false); // inicialmente, o ACK nao foi recebido
  } // fim do metodo adicionarNoBuffer

  /**
   * recupera o quadro do buffer para retransmissao baseado no numero de sequencia
   * 
   * @param numeroDeSequencia o numero de sequencia do quadro
   * @return o quadro armazenado com aquele numero de sequencia
   */
  public int[] getQuadro(int numeroDeSequencia) {
    return bufferQuadros.get(numeroDeSequencia);
  } // fim do metodo getQuadro

  /**
   * marca o ACK como recebido para o numero de sequencia especificado, (USADO
   * PARA A RETRANSMISSAO SELETIVA)
   * 
   * @param numeroDeSequencia o numero de sequencia do quadro que teve o ACK
   *                          recebido
   */
  public void marcarAckRecebido(int numeroDeSequencia) {
    if (ackRecebidos.containsKey(numeroDeSequencia)) {
      ackRecebidos.put(numeroDeSequencia, true);
    }
  } // fim do metodo marcarAckRecebido

  /**
   * verifica se o ack foi recebido para o numero de sequencia especificado,
   * (USADO PARA A RETRANSMISSAO SELETIVA)
   * 
   * @param numeroDeSequencia o numero de sequencia do quadro
   * @return true se o ACK foi recebido, false caso contrario
   */
  public boolean isAckRecebido(int numeroDeSequencia) {
    return ackRecebidos.getOrDefault(numeroDeSequencia, false);
  } // fim do metodo isAckRecebido

  /**
   * verifica se o numero de sequencia esta dentro dos limites da janela
   * 
   * @param numeroDeSequencia o numero de sequencia a ser verificado
   * @return true se estiver dentro da janela, false caso contrario
   */
  public boolean estaDentroDaJanela(int numeroDeSequencia) {
    int distanciaInicio = (numeroDeSequencia - base + espacoSequencia) % espacoSequencia;
    int tamanhoAtual = (proximoNumeroDeSequencia - base + espacoSequencia) % espacoSequencia;
    return distanciaInicio < tamanhoAtual;
  } // fim do metodo estaDentroDaJanela

  /**
   * limpa os quadros que ja sairam da janela de transmissao
   */
  public void limparQuadrosAntigos() {
    // remove qualquer quadro que nao esteja dentro da janela atual
    bufferQuadros.keySet().removeIf(numeroDeSequencia -> !estaDentroDaJanela(numeroDeSequencia));
    ackRecebidos.keySet().removeIf(numeroDeSequencia -> !estaDentroDaJanela(numeroDeSequencia));
  } // fim do metodo limparQuadrosAntigos

  /**
   * avanca o ponteiro sequencia para o proximo numero de sequencia, mantendo a
   * logica de ciclos de numero de sequencia
   */
  public void avancarSequencia() {
    this.proximoNumeroDeSequencia = (this.proximoNumeroDeSequencia + 1) % espacoSequencia;
  } // fim do metodo avancarSequencia

  /**
   * atualiza a base da janela para o numero de sequencia especificado
   * 
   * @param numeroDeSequencia o numero de sequencia para atualizar a base
   */
  public void atualizarBase(int numeroDeSequencia) {
    // define a nova base a apassar o ponteiro base ate o ACK recebido
    this.base = (numeroDeSequencia + 1) % espacoSequencia;

    // remove os quadros antigos do buffer
    limparQuadrosAntigos();
  } // fim do metodo atualizarBase

  /**
   * avanca a base da janela enquanto os ACKs forem recebidos, (USADO PARA A
   * RETRANSMISSAO SELETIVA)
   */
  public void deslizarBaseSeletiva() {
    boolean houveMovimento = false;
    // enquanto o quadro da base tiver como falso no ackRecebidos
    while (ackRecebidos.getOrDefault(base, false)) {
      // avanca a base
      this.base = (this.base + 1) % espacoSequencia;
      houveMovimento = true;
    }

    // se a janaela deslizou limpa os quadros antigos
    if (houveMovimento) {
      limparQuadrosAntigos();
    }
  }// fim do deslizarBaseSeletiva

  // --- gets e sets ---
  public int getBase() {
    return base;
  }

  public int getProximoNumeroSequencia() {
    return proximoNumeroDeSequencia;
  }

  public int getTamanhoJanela() {
    return tamanhoJanela;
  }

} // fim da classe JanelaDeslizante
