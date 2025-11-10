/* ***************************************************************
* Autor............: Iury Ramos Sodre 
* Matricula........: 202310440
* Inicio...........: 22/09/2025
* Ultima alteracao.: 02/11/2025
* Nome.............: E.D.E.N. (sistema de simulacao de transmicao de sinais)
* Funcao...........: simula a transmissao de sinais que acontece numa rede.
*************************************************************** */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import util.ManipulacaoBits;
import util.ErroDeVerificacaoException;

import controller.ControlerTelaPrincipal;
import controller.ControleRede;
import controller.Host;

import model.AplicacaoReceptora;
import model.AplicacaoTransmissora;
import model.CamadaAplicacaoReceptora;
import model.CamadaAplicacaoTransmissora;
import model.CamadaFisicaReceptora;
import model.CamadaFisicaTransmissora;
import model.MeioDeComunicacao;
import model.CamadaEnlaceDadosTransmissora;
import model.CamadaEnlaceDadosReceptora;

@SuppressWarnings("unused")
public class Principal extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader layout = new FXMLLoader(getClass().getResource("/view/telaPrincipal.fxml")); // encontra e armazena o
                                                                                            // fxml usado no
                                                                                            // programa
    Parent root = layout.load(); // define a root como sendo o layout para exibir a tela.

    // criar cena e exibir:
    Scene telaPrincipal = new Scene(root, 900, 600);

    primaryStage.setScene(telaPrincipal); // define a cena a ser exibida

    primaryStage.setTitle("E.D.E.N."); // define o titulo do projeto

    
    //Pra setar um icone, mas eh detalhe opcional
    Image icone = new Image("./img/logoEDEN.png"); //define a imagem que sera o
    primaryStage.getIcons().add(icone); //adiciona o icone ao stage
    

    primaryStage.setResizable(false); // impete que tamanho da tela seja alterado

    primaryStage.show();

    primaryStage.setOnCloseRequest(e -> System.exit(0)); // garante que tudo eh finalizado quando o programa eh
                                                         // fechado

  }

}
