package com.chenj.cjaiagent.chatmemory;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class FileBasedChatMemory implements ChatMemory, ChatMemoryRepository {

    private final String BASE_DIR; // 存储路径
    private static final String FILE_EXTENSION = ".kryo";
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        //设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory (String dir){
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if(!baseDir.exists()){
            baseDir.mkdirs();
        }
    }

    /**
     * 获取或创建会话消息的列表
     * @param conversation
     * @return
     */
    private List<Message> getOrCreateConversation(String conversation) {
        File file = getConversationFile(conversation);   // ① 获取会话对应的文件对象
        List<Message> messages = new ArrayList<>();      // ② 准备一个空列表（默认返回值）

        if (file.exists()) {                             // ③ 文件存在才尝试读取
            try (Input input = new Input(new FileInputStream(file))) {  // ④ 建立输入流
                messages = kryo.readObject(input, ArrayList.class);     // ⑤ 核心反序列化 （将输入信息转化为特定类型的消息）
            } catch (IOException e) {
                e.printStackTrace();                     // ⑥ 异常处理（仅打印，不抛出）
            }
        }
        return messages;                                 // ⑦ 返回消息列表
    }

    /**
     *保存会话列表
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId,List<Message> messages ){
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
                kryo.writeObject(output,messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每个会话文件单独保存（几个会话几个文件）
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId){
        return new File(BASE_DIR,conversationId + ".kryo"); //conversationId作为文件名

    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> messageList = getOrCreateConversation(conversationId); //给你一个对话id 返回一个空？消息列表
        messageList.addAll(messages);  //往消息列表中添入我的对话
        saveConversation(conversationId,messageList); //添完了保存为一个文件

    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        return messageList;
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if(file.exists()){
            file.delete();
        }

    }

    @Override
    public List<String> findConversationIds() {
        File dir = new File(BASE_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(FILE_EXTENSION));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .map(File::getName)
                .map(name -> name.substring(0, name.length() - FILE_EXTENSION.length()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return get(conversationId); // 复用 ChatMemory.get
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        // 注意：此方法为覆盖写入，非追加
        saveConversation(conversationId, messages != null ? new ArrayList<>(messages) : Collections.emptyList());
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        clear(conversationId); // 复用 ChatMemory.clear
    }
}
