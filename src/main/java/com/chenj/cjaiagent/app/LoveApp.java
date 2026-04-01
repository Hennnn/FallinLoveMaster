package com.chenj.cjaiagent.app;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.chenj.cjaiagent.advisor.MyLoggerAdvisor;
import com.chenj.cjaiagent.chatmemory.FileBasedChatMemory;
import com.chenj.cjaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.chenj.cjaiagent.rag.QueryRewrite;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;


import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 初始化AI客户端
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {  //传入自带大模型

        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory"; //在本项目文件中生成一个包来放置产生的临时文件数据

        ChatMemory chatMemory = MessageWindowChatMemory.builder()   //构建chatMemory
                .chatMemoryRepository(new FileBasedChatMemory(fileDir)) //记忆地点(可以自己创建一个xxChatMemory）
                .maxMessages(10)
                .build();
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)  //系统提示词
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),//默认拦截器
                        new MyLoggerAdvisor()) //自定义 日志拦截器)
//                .defaultAdvisors(new ReReadingAdvisor()) //自定义重读拦截器 token翻倍
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message 用户消息
     * @param conversationId 会话ID（用于区分不同用户的对话）
     * @return AI 回复内容
     */
    public String doChat(String message, String conversationId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,conversationId)) //设置拦截器参数
                .call()
//                .entity(User.class)
//                .chatResponse().getResult().getOutput().getText() 等价于下一行 也可以用.entity()来指定输出的格式如上一行所示
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("AI 回复内容: {}", content);
        return content;
    }


    record LoveReport(String title, List<String> suggetions){

    }

    /**
     * AI恋爱报告功能  （支持结构化输出的对话 ）
     * @param message
     * @param conversationId
     * @return
     */
    public LoveReport doChatWithReport(String message, String conversationId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,conversationId)) //设置拦截器参数
                .call()
                .entity(LoveReport.class);

        log.info("AI LoveReport: {}", loveReport);
        return loveReport;
    }

    /**
     * 和 AI知识库进行对话 (知识库：本地/云平台）
     */
    @Resource
    private VectorStore loveAppVectorStore;
    @Resource
    private Advisor loveAppRagCloudAdvisor;
    @Resource
    private VectorStore pgVectorVectorStore;
    @Resource
    private QueryRewrite queryRewrite;

    public String doChatWithRag(String message, String conversationId) {
        //将用户提示词进行重写
        String rewriteMessage = queryRewrite.doQueryRewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
//                .user(message) //原始用户输入提示词
                .user(rewriteMessage)//使用改写后的提示词
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,conversationId)) //设置拦截器参数
                .advisors(new MyLoggerAdvisor())

                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build()) //QuestionAnswerAdvisor比较适用于单个测试 RetrievalAugmentationAdvisor适用于批量
//                .advisors(loveAppRagCloudAdvisor)//利用RAG 检索增强服务 (基于云知识库服务）
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())//利用RAG 检索增强服务 (基于PGVector向量存储）

//                .advisors(LoveAppRagCustomAdvisorFactory.create(loveAppVectorStore,"单身"))//自定义RAG检索增强的顾问去服务（文档查询器+上下文增强器）

                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}