package smartgrid.newsimcontrol.controller;
import input.ScenarioState;
public class AnalysisLocalController extends DomainLocalController {
    protected ScenarioState getInitalState() {
        return this.reactiveSimControl.getInitialState();
    }

    protected void setInitalState(ScenarioState initialState) {
        this.reactiveSimControl.setInitialState(initialState);
    }

    protected void setImpactInput(ScenarioState impactInput) {
        this.reactiveSimControl.setImpactInput(impactInput);
    }
}