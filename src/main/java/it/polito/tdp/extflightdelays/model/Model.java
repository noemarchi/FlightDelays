package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	private Graph<Airport,DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport> idMap;
	
	public Model() 
	{
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer,Airport>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) 
	{
		// creo il grafo vuoto
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungere i vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(x, idMap));
		
		//aggiungere gli archi
		for (Rotta r : dao.getRotte(idMap)) 
		{
			if(this.grafo.containsVertex(r.getA1()) 
					&& this.grafo.containsVertex(r.getA2())) 
			{// se il grafo contiene i due vertici
				
				DefaultWeightedEdge edge = this.grafo.getEdge(r.getA1(),r.getA2());
				// estraggo l'arco che collega i due vertici
				
				if(edge == null) 
				{
					// se il grafo non contiene ancora quell'arco, ne creo uno nuovo
					Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getnVoli());
				} 
				else 
				{
					// l'arco esiste già, modifico il peso
					double pesoVecchio = this.grafo.getEdgeWeight(edge);
					double pesoNuovo = pesoVecchio + r.getnVoli();
					this.grafo.setEdgeWeight(edge, pesoNuovo);
				}
			}
		}
		
	}
	
	public int nVertici() 
	{
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() 
	{
		return this.grafo.edgeSet().size();
	}
	
	public List<Airport> getVertici()
	{
		List<Airport> vertici = new ArrayList<>(this.grafo.vertexSet());
		Collections.sort(vertici);
		return vertici;
	}
	
	public List<Airport> getPercorso (Airport a1, Airport a2)
	{
		 List<Airport> percorso = new ArrayList<Airport>();
		 
		 BreadthFirstIterator<Airport,DefaultWeightedEdge> it =
			 new BreadthFirstIterator<>(this.grafo, a1);
		 						// (grafo, vertice di partenza)
		 
		 Boolean trovato = false;
		 
		 //visito il grafo
		 while(it.hasNext()) 
		 {
			 Airport visitato = it.next();
			 
			 // se troviamo l'aeroporto a2 durante la visita
			 // i due aeroporti a1 e a2 sono collegati
			
			 if(visitato.equals(a2))
				 trovato = true;
		 }
		 
		 //ottengo il percorso
		 if(trovato) 
		 {
			 // aggiungo la destinazione
			 percorso.add(a2);
			 
			 // risalgo, dalla destinazione al padre
			 Airport step = it.getParent(a2);
			 
			 // continuo a risalire, fin quando non arrivo alla sorgente
			 while (!step.equals(a1)) 
			 {
				 // aggiungo l'aeroporto in testa alla lista
				 percorso.add(0,step);
				 
				 // calcolo il nuovo predecessore
				 step = it.getParent(step);
			 }
			 
			 // aggiungo la sorgente
			 percorso.add(0,a1);
			 return percorso;
		 } 
		 else 
		 {
			 // i due aeroporti a1 e a2 NON sono collegati
			 return null;
		 }
	}
}



















