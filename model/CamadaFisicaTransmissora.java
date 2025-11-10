package model;

import controller.ControlerTelaPrincipal;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import util.ErroDeVerificacaoException;
import util.ManipulacaoBits;

/**
 * classe responsavel por simular a funcao da Camada Fissica Transmissora
 * pega a mensagem que veio da camada anterior (em binario) e codifica de acordo
 * com a opcao selecionada pelo usuario
 */
public class CamadaFisicaTransmissora {

  private ControlerTelaPrincipal controleTelaPrincipal; // referencia para a interface grafica
  private MeioDeComunicacao meioDeComunicacao; // referencia para o meio de comunicacao

  private CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora; // referencia a classe superior

  /**
   * contrutor da classe
   * 
   * @param controleTelaPrincipal referencia para controle da interface
   */
  public CamadaFisicaTransmissora(ControlerTelaPrincipal controleTelaPrincipal) {
    this.controleTelaPrincipal = controleTelaPrincipal;
  } // fim construtor

  /**
   * metodo usado para definir o meio de comunicacao que sera utilizado
   * 
   * @param meioDeComunicacao o meio de comunicacao que sera utilizado
   */
  public void setMeioDeComunicacao(MeioDeComunicacao meioDeComunicacao) {
    this.meioDeComunicacao = meioDeComunicacao;
  } // fim setMeioComunicacao

  /**
   * metodo responsavel por transmitir o quadro apos aplicar a codificacao
   * selecionada, chamado pela camada superior
   * 
   * @param quadro o quadro de bits ja passado pela enlace
   */
  public void transmitirQuadro(int quadro[]) throws ErroDeVerificacaoException {
    int tipoDeCodificacao = this.controleTelaPrincipal.opcaoSelecionada();
    int fluxoBrutoDeBits[] = null; // eh a representacao do sinal que sera enviado

    int tipoDeEnquadramento = this.controleTelaPrincipal.opcaoEnquadramentoSelecionada();

    if (tipoDeCodificacao == 0 && tipoDeEnquadramento == 3) {
      Platform.runLater(() -> { // mostra visualmente o alerta de que a combinacao nao eh permitida
        Alert alert;
        alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Mensagem de Erro");
        alert.setHeaderText("ERRO! COMBINACAO NAO PERMITIDA");
        alert.setContentText("Nao eh possivel utilizar codificacao binaria e violacao da camada fisica");
        alert.showAndWait();

        // aborta a transmissao
        if (this.camadaEnlaceDadosTransmissora != null) {
          camadaEnlaceDadosTransmissora.abortarTranmissao();
        } // fim if

      });
      return;
    } // fim do if

    if (tipoDeEnquadramento == 3) { // se for enquadramento por violacao da camada fisica

      fluxoBrutoDeBits = CamadaFisicaTransmissoraComViolacao(quadro, tipoDeCodificacao);

    } else { // se nao faz o normal
      switch (tipoDeCodificacao) {
        case 0: // codificao binaria
          fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoBinaria(quadro);
          break;
        case 1: // codificacao manchester
          fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoManchester(quadro);
          break;
        case 2: // codificacao manchester diferencial
          fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoManchesterDiferencial(quadro);
          break;
      }// fim do switch/case
    }
    final int[] fluxoBrutoBitsExibir = fluxoBrutoDeBits;
    Platform.runLater(() -> {
      this.controleTelaPrincipal.exibirRepresentSinalTransmitido(fluxoBrutoBitsExibir);
    });

    // manda pra proxima camada
    meioDeComunicacao.transmitirMensagem(fluxoBrutoDeBits, this);
  } // fim do construtor

  /**
   * aplica a codificacao binaria na mensagem a ser transmitida
   * 
   * @param quadro traduzida em bits agrupados no array de
   *               inteiros, cada inteiro possui ate 32 bits (4
   *               char) da
   *               mensagem
   * @return como o sinal em binario eh uma traducao direta da mensagem em bits,
   *         retorna a propria
   */
  public int[] CamadaFisicaTransmissoraCodificacaoBinaria(int[] quadro) {
    return quadro;
  }// fim do metodo

  /**
   * aplica a codificacao de manchester na mensagem a ser transmitida
   * bit 1 -> 10
   * bit 0 -> 01
   * 
   * @param quadro string traduzida em bits agrupados no array de
   *               inteiros, cada inteiro possui ate 32 bits (4 char) da
   *               mensagem
   * @return retorna no array como sera o sinal transmitido em manchester
   */
  public int[] CamadaFisicaTransmissoraCodificacaoManchester(int[] quadro) {

    int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadro); // quantidade de bit validos armazenado no array
                                                                       // de int
    int totalBitsManchester = totalBits * 2; // // quatidade de bits que tera o array pos codificacao
    int tamanhoArrayManchester = (totalBitsManchester + 31) / 32; // para garantir que arredonde pra cima em casos nao
                                                                  // exatos

    int[] pacoteManchester = new int[tamanhoArrayManchester]; // cria o array que vai ser devolvido

    // percorre cada bit do fluxo original e aplica codificacao manchester
    for (int i = 0; i < totalBits; i++) {

      // ler o bit original
      int indiceNoPacoteOriginal = i / 32;
      int posicaobitOriginal = 31 - (i % 32);

      int bitOriginal = (quadro[indiceNoPacoteOriginal] >> posicaobitOriginal) & 1;

      int sinal1, sinal2; // definir quais os sinais que o bit tera codificado
      if (bitOriginal == 1) {
        sinal1 = 1;
        sinal2 = 0;
        // 1 -> 10
      } else {
        sinal1 = 0;
        sinal2 = 1;
        // 0 ->01
      }

      // escreve o sinal 1 no pacoteManchester
      int posicaoSinal1 = i * 2;
      int indiceNoPacoteManchesterSinal1 = posicaoSinal1 / 32;
      int posicaoBitSinal1 = 31 - (posicaoSinal1 % 32);

      if (sinal1 == 1) {
        pacoteManchester[indiceNoPacoteManchesterSinal1] = pacoteManchester[indiceNoPacoteManchesterSinal1]
            | (1 << posicaoBitSinal1); // liga o bit no local certo
      }

      // escreve o sinal 2 no pacote manchester
      int posicaoSinal2 = (i * 2) + 1;
      int indiceNoPacoteManchesterSinal2 = posicaoSinal2 / 32;
      int posicaoBitSinal2 = 31 - (posicaoSinal2 % 32);

      if (sinal2 == 1) {
        pacoteManchester[indiceNoPacoteManchesterSinal2] = pacoteManchester[indiceNoPacoteManchesterSinal2]
            | (1 << posicaoBitSinal2);
      } // fim if

    } // fim for

    return pacoteManchester;
  }// fim do metodo

  /**
   * aplica a codificacao de manchester na mensagem a ser transmitida
   * 0 -> transicao
   * 1 -> sem transicao
   * 
   * @param quadro string traduzida em bits agrupados no array de
   *               inteiros, cada inteiro possui ate 32 bits (4 char) da
   *               mensagem
   * @return retorna no array como sera o sinal transmitido em
   *         manchesterdiferencial
   */
  public int[] CamadaFisicaTransmissoraCodificacaoManchesterDiferencial(int[] quadro) {

    int totalBits = ManipulacaoBits.descobrirTotalDeBitsReais(quadro); // calcula o total de bit validos enviados pelo
                                                                       // array
    int totalBitsDiferencial = totalBits * 2; // define qual o total dde bits final do manchester diferencial
    int tamanhoArrayDiferencial = (totalBitsDiferencial + 31) / 32; // define o tamanho necessario para o novo array
                                                                    // arredondando para cima
    int[] pacoteMancheterDiferencial = new int[tamanhoArrayDiferencial]; // cria o array

    int nivelAtual = 1; // variavel que controla o nivel atual do sinal, inicializacdo arbitrariamente

    // percorrer bit da mensagem original e aplicar manchester diferencial
    for (int i = 0; i < totalBits; i++) {

      // ler o bit original
      int indiceNoPacoteOriginal = i / 32;
      int posicaobitOriginal = 31 - (i % 32);

      int bitOriginal = (quadro[indiceNoPacoteOriginal] >> posicaobitOriginal) & 1;

      if (bitOriginal == 0) { // caso o bit seja 0 forca transicao invertendo o nivel do sinalatual
        nivelAtual = 1 - nivelAtual; // inverte (0 -> 1 e 1 -> 0)
      }
      // caso o bit original seja 1, mantem o mesmo sinal

      // escreve a primeira parte do sinal
      int posicaoSinal1 = i * 2;
      if (nivelAtual == 1) {
        int indicePacoteDiferencial = posicaoSinal1 / 32;
        int posicaoNovaDiferencial = 31 - (posicaoSinal1 % 32);
        pacoteMancheterDiferencial[indicePacoteDiferencial] = pacoteMancheterDiferencial[indicePacoteDiferencial]
            | (1 << posicaoNovaDiferencial);
      }

      nivelAtual = 1 - nivelAtual; // faz a inversao padrao para codificar o bit como um par de sinais

      // escreve a segunda parte do sinal
      int posicaoSinal2 = i * 2 + 1;
      if (nivelAtual == 1) {
        int indicePacoteDiferencial = posicaoSinal2 / 32;
        int posicaoNovaDiferencial = 31 - (posicaoSinal2 % 32);
        pacoteMancheterDiferencial[indicePacoteDiferencial] = pacoteMancheterDiferencial[indicePacoteDiferencial]
            | (1 << posicaoNovaDiferencial);
      }

    }

    return pacoteMancheterDiferencial;
  }// fim do metodo

  /**
   * Codifica o quadro de dados e o enquadra com sinais de violacao (11)
   * no inicio e no fim.
   * 
   * @param quadro            O quadro de dados PURO vindo da camada de enlace.
   * @param tipoDeCodificacao A codificacao a ser usada (Manchester, etc.).
   * @return O fluxo de bits pronto para o meio fisico.
   */
  private int[] CamadaFisicaTransmissoraComViolacao(int quadro[], int tipoDeCodificacao) {

    final int VIOLACAO = 0b1111;
    final int TAMANHO_VIOLACAO_BITS = 4;

    final int TAMANHO_SUBQUADRO_EM_BITS = 32; // a cada 32 bits adiciona uma flag

    int totalBitsMensagem = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
    if (totalBitsMensagem == 0)
      return new int[0]; // se a mensagem ta vazia nem finaliza o processamento

    // calcula um tamanho MAXIMO estimado para o buffer temporario, nao exato pois
    // sera aparado depois
    int numSubquadrosEstimado = (totalBitsMensagem + TAMANHO_SUBQUADRO_EM_BITS - 1) / TAMANHO_SUBQUADRO_EM_BITS;
    int totalBitsSinalEstimado = (TAMANHO_VIOLACAO_BITS * (numSubquadrosEstimado + 1)) + (totalBitsMensagem * 2);
    int[] bufferTemporario = new int[(totalBitsSinalEstimado + 31) / 32];
    int bitEscritaGlobal = 0;

    // escreve a violacao de INICIO (1111)
    ManipulacaoBits.escreverBits(bufferTemporario, bitEscritaGlobal, VIOLACAO, TAMANHO_VIOLACAO_BITS);
    bitEscritaGlobal += TAMANHO_VIOLACAO_BITS; // pula os 4 bits que foram escritos pra violacao

    // codifica os dados da mensagem
    int nivelAtual = 1; // para Manchester Diferencial

    int contadorBitsSubquadro = 0;

    for (int i = 0; i < totalBitsMensagem; i++) {
      int bitOriginal = ManipulacaoBits.lerBits(quadro, i, 1);

      if (tipoDeCodificacao == 1 || tipoDeCodificacao == 2) { // Manchester ou Diferencial
        int sinal1, sinal2;
        if (tipoDeCodificacao == 1) { // Manchester
          sinal1 = (bitOriginal == 1) ? 1 : 0;
          sinal2 = (bitOriginal == 1) ? 0 : 1;
        } else { // Manchester Diferencial
          if (bitOriginal == 0)
            nivelAtual = 1 - nivelAtual;
          sinal1 = nivelAtual;
          nivelAtual = 1 - nivelAtual;
          sinal2 = nivelAtual;
        }
        ManipulacaoBits.escreverBits(bufferTemporario, bitEscritaGlobal++, sinal1, 1);
        ManipulacaoBits.escreverBits(bufferTemporario, bitEscritaGlobal++, sinal2, 1);
      } // fim if

      contadorBitsSubquadro++;

      // verifica se o quadro acabou ou se eh o fim da mensagem para adiconar a flag
      boolean ehFimDoSubquadro = (contadorBitsSubquadro == TAMANHO_SUBQUADRO_EM_BITS);
      boolean ehFimDaMensagem = (i == totalBitsMensagem - 1);

      if (ehFimDoSubquadro || ehFimDaMensagem) {
        // Escreve a violacao de FIM de subquadro (que tambem serve como FIM da
        // mensagem)
        ManipulacaoBits.escreverBits(bufferTemporario, bitEscritaGlobal, VIOLACAO, TAMANHO_VIOLACAO_BITS);
        bitEscritaGlobal += TAMANHO_VIOLACAO_BITS;

        // Zera o contador para o proximo subquadro
        contadorBitsSubquadro = 0;

        nivelAtual = 1;
      } // fim do if

    } // fim for

    // reorganiza o array para ficar com tamanho exato
    int tamanhoArrayFinal = (bitEscritaGlobal + 31) / 32;
    int[] fluxoBrutoDeBitsFinal = new int[tamanhoArrayFinal];

    // copia apenas os bits validos do buffer temporario para o array final
    for (int i = 0; i < bitEscritaGlobal; i++) {
      int bit = ManipulacaoBits.lerBits(bufferTemporario, i, 1);
      ManipulacaoBits.escreverBits(fluxoBrutoDeBitsFinal, i, bit, 1);
    } // fim for

    return fluxoBrutoDeBitsFinal; // retorna o array perfeitamente ajustado
  }// fim metodo CamadaFisicaTransmissoraComViolacao

  /**
   * define qual a camda superior a essa, chamado pelo host para tratar possiveis
   * transmissoes invalidas
   * 
   * @param camadaEnlaceDadosTransmissora a camada superior a atual
   */
  public void setCamadaEnlaceSuperior(CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora) {
    this.camadaEnlaceDadosTransmissora = camadaEnlaceDadosTransmissora;
  } // fim setCamadaEnlaceSuperior

}// fim da classe
