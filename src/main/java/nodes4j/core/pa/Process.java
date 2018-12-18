package nodes4j.core.pa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.reactivex.Observable;
import nodes4j.core.Node;

public class Process<T, R> {
	protected Node<T, R> node;
	
	protected Map<UUID, List<?>> data; // initial set over ProcessManager
	protected Map<UUID, List<?>> result; // initial set over ProcessManager
	protected Map<String, UUID> aliases; // initial set over ProcessManager
	
	protected ProcessOperations<T, R> processOperations;
	
	public Process() {
		this(null);
	}
	
	public Process(String alias) {
		super();
		
		node = new Node<>(alias);
		node.id = UUID.randomUUID();
		node.sucs = new HashSet<>();
		node.pres = new HashSet<>();
		
		processOperations = new ProcessOperations<>(this);
	}
	
	public Process(Function<List<T>, List<R>> flatMapOp, BinaryOperator<List<R>> reduceOp) {
		this();
		
		node.operations.flatMapOp = flatMapOp;
		node.operations.reduceOp = reduceOp;
	}
	
	public UUID getId() {
		return node.id;
	}
	
	public ProcessOperations<T, R> data(List<T> data, int min_range) {
		return processOperations.data(data, min_range);
	}
	
	public ProcessOperations<T, R> data(List<T> data) {
		return processOperations.data(data);
	}
	
	public ProcessOperations<T, R> filter(Predicate<T> filterOp) {
		return processOperations.filter(filterOp);
	}
	
	public ProcessOperations<T, R> map(Function<T, R> mapOp) {
		return processOperations.map(mapOp);
	}
	
	public ProcessOperations<T, R> forEach(Consumer<T> forEachOp) {
		return processOperations.forEach(forEachOp);
	}
	
	public ProcessOperations<T, R> flatMap(Function<List<T>, List<R>> flatMapOp) {
		return processOperations.flatMap(flatMapOp);
	}
	
	public ProcessOperations<T, R> stream(Function<Stream<T>, List<R>> streamOp) {
		return processOperations.stream(streamOp);
	}
	
	public ProcessOperations<T, R> streamRx(Function<Observable<T>, Observable<R>> streamRxOp) {
		return processOperations.streamRx(streamRxOp);
	}
	
	public ProcessOperations<T, R> reduce(BinaryOperator<List<R>> reduceOp) {
		return processOperations.reduce(reduceOp);
	}	
	
	public ProcessOperations<?, ?> sortedASC() {
		return processOperations.sortedASC();
	}
	
	public ProcessOperations<?, ?> sortedDESC() {
		return processOperations.sortedDESC();
	}
			
	public <S> Process<R, S> sequence(Process<R, S> process) {
		node.sucs.add(process.node);
		process.data = data;
		process.result = result;
		
		return process;
	}
	
	public Process<?, ?> sequence(List<Process<?, ?>> processes) {
		Process<?, ?> parent = this;
		if (processes!=null) {
			for (Process<?, ?> p : processes) {
				parent.node.sucs.add(p.node);
				p.node.pres.add(parent.node);
				parent = p;
				p.data = data;
				p.result = result;
			}
		}
			
		return parent;
	}
	
	public Process<?, ?> sequence(Process<?, ?>... processes) {
		return sequence(Arrays.asList(processes));
	}
	
	public List<Process<R, ?>> parallel(List<Process<R, ?>> processes) {
		if (processes!=null)
			for (Process<R, ?> p : processes)
				sequence(p);
		
		return processes;
	}
	
	@SuppressWarnings("unchecked")
	public List<Process<R, ?>> parallel(Process<R, ?>... processes) {
		return parallel(Arrays.asList(processes));
	}
	
	public Process<T, R> merge(List<Process<?, ?>> processes) {
		if (processes!=null) {
			if (processes.size()>0) {
				data = processes.get(0).data;
				result = processes.get(0).result;
			}
			for (Process<?, ?> p : processes) {
				node.pres.add(p.node);
				p.node.sucs.add(node);
			}
		}
		
		return this;
	}
	
	public Process<T, R> merge(Process<?, ?>... processes) {
		return merge(Arrays.asList(processes));
	}
	
	public List<?> getData() {
		return node.data;
	}
	
	public List<?> getResult() {
		System.out.println(result); // TODO: BUG
		return result.get(node.id);
	}
}
