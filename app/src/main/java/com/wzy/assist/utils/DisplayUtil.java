package com.wzy.assist.utils;

import static android.view.View.NO_ID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.ColorInt;


import com.wzy.assist.App;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DisplayUtil {


    /**
     * 利用反射获取状态栏高度
     */
    public static int statusBarHeight() {
        int result = 0;
        //获取状态栏高度的资源id
        try {
            Resources resources = App.getApp().getResources();
            @SuppressLint("InternalInsetResource")
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId);
            }
            if (result <= 0) // 双重校验
            {
                @SuppressLint("PrivateApi") Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object obj = c.newInstance();
                Field field = c.getField("status_bar_height");
                Object o = field.get(obj);
                if (o != null) {
                    int x = Integer.parseInt(o.toString());
                    result = App.getApp().getResources().getDimensionPixelSize(x);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * 获取 虚拟按键的高度
     */
    public static int bottomStatusHeight(Context context) {
        try {
            if (checkNavigationBarShow(context)) {
                int totalHeight = getDpi();
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                int contentHeight = dm.heightPixels;
                return totalHeight - contentHeight;
            } else {
                @SuppressLint("InternalInsetResource") int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                return context.getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * 该方法需要在View完全被绘制出来之后调用，否则判断不了
     * 在比如 onWindowFocusChanged（）方法中可以得到正确的结果
     */
    public static boolean isNavigationBarExist(Activity activity) {
        if (activity == null) return false;
        ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
        if (vp != null) {
            for (int i = 0; i < vp.getChildCount(); i++) {
                vp.getChildAt(i).getContext().getPackageName();
                if (vp.getChildAt(i).getId() != NO_ID && "navigationBarBackground".equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取屏幕宽高
     */
    public static DisplayMetrics metrics() {
        Resources resources = App.getApp().getResources();
        //        float density = dm.density;
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
        return resources.getDisplayMetrics();
    }

    /**
     * @return 屏幕宽度
     */
    public static int metricsWidth() {
        DisplayMetrics metrics = metrics();
        if (metrics == null)
            return 0;
        else
            return metrics.widthPixels;
    }

    /**
     * @return 屏幕高度
     */
    public static int metricsHeight() {
        DisplayMetrics metrics = metrics();
        if (metrics == null)
            return 0;
        else
            return metrics.heightPixels;
    }

    public static int widthPixels() {
        try {
            return displayMetrics().widthPixels;
        } catch (Exception e) {
            return 750;
        }
    }

    public static int heightPixels() {
        try {
            return displayMetrics().heightPixels;
        } catch (Exception e) {
            return 1334;
        }
    }

    private static DisplayMetrics displayMetrics() {
        WindowManager windowManager = (WindowManager) App.getApp()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    /**
     * convert px to its equivalent dp
     * <p>
     * 将px转换为与之相等的dp
     */
    public static int px2dp(float pxValue) {
        final float scale = App.getApp().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * convert dp to its equivalent px
     * <p>
     * 将dp转换为与之相等的px
     */
    public static int dip2px(float dipValue) {
        final float scale = App.getApp().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * convert px to its equivalent sp
     * <p>
     * 将px转换为sp
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    /**
     * convert sp to its equivalent px
     * <p>
     * 将sp转换为px
     */
    public static float sp2px(float spValue) {
        final float fontScale = App.getApp().getResources().getDisplayMetrics().scaledDensity;
        return (spValue * fontScale + 0.5f);
    }

    /**
     * 获取屏幕原始尺寸高度，包括虚拟功能键高度
     */
    public static int getDpi() {
        int dpi = 0;
        WindowManager windowManager = (WindowManager)
                App.getApp().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * 判断虚拟导航栏是否显示
     *
     * @return true(显示虚拟导航栏)，false(不显示或不支持虚拟导航栏)
     */
    public static boolean checkNavigationBarShow(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            @SuppressLint("PrivateApi") Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            // 判断是否隐藏了底部虚拟导航
            int navigationBarIsMin;
            navigationBarIsMin = Settings.Global.getInt(context.getContentResolver(),
                    "navigationbar_is_min", 0);
            if ("1".equals(navBarOverride) || 1 == navigationBarIsMin) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception ignored) {
        }
        return hasNavigationBar;
    }

    /**
     * 创建背景颜色
     *
     * @param color       填充色
     * @param strokeColor 线条颜色
     * @param strokeWidth 线条宽度  单位px
     * @param radius      角度  px
     */
    public static GradientDrawable createRectangleDrawable(@ColorInt int[] color, @ColorInt int strokeColor, int strokeWidth, float radius) {
        try {
            GradientDrawable radiusBg = new GradientDrawable();
            //设置Shape类型
            radiusBg.setShape(GradientDrawable.RECTANGLE);
            //设置填充颜色
            radiusBg.setColors(color);
            if (strokeWidth > 0) {
                //设置线条粗心和颜色,px
                radiusBg.setStroke(strokeWidth, strokeColor);
            }
            //设置圆角角度,如果每个角度都一样,则使用此方法
            radiusBg.setCornerRadius(radius);
            return radiusBg;
        } catch (Exception e) {
            return new GradientDrawable();
        }
    }

}