package util;

/**
 * classe para tratar execoes personalizadas para melhorar o visual do alertas
 * exibidos na UI
 */
public class ErroDeVerificacaoException extends Exception {
    private String titulo; // o header do alert

    /**
     * construtor
     * 
     * @param titulo   o header do alerta
     * @param mensagem a explicacao do erro
     */
    public ErroDeVerificacaoException(String titulo, String mensagem) {
        super(mensagem); // mensagem padrao
        this.titulo = titulo;

    } // fim construtor

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return getMessage();
    }
}// fim da classe 
