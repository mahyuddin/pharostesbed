package robotPerimeter;
import java.util.Set;

import edu.utexas.ece.mpc.context.ContextHandler;
import edu.utexas.ece.mpc.context.group.LabeledGroupDefinition;
import edu.utexas.ece.mpc.context.summary.ContextSummary;
import edu.utexas.ece.mpc.context.summary.GroupContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapContextSummary;
import edu.utexas.ece.mpc.context.util.GroupUtils;

public class TargetGroupDefinition extends LabeledGroupDefinition {

	private static final ContextHandler handler = ContextHandler.getInstance();

	public TargetGroupDefinition(int gId) {
		super(TargetGroupContextSummary.class, gId);
	}

	// following functions adapted from label group definition
	@Override
	public void handleContextSummary(GroupContextSummary currentGroupSummary,
			ContextSummary newSummary) {
		int id = newSummary.getId();
		int gId = currentGroupSummary.getId();
		Set<Integer> groupIds = GroupUtils.getDeclaredMemberships(newSummary);
		if (groupIds.contains(gId)) {
			if (!currentGroupSummary.getMemberIds().contains(id)) {
				handler.logDbg("Adding member " + id + " to group " + getId());
				currentGroupSummary.addMemberId(id);
				if (newSummary instanceof HashMapContextSummary)
					((TargetGroupContextSummary) currentGroupSummary)
							.addLocalSummary((HashMapContextSummary) newSummary);
			}
		}
	}

	@Override
	public void handleGroupSummary(GroupContextSummary currentGroupSummary,
			ContextSummary newGroupSummary) {
		Set<Integer> memberIds = currentGroupSummary.getMemberIds();
		Set<Integer> newMemberIds = GroupUtils.getGroupMembers(newGroupSummary);
		newMemberIds.removeAll(memberIds);
		if (!newMemberIds.isEmpty()) {
			handler.logDbg("Adding members " + newMemberIds + " to group "
					+ getId());
			currentGroupSummary.addMemberIds(newMemberIds);
		}
		// TODO: smart merging, based on timestamps of targetSightings
//		if (newGroupSummary instanceof GenericGroupContextSummary && currentGroupSummary instanceof GenericGroupContextSummary)
//		{
//			for (Integer mem : newMemberIds)
//			{
//				((GenericGroupContextSummary)currentGroupSummary).addLocalSummary(newGroupSummary.get)
//			}
//		}
	}
}
