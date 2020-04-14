package com.bubble.crawler.job.zk.election;


/**
 * 监控选举的状态：什么时候开始和结束
 * An interface to be implemented by clients that want to receive election
 * events.
 */
public interface LeaderElectionAware {

    /**
     * Called during each state transition. Current, low level events are provided
     * at the beginning and end of each state. For instance, START may be followed
     * by OFFER_START, OFFER_COMPLETE, DETERMINE_START, DETERMINE_COMPLETE, and so
     * on.
     *
     * @param eventType
     */
    void onElectionEvent(LeaderElectionSupport.EventType eventType);

}
