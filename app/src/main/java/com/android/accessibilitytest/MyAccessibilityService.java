package com.android.accessibilitytest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;


public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        String activityName = getActivityName(event);

        // 获取当前活动窗口的根节点
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            // 遍历整个控件树
            try {
                JSONObject jsonObject = accessibilityNodeInfoToJson(rootNode, 0);
                Log.d("JSON", jsonObject.toString());
                JsonToFIle(jsonObject, activityName);
            } catch (JSONException | IOException e) {
                Log.e("JSONException", e.toString());
            }
        }
    }

    /**
     * 获取ActivityName
     *
     * @param event
     */
    private String getActivityName(AccessibilityEvent event) {
        String activityName = "";
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && source.getClassName() != null) {
                activityName = source.getClassName().toString();
                Log.d("ActivityName", "Current Activity: " + activityName);
            }
        }
        return activityName;
    }


    /**
     * JSONObject2File
     *
     * @param jsonObject
     * @throws IOException
     */
    public void JsonToFIle(JSONObject jsonObject, String activityName) throws IOException {
        String json = jsonObject.toString();

        String fileName = activityName + "_" + System.currentTimeMillis() + "_" + "TreeView.json";

        FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
        // 将字符串写入文件
        fileOutputStream.write(json.getBytes());
        fileOutputStream.close();

    }

    /**
     * 将AccessibilityNodeInfo转化为Json
     *
     * @param node AccessibilityNodeInfo
     * @return JSONObject
     * @throws JSONException
     */
    public JSONObject accessibilityNodeInfoToJson(AccessibilityNodeInfo node, int index) throws JSONException {
        if (node == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("index", index);
        jsonObject.put("text", node.getText());
        String resourceId = node.getViewIdResourceName();

        if (resourceId != null) jsonObject.put("resource-id", resourceId);
        jsonObject.put("class", node.getClassName());
        jsonObject.put("package", node.getPackageName());
        jsonObject.put("content-desc", node.getContentDescription());
        jsonObject.put("checkable", node.isCheckable());
        jsonObject.put("checked", node.isChecked());
        jsonObject.put("clickable", node.isClickable());
        jsonObject.put("enabled", node.isEnabled());
        jsonObject.put("focusable", node.isFocusable());
        jsonObject.put("focused", node.isFocused());
        jsonObject.put("scrollable", node.isScrollable());
        jsonObject.put("long-clickable", node.isLongClickable());
        jsonObject.put("password", node.isPassword());
        jsonObject.put("selected", node.isSelected());
        Rect bounds = new Rect();
        node.getBoundsInParent(bounds);
        jsonObject.put("boundsInParent", bounds.toString().substring(4));
        node.getBoundsInScreen(bounds);
        jsonObject.put("boundsInScreen", bounds.toString().substring(4));
        JSONArray children = new JSONArray();
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            JSONObject childNodeJSONObj = accessibilityNodeInfoToJson(child, i);
            if (childNodeJSONObj != null) {
                children.put(childNodeJSONObj);
            }
            child.recycle();
        }
        if (children.length() > 0) {
            jsonObject.put("children", children);
        }
        return jsonObject;
    }

    @Override
    public void onInterrupt() {
    }


    /**
     * 注册AccessibilityServiceInfo
     */
    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // 处理事件的类型
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        // 处理事件的应用程序包名称
        info.packageNames = new String[]{"com.husky.sztumap"};
        // 反馈的类型
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        setServiceInfo(info);
    }

}


