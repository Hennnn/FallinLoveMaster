package com.chenj.cjaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.chenj.cjaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类 ：管理代理状态和执行流程
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现step方法
 */
@Data
@Slf4j
public abstract class BaseAgent { //你自己的agent要想创建实例都必须写这些属性

    //核心属性 为后续不同agent的属性提供一个模板/套公式
    private String name;
    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //代理状态 默认我们设置为空闲状态
    private AgentState state = AgentState.IDLE;
    //执行步骤控制
    private int currentStep = 0; //当前步骤
    private int maxSteps = 10; //取决于经费
    // LLM 大模型对象: 为后续自己的agent直接套用已经用ai大模型实现的Client
    private ChatClient chatClient;

    // Memory 记忆(需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();


    /**
     * 运行代理
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt){ //只会反复的对话
        //1.基础校验
        if (this.state != AgentState.IDLE){
            throw new RuntimeException("Cannot run agent from state:" + this.state);
        }
        if(StrUtil.isBlank(userPrompt)){
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        //2.执行 （更改状态防止重复执行）
        this.state = AgentState.RUNNING;
        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> results = new ArrayList<>();
        try{
            //执行循环
            for(int i = 0; i < maxSteps && state != AgentState.FINISHED; i++){
                int stepNumber = i + 1;
                currentStep= stepNumber;
                log.info("Executing step {}/{}",stepNumber,maxSteps);
                //单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            //检查是否超出步骤限制
            if (currentStep >= maxSteps){
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            //列表里的字符串用回车分隔
            return String.join("\n",results);

        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent",e);
            return "执行错误:" + e.getMessage();
        }finally {
            //3.清理资源
            this.cleanup();
        }
    }
    /**
     * 运行代理(流式输出）
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt){ //只会反复的对话
        SseEmitter emitter = new SseEmitter(300000L);
        //使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() ->{
            //1.基础校验
            try{
                if (this.state != AgentState.IDLE){
                    emitter.send("错误：无法从状态运行代理：" + this.state);
                    emitter.complete();
                    return;
                }
                if(StrUtil.isBlank(userPrompt)){
                    emitter.send("错误：不能使用空提示词代理：");
                    emitter.complete();
                    return;
                }

            } catch (Exception e) {
                emitter.completeWithError(e);
            }

            //2.执行 （更改状态防止重复执行）
            this.state = AgentState.RUNNING;
            //记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            //保存结果列表
            List<String> results = new ArrayList<>();
            try{
                //执行循环
                for(int i = 0; i < maxSteps && state != AgentState.FINISHED; i++){
                    int stepNumber = i + 1;
                    currentStep= stepNumber;
                    log.info("Executing step {}/{}",stepNumber,maxSteps);
                    //单步执行
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    //输出当前每一步的结果到 SSE
                    emitter.send(result);
                }
                //检查是否超出步骤限制
                if (currentStep >= maxSteps){
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    emitter.send("执行结束，拿到最大步骤：(" + maxSteps + ")");

                }
                //正常完成
                emitter.complete();
//                //列表里的字符串用回车分隔
//                return String.join("\n",results);

            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent",e);
                try {
                    emitter.send("执行错误:" + e.getMessage());
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
//                return "执行错误:" + e.getMessage();
            }finally {
                //3.清理资源
                this.cleanup();
            }
        });
        //设置超时回调
        emitter.onTimeout(()->{
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        //设置完成回调
        emitter.onCompletion(()->{
            if(this.state == AgentState.RUNNING){
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }

    /**
     * 定义单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){
        //子类可以重写此方法来清洗资源
    }
}
