/**
 * Project Name:autoJob
 * File Name:StringUtil.java
 * Package Name:com.ec.autojob.util
 * Date:2015年6月24日上午11:22:23
 * Copyright (c) 2015, 深圳市六度人和 All Rights Reserved.
 *
 */
package com.ec.monitor.util;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName:StringUtil <br/>
 * Function: 字符串工具类
 *
 * <br/>
 * Date: 2015年6月24日 上午11:22:23 <br/>
 *
 * @author xxg
 * @version
 * @since JDK 1.7
 * @see
 */
public class StringUtil {

    private static final String NUMERIC_REGEX = "[0-9]*";

    /**
     *
     * isNullString:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author xxg
     * @param input
     * @return
     * @since JDK 1.7
     */
    public static boolean isNullString(String input) {
        return (input == null || "".equals(input.trim()));
    }

    /**
     *
     * isChineseChar:(这里用一句话描述这个方法的作用). <br/>
     * TODO 判断是否为中文.<br/>
     *
     * @author xxg
     * @param str
     * @return
     * @since JDK 1.7
     */
    public static boolean isChineseChar(String str) {
        boolean temp = false;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            temp = true;
        }
        return temp;
    }

    /**
     *
     * pareColelctionName:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     * 将cmsg20150524
     *
     * @author xxg
     * @param str
     * @return
     * @since JDK 1.7
     */
    public static String pareColelctionName(String str) {
        return str.substring(4);
    }

    /**
     *
     * convertFolat2String:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     * 将float类型该类型为20150614格式的 转换成时间格式2015-06-14
     *
     * @author xxg
     * @return
     * @since JDK 1.7
     */
    public static String convertFolat2String(double str) {
        String tmp = (int) str + ""; // 先转成string类型
        return tmp.substring(0, 4) + "-" + tmp.substring(4, 6) + "-" + tmp.substring(6, 8);
    }

    /**
     * List转换String, 不支持嵌套List
     *
     * @param list
     *            :需要转换的List
     * @return String转换后的字符串
     */
    @SuppressWarnings("rawtypes")
    public static String listToString(List list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static String toUpperFildeName(String fileName) {
        String fristChar = fileName.substring(0, 1).toUpperCase();
        String newS = fristChar + fileName.substring(1, fileName.length());
        return newS;
    }

    /**
     * 字符串时间转时间戳
     *
     * @param user_time
     * @return
     * @throws Exception
     */
    /*public static String getTimeStamp(String user_time) throws Exception {
        Date date = null;
        long timeStamp = 0;

        try {
            date = new SimpleDateFormat(Constants.DATE_FORMAT).parse(user_time);
        } catch (ParseException e) {
            throw new Exception("字符串时间转时间戳失败");
        }

        if (date == null) {
            timeStamp = 0;
        } else {
            timeStamp = date.getTime();
        }

        if (0 < timeStamp) {
            // java 时间戳和PHP时间戳 的转换 由于精度不同, 前者13位，后者10位
            // 导致长度不一致，直接转换错误. JAVA时间戳在PHP中使用，去掉后三位
            return String.valueOf(timeStamp).substring(0, 10);
        } else {
            return String.valueOf(timeStamp);
        }
    }*/



    /**
     * @Title: isNumeric
     * @Description: 判断字符串是否是数字
     * @param str
     * @return
     * @return boolean
     * @throws
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile(NUMERIC_REGEX);
        Matcher isNum = pattern.matcher(str);
        if (isNum.matches()) {
            return true;
        }
        return false;
    }
    
    //防止sql过滤
    public static boolean sqlValidate(String str) {  
        str = str.toLowerCase();//统一转为小写  
        String badStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|" +  
                "char|declare|sitename|net user|xp_cmdshell|;|or|-|+|,|like'|and|exec|execute|insert|create|drop|" +  
                "table|from|grant|use|group_concat|column_name|" +  
                "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +  
                "chr|mid|master|truncate|char|declare|or|;|-|--|+|,|like|//|/|%|#";//过滤掉的sql关键字，可以手动添加  
        String[] badStrs = badStr.split("\\|");  
        for (int i = 0; i < badStrs.length; i++) {  
            if (str.indexOf(badStrs[i]) >= 0) {  
                return true;  
            }  
        }  
        return false;  
    }  


    public static void main(String[] args) throws IOException {
        /*
         * File file = new File("d:/wewe/xxg/ee.txt"); // System.out.println(file.isDirectory());
         * String s = file.getParent(); System.out.println(s);
         * 
         * File parentFI = new File(s); System.out.println(parentFI.mkdirs());
         * System.out.println(file.exists()); //如果文件不存在，则创建新文件 file.createNewFile();
         */

        // writeString2File("d:/xxg/2.txt","i love you");
        /*
         * File f = new File("d://xxg");
         * 
         * File[] fs = f.listFiles(new FilenameFilter() {
         * 
         * @Override public boolean accept(File dir, String name) {
         * if(name.startsWith("immsg-2015-05-03")) return true; else return false; } });
         * System.out.println(f.isFile()); System.out.println(f.exists()); for(File ff:fs){
         * System.out.println(ff.getName()); }
         */

        /*
         * String[] araay = {"1","2","3","4","5","321","123123","123"}; Long[] lon ={1l,
         * 2l,3l,4l,5l,65l,52l,123l}; List<Long> resultLists = new ArrayList<Long>();
         * resultLists.add(1l); resultLists.add(1l); resultLists.add(1l); resultLists.add(1l);
         * resultLists.add(1l); resultLists.add(1l); resultLists.add(1l); resultLists.add(1l);
         */

        // Long[] ss =(Long[])resultLists.toArray();

        // StaticSumResultBean SUM = convertStringArray2Object(lon,new StaticSumResultBean());
        // System.out.println(SUM.getAndroidEIM());immsg-2015-05-14_00000 j
        /*
         * String msg = "immsg-2015-05-14_00000 "; String ssss = "20150514"; float bbb =
         * Float.parseFloat(ssss); // System.out.println((int)bbb+"".); float ss = 20150514;
         * System.out.println("cmsg"+msg.substring(6,16 ).replace("-", ""));
         * System.out.println("20150514".substring(0,
         * 4)+"-"+"20150514".substring(4,6)+"-"+"20150514".substring(6, 8));
         * 
         * 
         * System.out.println(pareColelctionName("cmsg2015014")); float sssss = 20150514;
         * System.out.println(convertFolat2String(sssss));
         */

        double kkk = Double.parseDouble("20150705");
        System.out.println(StringUtil.sqlValidate("1231"));
        System.out.println(!StringUtil.isNullString(" ") && !StringUtil.sqlValidate(" "));
        String xx = "abcedfg";
        System.out.println(xx.contains("abc"));
        System.out.println((int) kkk);
    }

}
