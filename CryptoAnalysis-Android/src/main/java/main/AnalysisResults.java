package main;


import crypto.analysis.errors.AbstractError;
import java.util.Set;

public class AnalysisResults {

    private String name;
    private Long analysisTime;
    Set<AbstractError> errors;

    public AnalysisResults(String name, Set<AbstractError> errors, Long analysisTime){
        this.name = name;
        this.errors = errors;
        this.analysisTime = analysisTime;
    }

    public String getApplication(){
        return name;
    }

    public Long getTime() {
        return analysisTime;
    }

    public Set<AbstractError> getErrors() {
       return errors;
    }
}

