package pharoslabut.navigate.motionscript;

public class ContextReset extends Instruction {

    @Override
    public InstructionType getType() {
        return InstructionType.CONTEXT_RESET;
    }

    @Override
    public boolean isCompatibleWith(Instruction instr) {
        return true;
    }

}
