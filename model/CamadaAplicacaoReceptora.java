package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;

/**
 * classe responsavel por simular a funcao da camada de aplicacao do sistema
 * receptor, basicamente recebe a mensagem decodificada em sua versao binaria e
 * a converte para a tring equivalente
 */
public class CamadaAplicacaoReceptora {

  private AplicacaoReceptora aplicacaoReceptora;
  private ControlerTelaPrincipal controlerTelaPrincipal;

  /**
   * construtor da classe
   * 
   * @param aplicacaoReceptora     a aplicacao receptora que ira receber a
   *                               mensagem convertida
   * @param controlerTelaPrincipal a interface grafica para atualizacoes
   */
  public CamadaAplicacaoReceptora(AplicacaoReceptora aplicacaoReceptora,
      ControlerTelaPrincipal controlerTelaPrincipal) {
    this.aplicacaoReceptora = aplicacaoReceptora;
    this.controlerTelaPrincipal = controlerTelaPrincipal;
  } // fim do construtor

  public void receberQuadro(int[] quadro) {

    this.controlerTelaPrincipal.exibirRepresentMensagemBinariaRecebida(quadro); // mostra o binario na tela

    String mensagem = ManipulacaoBits.intAgrupadoParaString(quadro); // converte o array de int
                                                                                           // para string
    this.aplicacaoReceptora.receberMensagem(mensagem);
  } // fim do metodo receberQuadro

} // fim da classe