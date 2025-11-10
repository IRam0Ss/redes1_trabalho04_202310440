package controller;

import model.AplicacaoReceptora;
import model.MeioDeComunicacao;
import util.ErroDeVerificacaoException;

/**
 * Classe responsavel por gerenciar a comunicacao entre as camadas da rede
 */
public class ControleRede {

	// referencia ao controle da UI
	private ControlerTelaPrincipal controlerTelaPrincipal;
	// instancia dos 2 Hosts
	private Host hostA;
	private Host hostB;

	// ponta final da transmissao de A para B
	private AplicacaoReceptora appReceptoraHostB;

	// ponta final da transmissao B para A
	private AplicacaoReceptora appReceptoraHostA;

	// instancia do meio de comunicacao usado na transmissao
	private MeioDeComunicacao meioDeComunicacao;

	public ControleRede(ControlerTelaPrincipal controlerTelaPrincipal) {

		this.controlerTelaPrincipal = controlerTelaPrincipal;

		// cria as pontas das comunicacaoes (as caixas de texto)

		// na simulacao HostB vai ser quem recebe a mensagem e envia o ACK entao ele
		// precisa atualizar o UI
		this.appReceptoraHostB = new AplicacaoReceptora(this.controlerTelaPrincipal);

		// na simulacao o HostA eh quem transmite e recebe o Ack entao nao precisa
		// exibir nada na tela
		this.appReceptoraHostA = new AplicacaoReceptora(null);

		// cria os dois hosts
		// HostA = Transmissor da nossa simulacao
		this.hostA = new Host(this.controlerTelaPrincipal, "HostA", this.appReceptoraHostA);

		// HostB = Receptor da nossa simulacao
		this.hostB = new Host(this.controlerTelaPrincipal, "HostB", this.appReceptoraHostB);

		// cria o meio de comunicacao que sera usado tanto para transmitir a mensagem
		// quanto o ACK
		// precisa ter conhecimento das camadas fisicas dos hosts
		this.meioDeComunicacao = new MeioDeComunicacao(this.hostA.camadaFisicaTransmissora,
				this.hostA.camadaFisicaReceptora, this.hostB.camadaFisicaTransmissora, this.hostB.camadaFisicaReceptora,
				this.controlerTelaPrincipal);

		// conecta os hosts ao meio de comunicacao
		this.hostA.setMeioDeComunicacao(this.meioDeComunicacao);
		this.hostB.setMeioDeComunicacao(this.meioDeComunicacao);

	} // fim construtor

	/**
	 * metodo que inicia simulacao, comunicacao entre hostA enviando uma mensagem
	 * 
	 * @param mensagem mensagem as ser transmitida
	 */
	public void iniciarSimulacao(String mensagem) throws ErroDeVerificacaoException {
		this.hostA.enviarMensagem(mensagem);
	}// fim de iniciarSimulacao

} // fim classe ControleRede
