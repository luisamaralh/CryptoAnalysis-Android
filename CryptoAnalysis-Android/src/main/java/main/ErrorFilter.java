package main;

import java.util.function.Predicate;

import crypto.analysis.errors.AbstractError;
import soot.SootMethod;

class ErrorFilter implements Predicate<AbstractError> {

	private String filterString;

	public ErrorFilter(String filterString) {
		this.filterString = filterString;
	}

	@Override
	public boolean test(AbstractError t) {
		return t.getErrorLocation().getMethod().getDeclaringClass().toString().contains(filterString);
	}

	@Override
	public String toString() {
		return filterString;
	}
}