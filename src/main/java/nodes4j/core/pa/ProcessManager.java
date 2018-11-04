package nodes4j.core.pa;

import static nodes4j.core.ActorMessageTag.DATA;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import actor4j.core.ActorSystem;
import actor4j.core.actors.Actor;
import actor4j.core.immutable.ImmutableList;
import actor4j.core.messages.ActorMessage;
import actor4j.core.utils.ActorFactory;
import nodes4j.core.NodeActor;

public class ProcessManager {
	protected ActorSystem system;
	protected Runnable onTermination;
	
	protected Process<?, ?> mainProcess;
	
	protected boolean debugDataEnabled;
	
	public ProcessManager() {
		this(false);
	}
	
	public ProcessManager(boolean debugDataEnabled) {
		super();
		this.debugDataEnabled = debugDataEnabled;
	}
	
	public ProcessManager onTermination(Runnable onTermination) {
		this.onTermination = onTermination;
		
		return this;
	}
	
	public void start(Process<?, ?> process) {
		mainProcess = process;
		
		system = new ActorSystem("nodes4j");
		mainProcess.node.nTasks = Runtime.getRuntime().availableProcessors()/*stand-alone*/;
		mainProcess.node.isRoot = true;
		mainProcess.data = new ConcurrentHashMap<>();
		mainProcess.result = new ConcurrentHashMap<>();
		mainProcess.aliases = new ConcurrentHashMap<>();
		
		UUID root = system.addActor(new ActorFactory() {
			@Override
			public Actor create() {
				return new NodeActor<>("root", mainProcess.node, mainProcess.result, mainProcess.aliases, debugDataEnabled, mainProcess.data);
			}
		});

		system.send(new ActorMessage<>(null, DATA, root, root));
		system.start(null, onTermination);
	}
	
	/*
	public void stop() {
		if (system!=null)
			system.shutdownWithActors(true);
	}
	*/
	
	public List<?> getData(UUID id) { 
		return mainProcess.data.get(id);
	}
	
	public List<?> getData(String alias) {
		List<?> result = null;
		
		UUID id = mainProcess.aliases.get(alias);
		if (id!=null)
			result = getData(id);
		
		return result;
	}
	
	public List<?> getResult(UUID id) {
		List<?> result = null;
		
		Object obj = mainProcess.result.get(id);
		if (obj!=null)
			result = ((ImmutableList<?>)mainProcess.result.get(id)).get(); 
		
		return result;
	}
	
	public List<?> getFirstResult() {
		if (mainProcess.result.values().iterator().hasNext())
			return ((ImmutableList<?>)mainProcess.result.values().iterator().next()).get();
		else
			return null;
	}
	
	public List<?> getResult(String alias) {
		List<?> result = null;
		
		UUID id = mainProcess.aliases.get(alias);
		if (id!=null)
			result = getResult(id);
		
		return result;
	}
}
