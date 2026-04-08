package com.chenj.cjaiagent.agent.model;

/**
 * 代理执行状态的枚举类
 */
public enum AgentState {
    /**
     * 空闲状态 （默认）
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR

}
