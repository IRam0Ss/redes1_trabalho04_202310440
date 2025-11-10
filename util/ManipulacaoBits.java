package util;

/**
 * Classe responsavel por todos os metodos relacionados a manipulacao de bits
 * que foram utilizados
 */
public class ManipulacaoBits {

  /**
   * converte uma String de qualquer tamanho em um array de inteiros (int[]),
   * onde cada inteiro armazena 32 bits da mensagem de forma agrupada.
   * 
   * @param mensagem A String a ser convertida.
   * @return Um array de int[] com os bits da mensagem agrupados.
   */
  public static int[] stringParaIntAgrupado(String mensagem) {

    char[] charMensagem = mensagem.toCharArray(); // converte a mensagem para o array de caracteres equivalentes
    int totalBits = (8 * charMensagem.length); // armazena o total de bits daquela mensagem

    // calcular quantos inteiros serao necessarios para armazenar a mensagem,
    // considerando que cada inteiro armazena 32 bits
    int tamanhoArray = (totalBits + 31) / 32; // para garantir que arredonde pra cima em casos nao exatos
    int[] pacoteBits = new int[tamanhoArray];

    int contadorBits = 0; // acompanhar os bits

    // loop por caractere da mensagem
    for (char caracter : charMensagem) {

      // loop pra cada bit do Char
      for (int i = 0; i < 8; i++) {

        int bit = (caracter >> (7 - i)) & 1; // 1 -> mascara usada (0000 0001);

        // Se o bit for 1, precisamos "liga-lo" na posicao correta
        if (bit == 1) {
          int indicePacote = contadorBits / 32; // calcula o indice que o bit sera armazenado
          int posicaoNoPacote = 31 - (contadorBits % 32); // calcula a posicao correta para nao sobrepor bits ja
                                                          // armazenados
          pacoteBits[indicePacote] = pacoteBits[indicePacote] | (1 << posicaoNoPacote); // armazena o bit no array
        }
        contadorBits++;
      }
    }
    return pacoteBits;
  } // fim metodo

  /**
   * Converte um array de inteiros (pacotes de 32 bits) de volta para uma String.
   * 
   * @param pacoteBits pacotesDeBits O array de int[] com os bits agrupados.
   * @param totalBits  totalDeBits O numero TOTAL de bits validos (a mensagem
   *                   original pode nao ocupar o ultimo int por completo)
   * @return A String resultante
   */
  public static String intAgrupadoParaString(int[] pacoteBits) {

    int totalBits = descobrirTotalDeBitsReais(pacoteBits); // descobre o total de bits validos no array
    int totalChar = totalBits / 8; // calcula o total de caracteres uma vez que cada char sao 8 bits
    char[] charMensagem = new char[totalChar]; // cria o array de chars para mensagem

    // loop de reconstrucao dos char
    for (int i = 0; i < totalChar; i++) {
      int valorChar = 0; // inicializa o valor numerico do char como 0

      // ler os 8 bits de um char
      for (int j = 0; j < 8; j++) {
        int contadorBits = i * 8 + j;
        int indicePacote = contadorBits / 32;
        int posicaoNoPacote = 31 - (contadorBits % 32);

        // extrai bit do pacote
        int bit = (pacoteBits[indicePacote] >> posicaoNoPacote) & 1;

        if (bit == 1) { // caso o bit seja 1 adiciona o bit um no valor do char, para gerar o binario
                        // equivalente que sera convertido novamente para char
          valorChar = valorChar | (1 << (7 - j));
        }

      } // fim do loop por char
      if (valorChar != 0) { // caso o valor do char seja valido, adiciona o char no array da mensagem
        charMensagem[i] = (char) valorChar; // converte o binario do bit num array de char
      }

    } // fim do loop

    String mensagemDecodificada = new String(charMensagem); // cria uma string com o array de char decodificado

    return mensagemDecodificada;
  }// fim do metodo

  /**
   * exibe os bits de cada um dos inteiros dentro de um array
   * 
   * @param public static String exibirBitsStr(int[] mensagemBinaria) {
   * @return retorna uma string para vizualizacao da representacao de bits
   */
  public static String exibirBitsStr(int[] mensagemBit) {

    int marcara = 1 << 31; // uma mascara com o 1 mais a esquerda possivel
    String bitsString = ""; // string que vai exibir os bits

    for (int bits : mensagemBit) { // percorre toda a mensagem
      for (int i = 1; i <= 32; i++) { // percorre todos os 32 bits dos inteiros

        bitsString += (bits & marcara) == 0 ? "0" : "1"; // adiciona a string o bit equivalente

        bits = bits << 1; // desloca para a direita o inteiro, garantindo que percorra os 32 bits

        if (i % 8 == 0) { // a cada 8 bits um espaco vazio
          bitsString += " ";
        }
      }
    }

    return bitsString; // a string com a representacao dos bits

  }// fimd do metodo

  /**
   * metodo que transforma os bits compactados em um array de fluxo simples de
   * inteiro, SOMENTE UTILIZADO PARA REALIZAR A ANIMACAO
   * 
   * @param pacotesDeBits     o array com os bits
   * @param totalDeBitsMaximo o total maximo de bits que a mensagem original pode
   *                          possuia
   * @return uma versao simplificada onde cada inteiro "eh" um bit.
   */
  public static int[] desempacotarBits(int[] pacotesDeBits, int totalDeBitsMaximo) {

    int verdadeiroTotalBits = 0;

    int totalBytes = pacotesDeBits.length * 4; // Calcula o numero maximo de bytes no array

    // Loop de tras para frente, byte por byte, para encontrar o ultimo byte que nao
    // eh zero.
    for (int i = totalBytes - 1; i >= 0; i--) {
      // Le 8 bits na posicao atual
      int byteAtual = lerBits(pacotesDeBits, i * 8, 8);

      // Se o byte lido for diferente de zero, encontramos o fim da mensagem real!
      if (byteAtual != 0) {
        // O tamanho real da mensagem eh a posicao deste byte + 1.
        verdadeiroTotalBits = (i + 1) * 8;
        break; // podemos parar o loop.
      } // fim if
    } // fim do for

    // Caso especial: se a mensagem for toda de zeros ou vazia, evitamos um erro.
    if (verdadeiroTotalBits == 0 && totalDeBitsMaximo > 0) {
      // Se a mensagem original era "0", por exemplo, o loop acima nao acharia nada.
      // Vamos checar o primeiro byte. Se ele for 0, mas a mensagem nao era vazia,
      // entao o tamanho eh de pelo menos 8 bits.
      if (lerBits(pacotesDeBits, 0, 8) == 0) {
        verdadeiroTotalBits = 8; // Assumimos que era um caractere NUL.
      } // fim if
    } // fim if

    // Cria o array final que tera o tamanho exato do numero de bits validos.
    int[] fluxoSimples = new int[verdadeiroTotalBits];

    // Percorre todos os bits VALIDOS do pacote e os adiciona a versao simplificada.
    for (int i = 0; i < verdadeiroTotalBits; i++) {
      int indiceDoPacote = i / 32;
      int posicaoNoPacote = 31 - (i % 32);

      // "Pesca" o bit e armazena no nosso array de fluxo simples
      int bit = (pacotesDeBits[indiceDoPacote] >> posicaoNoPacote) & 1;
      fluxoSimples[i] = bit;
    }

    return fluxoSimples; // retorna o array simplificado
  }// fim do metodo

  /**
   * metodo que escreve um valor binario (0 ou 1) em um array de inteiros, em uma
   * posicao especifica, considerando que cada inteiro armazena 32 bits
   * 
   * @param destino          array onde sera escrito o bit
   * @param bitInicial       a posicao do bit inicial no array de destino onde o
   *                         bit sera escrito
   * @param valorEscrever    o valor que contem os bits a serem escritos
   * @param quantidadeDeBits o numero de bit a serem lidos do valor a escrever (da
   *                         direita para esquerda) normalmente sera 8. Visando
   *                         controlar a carga de controle
   */
  public static void escreverBits(int[] destino, int bitInicial, int valorEscrever, int quantidadeDeBits) {

    for (int i = 0; i < quantidadeDeBits; i++) {

      int bitEscrever = valorEscrever >> (quantidadeDeBits - 1 - i) & 1; // extrai o bit a ser escrito

      int posicaoGlobal = bitInicial + i; // calcula a posicao global do bit a ser escrito
      int indiceDoPacote = posicaoGlobal / 32; // calcula o indice do pacote, descobre em qual inteiro do array sera
                                               // escrito
      int posicaoNoPacote = 31 - (posicaoGlobal % 32); // calcula a posicao dentro do pacote onde o bit sera escrito

      if (bitEscrever == 1) {
        // "Liga" o bit (usa OR)
        destino[indiceDoPacote] = destino[indiceDoPacote] | (1 << posicaoNoPacote); // escreve o bit 1 na posicao
      } // fim if
      else { // ou seja bit 0
        // "Desliga" o bit (usa AND com uma mascara invertida)
        destino[indiceDoPacote] = destino[indiceDoPacote] & ~(1 << posicaoNoPacote); // escreve o bit 0
      }

    } // fim for

  }// metodo escreverBits

  /**
   * metodo que le um conjunto de bits de um array de inteiros, em uma posicao
   * especifica, considerando que cada inteiro armazena 32 bits
   * 
   * @param origem           o array de onde os bits serao lidos
   * @param bitInicial       a posicao do bit inicial no array de destino onde o
   *                         bit sera lido
   * @param quantidadeDeBits o numero de bit a serem lidos do valor a escrever (da
   *                         direita para esquerda) normalmente sera 8. Visando
   *                         controlar a carga de controle
   * @return o conjunto de bits lido
   */
  public static int lerBits(int[] origem, int bitInicial, int quantidadeDeBits) {

    int valorLido = 0; // o valor que sera retornado

    // Garante que não tentemos ler mais de 32 bits (o limite de um int)
    if (quantidadeDeBits > 32) {
      System.out.println("Erro: Nao eh possivel ler mais de 32 bits para um unico int.");
      return 0;
    }

    for (int i = 0; i < quantidadeDeBits; i++) {

      int posicaoGlobal = bitInicial + i; // calcula a posicao global do bit a ser lido
      int indiceDoPacote = posicaoGlobal / 32; // calcula o indice do pacote, descobre em qual inteiro do array sera
                                               // escrito
      int posicaoNoPacote = 31 - (posicaoGlobal % 32); // calcula a posicao dentro do pacote onde o bit sera escrito

      int bitLido = (origem[indiceDoPacote] >> posicaoNoPacote) & 1; // extrai o bit da posicao correta

      // se o bit lido for 1 adiciona ao valor lido
      if (bitLido == 1) {
        valorLido = valorLido | (1 << (quantidadeDeBits - 1 - i));
      }

    } // fim for

    return valorLido; // retorna o valor lido

  } // fim metodo lerBits

  /**
   * Inspeciona um quadro e descobre o numero total de bits significantes,
   * procurando pelo ultimo bit '1' no array.
   * 
   * @param quadro O array de inteiros contendo o fluxo de bits.
   * @return O numero total de bits validos no quadro.
   */
  public static int descobrirTotalDeBitsReais(int[] quadro) {
    if (quadro == null || quadro.length == 0) { // quadro vazio
      return 0;
    }

    int ultimoBitUm = -1;
    // Percorre o quadro do ULTIMO bit possivel para o PRIMEIRO
    // (quadro.length * 32) - 1 e a posicao do ultimo bit no array
    for (int i = (quadro.length * 32) - 1; i >= 0; i--) {
      // le o bit na posicao atual
      if (lerBits(quadro, i, 1) == 1) {
        ultimoBitUm = i; // Encontrou a posicao (indice 0 a N) do ultimo '1'
        break; // Pode parar de procurar
      }
    }

    // nao encontrou nenhum bit '1'.
    if (ultimoBitUm == -1) {
      if (quadro.length > 0)
        return 8;
      return 0; // Quadro vazio
    }

    int byteOndeOcorreu = ultimoBitUm / 8;

    return (byteOndeOcorreu + 1) * 8;
  } // fim do metodo descobrirTotalDeBitsReais

}// fim da classe
