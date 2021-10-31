package smartgrid.newsimcontrol.controller;
import topo.SmartGridTopology;
public class DomainLocalController extends CommonsLocalController {
    protected SmartGridTopology getTopo() {
        return this.reactiveSimControl.getTopo();
    }

    protected void setTopo(SmartGridTopology topo) {
        this.reactiveSimControl.setTopo(topo);
    }
}