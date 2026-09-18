package cn.iocoder.yudao.module.rental.util;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 付款方式工具类
 *
 * 约定格式为「押N付M」，中文数字：
 *   N = 押几个月租金，押金 = N × 月租金
 *   M = 一次付几个月租金，首期应缴 = 押金 + M × 月租金，账期 = M 个月
 *
 * N 与 M 各自取值 1~3，共 9 种组合（押一付一 … 押三付三）。
 *
 * 解析对脏数据保持宽容：格式不认识时按「押一付一」处理，不抛异常 ——
 * 付款方式只影响账单金额的展示与生成，为一条历史脏数据让整个流程挂掉不值得。
 */
public class PaymentMethodUtils {

    /** 中文数字，下标即数值（0 位占位不用） */
    private static final String[] CN_NUMBERS = {"", "一", "二", "三"};

    /** 押/付的最小值 */
    public static final int MIN_MONTHS = 1;
    /** 押/付的最大值 */
    public static final int MAX_MONTHS = 3;

    /** 兜底值：解析不出来时按「押一付一」处理 */
    public static final String DEFAULT = "押一付一";

    private static final String PREFIX_DEPOSIT = "押";
    private static final String PREFIX_PAY = "付";

    private PaymentMethodUtils() {
    }

    /**
     * 押几个月（押金 = N × 月租金）
     */
    public static int depositMonths(String paymentMethod) {
        return parseMonths(paymentMethod, PREFIX_DEPOSIT);
    }

    /**
     * 一次付几个月（首期租金 = M × 月租金）
     */
    public static int payMonths(String paymentMethod) {
        return parseMonths(paymentMethod, PREFIX_PAY);
    }

    /**
     * 拼出「押N付M」的文案
     */
    public static String build(int depositMonths, int payMonths) {
        return PREFIX_DEPOSIT + toCn(depositMonths) + PREFIX_PAY + toCn(payMonths);
    }

    /**
     * 全部 9 种组合，按「押几」优先排序
     */
    public static List<String> allOptions() {
        List<String> options = new ArrayList<>(MAX_MONTHS * MAX_MONTHS);
        for (int deposit = MIN_MONTHS; deposit <= MAX_MONTHS; deposit++) {
            for (int pay = MIN_MONTHS; pay <= MAX_MONTHS; pay++) {
                options.add(build(deposit, pay));
            }
        }
        return options;
    }

    /**
     * 从「押一付三」里取 押/付 后面跟的月数
     *
     * 同时认中文数字（一二三）与阿拉伯数字（123），因为这两种在历史数据里都可能出现。
     */
    private static int parseMonths(String paymentMethod, String prefix) {
        if (StrUtil.isBlank(paymentMethod)) {
            return MIN_MONTHS;
        }
        int index = paymentMethod.indexOf(prefix);
        if (index < 0 || index + 1 >= paymentMethod.length()) {
            return MIN_MONTHS;
        }
        char value = paymentMethod.charAt(index + 1);
        // 阿拉伯数字
        if (value >= '1' && value <= '9') {
            return clamp(value - '0');
        }
        // 中文数字
        for (int i = MIN_MONTHS; i <= MAX_MONTHS; i++) {
            if (CN_NUMBERS[i].charAt(0) == value) {
                return i;
            }
        }
        return MIN_MONTHS;
    }

    private static int clamp(int months) {
        if (months < MIN_MONTHS) {
            return MIN_MONTHS;
        }
        return Math.min(months, MAX_MONTHS);
    }

    private static String toCn(int months) {
        int safe = clamp(months);
        return CN_NUMBERS[safe];
    }

}
