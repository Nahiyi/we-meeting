package cn.clazs.easymeeting.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class StringUtil {

    // 将字符串的首字母转换为大写
    public static String upperCaseFirstLetter(String field) {
        // 判断传入的字符串是否为null或空字符串
        if (StringUtils.isEmpty(field)) {
            return field; // 如果为空，直接返回原字符串
        }
        // 截取字符串的第一个字符并转为大写，再拼接剩余部分
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    // 将字符串的首字母转换为小写
    public static String lowerCaseFirstLetter(String field) {
        // 判断传入的字符串是否为null或空字符串
        if (StringUtils.isEmpty(field)) {
            return field; // 如果为空，直接返回原字符串
        }
        // 截取字符串的第一个字符并转为小写，再拼接剩余部分
        return field.substring(0, 1).toLowerCase() + field.substring(1);
    }

    public static boolean isEmpty(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        int length = cs.length();
        if (length == 0) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 生成12位用户ID
     * @return 12位随机字符串
     */
    public static String generateUserId() {
        return getRandomString(12);
    }

    public static String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    public static String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static String encodeMd5(String originString) {
        return isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }

    // 返回长度为10的随机数作为个人会议号或者系统生成的会议号
    public static String generateMeetingNo() {
        return StringUtil.getRandomNumber(10);
    }

    public static String cleanHtmlTag(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace("\r\n", "<br>");
        content = content.replace("\n", "<br>");
        return content;
    }

    public static String getChatSessionId4User(String[] userIds) {
        Arrays.sort(userIds);
        return encodeMd5(StringUtils.join(userIds, ""));
    }

    public static String getChatSessionId4Group(String groupId) {
        return encodeMd5(groupId);
    }

    public static String getFileSuffix(String fileName) {
        if (isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static boolean isNumber(String str) {
        String checkNumber = "^[0-9]+$";
        if (null == str) {
            return false;
        }
        return str.matches(checkNumber);
    }
}
