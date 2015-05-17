package com.wrh.assistant.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.a.b.m;

public class WeiboUtil {
	public static final String PACKAGENAME = "com.sina.weibo";
	
	private Context context;
	private String appId;
	private IWeiboShareAPI api ;
	private boolean isRegisterSuccess ;
	
	
	public WeiboUtil(Context context,String APP_KEY) {
		this.context = context;
		this.appId = APP_KEY;
		api = WeiboShareSDK.createWeiboAPI(context, APP_KEY);
		isRegisterSuccess = api.registerApp(); //注册到微博客户端
	}
	public IWeiboShareAPI getApi() {
		return api;
	}
	/**
	 * 
	 * @return 返回注册成功与否
	 */
	public boolean isRegisterSuccess() {
		return isRegisterSuccess;
	}
	
	/****
	 * 分享文本消息到新浪微博
	 * @param shareText 文本内容
	 * @return
	 */
	public boolean sendTextMessage(String shareText){
		TextObject textObject = new TextObject();
		textObject.text = shareText;
		
		WeiboMessage message = new WeiboMessage();
		message.mediaObject = textObject;
		
		// 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = message;
        
        // 3. 发送请求消息到微博，唤起微博分享界面
        return api.sendRequest((Activity)context, request);
		
	}
	/***
	 * 分享图片到新浪微博
	 * @param shareBitmap
	 * @return
	 */
	public boolean sendImageMessage(Bitmap shareBitmap){
		ImageObject object = new ImageObject();
		object.setImageObject(shareBitmap);
		
		WeiboMessage message = new WeiboMessage();
		message.mediaObject = object;
		
		// 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = message;
        
        // 3. 发送请求消息到微博，唤起微博分享界面
        return api.sendRequest((Activity)context, request);
	}
	
	/****
	 * 分享图文消息到新浪微微
	 * @param shareText   文本内容
	 * @param shareBitmap 图片
	 * @return 
	 */
    public boolean sendImageMessage(String shareText, Bitmap shareBitmap) {
    	WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
    	
    	TextObject textObject = new TextObject();
    	textObject.text = shareText;
    	weiboMultiMessage.textObject = textObject;
    	
    	ImageObject imageObject = new ImageObject();
    	imageObject.setImageObject(shareBitmap);
    	weiboMultiMessage.imageObject = imageObject;
    	
    	SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
    	request.transaction = String.valueOf(System.currentTimeMillis());
    	request.multiMessage = weiboMultiMessage;
    	return api.sendRequest((Activity)context,request);
    }
   
    
}
