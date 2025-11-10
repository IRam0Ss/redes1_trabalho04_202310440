package controller;

import model.AplicacaoReceptora;
import model.AplicacaoTransmissora;
import model.CamadaAplicacaoReceptora;
import model.CamadaAplicacaoTransmissora;
import model.CamadaEnlaceDadosReceptora;
import model.CamadaEnlaceDadosTransmissora;
import model.CamadaFisicaReceptora;
import model.CamadaFisicaTransmissora;
import model.MeioDeComunicacao;
import util.ErroDeVerificacaoException;

/**
 * representa um no da rede, um dos computadores com toda a arquitetura de
 * camadas presente no model
 */
public class Host {

	// variaveis publicas para alterar menos o codigo ja existente
	// pilha transmissora,
	public AplicacaoTransmissora aplicacaoTransmissora;
	public CamadaAplicacaoTransmissora camadaAplicacaoTransmissora;
	public CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora;
	public CamadaFisicaTransmissora camadaFisicaTransmissora;

	// pilha receptora
	public AplicacaoReceptora aplicacaoReceptora;
	public CamadaAplicacaoReceptora camadaAplicacaoReceptora;
	public CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora;
	public CamadaFisicaReceptora camadaFisicaReceptora;

	// identificados do host
	private String nome;

	/**
	 * monta as pilhas referentes ao Host
	 * 
	 * @param controlerTelaPrincipal referencia para a UI
	 * @param nome                   nome do host, identificador
	 * @param appReceptoraAlvo       caixa de texto que sera exibido o que for
	 *                               recebido
	 */
	public Host(ControlerTelaPrincipal controlerTelaPrincipal, String nome, AplicacaoReceptora appReceptoraAlvo) {

		this.nome = nome;

		System.out.println(" Montando Host : " + nome + "...");
		// montando pilha receptora
		this.aplicacaoReceptora = appReceptoraAlvo; // ponta final do sistema, onde sera exibido o recebido

		this.camadaAplicacaoReceptora = new CamadaAplicacaoReceptora(this.aplicacaoReceptora, controlerTelaPrincipal);
		this.camadaEnlaceDadosReceptora = new CamadaEnlaceDadosReceptora(this.camadaAplicacaoReceptora,
				controlerTelaPrincipal);
		this.camadaFisicaReceptora = new CamadaFisicaReceptora(this.camadaEnlaceDadosReceptora, controlerTelaPrincipal);

		// montando pilha transmissora
		this.camadaFisicaTransmissora = new CamadaFisicaTransmissora(controlerTelaPrincipal);
		this.camadaEnlaceDadosTransmissora = new CamadaEnlaceDadosTransmissora(this.camadaFisicaTransmissora,
				controlerTelaPrincipal);
		this.camadaAplicacaoTransmissora = new CamadaAplicacaoTransmissora(this.camadaEnlaceDadosTransmissora,
				controlerTelaPrincipal);
		this.aplicacaoTransmissora = new AplicacaoTransmissora(this.camadaAplicacaoTransmissora);

		// conexao para que a camada fisica saiba quem eh a superior 
		this.camadaFisicaTransmissora.setCamadaEnlaceSuperior(this.camadaEnlaceDadosTransmissora);

		// conexoes virtuais para que as camdas irmas se conhecam, saibam onde o ACK tem
		// que chegar
		this.camadaEnlaceDadosReceptora.setCamadaEnlaceTransmissoraIrma(this.camadaEnlaceDadosTransmissora);

		// A Camada Enlace (RX) precisa enviar ACKs de volta usando
		// a sua propria pilha de transmissao (TX).
		this.camadaEnlaceDadosReceptora.setAplicacaoTransmissoraIrma(this.aplicacaoTransmissora);

		System.out.println(nome + " construído com sucesso.");

	} // fim class

	/**
	 * metodo para ligar o Host ao meio de comunicacao usado
	 * 
	 * @param meio meio de comunicacao
	 */
	public void setMeioDeComunicacao(MeioDeComunicacao meio) {
		this.camadaFisicaTransmissora.setMeioDeComunicacao(meio);
		this.camadaFisicaReceptora.setMeioDeComunicacao(meio);
	} // fim setMeioDeComunicacao

	/**
	 * metodo atalho para iniciar o envio de mensagem entre um host e outro
	 * 
	 * @param mensagem mensagem a ser enviada
	 */
	public void enviarMensagem(String mensagem) throws ErroDeVerificacaoException{
		System.out.println(nome + ": Iniciando transmissão de DADOS.");
		this.aplicacaoTransmissora.iniciarTransmissao(mensagem);
	}// fim enviarMensagem

} // fim class
