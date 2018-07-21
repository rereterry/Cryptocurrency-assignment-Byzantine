package hw4.consensus.majority;

import hw4.consensus.follow.FollowLeaderPayload;
import hw4.net.Id;
import hw4.net.Message;
import hw4.net.Send;
import hw4.net.Value;
import hw4.util.HashMapList;
import hw4.net.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MajorityVotingMaliciousNode extends Node {

	private boolean isLeaderAndSentInitialValue;

    private Value receivedLeaderDecisionValue;
    private boolean hasRelayedLeaderValue;
    private Map<Id,Value> peerId2ReceivedLeaderDecisionValue = new HashMap();
    private boolean anotherMaliciousValue = true;
    private boolean isMalicious = false;
    
	
	public MajorityVotingMaliciousNode() {

    }

    @Override
    public List<Send> send(int round) {
        List<Send> sends = new ArrayList();
        if (getIsLeader()) {
            if (getLeaderInitialValue() == null) {
                throw new RuntimeException("Leader decision not set");
            }
            Value fakeInitalValue = null;
            for (Value v : getValueSet()) {
                if (!v.equals(getLeaderInitialValue())) {
                	fakeInitalValue = v;
                    break;
                }
            }

            if (!isLeaderAndSentInitialValue) {
              /**  for (Id to : getPeerIds()) {
                    //send different message to different node
                	if(getLeaderInitialValue().getNumber() == 0){
                		Value VALUE_1 = new Value(1);
                		sends.add(new Send(to, new MajorityVotingPayload(VALUE_1)));
                	}else{
                		Value VALUE_0 = new Value(0);
                		sends.add(new Send(to, new MajorityVotingPayload(VALUE_0)));
                	}**/
            	int x = getPeerIds().size();
                for (Id to : getPeerIds()) {
                    if (to.getNumber() % 2 == 0) {
                    	
                        sends.add(new Send(to, new MajorityVotingPayload(getLeaderInitialValue())));
                    } else {
                    	if(to.getNumber() == 1){
                        	sends.add(new Send(to, new MajorityVotingPayload(getLeaderInitialValue())));
                        }else
                        	sends.add(new Send(to, new MajorityVotingPayload(fakeInitalValue)));
                    }
                    
                	
                }
                

                isLeaderAndSentInitialValue = true;
            }
        } else {
            if (receivedLeaderDecisionValue != null) {
            	Value fakeInitalValue = null;
                for (Value v : getValueSet()) {
                    if (!v.equals(receivedLeaderDecisionValue)) {
                    	fakeInitalValue = v;
                        break;
                    }
                }
                if (hasRelayedLeaderValue) {
                    //Do nothing. Already relayed leader value;
                } else {
                    for (Id node : getPeerIds()) {
                    	if(anotherMaliciousValue){
                    		if (node.getNumber() % 2 == 0) {
                                sends.add(new Send(node, new MajorityVotingPayload(receivedLeaderDecisionValue)));
                            } else {
                            	if(node.getNumber() == 1){
                                	sends.add(new Send(node, new MajorityVotingPayload(getLeaderInitialValue())));
                                }else
                                	sends.add(new Send(node, new MajorityVotingPayload(fakeInitalValue)));
                            }
                    	} else{
                    		if (node.getNumber() % 2 == 0) {
                                sends.add(new Send(node, new MajorityVotingPayload(fakeInitalValue)));
                            } else {
                                sends.add(new Send(node, new MajorityVotingPayload(receivedLeaderDecisionValue)));
                            }
                    	}
                    	
                    	//get from another general give the right value to hide self
                        //sends.add(new Send(node, new MajorityVotingPayload(receivedLeaderDecisionValue)));
                    }
                    hasRelayedLeaderValue = true;
                }
            } else {
                //Do nothing. Haven't heard from leader.
            }
        }

        return sends;
    }

    @Override
    public void receive(int round, List<Message> messages) {
    	//same with loyal
    	if (getIsLeader()) {

        } else {
            for (Message m : messages) {
                MajorityVotingPayload payload = m.getSend().getPayload(MajorityVotingPayload.class);
                if (payload != null) {
                    if (m.getFrom().equals(getLeaderNodeId())) {
                        if (receivedLeaderDecisionValue == null) {
                            receivedLeaderDecisionValue = payload.getDecisionValue();
                            peerId2ReceivedLeaderDecisionValue.put(m.getFrom(), payload.getDecisionValue());
                        }
                    } else {
                        if (!peerId2ReceivedLeaderDecisionValue.containsKey(m.getFrom())) {
                            peerId2ReceivedLeaderDecisionValue.put(m.getFrom(), payload.getDecisionValue());
                        }
                    }
                }
            }
        }

    }

    @Override
    public void commit() {
    	
    	Value fakeInitalValue = null;
        for (Value vn : getValueSet()) {
            if (!vn.equals(receivedLeaderDecisionValue)) {
            	fakeInitalValue = vn;
            	isMalicious = true;
                break;
            }
        }
    	if (getIsLeader()) {
    		 	
    		setDecisionValue(getLeaderInitialValue());
        } else {
            int majority = getPeerIds().size()/2+1;
            int count =0;
            HashMapList<Value,Id> value2votes = new HashMapList();
            for (Id n : getPeerIds()) {
                Value nv = peerId2ReceivedLeaderDecisionValue.get(n);
                if (nv == null) {
                    nv = getDefaultValue();
                    count ++;
                }
                value2votes.put(nv, n);
            }
            System.out.println("Node " + getId() + "-> FromLeader: " + receivedLeaderDecisionValue + "; PeerVotes: " + peerId2ReceivedLeaderDecisionValue);

            boolean hasMajority = false;
            for (Value v : value2votes.keySet()) {
                //value2votes.get(v).size()
            	if (count >= majority) {
                    if(isMalicious)
                    	setDecisionValue(fakeInitalValue);
                    else
                    	setDecisionValue(v);
                    hasMajority = true;
                    break;
                }else{
                	if(isMalicious)
                    	setDecisionValue(fakeInitalValue);
                    else
                    	setDecisionValue(v);
                    hasMajority = true;
                    break;
                }
            }

            if (!hasMajority) {
                System.out.println("\tNo majority. Use default.");
                if(isMalicious)
                	setDecisionValue(fakeInitalValue);
                else
                	setDecisionValue(getDefaultValue());
            }
        }

    }

    public void addSybil(MajorityVotingMaliciousNode n) {
    	
    	int valueofone = 0;
		int valueofzero = 0;
		Value VALUE_1 = new Value(1);
		Value VALUE_0 = new Value(0);
    	if (n.getIsLeader()) {
            //setDecisionValue(getLeaderInitialValue());
    		isMalicious = true;
        } else {
            int majority = n.getPeerIds().size()/2+1;
            HashMapList<Value,Id> value2votes = new HashMapList();
            for (Id nid : n.getPeerIds()) {
                Value nv = n.peerId2ReceivedLeaderDecisionValue.get(nid);
                if (nv == null) {
                    nv = n.getDefaultValue();
                }
                value2votes.put(nv, nid);
            }
            //System.out.println("Node " + getId() + "-> FromLeader: " + receivedLeaderDecisionValue + "; PeerVotes: " + peerId2ReceivedLeaderDecisionValue);

            
            for (Value v : value2votes.keySet()) {
                if (v.equals(VALUE_0)) {
                    valueofzero++;
                } else{
                	valueofone++;
                }
            }
            if(valueofzero > valueofone)
            	anotherMaliciousValue = false;

            
        }
    	
    /**	List<Send> msends = new ArrayList();
    	List<Node> knowotherMalicious = new ArrayList();
    	knowotherMalicious.add(n);
    	
    	if (n.getIsLeader()) {
            if (n.getLeaderInitialValue() == null) {
                throw new RuntimeException("mLeader decision not set");
            }

            if (!isLeaderAndSentInitialValue) {
                for (Id to : n.getPeerIds()) {
                    msends.add(new Send(to, new MajorityVotingPayload(getLeaderInitialValue())));
                }

                isLeaderAndSentInitialValue = true;
            }
        } else {
            if (receivedLeaderDecisionValue != null) {
                if (hasRelayedLeaderValue) {
                    //Do nothing. Already relayed leader value;
                } else {
                    for (Id node : n.getPeerIds()) {
                        msends.add(new Send(node, new MajorityVotingPayload(receivedLeaderDecisionValue)));
                    }
                    hasRelayedLeaderValue = true;
                }
            } else {
                //Do nothing. Haven't heard from leader.
            }
        }**/

    }
}
