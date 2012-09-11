package edu.utexas.ece.mpc.context.group;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import edu.utexas.ece.mpc.context.ContextHandler;
import edu.utexas.ece.mpc.context.summary.ContextSummary;
import edu.utexas.ece.mpc.context.summary.GroupContextSummary;
import edu.utexas.ece.mpc.context.summary.HashMapGroupContextSummary;
import edu.utexas.ece.mpc.context.util.GroupUtils;

public class LabeledGroupDefinition<T extends HashMapGroupContextSummary> implements GroupDefinition<T> {
    private static final ContextHandler handler = ContextHandler.getInstance();
    private final int gId;
    Class<T> groupType;

    public LabeledGroupDefinition(Class<T> type, int gId) {
        this.gId = gId;
        groupType = type;
    }

    @Override
    public int getId() {
        return gId;
    }

    @Override
    public void handleContextSummary(GroupContextSummary currentGroupSummary,
                                     ContextSummary newSummary) {
        int id = newSummary.getId();
        int gId = currentGroupSummary.getId();
        Set<Integer> groupIds = GroupUtils.getDeclaredMemberships(newSummary);
        if (groupIds.contains(gId)) {
            if (!currentGroupSummary.getMemberIds().contains(id)) {
                handler.logDbg("Adding member " + id + " to group " + gId);
                currentGroupSummary.addMemberId(id);
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
            handler.logDbg("Adding members " + newMemberIds + " to group " + gId);
            currentGroupSummary.addMemberIds(newMemberIds);
        }
    }

	@Override
	public T createGroupInstance() {
			try {
				Constructor c = groupType.getClass().getConstructor(new Class[]{Integer.TYPE});
				return (T) c.newInstance(new Object[]{new Integer(gId)});
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
}
