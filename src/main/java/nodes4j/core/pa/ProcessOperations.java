package nodes4j.core.pa;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.reactivex.Observable;
import nodes4j.core.exceptions.DataException;
import nodes4j.core.pa.utils.SortProcess;
import nodes4j.core.pa.utils.SortType;

public class ProcessOperations<T, R> {
	protected Process<T, R> process;
	
	public ProcessOperations(Process<T, R> process) {
		this.process = process;
	}
	
	public ProcessOperations<T, R> data(List<T> data, int min_range) {
		checkData(data);
		
		process.node.data = data;
		process.node.min_range = min_range;
		
		return this;
	}
	
	public ProcessOperations<T, R> data(List<T> data) {
		return data(data, -1);
	}
	
	public ProcessOperations<T, R> filter(Predicate<T> filterOp) {
		process.node.operations.filterOp = filterOp;
		return this;
	}
	
	public ProcessOperations<T, R> map(Function<T, R> mapOp) {
		process.node.operations.mapOp = mapOp;
		return this;
	}
	
	public ProcessOperations<T, R> forEach(Consumer<T> forEachOp) {
		process.node.operations.forEachOp = forEachOp;
		return this;
	}

	public ProcessOperations<T, R> flatMap(Function<List<T>, List<R>> flatMapOp) {
		process.node.operations.flatMapOp = flatMapOp;
		return this;
	}

	public ProcessOperations<T, R> stream(Function<Stream<T>, Stream<R>> streamOp) {
		process.node.operations.streamOp = streamOp;
		return this;
	}
	
	public ProcessOperations<T, R> streamRx(Function<Observable<T>, Observable<R>> streamRxOp) {
		process.node.operations.streamRxOp = streamRxOp;
		return this;
	}
	
	public ProcessOperations<T, R> reduce(BinaryOperator<List<R>> reduceOp) {
		process.node.operations.reduceOp = reduceOp;
		return this;
	}	
	
	public ProcessOperations<?, ?> sortedASC() {
		process.sequence(new SortProcess<>(SortType.SORT_ASCENDING));
		return this;
	}
	
	public ProcessOperations<?, ?> sortedDESC() {
		process.sequence(new SortProcess<>(SortType.SORT_DESCENDING));
		return this;
	}
	
	protected void checkData(List<T> data) {
		if (data==null)
			throw new DataException();
	}
}
