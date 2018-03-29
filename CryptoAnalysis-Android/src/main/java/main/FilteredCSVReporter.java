package main;

import java.util.List;
import java.util.function.Predicate;

import crypto.analysis.errors.AbstractError;
import crypto.reporting.CSVReporter;
import crypto.rules.CryptSLRule;

public class FilteredCSVReporter extends CSVReporter {

	private Predicate<AbstractError> filter;

	public FilteredCSVReporter(Predicate<AbstractError> filter, String csvReportFileName, String softwareId, List<CryptSLRule> rules,
			long callGraphConstructionTime) {
		super(csvReportFileName, softwareId, rules, callGraphConstructionTime);
		this.filter = filter;
	}


	@Override
	public void reportError(AbstractError error) {
		if(filter.test(error))
			return;
		super.reportError(error);
	}
}
