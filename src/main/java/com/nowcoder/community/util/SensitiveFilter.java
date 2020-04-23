package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    //日志记录
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //定义替换敏感词的常量
    private static final String REPLACEMENT = "***";

    //定义前缀树的某一个结点
    private class TrieNode{

        //尾结点:敏感词结束的标识
        private boolean isKeywordEnd = false;

        //子节点(key是下级字符，value是下级结点)
        private Map<Character,TrieNode>  subNodes = new HashMap<>();

        //判断是否是尾节点
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        //设置关键词的尾结点
        public void setKeywordEnd(boolean keywordEnd) {
            this.isKeywordEnd = keywordEnd;
        }

        //添加子节点的方法
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }

    //初始化根节点
    private TrieNode rootNode = new TrieNode();

    //初始化
    @PostConstruct
    public void init(){
        try (
                //类路径加载器:加载敏感词文件
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
                //读取敏感词
                String keyword;
                while ((keyword = reader.readLine()) != null){
                    //添加到前缀树
                    this.addKeyword(keyword);
                }
        } catch (Exception e) {
            logger.error("加载敏感词失败" + e.getMessage());
        }

    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        //设置一个指针
        TrieNode tempNode = rootNode;
        for (int i = 0 ; i < keyword.length() ; i++){
            char c = keyword.charAt(i);
            //判断指针指向的字符下有没有子节点
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                //将子节点挂到当前字符下
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一个循环
            tempNode = subNode;

            //设置结束标识
            if (i == keyword.length()-1){
                //将指针指向的节点标识为尾节点，前缀树的一条分支扫描结束
                tempNode.setKeywordEnd(true);
            }
        }
    }

    //判断是否为特殊符号
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphaLower(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    /**
     *过滤敏感词
     * @param text ：待过滤的文本
     * @return ：过滤之后的文本
     */
    public String filter(String text){
       //判断待过滤文本是否为空
        if (StringUtils.isBlank(text)){
            return null;
        }
        //指针1：指向根节点
        TrieNode tempNode = rootNode;
        //指针2：指向待过滤文本的头
        int begin = 0 ;
        //指针3：待过滤文本中的一个偏移指针
        int position = 0 ;
        //过滤后的结果用StringBuilder装载
        StringBuilder sb = new StringBuilder();

        //移动指针3
        while (begin < text.length()){
            char c = text.charAt(position);
            //跳过符号
            if (isSymbol(c)){
                //若指针1是根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或者中间，指针3都向下走一步
                position++;
                continue;
            }

            //非符号字符，检查下级节点
            tempNode = tempNode.getSubNode(c);
            //如果当前字符没有子节点
            if (tempNode == null){
                //以bigin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //指针1重新指向根节点
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                //发现了敏感词将begin开头，position结尾的字符串替换掉
                sb.append(REPLACEMENT);
                //指针进入下个位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            }else {
                //继续检查下一个字符
                if (position<text.length()-1){
                    position++;
                }
            }
        }
        //将最后的字符计入结果
        sb.append(text.substring(begin));

        //返回过滤后的字符串
        return sb.toString();
    }
}

