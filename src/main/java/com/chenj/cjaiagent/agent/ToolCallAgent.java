package com.chenj.cjaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.chenj.cjaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类 ：具体实现了think和 act方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{

    //可用的工具
    private final ToolCallback[] availableTools;

    //保存工具调用信息的响应结果（要调用哪些工具） 临时变量
    private ChatResponse toolCallChatResponse;

    //工具调用管理者（控制什么时候调用。。。）
    private final ToolCallingManager toolCallingManager;

    //禁用 springAI 内部的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        //自主维护控制工具调用的上下过程与结果，禁用了springAI 内置的工具调用机制（托管给springAi)
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        //1.校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())){
            //若要发送给ai的下一步提示词不为空，则发送给ai
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);//加到当前上下文列表中
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList,this.chatOptions);//为什么用Prompt而不用String因为有chatOption选项可控制工具

        try {
            //2.调用AI大模型，获取工具调用列表
            ChatResponse chatResponse = getChatClient()
                    .prompt(prompt)  //提示词
                    .system(getSystemPrompt()) //系统预设
                    .toolCallbacks(availableTools) //工具们
                    .call()
                    .chatResponse();
            //记录响应，用于等下 Act
            this.toolCallChatResponse = chatResponse;

            //3.解析工具调用结果，获取要调用的工具
            //获取助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            //输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考：" + result); //输出AI名称+的思考：+ 思考结果
            log.info(getName() + "选择了" + toolCallList.size() + "个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            //若不需要调用工具，返回false
            if (toolCallList.isEmpty()){
                //只有调用工具时才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            }else {
                //需要调用工具时，无需手动记录助手消息,因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            //异常处理
            log.error(getName() + "的思考过程遇到了问题:" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     * @return 执行结果
     */
    @Override
    public String act() {
        if (toolCallChatResponse.hasToolCalls()){
            return "没有工具需要调用";
        }
        //调用工具
        Prompt prompt = new Prompt(getMessageList(),this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录消息上下文
        setMessageList(toolExecutionResult.conversationHistory());//也可以用add方法
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        //判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled){
            //任务结束，更改状态
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具" + response.name() + "返回的结果" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}
