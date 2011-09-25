package pharoslabut.navigate.motionscript;

import edu.utexas.ece.mpc.context.ContextHandler.WireSummaryType;

public class Context extends Instruction {

    int contextPadding;
    int tau;
    WireSummaryType summaryType;
    
    public Context(int padContextInfoTo, int tau, WireSummaryType summaryType) {
        this.contextPadding = padContextInfoTo;
        this.tau = tau;
        this.summaryType = summaryType;
    }

    public int getContextPadding() {
        return contextPadding;
    }
    
    public int getTau() {
        return tau;
    }
    
    public WireSummaryType getSummaryType() {
        return summaryType;
    }
    
    @Override
    public InstructionType getType() {
        return InstructionType.CONTEXT;
    }

    @Override
    public boolean isCompatibleWith(Instruction instr) {
        return true;
    }

}
