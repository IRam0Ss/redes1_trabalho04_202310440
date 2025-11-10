package model;

import controller.ControlerTelaPrincipal;
import javafx.application.Platform;

/**
 * classe que representa a aplicacao do sistema receptor
 */
public class AplicacaoReceptora {

  private ControlerTelaPrincipal controlerTelaPrincipal; // Armazena a referencia da UI

  /**
   * construtor da classe
   * 
   * @param controlerTelaPrincipal a referencia da interface grafica para
   *                               atualizacoes
   */
  public AplicacaoReceptora(ControlerTelaPrincipal controlerTelaPrincipal) {
    this.controlerTelaPrincipal = controlerTelaPrincipal;
  }// fim do construtor

  /**
   * metodo responsavel por receber a mensagem convertida para string e
   * exibi-la na interface grafica
   * 
   * @param mensagem mensagem que chegou ao receptor
   */
  public void receberMensagem(String mensagem) {
    // exibe a mensagem recebida na interface grafica
    Platform.runLater(() -> {
      this.controlerTelaPrincipal.exibirMensagemRecebida(mensagem);
    });
  } // fim do metodo receberMensagem

} // fim da classe
