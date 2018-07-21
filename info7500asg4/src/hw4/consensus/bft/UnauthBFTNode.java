package hw4.consensus.bft;

import hw4.consensus.follow.FollowLeaderPayload;
import hw4.consensus.majority.MajorityVotingPayload;
import hw4.net.Id;
import hw4.net.Message;
import hw4.net.Send;
import hw4.net.SynchronousExecutor;
import hw4.net.Value;
import hw4.util.HashMapList;
import hw4.net.Node;

import java.util.*;

public class UnauthBFTNode extends Node {

    //All loyal lieutenants obey the same order.
	//private Boolean isAgreement = false;
    //If the commander is loyal, then all loyal lieutenants obey his order.
    //private Boolean isValidity = false;
  
    private boolean isLeaderAndSentInitialValue;

    private Value receivedLeaderDecisionValue;
    private boolean hasRelayedLeaderValue;
    private Map<Trace,Value> peerId2ReceivedLeaderDecisionValue = new HashMap();
    private List<Id> preidlist = new ArrayList();
    private Trace newtrace;
	
	
	public UnauthBFTNode() {
    }

    @Override
    public List<Send> send(int round) {
    	//trace 跟 round 應該有關，之後才能投票
    	
    	List<Send> sends = new ArrayList();
    	
        if (getIsLeader()) {
            if (getLeaderInitialValue() == null) {
                throw new RuntimeException("Leader decision not set");
            }

            if (!isLeaderAndSentInitialValue) {
            	
            	//newtrace = new Trace(getPeerIds());
            	preidlist.add(getId());
        		newtrace = new Trace(preidlist);
            	for (Id to : getPeerIds()) {
            		//preidlist.add(to);
            		//newtrace = new Trace(preidlist);
                	sends.add(new Send(to, new UnauthBFTPayload(newtrace,getLeaderInitialValue())));
                	//preidlist.remove(to);
                }
                

                isLeaderAndSentInitialValue = true;
                return sends;
            }
        } else {
            if (receivedLeaderDecisionValue != null) {
                if (hasRelayedLeaderValue) {
                    //Do nothing. Already relayed leader value;
                } else {
                	preidlist.add(getLeaderNodeId());
                	preidlist.add(getId());
                	newtrace = new Trace(preidlist);
                	for (Id node : getPeerIds()) {
                		//preidlist.add(node);
                		//newtrace = new Trace(preidlist);
                        sends.add(new Send(node, new UnauthBFTPayload(newtrace,receivedLeaderDecisionValue)));
                        //preidlist.remove(node);
                	}
                	
                	
                    hasRelayedLeaderValue = true;
                    return sends;
                }
            } else {
                //Do nothing. Haven't heard from leader.
            }
        }
        
    	return Collections.EMPTY_LIST;
    }

    @Override
    public void receive(int round, List<Message> messages) {
    	
    	
    	if (getIsLeader()) {

        } else {
        	
        /**	if (this.receivedLeaderDecisionValue == null) {
                for (Message m : messages) {
                    if (m.getFrom().equals(getLeaderNodeId())) {
                    	UnauthBFTPayload payload = m.getSend().getPayload(UnauthBFTPayload.class);
                        if (payload != null) {
                            this.receivedLeaderDecisionValue = payload.getDecisionValue();
                        }
                    }
                }
            }**/
        	
        	
        	for (Message m : messages) {
        		UnauthBFTPayload payload = m.getSend().getPayload(UnauthBFTPayload.class);       		
        		if (payload != null) {
        			//preidlist = payload.getTrace().getTrace();
            			if (m.getFrom().equals(getLeaderNodeId())) {
            				if (receivedLeaderDecisionValue == null) {
                                receivedLeaderDecisionValue = payload.getDecisionValue();
                                peerId2ReceivedLeaderDecisionValue.put(payload.getTrace(), payload.getDecisionValue());
                            }
                    
            			} else {
            				//preidlist = payload.getTrace().getTrace();	
            				if (!peerId2ReceivedLeaderDecisionValue.containsKey(m.getFrom())) {      					
            					//Trace.append(payload.getTrace(), m.getFrom());
            					peerId2ReceivedLeaderDecisionValue.put(payload.getTrace(), payload.getDecisionValue());		
                            }
            			}
            		
        		}
        	}
        	
        }  
    	

    }

    @Override
    public void commit() {
    	
    	if (getIsLeader()) {
            setDecisionValue(getLeaderInitialValue());
        } else {
            setDecisionValue(receivedLeaderDecisionValue);
        }
    	
    	
    /**	  if (getIsLeader()) {
              setDecisionValue(getLeaderInitialValue());
          } else {
              int majority = getPeerIds().size()/2+1;
              HashMapList<Value,Id> value2votes = new HashMapList();
              for (Id n : getPeerIds()) {
                  Value nv = peerId2ReceivedLeaderDecisionValue.get(n);
                  if (nv == null) {
                      nv = getDefaultValue();
                  }
                  value2votes.put(nv, n);
              }
              System.out.println("Node " + getId() + "-> FromLeader: " + receivedLeaderDecisionValue + "; PeerVotes: " + peerId2ReceivedLeaderDecisionValue);

              boolean hasMajority = false;
              for (Value v : value2votes.keySet()) {
                  if (value2votes.get(v).size() >= majority) {
                      setDecisionValue(v);
                      hasMajority = true;
                      break;
                  }
              }

              if (!hasMajority) {
                  System.out.println("\tNo majority. Use default.");
                  setDecisionValue(getDefaultValue());
              }
          }**/
    	
    	

    }
}
