package br.com.alura.loja;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import br.com.alura.loja.modelo.Carrinho;
import br.com.alura.loja.modelo.Produto;
import br.com.alura.loja.modelo.Projeto;
import junit.framework.Assert;

public class ClienteTest {
	
	private HttpServer server;
	private Client client;

	@Before
	public void startServidor() {
		this.server = Servidor.inicializaServidor();
		ClientConfig config = new ClientConfig();
		config.register(new LoggingFilter());
		this.client = ClientBuilder.newClient(config);
		
	}
	
	@After
	public void stopServidor() {
		this.server.stop();
	}
	
	@Test
	public void testaQueAConexaoComOServidorFunciona() {
		
		WebTarget target = client.target("http://www.mocky.io");
		String conteudo = target.path("/v2/52aaf5deee7ba8c70329fb7d").request().get(String.class);
		Assert.assertTrue(conteudo.contains("<rua>Rua Vergueiro 3185"));
	}

	@Test
	public void testaConexaoComProjeto() {
		var target = client.target("http://localhost:8080");
		var projeto = target.path("/projetos/1").request().get(Projeto.class); //projeto serializado com JAXB
		Assert.assertEquals("Minha loja", projeto.getNome());
	}
	
	@Test
	public void testaPostDoProjeto() {
		WebTarget target = client.target("http://localhost:8080");
        Projeto projeto = new Projeto(125, "Quero ser um Dev", 2017);
        
		Entity<Projeto> entity = Entity.entity(projeto, MediaType.APPLICATION_XML);
		Response response = target.path("/projetos").request().post(entity);
		Assert.assertEquals(201, response.getStatus());
		
		String location = response.getHeaderString("Location");
		Projeto projetoCarregado = client.target(location).request().get(Projeto.class); //projeto serializado com JAXB
		Assert.assertTrue(projetoCarregado.getNome().contains("Dev"));
	}
	
	
	
	@Test
	public void testaPostDoCarrinho() {
		WebTarget target = client.target("http://localhost:8080");
		Carrinho carrinho = new Carrinho();
		carrinho.adiciona(new Produto(314L, "Tablet", 999, 1));
        carrinho.setRua("Rua Vergueiro");
        carrinho.setCidade("Sao Paulo");
        String xml = carrinho.toXML();
        
        Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML);
        Response response = target.path("/carrinhos").request().post(entity);
        Assert.assertEquals(201, response.getStatus());
        
        String location = response.getHeaderString("Location");
        String conteudo = client.target(location).request().get(String.class);
        Assert.assertTrue(conteudo.contains("Tablet"));
        
        
	}
	
	

	@Test
	public void testaQueBuscarUmCarrinhoTrazOCarrinhoEsperado() {
		WebTarget target = client.target("http://localhost:8080");
		String conteudo = target.path("/carrinhos/1").request().get(String.class);
		Carrinho carrinho = (Carrinho) new XStream().fromXML(conteudo);
		Assert.assertTrue(carrinho.getCidade().equals("São Paulo"));
		Assert.assertTrue(carrinho.getRua().contains("Vergueiro"));
		Assert.assertEquals("Rua Vergueiro 3185, 8 andar", carrinho.getRua());
	}
}
