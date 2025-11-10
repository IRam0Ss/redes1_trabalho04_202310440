package controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import util.ErroDeVerificacaoException;
import util.ManipulacaoBits;

/**
 * Classe responsavel por controlar os elementos visuais do javaFX
 */
public class ControlerTelaPrincipal {

  @FXML
  private Button butaoTransmitir;

  @FXML
  private TextArea caixaTextoRecebido;

  @FXML
  private TextArea caixaTextoTransmitido;

  @FXML
  private TextArea representMensagemBinariaRecebida;

  @FXML
  private TextArea representMensagemBinariaTransmitida;

  @FXML
  private TextArea representSinalTransmitido;

  @FXML
  private TextArea representSinalRecebido;

  @FXML
  private ChoiceBox<String> opcoesTransmissao;

  @FXML
  private ChoiceBox<String> opcaoEnquadramento;

  @FXML
  private ChoiceBox<String> opcaoTaxaErro;

  @FXML
  private ChoiceBox<String> opcaoControleErro;

  @FXML
  private Canvas quadroAnimacaoTransmissao;

  private GraphicsContext gc; // o "pincel" que vai gerar o desenho para simular a transmissao

  private AnimationTimer animacao; // o controle do loop da animacao

  private ControleRede controleRede; // o controlador da rede

  /**
   * classe interna para representar cada quadro na animacao
   * para organizar a sequencia de quadros a serem animados
   */
  private class QuadroAnimacao {
    int[] fluxoBits;
    QuadroAnimacao proxQuadroAnimacao;

    QuadroAnimacao(int[] fluxoBits) {
      this.fluxoBits = fluxoBits;
      this.proxQuadroAnimacao = null;
    }// fim construtor
  } // fim classe QuadroAnimacao

  private QuadroAnimacao inicioFilaAnimacao = null;
  private QuadroAnimacao fimFilaAnimacao = null;
  private boolean animacaoEmAndamento = false;

  @FXML
  public void initialize() {
    // Adiciona a choiceBox as opcoes de escolha e inicializa por padrao como
    // binario
    opcoesTransmissao.getItems().addAll("Binario", "Manchester", "Manchester Diferencial");
    opcoesTransmissao.setValue("Binario");

    // adiciona as opcoes de enquadramento e inicializa como padrao a contagem de
    // caracteres
    opcaoEnquadramento.getItems().addAll("Contagem de Caracteres", "Insercao de Bytes",
        "Insercao de Bits", "Violacao da Camada Fisica");
    opcaoEnquadramento.setValue("Contagem de Caracteres");

    // adiciona as opcoes de controle de erro e inicializa como padrao
    opcaoControleErro.getItems().addAll("Bit de Paridade Par", "Bit de Paridade Impar",
        "CRC-32 (IEEE 802)", "Codigo de Hamming");
    opcaoControleErro.setValue("Bit de Paridade Par");

    // adiciona as opcoes de taxa de erro e inicializa como padrao 0%
    opcaoTaxaErro.getItems().addAll("0%", "1%", "5%", "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%",
        "100%");
    opcaoTaxaErro.setValue("0%");

    gc = quadroAnimacaoTransmissao.getGraphicsContext2D(); // "assossia" o nosso "pincel" a tela onde a animacao vai
                                                           // surgir

    this.controleRede = new ControleRede(this); // cria o controlador da rede e passa a referencia desta tela para ele

  } // fim initialize

  /**
   * metodo que inicia a simulacoa ao pressinor de um botao
   * 
   * @param pressionarBotaoTransmitir condicao de disparada do metodo,
   *                                  pressionar
   *                                  o botao transmitir!
   * @throws ErroDeVerificacaoException trata erros de violacao de canais
   */
  public void realizarSimulacaoTransmissao(ActionEvent pressionarBotaoTransmitir) throws ErroDeVerificacaoException {
    if (!caixaTextoTransmitido.getText().isEmpty()) {

      String mensagem = getCaixaTextoTransmitido(); // coleta a mensagem digitada pelo usuario

      limparInterface(); // limpa a interface da simulacao passada

      // iniciar a simulacao em uma thread exclusiva, para evitar conflitos de
      // animacao
      new Thread(() -> {
        try {
          this.controleRede.iniciarSimulacao(mensagem);
        } catch (ErroDeVerificacaoException e) {
          e.printStackTrace();
        } // inicia a simulacao de transmissao
      }).start();

    } else {
      System.out.println("DIGITE UM TEXTO A SER TRANSMITIDO");
    }

  }// fim metodo

  /**
   * enfileira o quadro para animacao de transmissao
   * 
   * @param fluxoBitsTransmitido bits a serem transmitidos
   */
  public void desenharSinalTransmissao(int[] fluxoBitsTransmitido) {

    // cria o no com os dados do quadro
    QuadroAnimacao novoQuadro = new QuadroAnimacao(fluxoBitsTransmitido);

    if (inicioFilaAnimacao == null) {
      // fila vazia
      inicioFilaAnimacao = novoQuadro;
      fimFilaAnimacao = novoQuadro;
    } else {
      // fila com elementos
      fimFilaAnimacao.proxQuadroAnimacao = novoQuadro;
      fimFilaAnimacao = novoQuadro;
    }

    // tenta processar a fila de animacao
    processarFilaAnimacao();
  }// fim metodo desenharSinalTransmissao

  /**
   * metodo responsavel por processar a fila de animacao, se nao houver nenhuma
   * animacao em andamento
   */
  public void processarFilaAnimacao() {
    // Se uma animação já está rodando, ou se a fila está vazia, não faz nada.
    // A animação atual irá chamar esse método quando terminar.
    if (animacaoEmAndamento || inicioFilaAnimacao == null) {
      return;
    }

    animacaoEmAndamento = true; // iniciamos uma animacao

    QuadroAnimacao quadroAtual = inicioFilaAnimacao; // pega o primeiro quadro para animar
    inicioFilaAnimacao = inicioFilaAnimacao.proxQuadroAnimacao; // atualiza o inicio da fila

    if (inicioFilaAnimacao == null) { // se a fila ficou vazia, atualiza o fim tambem
      fimFilaAnimacao = null;
    }

    // extrai o fluxo a ser animado
    int[] fluxoBitsTransmitido = quadroAtual.fluxoBits;

    // Inicia a animação do fluxo de bits(quadroAtual)

    // define a largura do bit simulado a partir da opcao selecionada
    final double LARGURA_BIT;
    if (opcaoSelecionada() == 0) {
      LARGURA_BIT = 40.0;
    } else {
      LARGURA_BIT = 20.0;
    }

    // pega os parametros do canvas
    final double ALTURA_GRAFICO = quadroAnimacaoTransmissao.getHeight();
    final double NIVEL_ALTO_Y = ALTURA_GRAFICO * 0.25;
    final double NIVEL_BAIXO_Y = ALTURA_GRAFICO * 0.75;
    final double VELOCIDADE_PX_POR_SEGUNDO = 200.0;

    // calcular a largura total da onda em pixels
    final double LARGURA_TOTAL_DA_ONDA = fluxoBitsTransmitido.length * LARGURA_BIT;

    final long tempoInicialNano = System.nanoTime(); // define o tempo inicial da animacao em nano segundos

    animacao = new AnimationTimer() { // cria a animacao
      @Override
      public void handle(long now) {

        double tempoDecorridoSeg = (now - tempoInicialNano) / 1_000_000_000.0; // quanto tempo de animacao ja passou
        double offsetX = tempoDecorridoSeg * VELOCIDADE_PX_POR_SEGUNDO;

        // posição inicial da onda começa "escondida" à esquerda
        // e se move para a direita com o offsetX.
        double posicaoInicialDaOnda = offsetX - LARGURA_TOTAL_DA_ONDA;

        // LIMPAR A TELA
        gc.clearRect(0, 0, quadroAnimacaoTransmissao.getWidth(), ALTURA_GRAFICO);

        // CONFIGURAR O PINCEL
        gc.setStroke(Color.CORNFLOWERBLUE);
        gc.setLineWidth(2.5);

        // DESENHAR A ONDA
        double nivelYAnterior = NIVEL_BAIXO_Y;

        for (int i = 0; i < fluxoBitsTransmitido.length; i++) {
          // a onda como um todo se move, e cada bit é desenhado em sua posição relativa a
          // ela.
          double startX = posicaoInicialDaOnda + (i * LARGURA_BIT);
          double endX = startX + LARGURA_BIT;

          // não desenha o que está muito fora da tela para economizar processamento
          if (endX < 0 || startX > quadroAnimacaoTransmissao.getWidth()) {
            // atualiza o nivelYAnterior mesmo se não desenhar, para a transição ficar
            // correta
            nivelYAnterior = (fluxoBitsTransmitido[i] == 1) ? NIVEL_ALTO_Y : NIVEL_BAIXO_Y;
            continue;
          }

          // desenha um marcador pos emissao de sinal
          boolean desenharMarcador = false;
          if (opcaoSelecionada() == 0) { // se for Binário, desenha um marcador a cada bit.
            if (i > 0) { // nao desenha no comeco da onda (i=0)
              desenharMarcador = true;
            }
          } else { // se nao for binario, desenha a cada 2 "meio-bits".
            if (i > 0 && i % 2 == 0) { // somente nos pares
              desenharMarcador = true;
            }
          }

          if (desenharMarcador) {
            // configura o pincel para o marcador (fino, cinza, tracejado)
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(2);
            gc.setLineDashes(4);

            // desenha a linha vertical
            gc.strokeLine(startX, NIVEL_ALTO_Y - 10, startX, NIVEL_BAIXO_Y + 10);

            // restaura o pincel para a onda principal
            gc.setStroke(Color.CORNFLOWERBLUE);
            gc.setLineWidth(2.5);
            gc.setLineDashes(null); // IMPORTANTE: remove o padrao tracejado
          }

          double nivelYAtual = (fluxoBitsTransmitido[i] == 1) ? NIVEL_ALTO_Y : NIVEL_BAIXO_Y;

          if (nivelYAtual != nivelYAnterior) {
            gc.strokeLine(startX, nivelYAnterior, startX, nivelYAtual);
          }

          gc.strokeLine(startX, nivelYAtual, endX, nivelYAtual);
          nivelYAnterior = nivelYAtual;
        }

        // condicao de parada verifica quando o inicio da onda
        // ultrapassou a largura do canvas.
        if (posicaoInicialDaOnda > quadroAnimacaoTransmissao.getWidth()) {
          this.stop(); // para esta animacao

          animacaoEmAndamento = false; // sinaliza que a animacao terminou, liberando para a proxima

          gc.clearRect(0, 0, quadroAnimacaoTransmissao.getWidth(), ALTURA_GRAFICO);

          processarFilaAnimacao(); // tenta processar a fila de animacao novamente

        }
      }
    };

    animacao.start(); // inicia a animacao configurada
  }// fim do metodo

  /**
   * metodo responsavel por detectar qual opcao esta sendo selecionada na choice
   * box e a "converter" para um inteiro.
   * 
   * @return um inteiro equivalente a opcao selecionada
   */
  public int opcaoSelecionada() {
    String opcaoChoiceBox = opcoesTransmissao.getValue();

    int opcaoSelecionada = 3;

    switch (opcaoChoiceBox) {
      case "Binario":
        opcaoSelecionada = 0;
        break;
      case "Manchester":
        opcaoSelecionada = 1;
        break;
      case "Manchester Diferencial":
        opcaoSelecionada = 2;
        break;
      default:
        System.out.println("Problemas no metodo opcaoSelecionada do ControlerTelaPrincipal");
        break;
    }

    return opcaoSelecionada;
  }// fim do metodo

  /**
   * metodo responsavel por detectar qual opcao de enquadramento esta sendo
   * selecionada na choice box e a "converter" para um inteiro.
   * 
   * @return um inteiro equivalente a opcao selecionada
   */
  public int opcaoEnquadramentoSelecionada() {
    String opcaoChoiceBox = opcaoEnquadramento.getValue();

    int opcaoSelecionada = 0;

    switch (opcaoChoiceBox) {
      case "Contagem de Caracteres":
        opcaoSelecionada = 0;
        break;
      case "Insercao de Bytes":
        opcaoSelecionada = 1;
        break;
      case "Insercao de Bits":
        opcaoSelecionada = 2;
        break;
      case "Violacao da Camada Fisica":
        opcaoSelecionada = 3;
        break;
      default:
        System.out.println("Problemas no metodo opcaoEnquadramentoSelecionada do ControlerTelaPrincipal");
        break;
    }

    return opcaoSelecionada;
  }// fim do metodo

  /**
   * metodo responsavel por detectar qual opcao de controle de erro esta sendo
   * selecionada na choice box e a "converter" para um inteiro.
   * 
   * @return um inteiro equivalente a opcao selecionada
   */
  public int opcaoControleErroSelecionada() {
    String opcaoChoiceBox = opcaoControleErro.getValue();

    int opcaoSelecionada = 0;

    switch (opcaoChoiceBox) {
      case "Bit de Paridade Par":
        opcaoSelecionada = 0;
        break;
      case "Bit de Paridade Impar":
        opcaoSelecionada = 1;
        break;
      case "CRC-32 (IEEE 802)":
        opcaoSelecionada = 2;
        break;
      case "Codigo de Hamming":
        opcaoSelecionada = 3;
        break;
      default:
        System.out.println("Problemas no metodo opcaoControleErroSelecionada do ControlerTelaPrincipal");
        break;
    }

    return opcaoSelecionada;
  }// fim do metodo

  /**
   * metodo para coletar a informacao de qual texto que deseja se transmitir
   * 
   * @return uma string com o texto digitado pelo usuario
   */
  public String getCaixaTextoTransmitido() {
    return caixaTextoTransmitido.getText();
  }

  /**
   * mostra na caixa reservada qual foi a mensagem recebida pos transferencia
   * 
   * @param mensagemRecebida a string que sera exibida
   */
  public void exibirMensagemRecebida(String mensagemRecebida) {
    Platform.runLater(() -> {
      caixaTextoRecebido.appendText(mensagemRecebida);
    });

  }// fim mensagem recebida

  /**
   * coleta a informacao de qual a opcao que esta selecionada na choice box
   * 
   * @return a string que define qual das 3 opcoes de codificacao
   */
  public String getOpcaoTransmissao() {
    return opcoesTransmissao.getValue();
  }// fim metodo

  /**
   * exibe na caixa reservada a forma binaria da mensagem enviada
   * * @param binarioMensagem recebe o array de int com a informacao em binario
   */
  public void exibirRepresentMensagemBinariaTransmitida(int[] binarioMensagem) {

    Platform.runLater(() -> {
      representMensagemBinariaTransmitida.setText(ManipulacaoBits.exibirBitsStr(binarioMensagem));
    });

  }// fim metodo

  /**
   * exibe na caixa reservada a forma binaria da mensagem recebida
   * * @param binarioMensagem recebe o array de int com a informacao em binario
   */
  public void exibirRepresentMensagemBinariaRecebida(int[] binarioMensagem) {

    Platform.runLater(() -> {
      representMensagemBinariaRecebida.appendText(ManipulacaoBits.exibirBitsStr(binarioMensagem));
    });

  }// fim metodo

  /**
   * exibe na caixa de texto reservada a representacao de como o sinal pos
   * codificacao eh enviado
   * 
   * @param representSinal array com a mensagem codificada que representa o
   *                       sinal
   */
  public void exibirRepresentSinalTransmitido(int[] representSinal) {

    Platform.runLater(() -> {
      representSinalTransmitido.appendText(ManipulacaoBits.exibirBitsStr(representSinal));
    });

  }// fim metodo

  /**
   * exibe na caixa de texto reservada a representacao de como o sinal pos
   * codificacao eh recebido
   * 
   * @param representSinal array com a mensagem codificada que representa o
   *                       sinal
   */
  public void exibirRepresentSinalRecebido(int[] representSinal) {
    Platform.runLater(() -> {
      representSinalRecebido.appendText(ManipulacaoBits.exibirBitsStr(representSinal));
    });

  }// fim metodo

  public String getTaxaErro() {
    return opcaoTaxaErro.getValue();
  }// fim metodo

  /**
   * retorna o valor em double da taxa de erro selecionada
   * 
   * @return valor double da taxa de erro
   */
  public double getValorTaxaErro() {
    String valorSelecionado = opcaoTaxaErro.getValue();

    if (valorSelecionado == null || valorSelecionado.isEmpty()) {
      return 0.0; // retorna 0 se nada for selecionado
    }

    try {
      // remove o caractere '%' e converte o resto da string para um numero
      String numeroString = valorSelecionado.replace("%", "").trim();
      int valorInteiro = Integer.parseInt(numeroString);
      return valorInteiro / 100.0; // Converte a porcentagem para um valor double (10 -> 0.1)
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return 0.0; // retorna 0 em caso de erro na conversao
    } // fim try
  } // fim getValorTaxaErro

  /**
   * metodo que limpa a interface a cada nova transmissao
   */
  public void limparInterface() {
    caixaTextoRecebido.clear();
    representMensagemBinariaRecebida.clear();
    representMensagemBinariaTransmitida.clear();
    representSinalRecebido.clear();
    representSinalTransmitido.clear();

    // Para qualquer animação que esteja rodando
    if (animacao != null) {
      animacao.stop();
    }
    // Limpa a fila de quadros pendentes
    inicioFilaAnimacao = null;
    fimFilaAnimacao = null;
    animacaoEmAndamento = false;

    // Limpa o canvas imediatamente
    if (gc != null) {
      gc.clearRect(0, 0, quadroAnimacaoTransmissao.getWidth(), quadroAnimacaoTransmissao.getHeight());
    }

  } // fim limparInterface

}// fim da classe