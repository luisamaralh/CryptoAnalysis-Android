package main;

import boomerang.BackwardQuery;
import boomerang.Query;
import boomerang.jimple.Statement;
import boomerang.jimple.Val;
import boomerang.results.ForwardBoomerangResults;
import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import crypto.analysis.AnalysisSeedWithSpecification;
import crypto.analysis.EnsuredCryptSLPredicate;
import crypto.analysis.IAnalysisSeed;
import crypto.analysis.errors.*;
import crypto.extractparameter.CallSiteWithParamIndex;
import crypto.extractparameter.ExtractedValue;
import crypto.interfaces.ISLConstraint;
import crypto.reporting.ErrorMarkerListener;
import crypto.rules.CryptSLPredicate;
import crypto.rules.CryptSLRule;
import soot.SootMethod;
import sync.pds.solver.nodes.Node;
import typestate.TransitionFunction;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PureReporter extends ErrorMarkerListener {

    private String name;
    private long callGraphConstructionTime;
    private Stopwatch analysisTime = Stopwatch.createUnstarted();
    private Set<AbstractError> errors = Sets.newHashSet();
    private List<CryptSLRule> rules;
    private Set<SootMethod> dataflowReachableMethods = Sets.newHashSet();
    private int seeds = 0;


    public PureReporter (String name, List<CryptSLRule> rules, long callGraphConstructionTime){
        this.rules = rules;
        this.callGraphConstructionTime = callGraphConstructionTime;
        this.name = name;
    }

    @Override
    public void beforeAnalysis() {
        analysisTime.start();
    }

    @Override
    public void afterAnalysis() {
        analysisTime.stop();
    }

    @Override
    public void beforeConstraintCheck(AnalysisSeedWithSpecification analysisSeedWithSpecification) {

    }

    @Override
    public void afterConstraintCheck(AnalysisSeedWithSpecification analysisSeedWithSpecification) {

    }

    @Override
    public void beforePredicateCheck(AnalysisSeedWithSpecification analysisSeedWithSpecification) {

    }

    @Override
    public void afterPredicateCheck(AnalysisSeedWithSpecification analysisSeedWithSpecification) {

    }

    @Override
    public void seedStarted(IAnalysisSeed analysisSeedWithSpecification) {

    }

    @Override
    public void boomerangQueryStarted(Query seed, BackwardQuery q) {

    }

    @Override
    public void boomerangQueryFinished(Query seed, BackwardQuery q) {

    }

    @Override
    public void ensuredPredicates(Table<Statement, Val, Set<EnsuredCryptSLPredicate>> existingPredicates,
                                  Table<Statement, IAnalysisSeed, Set<CryptSLPredicate>> expectedPredicates,
                                  Table<Statement, IAnalysisSeed, Set<CryptSLPredicate>> missingPredicates) {

    }

    @Override
    public void reportError(AbstractError error) {
        errors.add(error);
    }

    @Override
    public void checkedConstraints(AnalysisSeedWithSpecification analysisSeedWithSpecification, Collection<ISLConstraint> relConstraints) {

    }

    @Override
    public void onSeedTimeout(Node<Statement, Val> seed) {

    }

    @Override
    public void onSeedFinished(IAnalysisSeed seed, ForwardBoomerangResults<TransitionFunction> forwardResults) {
        dataflowReachableMethods.addAll(forwardResults.getStats().getCallVisitedMethods());
    }

    @Override
    public void discoveredSeed(IAnalysisSeed curr) {
        seeds++;
    }

    @Override
    public void collectedValues(AnalysisSeedWithSpecification seed, Multimap<CallSiteWithParamIndex, ExtractedValue> collectedValues) {

    }
    
    @Override
    public void onSecureObjectFound(IAnalysisSeed analysisObject) {

    }

    public Set<AbstractError> getErrors() {
        return errors;
    }

    public Long analysisTime() {
        return analysisTime.elapsed(TimeUnit.MILLISECONDS);
    }

    public AnalysisResults getResult()  {
        return new AnalysisResults(name, errors, analysisTime());
    }

}