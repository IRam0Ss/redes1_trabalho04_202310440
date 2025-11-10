package model;

import util.ErroDeVerificacaoException;

/**
 * classe responsavel pela aplicacao do sistema que tranmite a mensagem
 */
public class AplicacaoTransmissora {

  private CamadaAplicacaoTransmissora camadaAplicacaoTransmissora;

  /**
   * contrutor da classe
   * 
   * @param camadaAplicacaoTransmissora a camada de aplicacao transmissora que ira
   *                                    enviar a mensagem para a proxima camada
   */
  public AplicacaoTransmissora(CamadaAplicacaoTransmissora camadaAplicacaoTransmissora) {
    this.camadaAplicacaoTransmissora = camadaAplicacaoTransmissora;
  } // fim do construtor

  /**
   * metodo responsavel por enviar a mensagem para a proxima camada
   * 
   * @param mensagem mensagem a ser transmitida
   */
  public void iniciarTransmissao(String mensagem) throws ErroDeVerificacaoException{

    if (!mensagem.isEmpty() && mensagem != null) {
      this.camadaAplicacaoTransmissora.transmitirMensagem(mensagem); // envia a mensagem para a proxima camada
    } else {
      System.out.println("DIGITE ALGO ");
    }
  } // fim do enviarMEnsagem
} // fim da classe
