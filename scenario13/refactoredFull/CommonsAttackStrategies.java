package smartgrid.attackersimulation.strategies;
public abstract class CommonsAttackStrategies {
    protected final boolean ignoreLogicalConnections;

    protected final int hackingSpeed;

    public CommonsAttackStrategies(final boolean ignoreLogicalConnections, final int hackingSpeed) {
        this.ignoreLogicalConnections = ignoreLogicalConnections;
        this.hackingSpeed = hackingSpeed;
    }

    protected boolean checkMaxHackingOperations(final int operationCount) {
        return operationCount < this.hackingSpeed;
    }

    protected int getHackingSpeed() {
        return this.hackingSpeed;
    }
}