package com.wulang.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.wulang.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具（文件读写功能）
 */
public class FileOperationTool {

    public String FIR_DIR = FileConstant.File_SAVE_DIR + "/file";

    @Tool(description = "Read Content form a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String filePath = FIR_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "error reading file: " + e.getMessage();
        }

    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of a file to write") String fileName, @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FIR_DIR + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(filePath);
            // 写入文件
            FileUtil.writeUtf8String(content, filePath);
            return "File write successfully to " + filePath;
        } catch (Exception e) {
            return "error writing file: " + e.getMessage();
        }

    }
}
