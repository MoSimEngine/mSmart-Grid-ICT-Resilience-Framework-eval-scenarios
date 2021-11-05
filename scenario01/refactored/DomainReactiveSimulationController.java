package smartgrid.newsimcontrol.controller;
import topo.SmartGridTopology;
public class DomainReactiveSimulationController extends CommonsReactiveSimulationController {
    protected SmartGridTopology topo;

    public DomainReactiveSimulationController() {
        this.timeStep = 0;
    }

    protected SmartGridTopology getTopo() {
        return this.topo;
    }

    protected void setTopo(SmartGridTopology topo) {
        this.topo = topo;
    }
}