package com.chenj.cjaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.chenj.cjaiagent.constant.FileConstant;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类（提供文件读写功能）
 */
public class FileOperationTool {
    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName){  //返回String因为结果还要作为上下文拼接传给ai，不定义这种也许但是还要ai框架多做这一步
        String filePath = FILE_DIR + "/" + fileName;
        try{
            return FileUtil.readUtf8String(filePath);

        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }

    }
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content){
        String filePath = FILE_DIR + "/" + fileName;
        try {
            //创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content,filePath);
            return  "File writeen successfully to:" +filePath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }

}
