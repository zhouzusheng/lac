package com.aitek.common.lac;

import com.aitek.common.lac.util.CloseableThreadLocal;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiduSegmenter implements Closeable {
    public static BaiduLac globalLac;
    private static CloseableThreadLocal<BaiduSegmenter> localSegmenter = new CloseableThreadLocal<BaiduSegmenter>(){

        @Override
        protected BaiduSegmenter initialValue() {
            return new BaiduSegmenter(globalLac, true);
        }
    };

    static Pattern NUMPAT = Pattern.compile("[幺零〇一二两三四五六七八九十廿百千万亿壹贰叁肆伍陆柒捌玖拾佰仟0123456789]{3,}");
    static final char[] DIGIT = {'幺','零','〇','一','二','三','四','五','六','七','八','九','十','廿','百','千','万','亿'};
    private final static char[][] TGDZ = new char[][]{
            {'甲','乙','丙','丁','戊','己','庚','辛','壬','癸'}
            ,{'子','丑','寅','卯','辰','巳','午','未','申','酉','戌','亥'}
    };

    static {
        Arrays.sort(TGDZ[0]);
        Arrays.sort(TGDZ[1]);
    }

    private BaiduLac lac;

    public static synchronized void loadLac(String modelDir) {
        synchronized(BaiduSegmenter.class) {
            if (globalLac != null) {
                throw new IllegalStateException("已经加载");
            }
            globalLac = new BaiduLac(modelDir);
        }
    }

    public static  void clearLac() {
        localSegmenter.remove();
        synchronized(BaiduSegmenter.class) {
            if (globalLac != null) {
                globalLac.close();
            }
            localSegmenter.close();
        }
    }

    public static BaiduSegmenter getInstance() {
        return localSegmenter.get();
    }

    public static void remove() {
        localSegmenter.remove();
    }

    public BaiduSegmenter(BaiduLac lac) {
        this.lac = lac;
    }

    public BaiduSegmenter(BaiduLac lac, boolean clone) {
        this.lac = clone ? new BaiduLac(lac) :lac;
    }

    @Override
    public void close() throws IOException {
        lac.close();
    }

    /**
     * 分词，返回词， 词性， 位置
     * 对百度的结果做了如下修正：
     *   对数字识别不全的纠正
     *   词性  m 有的情况修改为 mq
     *   对时间 识别粒度修改了一下,原来的相似的时间分出的粒度各部相同，现在统一
     *
     *   加了几个词到训练语料中使得时间可以召回
     *     下下周X
     *     下个周X
     *     大前天
     *     大后天
     *     两点
     *
     * 具体训练内容如下：
     *
     * 下周三/TIME 下午/TIME 两点/TIME
     * 下周三/TIME 两点/TIME
     * 下下周/TIME
     * 大前天/TIME
     * 大后天/TIME
     * 下个周一/TIME
     * 下个周二/TIME
     * 下个周三/TIME
     * 下个周四/TIME
     * 下个周五/TIME
     * 下个周六/TIME
     * 下个周日/TIME
     * 下个礼拜天/TIME
     * 下下周一/TIME
     * 下下周二/TIME
     * 下下周三/TIME
     * 下下周四/TIME
     * 下下周五/TIME
     * 下下周六/TIME
     * 下下周日/TIME
     * 我/r 给/p 你/r 提/v 两点/m 建议/n
     * 两点/m 意见/n
     * 我们/r 约/d 在/p 两点后/TIME 见面/v
     * @param sentence
     * @return
     */
    public List<Term> segment(String sentence){
        if(lac.isClosed()) {
            throw  new IllegalStateException("lac is closed");
        }

        ArrayList<String> labels = new ArrayList<>(sentence.length());
        lac.runLabels(sentence, labels);

        //先不管定制词典， 先修正一些标签
        fixLabels(sentence, labels);

        ArrayList<Term> results = new ArrayList<>();
        for (int i = 0; i < labels.size(); ++i){
            String label = labels.get(i);
            if (results.isEmpty() || label.endsWith("B") || label.endsWith("S")){
                Term term = new Term();
                term.offset = i;
                term.tag = Attribute.valueOf(label.substring(0, label.length()-2));
                results.add(term);
            }
        }
        for(int i = 0; i <  results.size(); ++i) {
            Term term = results.get(i);
            int end = (i+1) < results.size() ? results.get(i+1).offset : sentence.length();
            term.word = sentence.substring(term.offset, end);
            if(term.tag == Attribute.m) {
                char ch = term.word.charAt(term.length()-1);
                if(!isDigit(ch)) {
                    //修正数字为数量词
                    term.tag = Attribute.mq;
                }
            }
        }
        return results;
    }


    private void fixLabels(String sentence, ArrayList<String> labels) {
        ////修正数字
        fixNumLabels(sentence, labels);

        //对时间修正
        fixTimeLabels(sentence, labels);
    }

    static boolean isDigit(char ch) {
        if(Character.isDigit(ch)) {
            return true;
        }
        for(char item : DIGIT) {
            if(item == ch) {
                return true;
            }
        }
        return false;
    }

    private void fixNumLabels(String sentence, ArrayList<String> labels) {
        Matcher m = NUMPAT.matcher(sentence);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            while(start < end) {
                String label = labels.get(start);
                if((label.endsWith("B") || label.endsWith("S")) && !label.startsWith("TIME")) {
                    break;
                }
                start++;
            }
            if((end - start) < 3){
                continue;
            }
            labels.set(start, "m-B");
            for(int i = start+1; i < end; i++) {
                labels.set(i, "m-I");
            }
        }
    }

    private void fixTimeLabels(String sentence, ArrayList<String> labels) {
        int max = labels.size() -1;
        for (int i = 0; i < max; ++i){
            char ch = sentence.charAt(i);
            String label = labels.get(i);
            if(label.equals("TIME-I")) {
                if (ch == '月') {
                    i++;
                    i = fixMonthLables(sentence, labels, i);
                }
                if (ch == '年') {
                    if (i > 0 && sentence.charAt(i - 1) == '大' && labels.get(i - 1).equals("TIME-B")) {
                        i++;
                        i = fixTimeNumberLabels(sentence, labels, i);
                    } else if (labels.get(i + 1).equals("TIME-I")) {
                        i++;
                        labels.set(i, "TIME-B");
                    }
                } else if (i > 0 && (sentence.startsWith("星期", i-1) || sentence.startsWith("礼拜", i-1))) {
                    i++;
                    i = fixTimeWeek(sentence, labels, i);
                }
            } else if(label.equals("TIME-B")) {
                if (ch == '周') {
                    i++;
                    i = fixTimeWeek(sentence, labels, i);
                } else if(isGanZhi(sentence, i)) {
                    i += 2;
                    if(i < max && isDigit(sentence.charAt(i)) && labels.get(i).equals("TIME-I")) {
                        labels.set(i, "TIME-B");
                    }
                }
            }
        }
    }

    /**
     * 是不是天干地支
     * @param sentence
     * @param i
     * @return
     */
    private boolean isGanZhi(String sentence, int i) {
        char ch  = sentence.charAt(i);
        char next  = sentence.charAt(i+1);
        return Arrays.binarySearch(TGDZ[0], ch) >= 0 && Arrays.binarySearch(TGDZ[1],  next) >= 0;
    }

    private int fixTimeWeek(String sentence, ArrayList<String> labels, int i) {
        if(!labels.get(i).equals("TIME-I")) {
            return i;
        }
        char ch = sentence.charAt(i);
        if(isDigit(ch)) {
            i++;
            if(i < labels.size() && labels.get(i).equals("TIME-I")){
                labels.set(i, "TIME-B");
            }
        }
        return i;
    }

    private int fixTimeNumberLabels(String sentence, ArrayList<String> labels, int i) {
        for (int j = 0; i < 6 && (i + j) < labels.size(); j++) {
            char ch = sentence.charAt(i + j);
            if (isDigit(ch)) {
                if (ch == '十') {
                    j++;
                    if ((i + j + 1) < labels.size()) {
                        char nextChar = sentence.charAt(i + j);
                        char nextNextChar = sentence.charAt(i + j + 1);
                        if (nextChar == '十' || (isDigit(nextChar) && nextNextChar == '点')) {
                            i = i + j;
                            labels.set(i, "TIME-B");
                        }
                    }
                    break;
                }
            } else {
                break;
            }
        }
        return i;
    }

    private int fixMonthLables(String sentence, ArrayList<String> labels, int i) {
        if(!labels.get(i).startsWith("TIME-")) {
            return i;
        }
        labels.set(i, "TIME-B");
        char ch = sentence.charAt(i);
        if(ch == '初') {
            i++;
            if(i < labels.size() && isDigit(sentence.charAt(i))) {
                labels.set(i, "TIME-I");
                i++;
                if (i < labels.size() && labels.get(i).equals("TIME-I")) {
                    labels.set(i, "TIME-B");
                    i++;
                }
            }
        } else if(isDigit(ch)) {
            int pos = i;
            for (int j = 1; j <= 3 && (i + j + 1) < labels.size(); j++) {
                if (labels.get(i + j).equals("TIME-I")) {
                    ch = sentence.charAt(i + j);
                    if (ch == '号' || ch == '日') {
                        i = i + j;
                        labels.set(i + 1, "TIME-B");
                        break;
                    } else if (ch != '点' && !isDigit(ch)) {
                        i = i + j;
                        labels.set(i, "TIME-B");
                        break;
                    }
                } else {
                    break;
                }
            }
            if (pos == i) {
                //neither 号 or 日
                i = fixTimeNumberLabels(sentence, labels, i);
            }
        }
        return i;
    }

    public static void testSegmenter(BaiduSegmenter segmenter) {
        System.out.println(segmenter.segment("打电话给幺五八六三二二幺二二幺"));
        System.out.println(segmenter.segment("给幺五八六三二二幺二二幺打电话"));
        System.out.println(segmenter.segment("明年腊月初八十九点"));
        System.out.println(segmenter.segment("下下周三下午两点"));
        System.out.println(segmenter.segment("两周后下午两点"));
        System.out.println(segmenter.segment("我给你提两点建议"));
        System.out.println(segmenter.segment("七月初十三点十五我们吃饭"));
        System.out.println(segmenter.segment("八月二十三"));
        System.out.println(segmenter.segment("八月二十三五点十三分"));
        System.out.println(segmenter.segment("八月二十三点十三分"));
        System.out.println(segmenter.segment("八月二十十三点十三分"));
        System.out.println(segmenter.segment("八月一号五点十三分"));
        System.out.println(segmenter.segment("八月二十二日五点十三分"));
        System.out.println(segmenter.segment("八月二十下午五点半"));
        System.out.println(segmenter.segment("大年三十一点"));
        System.out.println(segmenter.segment("一号楼"));
        System.out.println(segmenter.segment("五月一号"));
        System.out.println(segmenter.segment("明年五月一号"));
        System.out.println(segmenter.segment("明年六月二十三五点十三分"));
        System.out.println(segmenter.segment("明年周四十三点"));
        System.out.println(segmenter.segment("周七十三点"));
        System.out.println(segmenter.segment("星期二十三点"));
        System.out.println(segmenter.segment("甲午一月"));
        System.out.println(segmenter.segment("甲午二月"));
        System.out.println(segmenter.segment("甲午三月"));
        System.out.println(segmenter.segment("甲午四月"));
        System.out.println(segmenter.segment("甲午五月"));
        System.out.println(segmenter.segment("甲午六月"));

    }

    public static void main(String[] args) {
        BaiduLac lac = new BaiduLac("my_seg_model");
        BaiduSegmenter segmenter = new BaiduSegmenter(lac);
        testSegmenter(segmenter);
        lac.close();

        BaiduSegmenter.loadLac("my_seg_model");
        segmenter = getInstance();
        testSegmenter(segmenter);
        BaiduSegmenter.remove();

        clearLac();
    }
}
