package cn.wubo.message.util;

import cn.wubo.message.message.*;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentUtils {
    
    private ContentUtils() {
    }
    
    public static String toMarkdown(MarkdownContent content) {
        return toMarkdown(content, "\n\n");
    }
    
    /**
     * 将MarkdownContent内容转换为Markdown格式。
     * @param content MarkdownContent内容对象，包含多行不同类型的SubLine内容。
     * @param join 连接符，用于行与行之间的连接。
     * @return 转换后的Markdown字符串。
     */
    public static String toMarkdown(MarkdownContent content, String join) {
        // 使用流处理每行内容，并根据行的类型转换为相应的Markdown格式
        return content.getLines().stream().map(line -> {
            switch (line.getLineType()) {
                case TITLE:
                    // 根据标题级别生成相应的#号和内容
                    SubTitleLine subTitleLine = (SubTitleLine) line;
                    return IntStream.range(0, subTitleLine.getLevel())
                            .mapToObj(i -> "#")
                            .collect(Collectors.joining()) + " " + subTitleLine.getContent();
                case LINK:
                    // 生成Markdown的链接格式
                    SubLinkLine subLinkLine = (SubLinkLine) line;
                    return String.format("[%s](%s)", subLinkLine.getContent(), subLinkLine.getLink());
                case QUOTE:
                    // 生成Markdown的引用格式
                    SubQuoteLine subQuoteLine = (SubQuoteLine) line;
                    return String.format("> %s", subQuoteLine.getContent());
                case BOLD:
                    // 生成Markdown的加粗格式
                    SubBoldLine subBoldLine = (SubBoldLine) line;
                    return String.format("**%s**", subBoldLine.getContent());
                case TEXT:
                default:
                    // 默认情况下直接返回内容
                    return line.getContent();
            }
        }).collect(Collectors.joining(join));
    }
    
    public static JSONArray toPost(MarkdownContent content) {
        JSONArray ja = new JSONArray();
        content.getLines().stream().forEach(line -> {
            JSONObject temp = new JSONObject();
            switch (line.getLineType()) {
                case LINK:
                    SubLinkLine subLinkLine = (SubLinkLine) line;
                    temp.put("tag", "a");
                    temp.put("text", subLinkLine.getContent());
                    temp.put("href", subLinkLine.getLink());
                    break;
                case BOLD:
                    SubBoldLine subBoldLine = (SubBoldLine) line;
                    temp.put("tag", "text");
                    temp.put("text", subBoldLine.getContent());
                    temp.put("style", new JSONArray().add("bold"));
                    break;
                case TEXT:
                case TITLE:
                case QUOTE:
                default:
                    temp.put("tag", "text");
                    temp.put("text", line.getContent());
            }
            ja.add(temp);
        });
        return ja;
    }
    
    public static String toHTML(MarkdownContent content) {
        return HtmlRenderer.builder().build().render(Parser.builder().build().parse(toMarkdown(content, "  ")));
    }
    
}
