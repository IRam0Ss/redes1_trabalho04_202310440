package model;

import controller.ControlerTelaPrincipal;
import javafx.application.Platform;
import util.ErroDeVerificacaoException;
import util.ManipulacaoBits;

/**
 * classe responsavel por simular a funcao da camada de aplicacao transmissora
 * de um sistema conectado a rede
 */
public class CamadaAplicacaoTransmissora {

  private CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora;
  private ControlerTelaPrincipal controlerTelaPrincipal;

  /**
   * construtor da classe
   * 
   * @param camadaEnlaceDadosTransmissora referencia para a proxima camada
   * @param controlerTelaPrincipal        referencia para comunicacao com
   *                                      interface
   */
  public CamadaAplicacaoTransmissora(CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora,
      ControlerTelaPrincipal controlerTelaPrincipal) {
    this.camadaEnlaceDadosTransmissora = camadaEnlaceDadosTransmissora;
    this.controlerTelaPrincipal = controlerTelaPrincipal;
  } // fim do construtor

  /**
   * metodo responsavel por transmitir a mensagem para a proxima camada
   * 
   * @param mensagem mensagem tranmitida
   */
  public void transmitirMensagem(String mensagem) throws ErroDeVerificacaoException{
    int[] quadro = ManipulacaoBits.stringParaIntAgrupado(mensagem); // converte a mensagem para array de int
    Platform.runLater(() -> {
      this.controlerTelaPrincipal.exibirRepresentMensagemBinariaTransmitida(quadro);
    });

    if (mensagem.equals("ACK")) { // se a mensagem a ser transmitida for o ACK, chama o metodo proprio para ACK
      this.camadaEnlaceDadosTransmissora.transmitirACK(quadro);
    } else { // caso contrario age normal
      this.camadaEnlaceDadosTransmissora.transmitirQuadro(quadro); // envia o quadro para a proxima camada
    } // fim if/else

  }// fim do metodo transmitirMensagem

} // fim da classe CamadaAplicacaoTransmissora
