package edu.utexas.ece.mpc.context.group;

import edu.utexas.ece.mpc.context.summary.ContextSummary;
import edu.utexas.ece.mpc.context.summary.GroupContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapGroupContextSummary;

public interface GroupDefinition<T extends HashMapGroupContextSummary> {
    public void handleContextSummary(GroupContextSummary currentGroupSummary,
                                                       ContextSummary newSummary);

    public void handleGroupSummary(GroupContextSummary currentGroupSummary,
                                   ContextSummary newGroupSummary);

    public int getId();
    public T createGroupInstance();
}
